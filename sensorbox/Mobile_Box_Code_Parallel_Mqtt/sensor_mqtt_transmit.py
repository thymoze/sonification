
'''

(c)2018 Andreas Seiderer

'''

import concurrent.futures
import time
import subprocess
import json

import traceback
import sys

import paho.mqtt.client as mqtt
import socket
import BMP280
import sds011
from sht_sensor import Sht

import datetime


mqttTransmit = True
filewrite = True
mqttServer = "localhost"

sysname = socket.gethostname()


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

def sensor_mics():
    mqttc = None
    if mqttTransmit:
        print("MICS: mqtt enable ...")
        mqttc = mqtt.Client()
        mqttc.connect(mqttServer, 1883, 60)
        mqttc.loop_start()

    file_writers = None
    isostr = datetime.datetime.utcnow().isoformat().replace(":","")

    if filewrite:
        file_writers = {
        "CO" : open(isostr+"_"+"CO.txt", "w"),
        "NO2" : open(isostr+"_"+"NO2.txt", "w"),
        "NH3" : open(isostr+"_"+"NH3.txt", "w"),
        "C3H8" : open(isostr+"_"+"C3H8.txt", "w"),
        "C4H10" : open(isostr+"_"+"C4H10.txt", "w"),
        "CH4" : open(isostr+"_"+"CH4.txt", "w"),
        "H2" : open(isostr+"_"+"H2.txt", "w"),
        "C2H5OH" : open(isostr+"_"+"C2H5OH.txt", "w"),
        }

    print("MICS: switching on ...")
    output = subprocess.getoutput("i2cgas/MultichannelGasSensor 0x4 1")

    print("MICS: starting loop ...")
    while True:
        print("MICS: getting data ...")
        try:

            for key in mics_sensors.keys():
                outputstr = subprocess.getoutput("i2cgas/MultichannelGasSensor 0x4 {}".format(mics_sensors[key]))
                #print(outputstr)
                output = json.loads(outputstr)
                if mqttc is not None:
                    mqttc.publish(sysname+"/mics/{}".format(key), payload=output["value"], qos=0, retain=False)
                    mqttc.publish(sysname+"/mics/{}/raw".format(key), payload=output["raw"], qos=0, retain=False)

                if filewrite:
                    timestampstr = datetime.datetime.utcnow().isoformat()
                    outstr = timestampstr+";"+str(output["value"])+";"+str(output["raw"])+"\n"
                    file_writers[key].write(outstr)
                    file_writers[key].flush()

            time.sleep(1)
        except Exception as e:
            traceback.print_exc()
            print(e)

    output = subprocess.getoutput("i2cgas/MultichannelGasSensor 0x4 3")
    print(output)



def sensor_bmp280():
    mqttc = None
    if mqttTransmit:
        print("BMP280: mqtt enable ...")
        mqttc = mqtt.Client()
        mqttc.connect(mqttServer, 1883, 60)
        mqttc.loop_start()

    file_writer = None
    if filewrite:
        file_writer = open(datetime.datetime.utcnow().isoformat().replace(":","")+"_"+"bmp280.txt", "w")

    while True:
        print("BMP280: getting data ...")

        cTemp, fTemp, pressure = BMP280.getTempPress()
        if mqttc is not None:
            mqttc.publish(sysname+"/bmp280/temperature", payload=cTemp, qos=0, retain=False)
            mqttc.publish(sysname+"/bmp280/pressure", payload=pressure, qos=0, retain=False)

        if filewrite:
            file_writer.write(datetime.datetime.utcnow().isoformat()+";"+str(cTemp)+";"+str(pressure)+"\n")
            file_writer.flush()
     
        time.sleep(1)


def sensor_particle():
    mqttc = None
    if mqttTransmit:
        print("SDS011: mqtt enable ...")
        mqttc = mqtt.Client()
        mqttc.connect(mqttServer, 1883, 60)
        mqttc.loop_start()

    file_writer = None
    if filewrite:
        file_writer = open(datetime.datetime.utcnow().isoformat().replace(":","")+"_"+"sds011.txt", "w")

    sensor = sds011.SDS011("/dev/ttyS0", use_query_mode=True)

    while True:
        print("SDS011: getting data ...")
        try:
            sensor.sleep(sleep=False)
            time.sleep(15)
            pm2_5, pm10 = sensor.query()
        
            if mqttc is not None:
                mqttc.publish(sysname+"/sds011/pm2.5", payload=pm2_5, qos=0, retain=False)
                mqttc.publish(sysname+"/sds011/pm10", payload=pm10, qos=0, retain=False)

            if filewrite:
                file_writer.write(datetime.datetime.utcnow().isoformat()+";"+str(pm2_5)+";"+str(pm10)+"\n")
                file_writer.flush()

            sensor.sleep()
            time.sleep(5)

        except Exception as e:
            print(e)


def sensor_sdht75():
    mqttc = None
    if mqttTransmit:
        print("SHT75: mqtt enable ...")
        mqttc = mqtt.Client()
        mqttc.connect(mqttServer, 1883, 60)
        mqttc.loop_start()

    file_writer = None
    if filewrite:
        file_writer = open(datetime.datetime.utcnow().isoformat().replace(":","")+"_"+"sht75.txt", "w")

    sht = Sht(23, 24)

    while True:
        print("SHT75: getting data ...")

        cTemp = sht.read_t()
        humidity = sht.read_rh()

        if mqttc is not None:
            mqttc.publish(sysname+"/sht75/temperature", payload=cTemp, qos=0, retain=False)
            mqttc.publish(sysname+"/sht75/humidity", payload=humidity, qos=0, retain=False)
     
        if filewrite:
            file_writer.write(datetime.datetime.utcnow().isoformat()+";"+str(cTemp)+";"+str(humidity)+"\n")
            file_writer.flush()

        time.sleep(1)


def sensor_cpu():
    mqttc = None
    if mqttTransmit:
        print("CPU: mqtt enable ...")
        mqttc = mqtt.Client()
        mqttc.connect(mqttServer, 1883, 60)
        mqttc.loop_start()

    file_writer = None
    if filewrite:
        file_writer = open(datetime.datetime.utcnow().isoformat().replace(":","")+"_"+"cpu.txt", "w")

    while True:
        print("CPU: getting data ...")

        outputstr = subprocess.getoutput("vcgencmd measure_temp").replace("temp=","")[:-2]

        if mqttc is not None:
            mqttc.publish(sysname+"/cpu/temperature", payload=outputstr, qos=0, retain=False)

        if filewrite:
            file_writer.write(datetime.datetime.utcnow().isoformat()+";"+str(outputstr)+"\n")
            file_writer.flush()
     
        time.sleep(5)


with concurrent.futures.ProcessPoolExecutor(max_workers=5) as executor:
    executor.submit(sensor_mics)
    executor.submit(sensor_bmp280)
    executor.submit(sensor_particle)
    executor.submit(sensor_sdht75)
    executor.submit(sensor_cpu)

