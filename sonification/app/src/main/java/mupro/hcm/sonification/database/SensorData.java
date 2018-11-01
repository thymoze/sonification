package mupro.hcm.sonification.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class SensorData {

    @PrimaryKey
    private int id;

    @ColumnInfo(name = "pm10")
    private String pm10;

    @ColumnInfo(name = "pm25")
    private String pm25;

    @ColumnInfo(name = "humidity")
    private String humidity;

    @ColumnInfo(name = "temperatureSHT")
    private String temperatureSHT;

    @ColumnInfo(name = "pressure")
    private String pressure;

    @ColumnInfo(name = "temperatureBMP")
    private String temperatureBMP;

    @ColumnInfo(name = "co")
    private String co;

    @ColumnInfo(name = "no2")
    private String no2;

    @ColumnInfo(name = "nh3")
    private String nh3;

    @ColumnInfo(name = "c3h8")
    private String c3h8;

    @ColumnInfo(name = "c4h10")
    private String c4h10;

    @ColumnInfo(name = "ch4")
    private String ch4;

    @ColumnInfo(name = "h2")
    private String h2;

    @ColumnInfo(name = "c2h5oh")
    private String c2h5oh;

    @ColumnInfo(name = "longitude")
    private int longitude;

    @ColumnInfo(name = "latitude")
    private int latitude;

    @ColumnInfo(name = "timestamp")
    private String timestamp;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPm10() {
        return pm10;
    }

    public void setPm10(String pm10) {
        this.pm10 = pm10;
    }

    public String getPm25() {
        return pm25;
    }

    public void setPm25(String pm25) {
        this.pm25 = pm25;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getTemperatureSHT() {
        return temperatureSHT;
    }

    public void setTemperatureSHT(String temperatureSHT) {
        this.temperatureSHT = temperatureSHT;
    }

    public String getPressure() {
        return pressure;
    }

    public void setPressure(String pressure) {
        this.pressure = pressure;
    }

    public String getTemperatureBMP() {
        return temperatureBMP;
    }

    public void setTemperatureBMP(String temperatureBMP) {
        this.temperatureBMP = temperatureBMP;
    }

    public String getCo() {
        return co;
    }

    public void setCo(String co) {
        this.co = co;
    }

    public String getNo2() {
        return no2;
    }

    public void setNo2(String no2) {
        this.no2 = no2;
    }

    public String getNh3() {
        return nh3;
    }

    public void setNh3(String nh3) {
        this.nh3 = nh3;
    }

    public String getC3h8() {
        return c3h8;
    }

    public void setC3h8(String c3h8) {
        this.c3h8 = c3h8;
    }

    public String getC4h10() {
        return c4h10;
    }

    public void setC4h10(String c4h10) {
        this.c4h10 = c4h10;
    }

    public String getCh4() {
        return ch4;
    }

    public void setCh4(String ch4) {
        this.ch4 = ch4;
    }

    public String getH2() {
        return h2;
    }

    public void setH2(String h2) {
        this.h2 = h2;
    }

    public String getC2h5oh() {
        return c2h5oh;
    }

    public void setC2h5oh(String c2h5oh) {
        this.c2h5oh = c2h5oh;
    }

    public int getLongitude() {
        return longitude;
    }

    public void setLongitude(int longitude) {
        this.longitude = longitude;
    }

    public int getLatitude() {
        return latitude;
    }

    public void setLatitude(int latitude) {
        this.latitude = latitude;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}