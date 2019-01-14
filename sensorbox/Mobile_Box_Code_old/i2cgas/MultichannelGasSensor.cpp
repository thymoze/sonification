/*
    MutichannelGasSensor.cpp
    2015 Copyright (c) Seeed Technology Inc.  All right reserved.

power on: 50 mA
power off: 4 mA
    http://www.seeed.cc/
*/
#include <math.h>
#include <unistd.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <linux/i2c-dev.h>
#include <sys/ioctl.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <cmath>
#include <errno.h>
#include <string.h>
#include <cstdlib>
#include <fstream>
#include <sstream>
#include <iostream>
using namespace std;

#include "MultichannelGasSensor.h"


/*********************************************************************************************************
** Function name:           begin
** Descriptions:            initialize I2C
*********************************************************************************************************/
MutichannelGasSensor::MutichannelGasSensor ()
{
    i2cFD = -1; useLED = true; icmd = 0;
    adcValueR0_NH3_Buf = 0;
    adcValueR0_CO_Buf = 0;
    adcValueR0_NO2_Buf = 0;
}

MutichannelGasSensor::~MutichannelGasSensor ()
{
    if ( i2cFD >= 0 ) {
        close ( i2cFD );
    }
}


bool MutichannelGasSensor::setAddr ()
{
    if ( ioctl ( i2cFD, I2C_SLAVE_FORCE, i2cAddress ) < 0 ) {
      perror ( "ioctl() I2C_SLAVE failed\n" );
      return false;
     }
     return true;
}

void MutichannelGasSensor::begin ( int address )
{
    __version = 1;          // version 1/2

    //printf ( "Opening device...\n" );
    
    if ( ( i2cFD = open ( "/dev/i2c-1", O_RDWR ) ) < 0 )
    {
		perror ( "open() failed\n" );
		exit (1);
    }
    
    i2cAddress = address;

    if ( !setAddr () ) {
        return;
    }
    
    if ( icmd == 6 ) {
    	__version = 2;
    }
    else {
    	__version = getVersion ();
    }
}

unsigned char MutichannelGasSensor::getVersion ( )
{
    int v = get_addr_dta ( CMD_READ_EEPROM, ADDR_IS_SET );
    //printf ( "getVersion: %i\n", v );

    if ( v == 1126 ) {
        __version = 2;
    }
    else __version = 1;

    return __version;
}

void MutichannelGasSensor::begin()
{
    begin ( DEFAULT_I2C_ADDR );
}

/*********************************************************************************************************
** Function name:           sendI2C
** Descriptions:            send one byte to I2C Wire
*********************************************************************************************************/
void MutichannelGasSensor::sendI2C ( unsigned char dta )
{
    write ( i2cFD, &dta, 1 );
}


unsigned int MutichannelGasSensor::get_addr_dta ( unsigned char addr_reg )
{
START:
    unsigned char raw [ 4 ];
    raw [ 0 ] = addr_reg;

    if ( write ( i2cFD, raw, 1 ) != 1 )
    {
        printf ( "ERROR: write() failed\n" );
        return -1;
    }

    unsigned int dta = 0;

    raw [ 0 ] = 0xFF;
    raw [ 1 ] = 0xFF;

    int rcv = 2;
    int cnt = 0;
    int rd = 0;
    int timeout = 0;
    
    usleep ( 2000 );

    while ( cnt < rcv )
    {
        rd = read ( i2cFD, raw + cnt, rcv );
        if ( rd > 0 ) {
            cnt += rd;
        }
        if ( cnt >= rcv ) {
            break;
        }
        rcv -= rd;
        
        printf ( "WARN: get_addr_dta1 - read() [%i | %i | 0x%X | 0x%X | 0x%X | 0x%X]\n", cnt, rd, raw [0], raw [1], raw [2], raw [3] );

        if(timeout++ > 100)
            return -2;//time out
        usleep ( 2000 );
    }

    if ( cnt < 2 )
    {
        printf ( "ERROR: get_addr_dta1 - read() failed [%i | %i | %i]\n", cnt, rd, timeout );
        return -1;
    }

    if ( cnt != 2 )
    {
        printf ( "WARN: get_addr_dta1 - read() [%i | %i | %i | 0x%X | 0x%X | 0x%X | 0x%X]\n", cnt, rd, timeout, raw [0], raw [1], raw [2], raw [3] );
    }
    
    dta = raw[0];
    dta <<= 8;
    dta += raw[1];
    
    switch ( addr_reg )
    {
        case CH_VALUE_NH3:
        
        if ( dta > 0 )
        {
            adcValueR0_NH3_Buf = dta;
        }
        else 
        {
            dta = adcValueR0_NH3_Buf;
        }
        
        break;
        
        case CH_VALUE_CO:
        
        if ( dta > 0 )
        {
            adcValueR0_CO_Buf = dta;
        }
        else 
        {
            dta = adcValueR0_CO_Buf;
        }
        
        break;
        
        case CH_VALUE_NO2:
        
        if ( dta > 0 )
        {
            adcValueR0_NO2_Buf = dta;
        }
        else 
        {
            dta = adcValueR0_NO2_Buf;
        }
        
        break;
        
        default:;
    }
    return dta;
}


