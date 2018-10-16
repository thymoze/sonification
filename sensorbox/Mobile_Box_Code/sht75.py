from sht_sensor import Sht

def read():
	sht = Sht(23, 24)
	return { "hum" : sht.read_rh(), "temp" : sht.read_t() }
