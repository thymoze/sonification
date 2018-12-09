package mupro.hcm.sonification.helpers;

import android.content.Context;

import java.util.Arrays;
import java.util.Optional;

import mupro.hcm.sonification.R;

public enum Sensor {
    PM25("pm25"),
    PM10("pm10"),
    CO("co"),
    NH3("nh3"),
    NO2("no2"),
    ETHANOL("c2h5oh"),
    H2("h2"),
    CH4("ch4"),
    PROPANE("c3h8"),
    ISOBUTANE("c4h10"),
    HUMIDITY("humidity"),
    PRESSURE("pressure"),
    TEMP_SHT("temperatureSHT"),
    TEMP_BMP("temperatureBMP"),
    ;
    private String id;
    Sensor(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static Optional<Sensor> fromId(String id) {
        return Arrays.stream(Sensor.values())
                .filter(sensor -> sensor.getId().equalsIgnoreCase(id))
                .findFirst();
    }

    public String getLocalizedName(Context context) {
        try {
            return context.getResources().getString(R.string.class.getField(id).getInt(R.string.class));
        } catch (IllegalAccessException | NoSuchFieldException e) {
            return id;
        }
    }
}