bool MutichannelGasSensor::get_addr_dta ( int * dta )
{
START:
    unsigned char raw [ 10 ];
    raw [ 0 ] = CMD_ADC_RESALL;

    if ( write ( i2cFD, raw, 1 ) != 1 )
    {
        printf ( "ERROR: write() failed\n" );
        return false;
    }

    raw [ 0 ] = 0xFF;
    raw [ 1 ] = 0xFF;

    int rcv = 6;
    int cnt = 0;
    int rd = 0;
    int timeout = 0;
    
    usleep ( 2000 );

    while ( cnt < rcv )
    {
        rd = read ( i2cFD, raw + cnt, rcv );
        if ( rd > 0 ) {
            cnt += rd;
        }
        if ( cnt >= rcv ) {
            break;
        }
        rcv -= rd;
        
        printf ( "WARN: get_addr_dta3 - read() [%i | %i | 0x%X | 0x%X | 0x%X | 0x%X]\n", cnt, rd, raw [0], raw [1], raw [2], raw [3] );

        if(timeout++ > 100)
            return -2;//time out
        usleep ( 2000 );
    }

    if ( cnt < 6 )
    {
        printf ( "ERROR: get_addr_dta3 - read() failed [%i | %i | %i]\n", cnt, rd, timeout );
        return false;
    }

    if ( cnt != 6 )
    {
        printf ( "WARN: get_addr_dta3 - read() [%i | %i | %i | 0x%X | 0x%X | 0x%X | 0x%X]\n", cnt, rd, timeout, raw [0], raw [1], raw [2], raw [3] );
    }
    
    int reg;
    
    reg = raw[0];
    reg <<= 8;
    reg += raw[1];
    
    if ( reg > 0 ) {
        adcValueR0_NH3_Buf = reg;
    }
    else {
        reg = adcValueR0_NH3_Buf;
    }
    dta [ 0 ] = reg;    
    
    reg = raw[2];
    reg <<= 8;
    reg += raw[3];
    
    if ( reg > 0 ) {
        adcValueR0_CO_Buf = reg;
    }
    else {
        reg = adcValueR0_CO_Buf;
    }
    dta [ 1 ] = reg;
    
    reg = raw[4];
    reg <<= 8;
    reg += raw[5];
    
    if ( reg > 0 ) {
        adcValueR0_NO2_Buf = reg;
    }
    else {
        reg = adcValueR0_NO2_Buf;
    }
    dta [ 2 ] = reg;
    
    return true;
}


unsigned int MutichannelGasSensor::get_addr_dta ( unsigned char addr_reg, unsigned char __dta )
{
    
START:
    unsigned char raw [ 4 ];
    raw [ 0 ] = addr_reg;
    raw [ 1 ] = __dta;

    if ( write ( i2cFD, raw, 2 ) != 2 )
    {
        printf ( "ERROR: write() failed\n" );
        return -1;
    }

    unsigned int dta = 0;

    raw [ 0 ] = 0xFF;
    raw [ 1 ] = 0xFF;

    int rcv = 2;
    int cnt = 0;
    int rd = 0;
    int timeout = 0;
    
    usleep ( 2000 );

    while ( cnt < rcv )
    {
        rd = read ( i2cFD, raw + cnt, rcv );
        if ( rd > 0 ) {
            cnt += rd;
        }
        if ( cnt >= rcv ) {
            break;
        }
        rcv -= rd;
        
        printf ( "WARN: get_addr_dta2 - read() [%i | %i | 0x%X | 0x%X | 0x%X | 0x%X]\n", cnt, rd, raw [0], raw [1], raw [2], raw [3] );

        if(timeout++ > 100)
            return -2;//time out
        usleep ( 2000 );
    }

    if ( cnt < 2 )
    {
        printf ( "ERROR: get_addr_dta2 - read() failed [%i | %i | %i]\n", cnt, rd, timeout );
        return -1;
    }

    if ( cnt != 2 )
    {
        printf ( "WARN: get_addr_dta2 - read() [%i | %i | %i | 0x%X | 0x%X | 0x%X | 0x%X]\n", cnt, rd, timeout, raw [0], raw [1], raw [2], raw [3] );
    }
    //printf ( "get_addr_dta22: 0x%X 0x%X\n", raw [ 0 ], raw [ 1 ] );

    dta = raw[0];
    dta <<= 8;
    dta += raw[1];
    

    return dta;
}

