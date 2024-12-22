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

        # Risk parameters
        self.min_price = Decimal('1.0')
        self.max_price = Decimal('9.0')
        self.min_time_between_orders = 60        # Minimum seconds between orders for same pulse/side
        self.max_position_per_pulse = 1000  # Maximum position size per pulse
        self.max_total_position = 5000      # Maximum total position across all pulses
        self.min_spread = Decimal('0.2')    # Minimum spread to maintain
        self.max_spread = Decimal('1.0')    # Maximum spread to maintain
        self.min_order_size = 5             # Minimum order size
        self.max_order_size = 50            # Maximum order size
        self.last_order_time = {}           # {pulse_id: timestamp}

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

            return OrderBookState(
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
        except Exception as e:
            self.logger.error(f"Error fetching order book for pulse {pulse_id}: {e}")
            return None

    def calculate_order_size(self, pulse_id: int, side: str, price: Decimal) -> int:
        """Calculate appropriate order size based on risk parameters"""
        # Get current position
        position = self.positions.get(pulse_id, {'Yes': 0, 'No': 0})
        current_position = position[side]

        # Base size calculation
        base_size = self.min_order_size

        # Adjust based on position limits
        position_utilization = abs(current_position) / self.max_position_per_pulse
        size_factor = 1 - position_utilization

        # Adjust based on price - larger sizes when price is more favorable
        price_factor = 1.0
        if side == 'Yes':
            price_factor = float((Decimal('10') - price) / Decimal('5')) # at 5, qty = 1, at 7, qty = 1
        else:
            price_factor = float(price / Decimal('10'))

        final_size = int(base_size * size_factor * price_factor)
        return max(self.min_order_size, min(final_size, self.max_order_size))

    def can_place_order(self, pulse_id: int, price: Decimal) -> bool:
        """Basic safety checks before placing order"""
        current_time = time.time()

        # Price range check
        if price < self.min_price or price > self.max_price:
            return False

        # Time check - don't spam orders
        if pulse_id in self.last_order_time:
            time_since_last = current_time - self.last_order_time[pulse_id]
            if time_since_last < self.min_time_between_orders:
                return False

        return True

    def should_create_orders(self, order_book: OrderBookState) -> bool:
        """Only create orders if needed"""
        # Don't create if market is already liquid
        if order_book.yes_volume > 50 and order_book.no_volume > 50:
            return False

        # Don't create if spread is tight
        best_yes = min([bid[0] for bid in order_book.yes_bids]) if order_book.yes_bids else None
        best_no = min([bid[0] for bid in order_book.no_bids]) if order_book.no_bids else None
        if best_yes and best_no:
            current_spread = abs(Decimal('10') - best_yes - best_no)
            if current_spread < Decimal('0.4'):
                return False

        return True

    def cleanup_old_orders(self, pulse_id: int):
        """Cancel old orders that are no longer needed"""
        try:
            response = requests.get(
                f"{self.base_url}/api/order/open/{pulse_id}/5",
                headers=self.headers
            )
            open_orders = response.json()['data']
            orders_to_cancel = []

            for order in open_orders:
                # Cancel if order is too old
                order_time = datetime.fromisoformat(order['createdAt'])
                if datetime.now() - order_time > timedelta(minutes=5):


                # Cancel if price is far from current fair price
                fair_price = self.calculate_fair_price(self.get_order_book(pulse_id))
                if abs(order['price'] - fair_price) > Decimal('0.5'):
                    self.cancel_order(order['id'])
                if(orders_to_cancel.length > 0):
                    self.cancel_order(order['id'])
        except Exception as e:
            self.logger.error(f"Error cleaning up orders: {e}")

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

    def calculate_fair_price(self, order_book: OrderBookState, pulse_id: int) -> Tuple[Decimal, Decimal]:
        # Start with last traded prices or mid-market
        if order_book.last_traded_yes_price and order_book.last_traded_no_price:
            fair_yes = order_book.last_traded_yes_price
            fair_no = order_book.last_traded_no_price
        else:
            fair_yes = Decimal('5.0')
            fair_no = Decimal('5.0')

        # Adjust for volume imbalance
        '''
        Example: "Will Virat score 50 runs?"
        Current State:
            YES volume: 800 orders
            NO volume: 200 orders
        
        Initial prices:
        YES: 5.0
        NO: 5.0

        If YES volume (800) >> NO volume (200):
        imbalance = (800 - 200) / 800 = 0.75
        fair_yes = 5.0 + (0.1 * 0.75) = 5.075 (more expensive to buy YES)
        fair_no = 5.0 - (0.1 * 0.75) = 4.925 (cheaper to buy NO)

        This encourages:
        - People to take NO positions (better value)
        - People to avoid taking more YES positions (more expensive)
        
        Volume_adjustment of 0.1 is a tuning parameter that determines 
        how aggressively the market maker adjusts prices in response to imbalances. 
        A larger value would mean more aggressive price adjustments.
        '''
        volume_adjustment = Decimal('0.1')
        if order_book.yes_volume > order_book.no_volume:
            # Too many YES orders, so:
            imbalance = (order_book.yes_volume - order_book.no_volume) / max(order_book.yes_volume, 1)
            fair_yes += volume_adjustment * Decimal(str(imbalance))  # Make YES more expensive
            fair_no -= volume_adjustment * Decimal(str(imbalance))   # Make NO cheaper
        elif order_book.no_volume > order_book.yes_volume:
            # Too many NO orders, so:
            imbalance = (order_book.no_volume - order_book.yes_volume) / max(order_book.no_volume, 1)
            fair_yes -= volume_adjustment * Decimal(str(imbalance))  # Make YES cheaper
            fair_no += volume_adjustment * Decimal(str(imbalance))   # Make NO more expensive

        # Adjust for our position
        position = self.positions.get(pulse_id, {'Yes': 0, 'No': 0})
        position_adjustment = Decimal('0.05')
        net_position = position['Yes'] - position['No']
        if net_position > 0:
            # We have more YES positions, adjust prices to encourage NO orders
            fair_yes += position_adjustment
            fair_no -= position_adjustment
        elif net_position < 0:
            # We have more NO positions, adjust prices to encourage YES orders
            fair_yes -= position_adjustment
            fair_no += position_adjustment

        return fair_yes, fair_no

    def process_pulse(self, pulse: Pulse, user_id: int):
        """Process a single pulse"""
        try:
            # 1. Get order book
            order_book = self.get_order_book(pulse.id)
            if not order_book:
                return

            # 2 Cleanup old orders first
            self.cleanup_old_orders(pulse.id)

            # Check if we should create new orders
            if not self.should_create_orders(order_book):
                self.logger.info(f"Skipping order creation for pulse {pulse.id} - market conditions not met")
                return

            # 3. Calculate fair prices
            fair_yes, fair_no = self.calculate_fair_price(order_book, pulse.id)

            # 4. Calculate spreads
            spread = Decimal('0.2')
            yes_price = (fair_yes - spread).quantize(Decimal('0.1'))
            no_price = (fair_no - spread).quantize(Decimal('0.1'))

            # 5. Calculate and place orders
            # yes_quantity = self.calculate_order_size(pulse.id, "Yes", yes_price)
            # no_quantity = self.calculate_order_size(pulse.id, "No", no_price)

            # Place orders with rate limiting
            for side, price in [("Yes", yes_price), ("No", no_price)]:
                if self.can_place_order(pulse.id, price):
                    quantity = self.calculate_order_size(pulse.id, side, price)
                    if quantity > 0:
                        success = self.place_order(pulse.id, pulse.match_id, user_id, side, price, quantity)
                        if success:
                            self.last_order_time[pulse.id] = time.time()
                            if side == "Yes":
                                self.logger.info(f"""
                                    Pulse {pulse.id} ({pulse.question_details}):
                                    Fair YES: {fair_yes:.2f}, Quote YES: {yes_price:.2f}
                                    Position YES: {self.positions.get(pulse.id, {'Yes': 0})['Yes']}
                                """)
                            else:
                                self.logger.info(f"""
                                    Pulse {pulse.id} ({pulse.question_details}):
                                    Fair NO: {fair_no:.2f}, Quote NO: {no_price:.2f}
                                    Position NO: {self.positions.get(pulse.id, {'No': 0})['No']}
                                """)

        except Exception as e:
            self.logger.error(f"Error processing pulse {pulse.id}: {e}")


    def run(self, user_id: int):
        """Main market making loop for all active pulses"""
        while True:
            try:
                start_time = time.time()

                # 1. Fetch all active pulses
                active_pulses = self.get_active_pulses()

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

