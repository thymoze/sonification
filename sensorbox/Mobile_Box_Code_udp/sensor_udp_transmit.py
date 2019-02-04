import json
import socket
import logging
import datetime
import subprocess
import time
from multiprocessing.dummy import Pool as ThreadPool
import BMP280
import sds011
from sht_sensor import Sht

IP   = '192.168.178.5'
PORT = 7777

logToFile = True
logLevel = logging.DEBUG

mics_sensors = {
    "CO" : 10,
    "NO2" : 11,
    "NH3" : 12,
    "C3H8" : 13,
    "C4H10" : 14,
    "CH4" : 15,
    "H2" : 16,
    "C2H5OH" : 17,
}

def init_mics():
    logging.debug("MICS: switching on ...")
    output = subprocess.getoutput("i2cgas/MultichannelGasSensor 0x4 1")
    logging.debug("MICS: " + output)

def stop_mics():
    logging.debug("MICS: switching off ...")
    output = subprocess.getoutput("i2cgas/MultichannelGasSensor 0x4 3")
    logging.debug("MICS: " + output)

def sensor_mics():
    result = dict.fromkeys(["MICS_" + k for k in mics_sensors.keys()], None)
    try:
        for key in mics_sensors.keys():
            outputstr = subprocess.getoutput("i2cgas/MultichannelGasSensor 0x4 {}".format(mics_sensors[key]))
            #print(outputstr)
            output = json.loads(outputstr)

            logging.info("MICS: %s - %s (raw: %s)" % (key, str(output["value"]), str(output["raw"])))
            
            result["MICS_" + key] = output["value"]
    except Exception as e:
        logging.error(e)

    return result

def sensor_bmp280():
    result = dict()
    result["BMP_Temperature"], _, result["BMP_Pressure"] = BMP280.getTempPress()

    logging.info("BMP280: TEMP - %s" % str(result["BMP_Temperature"]))
    logging.info("BMP280: PRESSURE - %s" % str(result["BMP_Pressure"]))

    return result;


def init_particle():
    global particle_sensor
    particle_sensor = sds011.SDS011("/dev/ttyS0", use_query_mode=True)

def sensor_particle():
    result = {
        "SDS011_PM2.5": None,
        "SDS011_PM10": None,
    }
    try:
        particle_sensor.sleep(sleep=False)
        time.sleep(15)
        result["SDS011_PM2.5"], result["SDS011_PM10"] = particle_sensor.query()   

        logging.info("SDS011: PM2.5 - %s" % str(result["SDS011_PM2.5"]))
        logging.info("SDS011: PM10 - %s" % str(result["SDS011_PM10"]))

        particle_sensor.sleep()
    except Exception as e:
        logging.error(e)

    return result


def init_sht75():
    global sht_sensor
    sht_sensor = Sht(23, 24)

def sensor_sht75():
    result = dict()
    result["SHT_Temperature"] = sht_sensor.read_t()
    result["SHT_Humidity"] = sht_sensor.read_rh()

    logging.info("SHT75: TEMP - %s" % str(result["SHT_Temperature"]))
    logging.info("SHT75: HUMIDITY - %s" % str(result["SHT_Humidity"]))

    return result

def execute(func):
    return func()

TASKS = [
    sensor_mics,
    sensor_bmp280,
    sensor_particle,
    sensor_sht75,
]

def main():
    handlers = [ logging.StreamHandler() ]
    if logToFile:
        handlers.append(logging.FileHandler(datetime.datetime.utcnow().isoformat().replace(":","")))

    logging.basicConfig(level=logLevel,
                        format='[%(asctime)s] %(levelname)s - %(message)s',
                        handlers=handlers)

    init_mics()
    init_particle()
    init_sht75()

    pool = ThreadPool(4)
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    #sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    #sock.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)

    while True:
        logging.info("Measuring sensors ...")
        results = pool.map(execute, TASKS)
        logging.info("Measurements complete.")
        data = json.dumps({k: v for res in results for k, v in res.items()}, indent=2)
        logging.info("Sending data to %s:%d..." % (IP, PORT))
        sock.sendto(bytes(data, "utf-8"), (IP, PORT))
        logging.info("Data sent.")
        time.sleep(5)
    stop_mics()

if __name__ == '__main__':
    main()