void MutichannelGasSensor::write_i2c(unsigned char addr, unsigned char *dta, unsigned char dta_len)
{
    if ( write ( i2cFD, dta, dta_len ) != dta_len ) {
        printf ( "ERROR: write_i2c - write() failed\n" );
    }
}


/*********************************************************************************************************
** Function name:           readData
** Descriptions:            read 4 bytes from I2C slave
*********************************************************************************************************/
int16_t MutichannelGasSensor::readData ( uint8_t cmd )
{
    uint16_t timeout = 0;
    uint8_t buffer[4];
    uint8_t checksum = 0;
    int16_t rtnData = 0;

    //send command
    sendI2C ( cmd );
    //wait for a while
    usleep ( 2000 );
    //get response
    int cnt = read ( i2cFD, buffer, 4 );
    if ( cnt != 4 ) {
        printf ( "ERROR: readData - read() failed\n" );
        return -2;
    }
    
    checksum = (uint8_t)(buffer[0] + buffer[1] + buffer[2]);
    //printf ( "readData checksum 0x%.2X : 0x%.2X\n", checksum, buffer[3] );
    if ( checksum != buffer[3] ) {
        printf ( "readData checksum wrong\n" );
        return -4;//checksum wrong
    }
    rtnData = ((buffer[1] << 8) + buffer[2]);

    return rtnData;//successful
}

/*********************************************************************************************************
** Function name:           readR0
** Descriptions:            read R0 stored in slave MCU
*********************************************************************************************************/
int16_t MutichannelGasSensor::readR0(void)
{
    int16_t rtnData = 0;

    rtnData = readData(0x11);
   
    if(rtnData > 0)
        res0[0] = rtnData;
    else
        return rtnData;         //unsuccessful

    rtnData = readData(0x12);
    if(rtnData > 0)
        res0[1] = rtnData;
    else
        return rtnData;         //unsuccessful

    rtnData = readData(0x13);
    if(rtnData > 0)
        res0[2] = rtnData;
    else
        return rtnData;         //unsuccessful

    return 1;//successful
}

/*********************************************************************************************************
** Function name:           readR
** Descriptions:            read resistance value of each channel from slave MCU
*********************************************************************************************************/
int16_t MutichannelGasSensor::readR(void)
{
    int16_t rtnData = 0;

    //printf ( "readData 0x01\n" );
    rtnData = readData ( 0x01 );
    if(rtnData >= 0)
        res[0] = rtnData;
    else
        return rtnData;//unsuccessful

    //printf ( "readData 0x02\n" );
    rtnData = readData ( 0x02 );
    if(rtnData >= 0)
        res[1] = rtnData;
    else
        return rtnData;//unsuccessful

    //printf ( "readData 0x03\n" );
    rtnData = readData ( 0x03 );
    if(rtnData >= 0)
        res[2] = rtnData;
    else
        return rtnData;//unsuccessful

    return 0;//successful
}


int gasresistors [ 3 ] = { 0, 0, 0 };
int gasRegs [ 3 ] = { 0, 0, 0 };

