#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
#  SDS011_Feinstaub_Sensor.py
#  
#  Copyright 2017 Dr. M. Luetzelberger <webmaster@raspberryblog.de>
#  
#  This program is free software; you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation; either version 2 of the License, or
#  (at your option) any later version.
#  
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#  
#  You should have received a copy of the GNU General Public License
#  along with this program; if not, write to the Free Software
#  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
#  MA 02110-1301, USA.
#  

from __future__ import print_function
import serial, struct, time

global ser

def init():
    global ser
    ser = serial.Serial()
    #ser.port = sys.argv[1]
    ser.port = "/dev/ttyS0"
    ser.baudrate = 9600
    ser.timeout = 10

    ser.open()
    ser.flushInput()


def dump_data(d):
    return(' '.join(x.encode('hex') for x in d))

def process_frame(d):
    #dump_data(d) #debug
    r = struct.unpack('<HHxxBBB', d[2:])
    pm25 = r[0]/10.0
    pm10 = r[1]/10.0
    checksum = sum(ord(v) for v in d[2:8])%256
    return { "PM 2.5" : pm25, "PM10" : pm10, "CRC" : "OK" if (checksum==r[2] and r[3]==0xab) else "NOK"  }
    #return("PM 2.5: {} μg/m^3  PM 10: {} μg/m^3 CRC={}".format(pm25, pm10, "OK" if (checksum==r[2] and r[3]==0xab) else "NOK"))

def sensor_read():
    global ser
    byte = 0
    i = 0
    while byte != "\xaa" and i < 60:
        byte = ser.read(size=1)
        i = i + 1
    if i >= 60:
        return { "PM 2.5" : "timeout", "PM10" : "timeout", "CRC" : "OK"}
    d = ser.read(size=10)
    if d[0] == "\xc0":
        return process_frame(byte + d)
    else:
        return dump_data(d)

def read():
    global ser
    byte = 0
    i = 0
    while byte != "\xaa" and i < 60:
        byte = ser.read(size=1)
        i = i + 1
    if i >= 60:
        return { "PM 2.5" : "timeout", "PM10" : "timeout", "CRC" : "OK"}
    d = ser.read(size=10)
    return dump_data(d)
    #if d[0] == "\xc0":
    #    process_frame(byte + d)

# 0xAA, 0xB4, 0x06, 0x01, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xFF, 0x06, 0xAB
def sensor_wake():
        global ser
#        ser.write("\x01")
#        time.sleep(12)
	bytes = ['\xaa', #head
		'\xb4', #command 1
		'\x06', #data byte 1
		'\x01', #data byte 2 (set mode)
		'\x01', #data byte 3 (sleep)
		'\x00', #data byte 4
		'\x00', #data byte 5
		'\x00', #data byte 6
		'\x00', #data byte 7
		'\x00', #data byte 8
		'\x00', #data byte 9
		'\x00', #data byte 10
		'\x00', #data byte 11
		'\x00', #data byte 12
		'\x00', #data byte 13
		'\xff', #data byte 14 (device id byte 1)
		'\xaf', #data byte 15 (device id byte 2)
		'\x06', #checksum
		'\xab'] #tail

	for b in bytes:
		ser.write(b)


def sensor_sleep():
#        ser.write("\x01")
#        time.sleep(12)
        global ser
	bytes = ['\xaa', #head
		'\xb4', #command 1
		'\x06', #data byte 1
		'\x01', #data byte 2 (set mode)
		'\x00', #data byte 3 (sleep)
		'\x00', #data byte 4
		'\x00', #data byte 5
		'\x00', #data byte 6
		'\x00', #data byte 7
		'\x00', #data byte 8
		'\x00', #data byte 9
		'\x00', #data byte 10
		'\x00', #data byte 11
		'\x00', #data byte 12
		'\x00', #data byte 13
		'\xff', #data byte 14 (device id byte 1)
		'\xff', #data byte 15 (device id byte 2)
		'\x05', #checksum
		'\xab'] #tail

	for b in bytes:
		ser.write(b)


def main():
    init()
    time.sleep(2)
    sensor_wake()
    ser.flushInput()
    time.sleep(15)
    data = read()
    data = sensor_read()
    time.sleep(3)
    sensor_sleep()
    ser.flushInput()
    return data

if __name__ == '__main__':
#    import sys
#    sys.exit(main())
#     sensor_wake()
#     sensor_software()
#     read()
#     read()
    print (main())
