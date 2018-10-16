import argparse
import random
import time

import logging
FORMAT = '%(asctime)-15s %(levelname)s:%(message)s'

def main():
    logging.basicConfig(format=FORMAT, level=logging.DEBUG)
    parser = argparse.ArgumentParser()
    parser.add_argument("--ip", default="192.168.1.109",
        help="The ip of the OSC server")
    parser.add_argument("--port", type=int, default=8000,
        help="The port the OSC server is listening on")
    args = parser.parse_args()

    from pythonosc import osc_message_builder
    from pythonosc import udp_client

    client = udp_client.SimpleUDPClient(args.ip, args.port)
    while True:
        val = random.random()

        msg = osc_message_builder.OscMessageBuilder(address="/foo/bar")
        msg.add_arg(val)
        msg.add_arg(42)
        msg.add_arg(42)
        client.send(msg.build())

        print("Send {} to {}:{}".format(val, args.ip, args.port))
        time.sleep(5)

if __name__ == '__main__':
    try:
        main()
    except KeyboardInterrupt:
        pass