int MutichannelGasSensor::readGases ()
{
	int regs [ 3 ] = { 0, 0, 0 };
	   
	regs [ 0 ] = get_addr_dta(CH_VALUE_NH3);
	regs [ 1 ] = get_addr_dta(CH_VALUE_CO);
	regs [ 2 ] = get_addr_dta(CH_VALUE_NO2);
	
	if ( !regs [ 0 ] || !regs [ 0 ] || !regs [ 0 ] ) {
		printf ( "ERROR: readGases - failed [ %i : %i : %i ]\n", regs [ 0 ], regs [ 1 ], regs [ 2 ] );
		return -1;
	}
	
	if ( regs [ 0 ] == gasRegs [ 0 ] || regs [ 1 ] == gasRegs [ 1 ] || regs [ 2 ] == gasRegs [ 2 ] ) {
		return 0;
	}
	
	gasRegs [ 0 ] = regs [ 0 ];
	gasRegs [ 1 ] = regs [ 1 ];
	gasRegs [ 2 ] = regs [ 2 ];
	return 1;
}


/*********************************************************************************************************
** Function name:           readR
** Descriptions:            calculate gas concentration of each channel from slave MCU
** Parameters:
                            gas - gas type
** Returns:
                            float value - concentration of the gas
*********************************************************************************************************/
float MutichannelGasSensor::calcGas ( int gas, bool useCache )
{

    float ratio0, ratio1, ratio2;
    if(1 == __version)
    {
        if(readR() < 0)
            return -2.0f;

        ratio0 = (float)res[0] / res0[0];
        ratio1 = (float)res[1] / res0[1];
        ratio2 = (float)res[2] / res0[2];
    }
    else if(2 == __version)
    {
        // how to calc ratio/123
        ledOn();

        int A0_0 = gasresistors [0];
        if ( A0_0 == 0 ) {
        	gasresistors [0] = A0_0 = get_addr_dta(6, ADDR_USER_ADC_HN3);
        }
        int A0_1 = gasresistors [1];
        if ( A0_1 == 0 ) {
        	gasresistors [1] = A0_1 = get_addr_dta(6, ADDR_USER_ADC_CO);
        }
        int A0_2 = gasresistors [2];
        if ( A0_2 == 0 ) {
        	gasresistors [2] = A0_2 = get_addr_dta(6, ADDR_USER_ADC_NO2);
        }
        
        int An [ 3 ];
        An [ 0 ] = gasRegs [ 0 ];
        An [ 1 ] = gasRegs [ 1 ];
        An [ 2 ] = gasRegs [ 2 ];
        
        if ( !useCache || !An [ 0 ] || !An [ 1 ] || !An [ 2 ] ) {    
        	An [ 0 ] = get_addr_dta(CH_VALUE_NH3);
        	An [ 1 ] = get_addr_dta(CH_VALUE_CO);
        	An [ 2 ] = get_addr_dta(CH_VALUE_NO2);
        	/*if ( !get_addr_dta ( An ) ) {
        		printf ( "ERROR: calcGas - get_addr_dta() failed\n" );
            	return -2.0f;
        	}*/
        }
        
        int An_0 = An [ 0 ];
        int An_1 = An [ 1 ];
        int An_2 = An [ 2 ];
                
        ratio0 = (float)An_0/(float)A0_0*(1023.0-A0_0)/(1023.0-An_0);
        ratio1 = (float)An_1/(float)A0_1*(1023.0-A0_1)/(1023.0-An_1);
        ratio2 = (float)An_2/(float)A0_2*(1023.0-A0_2)/(1023.0-An_2);        
    }
    
    float c = 0;

    switch(gas)
    {
        case CO:
        {
            c = pow(ratio1, -1.179)*4.385;  //mod by jack
            break;
        }
        case NO2:
        {
            c = pow(ratio2, 1.007)/6.855;  //mod by jack
            break;
        }
        case NH3:
        {
            c = pow(ratio0, -1.67)/1.47;  //modi by jack
            break;
        }
        case C3H8:  //add by jack
        {
            c = pow(ratio0, -2.518)*570.164;
            break;
        }
        case C4H10:  //add by jack
        {
            c = pow(ratio0, -2.138)*398.107;
            break;
        }
        case CH4:  //add by jack
        {
            c = pow(ratio1, -4.363)*630.957;
            break;
        }
        case H2:  //add by jack
        {
            c = pow(ratio1, -1.8)*0.73;
            break;
        }
        case C2H5OH:  //add by jack
        {
            c = pow(ratio1, -1.552)*1.622;
            break;
        }
        default:
            break;
    }
    
    if ( 2 ==__version ) ledOff ();
    return isnan(c)?-3:c;
}

