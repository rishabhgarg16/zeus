import requests
import json


def get_user():
    url = "https://zeus.anmolsingh.in/api/users/internal/52"

    payload = {}
    headers = {
        # 'Authorization': '{{login_token}}'
    }

    response = requests.request("GET", url, headers=headers, data=payload)

    return response.json()


def create_order(userId, matchId, pulseId):
    url = "https://zeus.anmolsingh.in/api/order/bookOrder"
    payload = json.dumps({
        "userId": 52,
        "pulseId": 1510,
        "matchId": 387,
        "userAnswer": "Yes",
        "price": 5.5,
        "quantity": 10
    })
    headers = {
        'Content-Type': 'application/json'
    }

    response = requests.request("POST", url, headers=headers, data=payload)

    return response


def test_insufficient_balance():
    user = get_user()
    quantity = user['data']['depositedBalance'] / 5.5
    order_res = create_order(52, 387, 1510)
    print(order_res)
    assert order_res.status_code == 401
    assert order_res.json()['message'] == "Insufficient balan"


if __name__ == "__main__":
    test_insufficient_balance()