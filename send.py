import socket
import json
from math import sin, cos
import time


def f(x):
    return 3*sin(x+3)

def g(x):
    return cos(x/2)

sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
#sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
#sock.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)

x = 0
delta = 1
while True:
    delta = -delta if x > 100 or x <= -100 else delta
    
    enc = json.dumps({
            "SDS011_PM2.5": f(x),
            "SDS011_PM10": g(x),
            "SHT_Humidity": g(x),
            "SHT_Temperature": g(x),
            "BMP_Pressure": g(x),
            "BMP_Temperature": g(x),
            "MICS_CO": g(x),
            "MICS_NO2": g(x),
            "MICS_NH3": g(x),
            "MICS_C3H8": g(x),
            "MICS_C4H10": g(x),
            "MICS_CH4": g(x),
            "MICS_H2": g(x),
            "MICS_C2H5OH": g(x),
        }, indent=2)
    sock.sendto(bytes(enc, "utf-8"), ("192.168.178.20", 7777))
    x += delta
    print(enc)
    time.sleep(10)