/*********************************************************************************************************
** Function name:           changeI2cAddr
** Descriptions:            change I2C address of the slave MCU, and this address will be stored in EEPROM of slave MCU
*********************************************************************************************************/
void MutichannelGasSensor::changeI2cAddr(uint8_t newAddr)
{
    unsigned char buffer [2];
    buffer [ 0 ] = 0x23;
    buffer [ 1 ] = newAddr;

    if ( write ( i2cFD, buffer, 2 ) != 2 ) {
        printf ( "ERROR: changeI2cAddr - write() failed\n" );
    }
    else {
    	i2cAddress = newAddr;
    	
    	printf ( "I2C address changed to 0x%X\n", newAddr );
    }
}

/*********************************************************************************************************
** Function name:           doCalibrate
** Descriptions:            tell slave to do a calibration, it will take about 8s
                            after the calibration, must reread the R0 values
*********************************************************************************************************/
void MutichannelGasSensor::doCalibrate(void)
{
    if ( 1 == __version )
    {
    START:
        sendI2C ( 0x22 );

        if ( readR0() > 0 )
        {
            for(int i=0; i<3; i++)
            {
                //printf(res0[i]);
                printf("\t");
            }
        }
        else
        {
            sleep(5);
            printf("continue...\n");
            for(int i=0; i<3; i++)
            {
                //printf(res0[i]);
                printf("\t");
            }
            printf("\n");
            goto START;
        }
    }
    else if(2 == __version)
    {
        unsigned int i, a0, a1, a2;
        while(1)
        {
            a0 = get_addr_dta(CH_VALUE_NH3);
            a1 = get_addr_dta(CH_VALUE_CO);
            a2 = get_addr_dta(CH_VALUE_NO2);
            /*
            printf(a0);
            printf("\t");
            printf(a1);
            printf("\t");
            printf(a2);
            printf("\t");
            */
            ledOn();
            
            int cnt = 0;
            for(i=0; i<20; i++)
            {
                if((a0 - get_addr_dta(CH_VALUE_NH3)) > 2 || (get_addr_dta(CH_VALUE_NH3) - a0) > 2)cnt++;
                if((a1 - get_addr_dta(CH_VALUE_CO)) > 2 || (get_addr_dta(CH_VALUE_CO) - a1) > 2)cnt++;
                if((a2 - get_addr_dta(CH_VALUE_NO2)) > 2 || (get_addr_dta(CH_VALUE_NO2) - a2) > 2)cnt++;
                
                if(cnt>5)
                {
                    break;
                }
                sleep(1);
            }
                        
            ledOff();
            if(cnt <= 5)break;
            sleep(1);
        }
        /*
        printf("write user adc value: ");
        printf(a0);printf("\t");
        printf(a1);printf("\t");
        printf(a2);printf("\n");
        */
        unsigned char tmp[7];
    
        tmp[0] = 7;

        tmp[1] = a0>>8;
        tmp[2] = a0&0xff;
           
        tmp[3] = a1>>8;
        tmp[4] = a1&0xff;

        tmp[5] = a2>>8;
        tmp[6] = a2&0xff;
            
        write_i2c ( i2cAddress, tmp, 7 );
    }
}

/*********************************************************************************************************
** Function name:           powerOn
** Descriptions:            power on sensor heater
*********************************************************************************************************/
void MutichannelGasSensor::powerOn(void)
{
    if ( __version == 1 )
        sendI2C ( 0x21 );
    else if ( __version == 2 )
    {
        dta_test[0] = 11;
        dta_test[1] = 1;
        write_i2c ( i2cAddress, dta_test, 2 );
    }
}

/*********************************************************************************************************
** Function name:           powerOff
** Descriptions:            power off sensor heater
*********************************************************************************************************/
void MutichannelGasSensor::powerOff(void)
{
    if ( __version == 1 )
        sendI2C ( 0x20 );
    else if ( __version == 2 )
    {
        dta_test[0] = 11;
        dta_test[1] = 0;
        write_i2c (i2cAddress, dta_test, 2 );
    }
}

