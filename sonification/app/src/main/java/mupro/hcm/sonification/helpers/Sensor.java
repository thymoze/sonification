package mupro.hcm.sonification.helpers;

import android.content.Context;

import java.util.Arrays;
import java.util.Optional;

import mupro.hcm.sonification.R;

public enum Sensor {
    PM25("pm25"),
    PM10("pm10"),

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