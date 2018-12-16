package mupro.hcm.sonification.database;



import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;


import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import mupro.hcm.sonification.sensors.Sensor;

import static androidx.room.ForeignKey.CASCADE;

@Entity(foreignKeys = @ForeignKey(entity = DataSet.class,
        parentColumns = "id",
        childColumns = "dataSetId",
        onDelete = CASCADE))
public class SensorData implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private long dataSetId;

    private double pm10;
    private double pm25;
    private double humidity;
    private double temperatureSHT;
    private double pressure;
    private double temperatureBMP;
    private double co;
    private double no2;
    private double nh3;
    private double c3h8;
    private double c4h10;
    private double ch4;
    private double h2;
    private double c2h5oh;
    private double longitude;
    private double latitude;

    @TypeConverters(AppDatabase.class)
    private Instant timestamp;

    public @Nullable Double get(Sensor sensor) {
        String id = sensor.getId();
        try {
            // call getSensorId by reflection
            return (Double) SensorData.class.getMethod("get" + id.substring(0, 1).toUpperCase() + id.substring(1)).invoke(this);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            return null;
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getDataSetId() {
        return dataSetId;
    }

    public void setDataSetId(long dataSetId) {
        this.dataSetId = dataSetId;
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

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String toString() {
        return "Time: \t" + getTimestamp() + "\n" +
                "Lat: \t" + getLatitude() + "\n" +
                "Long: \t" + getLongitude() + "\n" +
                "PM10: \t" + getPm10() + "\n" +
                "PM2.5: \t" + getPm25() + "\n" +
                "Humidity: \t" + getHumidity() + "\n" +
                "Temperature: \t" + getTemperatureSHT() + "\n" +
                "CO: \t" + getCo() + "\n" +
                "NO2: \t" + getNo2() + "\n" +
                "NH3: \t" + getNh3() + "\n" +
                "C3H8: \t" + getC3h8() + "\n" +
                "C4H10: \t" + getC4h10() + "\n" +
                "H2: \t" + getH2() + "\n" +
                "C2H5OH: \t" + getC2h5oh() + "\n";
    }
}