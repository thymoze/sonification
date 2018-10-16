## Nutzerdaten
### Wifi
* SSID: AP01
* pw: hcmTherapeutisch2017

### PiZeroW (SSH)
* user: pi
* pw: sm@rtAirMobile2017

## HW
### Sensoren
* [Gas-Sensor: MiCS-6814](http://wiki.seeedstudio.com/Grove-Multichannel_Gas_Sensor/)
* [Temperatur / Luftfeuchtigkeit: SHT75](https://www.sensirion.com/de/umweltsensoren/feuchtesensoren/steckbare-digitale-feuchtesensoren/)
* [Feinstaub: SDS011](https://eckstein-shop.de/Nova-Fitness-SDS011-Laser-PM25-PM10-Dust-Feinstaub-Sensor-Modul-Luft-Qualitaet-Detector-Built-in-Fan)
* [Luftdruck: BMP280](https://www.bosch-sensortec.com/bst/products/all_products/bmp280)

### Andere Hardware
* RTC
* Lüfter

## Software
### UDP Protocol
Sendet an: 192.168.43.1:7777

#### Aufbau
time : SDS011_PM2.5; SDS011_PM10; SHT_Humidity; SHT_Temperature; MICS_CO; MICS_NO2; MICS_NH3; MICS_C3H8; MICS_C4H10; MICS_CH4; MICS_H2; MICS_C2h5OH; BMP_Pressure; BMP_Temperature
