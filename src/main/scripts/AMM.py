import requests
import random
import time
from concurrent import futures
import logging
from decimal import Decimal
from dataclasses import dataclass
from typing import List, Tuple, Optional, Dict
from datetime import datetime, timezone

@dataclass
class Pulse:
    id: int
    match_id: int
    question_details: str
    status: str

@dataclass
class OrderBookState:
    yes_bids: List[Tuple[Decimal, int]]
    no_bids: List[Tuple[Decimal, int]]
    last_traded_yes_price: Optional[Decimal]
    last_traded_no_price: Optional[Decimal]
    yes_volume: int
    no_volume: int

class MarketMaker:
    def __init__(self, base_url: str, headers: dict):
        self.base_url = base_url
        self.headers = headers

        # Order cleanup parameters
        self.MAX_ORDER_AGE = 60  # 1 minute in seconds
        self.MAX_PRICE_DEVIATION = Decimal('0.5')  # Maximum acceptable price deviation
        self.last_cleanup_time = {}  # Track last cleanup per pulse

        # Risk parameters
        self.min_price = Decimal('0.5')
        self.max_price = Decimal('9.5')
        self.max_position_per_pulse = 1000  # Maximum position size per pulse
        self.min_order_size = 5             # Minimum order size
        self.max_order_size = 50            # Maximum order size
        self.last_order_time = {}           # {pulse_id: timestamp}

        # Layered order parameters
        self.LAYER_SPREADS = [Decimal('0.0'), Decimal('0.1'), Decimal('0.2')]  # Spreads for each layer
        self.LAYER_SIZES = [0.3, 0.3, 0.4]  # Size distribution for each layer

        # Position tracking
        self.positions: Dict[int, Dict[str, int]] = {}  # {pulse_id: {'Yes': qty, 'No': qty}}

        # Active pulses tracking
        self.active_pulses: List[Pulse] = []

        # Setup logging
        logging.basicConfig(
            level=logging.INFO,
            format='%(asctime)s - %(levelname)s - %(message)s',
            filename='market_maker.log'
        )
        self.logger = logging.getLogger('MarketMaker')

    def get_active_pulses(self) -> List[Pulse]:
        """Fetch all active pulses from the system"""
        try:
            response = requests.get(
                f"{self.base_url}/api/pulse/all-active",
                headers=self.headers,
                timeout=5
            )
            response.raise_for_status()

            pulses = []
            data = response.json()
            if data.get('status') == 200:
                for pulse_data in data.get('data', []):
                    pulse = Pulse(
                        id=pulse_data['id'],
                        match_id=pulse_data['matchId'],
                        question_details=pulse_data['pulseQuestion'],
                        status=pulse_data['status']
                    )
                    pulses.append(pulse)

            self.logger.info(f"Fetched {len(pulses)} active pulses")
            return pulses

        except Exception as e:
            self.logger.error(f"Error fetching active pulses: {e}")
            return []

    def calculate_layer_quantities(self, total_size: int) -> List[int]:
        """Calculate order sizes for each layer"""
        return [max(self.min_order_size, int(total_size * pct)) for pct in self.LAYER_SIZES]

    def get_layer_prices(self, base_price: Decimal) -> List[Decimal]:
        """Calculate prices for each layer"""
        prices = []
        for spread in self.LAYER_SPREADS:
            price = (base_price - spread).quantize(Decimal('0.1'))
            prices.append(price)
        return prices

    def get_order_book(self, pulse_id: int) -> Optional[OrderBookState]:
        """Fetch order book for a specific pulse"""
        try:
            response = requests.get(
                f"{self.base_url}/api/order/orderbook/{pulse_id}",
                params={'levels': 5},
                headers=self.headers,
                timeout=5
            )
            response.raise_for_status()
            data = response.json()['data']

            order_book = OrderBookState(
                yes_bids=[(Decimal(str(bid['first'])), int(bid['second']))
                          for bid in data['yesBids']],
                no_bids=[(Decimal(str(bid['first'])), int(bid['second']))
                         for bid in data['noBids']],
                last_traded_yes_price=Decimal(str(data['lastTradedYesPrice']))
                if data.get('lastTradedYesPrice') else None,
                last_traded_no_price=Decimal(str(data['lastTradedNoPrice']))
                if data.get('lastTradedNoPrice') else None,
                yes_volume=int(data['yesVolume']),
                no_volume=int(data['noVolume'])
            )
            self.logger.info(f"OrderBookState for pulse ${pulse_id} is ${order_book}")
            return order_book

        except Exception as e:
            self.logger.error(f"Error fetching order book for pulse {pulse_id}: {e}")
            return None

    def can_place_order(self, pulse_id: int, price: Decimal) -> bool:
        # Price range check
        if price < self.min_price or price > self.max_price:
            self.logger.info(f"[cannot_place_order] Order cannot be placed for pulse ${pulse_id}")
            return False

        # Position check
        position = self.positions.get(pulse_id, {'Yes': 0, 'No': 0})
        total_position = abs(position['Yes']) + abs(position['No'])
        if total_position >= self.max_position_per_pulse:
            self.logger.info(f"[cannot_place_order] Order cannot be placed for pulse ${pulse_id}")
            return False

        self.logger.info(f"[can_place_order] Order can be placed for pulse ${pulse_id}")
        return True

    def should_create_orders(self, order_book: OrderBookState, pulse_id: int) -> bool:
        """Only create orders if needed"""
        # Don't create if market is already liquid
        if order_book.yes_volume > 150 and order_book.no_volume > 150:
            self.logger.info(f"[should_create_orders] volume more than 50 for pulse {pulse_id} & ${order_book}")
            return False

        self.logger.info(f"[should_create_orders] should create orders for ${order_book}")
        return True

    def bulk_cancel_orders(self, order_ids: List[int]) -> bool:
        """Cancel multiple orders in a single API call"""
        long_order_ids = [int(order_id) for order_id in order_ids]

        try:
            payload = {
                "orderIds": long_order_ids
            }
            self.logger.info(f"[bulk_cancel_orders] Found ${order_ids} to be canceled")

            response = requests.post(
                f"{self.base_url}/api/order/bulkCancel",  # New endpoint for bulk cancellation
                headers=self.headers,
                json=payload,
                timeout=10  # Increased timeout for bulk operation
            )

            # Log the actual response
            self.logger.info(f"Bulk cancel response: {response.text}")

            if response.status_code == 200:
                response_data = response.json()
                if response_data.get('data') == True:
                    self.logger.info(f"Successfully canceled {len(order_ids)} orders")
                    return True
                else:
                    self.logger.error(f"Bulk cancellation failed: {response_data}")
                    return False
            else:
                self.logger.error(f"Bulk cancellation failed: HTTP {response.status_code}, Response: {response.text}")
                return False

        except Exception as e:
            self.logger.error(f"Error in bulk order cancellation: {str(e)}", exc_info=True)
            return False

    def cleanup_old_orders(self, pulse_id: int):
        """Cancel old orders that are no longer needed"""
        self.logger.info(f"Fetching open orders for pulse for clean up {pulse_id}")
        try:
            response = requests.get(
                f"{self.base_url}/api/order/open/{pulse_id}/5",
                headers=self.headers
            )
            if response.status_code != 200:
                self.logger.error(f"Failed to fetch open orders for pulse {pulse_id}")
                return

            response.raise_for_status()  # This will raise an exception for non-200 status codes

            data = response.json()
            orders = data.get('data', [])

            if not orders:
                self.logger.info("No open orders found")
                return

            self.logger.info(f"Found {len(orders)} open orders")
            orders_to_cancel = []
            current_time = datetime.now(timezone.utc)

            # Identify orders that need cancellation
            for order in orders:
                order_time = datetime.fromisoformat(order['createdAt'].replace('Z', '+00:00'))
                order_age = (current_time - order_time).total_seconds()

                self.logger.info(f"Order {order['id']} age: {order_age}s")

                if order_age > self.MAX_ORDER_AGE:
                    orders_to_cancel.append(order['id'])
                    self.logger.info(f"Order {order['id']} queued for cancellation (age: {order_age}s)")

            # Bulk cancel orders if any found
            if orders_to_cancel:
                self.logger.info(f"Attempting to cancel {len(orders_to_cancel)} orders")
                success = self.bulk_cancel_orders(orders_to_cancel)
                if success:
                    self.logger.info(f"Successfully cancelled {len(orders_to_cancel)} orders")
                    # Update positions after successful cancellation
                    for order in orders:
                        if order['id'] in orders_to_cancel:
                            side = order['orderSide']
                            qty = order['remainingQuantity']
                            if pulse_id in self.positions:
                                self.positions[pulse_id][side] -= qty
                        else:
                            self.logger.error("Failed to cancel orders")

        except Exception as e:
            self.logger.error(f"Error cleaning up orders for pulse {pulse_id}: {e}")

    def provide_layered_liquidity(self, pulse: Pulse, user_id: int, order_book: OrderBookState):
        """Provide layered liquidity on both sides"""
        # Base size for total liquidity
        base_size = 100  # Adjust based on your needs

        # Calculate sizes for each layer
        layer_sizes = self.calculate_layer_quantities(base_size)

        # Determine base prices
        base_yes_price = Decimal('5.0')
        base_no_price = Decimal('5.0')

        if order_book.last_traded_yes_price:
            base_yes_price = order_book.last_traded_yes_price
        if order_book.last_traded_no_price:
            base_no_price = order_book.last_traded_no_price

        # Get layer prices
        yes_prices = self.get_layer_prices(base_yes_price)
        no_prices = self.get_layer_prices(base_no_price)

        # Place layered orders
        for i in range(len(self.LAYER_SPREADS)):
            # Place Yes orders
            if self.can_place_order(pulse.id, yes_prices[i]):
                self.place_order(
                    pulse_id=pulse.id,
                    match_id=pulse.match_id,
                    user_id=user_id,
                    side="Yes",
                    price=yes_prices[i],
                    quantity=layer_sizes[i]
                )

            # Place No orders
            if self.can_place_order(pulse.id, no_prices[i]):
                self.place_order(
                    pulse_id=pulse.id,
                    match_id=pulse.match_id,
                    user_id=user_id,
                    side="No",
                    price=no_prices[i],
                    quantity=layer_sizes[i]
                )

            # Add small delay between orders to prevent overwhelming the system
            time.sleep(0.1)

    def place_order(self, pulse_id: int, match_id: int, user_id: int, side: str, price: Decimal, quantity: int) -> bool:
        try:
            payload = {
                "userId": user_id,
                "pulseId": pulse_id,
                "matchId": match_id,
                "userAnswer": side,
                "price": float(price),
                "quantity": quantity,
                # "answerTime": datetime.now(timezone.utc).isoformat(),
                "orderType": "BUY",
                "executionType": "MARKET"
            }

            self.logger.info(f"Payload for order placement: {payload}")

            response = requests.post(
                f"{self.base_url}/api/order/bookOrder",
                headers=self.headers,
                json=payload,
                timeout=5
            )
            response_data = response.json()

            # If order was successful
            if response_data.get('data') == True:
                self.logger.info(f"Order placed successfully: {payload}")
                # Update position tracking
                if pulse_id not in self.positions:
                    self.positions[pulse_id] = {'Yes': 0, 'No': 0}
                self.positions[pulse_id][side] += quantity

                self.logger.info(f"Order placed successfully: {payload}")
                return True

            # Log error message from API
            error_message = response_data.get('message', 'Unknown error')
            self.logger.error(f"Failed to place order: {error_message}")
            return False

        except Exception as e:
            self.logger.error(f"Error placing order: {e}")
            return False

    def process_pulse(self, pulse: Pulse, user_id: int):
        """Process a single pulse"""
        self.logger.info(f"processing pulse ${pulse.id}")
        try:
            # 1. Get order book
            order_book = self.get_order_book(pulse.id)
            if not order_book:
                self.logger.error(f"No order book exist for pulse ${pulse.id}")
                return

            # 2 Cleanup old orders first
            self.logger.info(f"2 processing pulse cleaning up old orders for ${pulse.id}")
            self.cleanup_old_orders(pulse.id)

            #3 Check if we should create new orders
            if not self.should_create_orders(order_book, pulse.id):
                self.logger.info(f"3 processing pulse ${pulse.id}")
                self.logger.info(f"Skipping order creation for pulse {pulse.id} - market conditions not met")
                return

            # 4. Calculate and place order
            self.logger.info(f"4 processing pulse ${pulse.id}")
            self.provide_layered_liquidity(pulse, user_id, order_book)

        except Exception as e:
            self.logger.error(f"Error processing pulse {pulse.id}: {e}")

    def run(self, user_id: int):
        """Main market making loop for all active pulses"""
        while True:
            try:
                start_time = time.time()

                # 1. Fetch all active pulses
                active_pulses = self.get_active_pulses()
                self.logger.info(f"active pulse ${active_pulses[0].id}")

                # 2. Process each pulse concurrently
                with futures.ThreadPoolExecutor(max_workers=5) as executor:
                    futures1 = [
                        executor.submit(self.process_pulse, pulse, user_id)
                        for pulse in active_pulses
                    ]
                    futures.wait(futures1)

                # Wait before next iteration
                # Calculate sleep time to maintain consistent interval
                elapsed = time.time() - start_time
                sleep_time = max(10 - elapsed, 0)  # Aim for 10-second intervals
                time.sleep(sleep_time)

            except Exception as e:
                self.logger.error(f"Error in main loop: {e}")
                time.sleep(5)

if __name__ == "__main__":
    config = {
        'base_url': 'http://localhost:8080',
        'headers': {
            'Authorization': 'Bearer YOUR_TOKEN_HERE',
            'Content-Type': 'application/json'
        },
        'user_id': 5  # Market maker bot ID
    }

    market_maker = MarketMaker(config['base_url'], config['headers'])
    market_maker.run(config['user_id'])