void MutichannelGasSensor::display_eeprom ()
{
    if ( __version == 1 )
    {
        printf("ERROR: display_eeprom() is NOT support by V1 firmware.\n");
        return;
    }
        
    printf("ADDR_IS_SET = %d\n", get_addr_dta(CMD_READ_EEPROM, ADDR_IS_SET));
    printf("ADDR_FACTORY_ADC_NH3 = %d\n", get_addr_dta(CMD_READ_EEPROM, ADDR_FACTORY_ADC_NH3));
    printf("ADDR_FACTORY_ADC_CO = %d\n", get_addr_dta(CMD_READ_EEPROM, ADDR_FACTORY_ADC_CO));
    printf("ADDR_FACTORY_ADC_NO2 = %d\n", get_addr_dta(CMD_READ_EEPROM, ADDR_FACTORY_ADC_NO2));
    printf("ADDR_USER_ADC_HN3 = %d\n", get_addr_dta(CMD_READ_EEPROM, ADDR_USER_ADC_HN3));
    printf("ADDR_USER_ADC_CO = %d\n", get_addr_dta(CMD_READ_EEPROM, ADDR_USER_ADC_CO));
    printf("ADDR_USER_ADC_NO2 = %d\n", get_addr_dta(CMD_READ_EEPROM, ADDR_USER_ADC_NO2));
    printf("ADDR_I2C_ADDRESS = %d\n", get_addr_dta(CMD_READ_EEPROM, ADDR_I2C_ADDRESS));
    
}

float MutichannelGasSensor::getR0(unsigned char ch)         // 0:CH3, 1:CO, 2:NO2
{
    if(__version == 1)
    {
        printf("ERROR: getR0() is NOT support by V1 firmware.");
        return -1;
    }
    
    int a = 0;
    switch(ch)
    {
        case 0:         // CH3
        a = get_addr_dta(CMD_READ_EEPROM, ADDR_USER_ADC_HN3);
        printf("a_ch3 = %i", a);
        break;
        
        case 1:         // CO
        a = get_addr_dta(CMD_READ_EEPROM, ADDR_USER_ADC_CO);
        printf("a_co = %i", a);
        break;
        
        case 2:         // NO2
        a = get_addr_dta(CMD_READ_EEPROM, ADDR_USER_ADC_NO2);
        printf("a_no2 = %i", a);
        break;
        
        default:;
    }

    float r = 56.0*(float)a/(1023.0-(float)a);
    return r;
}

float MutichannelGasSensor::getRs(unsigned char ch)         // 0:CH3, 1:CO, 2:NO2
{
    
    if(__version == 1)
    {
        printf("ERROR: getRs() is NOT support by V1 firmware.");
        return -1;
    }
    
    int a = 0;
    switch(ch)
    {
        case 0:         // NH3
        a = get_addr_dta(1);
        break;
        
        case 1:         // CO
        a = get_addr_dta(2);
        break;
        
        case 2:         // NO2
        a = get_addr_dta(3);
        break;
        
        default:;
    }
    
    float r = 56.0*(float)a/(1023.0-(float)a);
    return r;
}

// 1. change i2c address to 0x04
// 2. change adc value of R0 to default
void MutichannelGasSensor::factory_setting()
{
    unsigned char tmp[7];

    unsigned char error;
    unsigned char address = 0;
    
    for ( address = 1; address < 127; address++ )
    {
        // The i2c_scanner uses the return value of
        // the Write.endTransmisstion to see if
        // a device did acknowledge to the address.
        //Wire.beginTransmission(address);
        //error = Wire.endTransmission();

        //if (error == 0)
        {
            // change i2c to 0x04
            
            printf("I2C address is: 0x%X", address);
            printf("Change I2C address to 0x04");
            
            dta_test[0] = CMD_CHANGE_I2C;
            dta_test[1] = 0x04;
            write_i2c ( address, dta_test, 2 );

            i2cAddress = 0x04;
            usleep(100000);
            getVersion();
            break;
        }
    }

    unsigned int a0 = get_addr_dta(CMD_READ_EEPROM, ADDR_FACTORY_ADC_NH3);
    unsigned int a1 = get_addr_dta(CMD_READ_EEPROM, ADDR_FACTORY_ADC_CO);
    unsigned int a2 = get_addr_dta(CMD_READ_EEPROM, ADDR_FACTORY_ADC_NO2);
    
    tmp[0] = 7;
    tmp[1] = a0>>8;
    tmp[2] = a0&0xff;     
    tmp[3] = a1>>8;
    tmp[4] = a1&0xff;

    tmp[5] = a2>>8;
    tmp[6] = a2&0xff;   
    usleep ( 100000 );
    write_i2c ( i2cAddress, tmp, 7 );
    usleep ( 100000 );
}

