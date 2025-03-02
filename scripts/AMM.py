import logging
import threading
import time
from concurrent import futures
from dataclasses import dataclass
from datetime import datetime, timezone
from decimal import Decimal
from typing import List, Tuple, Optional, Dict
import json as json
import requests
import sys

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
        self.min_order_size = 5  # Minimum order size
        self.max_order_size = 50  # Maximum order size
        self.last_order_time = {}  # {pulse_id: timestamp}

        # Minimum time between orders for a pulse (in seconds) to prevent order spam
        self.MIN_ORDER_INTERVAL = 2

        # Layered order parameters
        self.LAYER_SPREADS = [Decimal('0.0'), Decimal('0.1'), Decimal('0.2')]  # Spreads for each layer
        self.LAYER_SIZES = [0.3, 0.3, 0.4]  # Size distribution for each layer

        # Position tracking
        # Each pulse_id maps to a dict with keys "Yes" and "No".
        # Each of those maps to another dict with "quantity" (int) and "amount" (Decimal).
        self.positions: Dict[int, Dict[str, Dict[str, Union[int, Decimal]]]] = {}

        # Active pulses tracking
        self.active_pulses: List[Pulse] = []

        # Setup logging
        logging.basicConfig(
            level=logging.INFO,
            format='%(asctime)s - %(levelname)s - %(message)s',
            filename='market_maker.log'
        )
        self.logger = logging.getLogger('MarketMaker')

        # Add a console (stream) handler so logs also appear in the terminal

        console_handler = logging.StreamHandler(sys.stdout)
        console_handler.setLevel(logging.INFO)
        formatter = logging.Formatter('%(asctime)s - %(levelname)s - %(message)s')
        console_handler.setFormatter(formatter)
        self.logger.addHandler(console_handler)

    def log_with_context(self, level, message, **context):
        # Helper to output structured log messages in JSON format
        context.update({"timestamp": datetime.now(timezone.utc).isoformat()})
        log_message = f"{message} | Context: {json.dumps(context)}"
        if level == "info":
            self.logger.info(log_message)
        elif level == "error":
            self.logger.error(log_message)
        else:
            self.logger.debug(log_message)

    def log_positions(self, pulse_id: int):
        pos = self.positions.get(pulse_id, {
            'Yes': {'quantity': 0, 'amount': Decimal('0.0')},
            'No': {'quantity': 0, 'amount': Decimal('0.0')}
        })
        self.log_with_context("info", "Current positions", pulse_id=pulse_id,
                          yes_quantity=pos['Yes']['quantity'], yes_amount=str(pos['Yes']['amount']),
                          no_quantity=pos['No']['quantity'], no_amount=str(pos['No']['amount']))

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

            self.log_with_context("info", "Fetched active pulses", count=len(pulses))
            return pulses

        except Exception as e:
            self.log_with_context("error", "Error fetching active pulses", error=str(e))
            return []

    def calculate_layer_quantities(self, total_size: int) -> List[int]:
        """Calculate order sizes for each layer"""
        quantities = [max(self.min_order_size, int(total_size * pct)) for pct in self.LAYER_SIZES]
        self.log_with_context("info", "Calculated layer quantities", quantities=quantities)
        return quantities

    def get_layer_prices(self, base_price: Decimal) -> List[Decimal]:
        """Calculate prices for each layer"""
        prices = []
        for spread in self.LAYER_SPREADS:
            price = (base_price - spread).quantize(Decimal('0.1'))
            prices.append(price)
        self.log_with_context("info", "Calculated layer prices", base_price=str(base_price), prices=[str(p) for p in prices])
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
            self.log_with_context("info", "Fetched order book", pulse_id=pulse_id, order_book=str(order_book))
            return order_book

        except Exception as e:
            self.log_with_context("error", "Error fetching order book", pulse_id=pulse_id, error=str(e))
            return None

    def can_place_order(self, pulse_id: int, price: Decimal) -> bool:
        current_time = time.time()
        last_order = self.last_order_time.get(pulse_id, 0)
        if current_time - last_order < self.MIN_ORDER_INTERVAL:
            self.log_with_context("info", "Preventing order spam: last order placed too recently",
                                  pulse_id=pulse_id, time_since_last=current_time - last_order)
            return False

        # Price range check
        if price < self.min_price or price > self.max_price:
            self.log_with_context("info", "Order price out of range", pulse_id=pulse_id, price=str(price))
            return False

        # Position check
        position = self.positions.get(
            pulse_id,
            {
                'Yes': {'quantity': 0, 'amount': Decimal('0.0')},
                'No': {'quantity': 0, 'amount': Decimal('0.0')}
            }
        )
        total_position = abs(position['Yes']['quantity']) + abs(position['No']['quantity'])
        if total_position >= self.max_position_per_pulse:
            self.log_with_context("info", "Max position reached", pulse_id=pulse_id, total_position=total_position)
            return False

        self.log_with_context("info", "Order can be placed", pulse_id=pulse_id, price=str(price))
        return True

    def should_create_orders(self, order_book: OrderBookState, pulse_id: int) -> bool:
        """Only create orders if needed"""
        # Don't create if market is already liquid
        if order_book.yes_volume > 150 and order_book.no_volume > 150:
            self.log_with_context("info", "Market already liquid", pulse_id=pulse_id,
                                  yes_volume=order_book.yes_volume, no_volume=order_book.no_volume)
            return False
        self.log_with_context("info", "Orders will be created", pulse_id=pulse_id)
        return True

    def bulk_cancel_orders(self, order_ids: List[int]) -> bool:
        """Cancel multiple orders in a single API call"""
        long_order_ids = [int(order_id) for order_id in order_ids]

        try:
            payload = {
                "orderIds": long_order_ids
            }
            self.log_with_context("info", "Attempting bulk cancellation", order_ids=order_ids)

            response = requests.post(
                f"{self.base_url}/api/order/bulkCancel",  # New endpoint for bulk cancellation
                headers=self.headers,
                json=payload,
                timeout=10  # Increased timeout for bulk operation
            )

            # Log the actual response
            self.log_with_context("info", "Bulk cancel response received", response_text=response.text)

            if response.status_code == 200:
                response_data = response.json()
                if response_data.get('data') == True:
                    self.log_with_context("info", "Bulk cancellation successful", cancelled_count=len(order_ids))
                    return True
                else:
                    self.log_with_context("error", "Bulk cancellation failed", response_data=response_data)
                    return False
            else:
                self.log_with_context("error", "Bulk cancellation HTTP error", status_code=response.status_code, response_text=response.text)
                return False

        except Exception as e:
            self.log_with_context("error", "Exception in bulk cancellation", error=str(e))
            return False

    def cleanup_old_orders(self, pulse_id: int):
        """Cancel old orders that are no longer needed"""
        self.log_with_context("info", "Starting cleanup of old orders", pulse_id=pulse_id)
        try:
            response = requests.get(
                f"{self.base_url}/api/order/open/{pulse_id}/5",
                headers=self.headers
            )
            if response.status_code != 200:
                self.log_with_context("error", "Failed to fetch open orders", pulse_id=pulse_id, status_code=response.status_code)
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
                self.logger.info(f"Attempting to cancel {len(orders_to_cancel)} orders for pulse {pulse_id}")
                success = self.bulk_cancel_orders(orders_to_cancel)
                if success:
                    self.logger.info(f"Successfully cancelled {len(orders_to_cancel)} orders for pulse {pulse_id}")
                    for order in orders:
                        if order['id'] in orders_to_cancel:
                            side = order['orderSide']
                            qty = order['remainingQuantity']
                            if pulse_id in self.positions:
                                self.positions[pulse_id][side] -= qty
                    else:
                        self.logger.error("Bulk cancellation failed")

        except Exception as e:
            self.log_with_context("error", "Error cleaning up orders", pulse_id=pulse_id, error=str(e))

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
                self.log_with_context("info", "Order placed successfully", pulse_id=pulse_id, side=side, price=str(price), quantity=quantity)
                if pulse_id not in self.positions:
                    self.positions[pulse_id] = {
                        'Yes': {'quantity': 0, 'amount': Decimal('0.0')},
                        'No': {'quantity': 0, 'amount': Decimal('0.0')}
                    }
                if side == "Yes":
                    self.positions[pulse_id]['Yes']['quantity'] += quantity
                    self.positions[pulse_id]['Yes']['amount'] += price * Decimal(quantity)
                else:
                    self.positions[pulse_id]['No']['quantity'] += quantity
                    self.positions[pulse_id]['No']['amount'] += price * Decimal(quantity)
                # Update last order time to prevent spamming
                self.last_order_time[pulse_id] = time.time()
                # Log current positions for basic position management
                self.log_positions(pulse_id)
                return True
            else:
                error_message = response_data.get('message', 'Unknown error')
                self.log_with_context("error", "Failed to place order", pulse_id=pulse_id, error=error_message)
                return False

        except Exception as e:
            self.log_with_context("error", "Exception while placing order", pulse_id=pulse_id, error=str(e))
            return False

    def process_pulse(self, pulse: Pulse, user_id: int):
        """Process a single pulse"""
        self.log_with_context("info", "Processing pulse", pulse_id=pulse.id)
        try:
            # 1. Get order book
            order_book = self.get_order_book(pulse.id)
            if not order_book:
                self.log_with_context("error", "No order book found", pulse_id=pulse.id)
                return

            # 2 Cleanup old orders first
            self.logger.info(f"2 processing pulse cleaning up old orders for ${pulse.id}")
            self.cleanup_old_orders(pulse.id)

            # 3 Check if we should create new orders
            if not self.should_create_orders(order_book, pulse.id):
                self.log_with_context("info", "Skipping order creation", pulse_id=pulse.id)
                return
            #4
            self.provide_layered_liquidity(pulse, user_id, order_book)

        except Exception as e:
            self.log_with_context("error", "Error processing pulse", pulse_id=pulse.id, error=str(e))

    def run(self, user_id: int):
        while True:
            try:
                start_time = time.time()
                active_pulses = self.get_active_pulses()
                if not active_pulses:
                    self.log_with_context("info", "No active pulses found, sleeping", sleep_time=10)
                    time.sleep(10)
                    continue

                # Process pulses concurrently
                with futures.ThreadPoolExecutor(max_workers=5) as executor:
                    tasks = [executor.submit(self.process_pulse, pulse, user_id) for pulse in active_pulses]
                    futures.wait(tasks)
                elapsed = time.time() - start_time
                sleep_time = max(10 - elapsed, 0)
                self.log_with_context("info", "Main loop iteration complete", iteration_time=elapsed, sleep_time=sleep_time)
                time.sleep(sleep_time)
            except Exception as e:
                self.log_with_context("error", "Error in main loop", error=str(e))
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
