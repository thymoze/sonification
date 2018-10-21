#!/usr/bin/python
# -*- coding: utf-8 -*-
import sys, time, os, datetime, json
import sht75 as temp
import SDS011 as sds011
import BMP280 as BMP
from threading import Thread
from time import strftime

import socket
import struct
import subprocess
import json

filename=strftime("%Y%m%d%H%M%S")
filename+="log.txt"


################# Methodes to read every sensor ##########################
def read_SDS011():
	return sds011.main()

def read_sht():
	return temp.read()

def read_bmp():
	return BMP.read()

#################################### Read the mics sensor with C++ software ###############################
#Note: absolute path is important!
#def read_mics_old():
#	os.system("/home/pi/mittrich/ChristianMittring/Mobile_Box_Code/i2cgas/MultichannelGasSensor 0x4 1")
#	time.sleep(2)
##	code = os.system("/home/pi/mittrich/ChristianMittring/Mobile_Box_Code/i2cgas/MultichannelGasSensor 0x4 2")
#	result = subprocess.check_output(["/home/pi/mittrich/ChristianMittring/Mobile_Box_Code/i2cgas/MultichannelGasSensor","0x4" ,"2"])
#	write("MICS:\n" + result)
##        print "Error Code" + str(code)
#	time.sleep(5)

#################################### Read the mics sensor with C++ software ###############################
#Note: absolute path is important!
def read_mics():
	try:
               	code = os.system("./i2cgas/MultichannelGasSensor 0x4 1 > /dev/null")
               	#code = os.system("./i2cgas/MultichannelGasSensor 0x4 1 > /dev/null")
                time.sleep(2)
		if code == 0:
 			for i in range(10,18):
				result = subprocess.check_output(["./i2cgas/MultichannelGasSensor","0x4" ,str(i)])
				data = json.loads(result)
				#print "MICS_" + data["name"] + " : " + str(data["value"])
				write("MICS_" + data["name"], data["value"])	
				#code = os.system("/home/hcm/mittrich/ChristianMittring/Code/lib/i2cgas/MultichannelGasSensor 0x49 6 \"/opt/fhem/custom/pi-to-fhem.sh SensorBox1\"")
        except Exception as ex:
                print "Error Reading MICS GasSensors!"
                print ex


############################### Tasks to read sensors and push data ######################################
def Task_SDS011():
	value = read_SDS011()
	write_SDS011(value)

def Task_sht():
	value = read_sht()
        write_sht(value)

def Task_bmp():
	value = read_bmp()
        write_bmp(value)

def Task_MICS():
	read_mics()


############################# Parse data and send it ######################################
def write_SDS011(data):
	if data["CRC"] == "OK":
		write("SDS011_PM2.5", data["PM 2.5"])
        	write("SDS011_PM10", data["PM10"])
	else: 
		write("SDS011_PM2.5", -1.0)
		write("SDS011_PM10", -1.0)

def write_sht(data):
        write("SHT_Humidity", data["hum"])
        write("SHT_Temperature", data["temp"])


def write_bmp(data):
        write("BMP_Pressure", data["pres"])
        write("BMP_Temperature", data["temp"])


############################# Write Sensor data to array and send the result if every sensor returned data #############################
log_data = []
json_data = {}

def write(name, data):
	global log_data
	global json_data
	log_data.append(data)
	json_data[name] = data
	
def write_log():
	global log_data
	global json_data
#	print len(log_data)
	if len(log_data) == 14:
		send_log(json_data)	# send to client
		data =  datetime.datetime.now().strftime("%d.%m.%Y %H:%M:%S") + ";"
		for item in log_data:
			data += str(item) + ";"
		print "Log Data Format:"
		print "time : SDS011_PM2.5; SDS011_PM10; SHT_Humidity; SHT_Temperature; MICS_CO; MICS_NO2; MICS_NH3; MICS_C3H8; MICS_C4H10; MICS_CH4; MICS_H2; MICS_C2h5OH; BMP_Pressure; BMP_Temperature"
#		print log_data
		print data
		write_to_log(data, filename)
	else:
		print "Error: Data not complete!"
	log_data = []

def write_to_log(data, filename):

	with open(filename, "a") as LogFile:
        	LogFile.write(data + "\n")

def send_log(data):
	sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
	sock.sendto(json.dumps(data), ("192.168.43.1", 7777))
	

############################### Main Control ################################
def run_once():
	print "Reading SDS011"
	Task_SDS011()
	print "Reading SHT"
	Task_sht()
	print "Reading MICS"
	Task_MICS()
	print "Reading BMP"
	Task_bmp()
	print "Writing Log"
	write_log()
	print "------------"
	
def run_ever():
	while True:
		try:
			run_once()
			time.sleep(15)
                except Exception as ex:
                        print "Error Reading"
                        print ex


if __name__ == '__main__':
	run_ever()