void MutichannelGasSensor::change_i2c_address(unsigned char addr)
{
    dta_test[0] = CMD_CHANGE_I2C;
    dta_test[1] = addr;
    write_i2c ( i2cAddress, dta_test, 2 );
    
    
    printf ( "FUNCTION: CHANGE I2C ADDRESS: 0X%X\n", addr );

    i2cAddress = addr;
}
    
MutichannelGasSensor gas;

const char * gases[] = {
    "CO", "NO2", "NH3", "C3H8", "C4H10", "CH4", "H2", "C2H5OH"
};

#define GAS_COUNT	(sizeof(gases)/sizeof(gases[0]))

const char * gasNames[] = {
    "Carbon-monoxide", "Nitrogen-dioxide", "Ammonia", "Propane", "Iso-butane", "Methane", "Hydrogen", "Ethanol"
};

float gascache [ GAS_COUNT ] = { 0 };

int cacheSeq = -1;

void LoadCache ()
{
    ifstream confFile ( "mcg_cache.txt" );

    if ( confFile.good () )
    {
		int i = 0;
		int r = 0;
		int rgs = 0;
		bool hasSeq = false;
	
        string line;
        while ( getline ( confFile, line ) )
        {
            const char * chars = line.c_str ();
            
            if ( !hasSeq ) {
            	hasSeq = true; cacheSeq = atoi ( chars );
        		continue;
            }
            
        	if ( r < 3 ) {
        		gasresistors [ r ] = atoi ( chars );
        		r++;
        		continue;
        	}
            
        	if ( rgs < 3 ) {
        		gasRegs [ rgs ] = atoi ( chars );
        		rgs++;
        		continue;
        	}
            
            gascache [ i ] = atof ( chars );
            i++;
            
            if ( i >= GAS_COUNT ) {
        		break;
            }
        }
        confFile.close ();
    }
}


void SaveCache ()
{
    ofstream confFile ( "mcg_cache.txt" );

    if ( confFile.good () )
    {
		char buffer [ 64 ];
        
        ++cacheSeq; if ( cacheSeq < 0 ) cacheSeq = 1;
        
        sprintf ( buffer, "%i", cacheSeq );
            
    	confFile << buffer << endl;
	
        for ( int r = 0; r < 3; ++r ) {
            
            sprintf ( buffer, "%i", gasresistors [r] );
            
    		confFile << buffer << endl;
        }
	
        for ( int r = 0; r < 3; ++r ) {
            
            sprintf ( buffer, "%i", gasRegs [r] );
            
    		confFile << buffer << endl;
        }
        
        for ( int g = 0; g < GAS_COUNT; ++g ) {
            
            sprintf ( buffer, "%.8f", gascache [g] );
            
    		confFile << buffer << endl;
        }
        confFile.close ();
    }
}


void printUsage ( const char * prog )
{
    printf ( "Usage: %s addr cmd [syscall|newi2c] [prefix]\n", prog );
    printf ( "  addr: i2c address as hex. must be >= 0x4\n" );
    printf ( "  cmd: 0  = power on, measure, power off\n" );
    printf ( "  cmd: 1  = power on\n" );
    printf ( "  cmd: 2  = measure\n" );
    printf ( "  cmd: 3  = power off\n" );
    printf ( "  cmd: 4  = show eeprom\n" );
    printf ( "  cmd: 5  = get version\n" );
    printf ( "  cmd: 6  = measure and use cache\n" );
    printf ( "  cmd: 9  = change i2c address\n" );
    printf ( "       newi2c = new i2c address as hex\n" );
    printf ( "  cmd: 10 = get %s     (%s)\n", gases [0], gasNames [0] );
    printf ( "  cmd: 11 = get %s    (%s)\n", gases [1], gasNames [1] );
    printf ( "  cmd: 12 = get %s    (%s)\n", gases [2], gasNames [2] );
    printf ( "  cmd: 13 = get %s   (%s)\n", gases [3], gasNames [3] );
    printf ( "  cmd: 14 = get %s  (%s)\n", gases [4], gasNames [4] );
    printf ( "  cmd: 15 = get %s    (%s)\n", gases [5], gasNames [5] );
    printf ( "  cmd: 16 = get %s     (%s)\n", gases [6], gasNames [6] );
    printf ( "  cmd: 17 = get %s (%s)\n", gases [7], gasNames [7] );
}


