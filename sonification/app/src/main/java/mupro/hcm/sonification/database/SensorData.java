package mupro.hcm.sonification.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class SensorData implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "pm10")
    private double pm10;

    @ColumnInfo(name = "pm25")
    private double pm25;

    @ColumnInfo(name = "humidity")
    private double humidity;

    @ColumnInfo(name = "temperatureSHT")
    private double temperatureSHT;

    @ColumnInfo(name = "pressure")
    private double pressure;

    @ColumnInfo(name = "temperatureBMP")
    private double temperatureBMP;

    @ColumnInfo(name = "co")
    private double co;

    @ColumnInfo(name = "no2")
    private double no2;

    @ColumnInfo(name = "nh3")
    private double nh3;

    @ColumnInfo(name = "c3h8")
    private double c3h8;

    @ColumnInfo(name = "c4h10")
    private double c4h10;

    @ColumnInfo(name = "ch4")
    private double ch4;

    @ColumnInfo(name = "h2")
    private double h2;

    @ColumnInfo(name = "c2h5oh")
    private double c2h5oh;

    @ColumnInfo(name = "longitude")
    private double longitude;

    @ColumnInfo(name = "latitude")
    private double latitude;

    @ColumnInfo(name = "timestamp")
    private String timestamp;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getPm10() {
        return pm10;
    }

    public void setPm10(double pm10) {
        this.pm10 = pm10;
    }

    public double getPm25() {
        return pm25;
    }

    public void setPm25(double pm25) {
        this.pm25 = pm25;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public double getTemperatureSHT() {
        return temperatureSHT;
    }

    public void setTemperatureSHT(double temperatureSHT) {
        this.temperatureSHT = temperatureSHT;
    }

    public double getPressure() {
        return pressure;
    }

    public void setPressure(double pressure) {
        this.pressure = pressure;
    }

    public double getTemperatureBMP() {
        return temperatureBMP;
    }

    public void setTemperatureBMP(double temperatureBMP) {
        this.temperatureBMP = temperatureBMP;
    }

    public double getCo() {
        return co;
    }

    public void setCo(double co) {
        this.co = co;
    }

    public double getNo2() {
        return no2;
    }

    public void setNo2(double no2) {
        this.no2 = no2;
    }

    public double getNh3() {
        return nh3;
    }

    public void setNh3(double nh3) {
        this.nh3 = nh3;
    }

    public double getC3h8() {
        return c3h8;
    }

    public void setC3h8(double c3h8) {
        this.c3h8 = c3h8;
    }

    public double getC4h10() {
        return c4h10;
    }

    public void setC4h10(double c4h10) {
        this.c4h10 = c4h10;
    }

    public double getCh4() {
        return ch4;
    }

    public void setCh4(double ch4) {
        this.ch4 = ch4;
    }

    public double getH2() {
        return h2;
    }

    public void setH2(double h2) {
        this.h2 = h2;
    }

    public double getC2h5oh() {
        return c2h5oh;
    }

    public void setC2h5oh(double c2h5oh) {
        this.c2h5oh = c2h5oh;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}