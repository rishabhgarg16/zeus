from websocket import create_connection
import json
import  threading

def f():
    ws = create_connection("ws://192.168.0.103:8080/ws")
    # print(ws.recv())
    print("Sending 'Hello, World'...")
    ws.send(json.dumps({
            "action": "subscribe",
            "topic": "match43"
        })
    )
    while True:
        print("Receiving...")
        result =  ws.recv()
        print("Received '%s'" % result)


for i in range(100):
    t1 = threading.Thread(target=f)
    print("Starting thread")
    t1.start()