int main ( int argc, char** argv )
{
    if ( argc < 3 ) {
        printUsage ( argv [0] );
        return 1;
    }
    
    int addr = 0;
    if ( argv[1] ) addr = strtol ( argv[1], NULL, 16 );
    if ( addr < 4 ) addr = 0;

    int inst = 0;
    if ( argv[2] ) inst = atoi ( argv[2] );
    if ( inst < 0 || ( inst > 6 && inst < 9 ) || inst > 17 ) inst = 0;
    	
    char prefix [ 16 ];
    char cmd [ 1024 ];
    int len = 0;

    if ( argc > 3 && inst != 9 && argv [3] ) {
        len = sprintf ( cmd, "%s ", argv [3] );
        
        printf ( "Cmd: %s\n", cmd );
    }

    if ( argc > 4 && inst != 9 && argv [4] ) {
        sprintf ( prefix, "%s", argv [4] );
        
        printf ( "Prefix: %s\n", prefix );
    }
    else {
        *prefix = 0;
    }

	gas.icmd = inst;
	
    gas.begin ( addr );

    if ( inst == 4 ) {
        gas.display_eeprom ();
        return 0;
    }

	if ( inst == 9 ) {
    	if ( argc > 3 && argv[3] ) {
			int addrn = strtol ( argv[3], NULL, 16 );
        	printf ( "Addr: 0x%X\n", addrn );
        
			if ( addrn >= 4 ) {
				gas.change_i2c_address ( addrn );
				return 0;
			}
		}
		return 1;
	}
    
    int ret = 0;

    if ( inst <= 1 ) {
        printf ( "Power on!\n" );
        gas.powerOn ();

        if ( inst == 1 ) gas.ledOn ();
    }

    if ( inst == 5 ) {
		printf ( "Firmware Version = %d\n", gas.getVersion () );
    }
    
    float c, v;
        
    if ( inst >= 10 ) {
		int g = inst - 10;
	
        c = gas.calcGas ( g, false );
        if ( c >= 0 ) { v = c; }
        else { v = -1; ret = 2; }
        
//        printf ( "%s \t %20.8f ppm (%s)\n", gases [g], v, gasNames [g] );

        printf ("{\"name\" : \"%s\", \"value\" :%20.8f}",gases[g], v);

        if ( len > 1 && v >= 0 ) {
            sprintf ( cmd + len, "%s%s %f", prefix, gases [g], v );
            system ( cmd );
        }
    }
    else if ( inst == 0 || inst == 2 || inst == 6 )
    {
		if ( inst == 6 )  { gas.useLED = true; LoadCache (); }
	
		int failCounter = 0, cacheCounter = 0;
				
REPEAT:		
		int res = 1;
		if ( inst == 6 ) res = gas.readGases ();
		
		if ( res > 0 ) {
			for ( int g = 0; g < GAS_COUNT; ++g ) {
				c = gas.calcGas ( g, inst == 6 );
		
				if ( c >= 0 ) {
					gascache [ g ] = c;
					v = c;
					if ( len > 1 ) {
						if ( inst == 2 || inst == 6 ) {					
							sprintf ( cmd + len, "%s%s %.8f", prefix, gasNames [g], v );
							system ( cmd );
						}
					}
				}
				else { v = -1; failCounter++; }
				printf ( "%s \t %20.8f ppm (%s)\n", gases [g], v, gasNames [g] );
			}
        	
        	cacheCounter = 0;
			failCounter = 0;
		}
		else if ( res == 0 ) {
        	ret = 4; cacheCounter++; failCounter = 0;
        	
			if ( inst == 6 && len > 1 && !(cacheCounter % 6) ) {
				for ( int g = 0; g < GAS_COUNT; ++g ) {	
					sprintf ( cmd + len, "%s%s %.8f", prefix, gasNames [g], gascache [ g ] );
					system ( cmd );
				}
			}
		}
		else {
			ret = 2; failCounter++; cacheCounter = 0;
		}
        
		SaveCache ();
		
		if ( inst == 6 )  {			
			if ( failCounter <= 4 ) {
				if ( cacheCounter <= 120 ) {
					sleep ( 30 );
					goto REPEAT;
				}
				else { ret = 4; }
			}
			else { ret = 2; }
		}
    }        
    
    if ( inst == 0 || inst == 3 ) {
        printf ( "Power off!\n" );
        gas.powerOff ();

        gas.ledOff ();
    }
    
    return ret;
}

