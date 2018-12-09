package mupro.hcm.sonification.helpers;
import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;


import mupro.hcm.sonification.R;
import mupro.hcm.sonification.database.SensorData;

public class SensorDataHelper {

    private static final String TAG = "SensorDataHelper";

    public static SensorData createSensorDataObjectFromValues(JSONObject data) {
        SensorData sensorData = new SensorData();

        // get all sensor values
        try {
            sensorData.setPm25(((Double) data.get("SDS011_PM2.5")));
            sensorData.setPm10(((Double) data.get("SDS011_PM10")));
            sensorData.setHumidity(((Double) data.get("SHT_Humidity")));
            sensorData.setTemperatureSHT(((Double) data.get("SHT_Temperature")));
            sensorData.setPressure(((Double) data.get("BMP_Pressure")));
            sensorData.setTemperatureBMP(((Double) data.get("BMP_Temperature")));

            sensorData.setCo(((Double) data.get("MICS_CO")));
            sensorData.setNo2(((Double) data.get("MICS_NO2")));
            sensorData.setNh3(((Double) data.get("MICS_NH3")));
            sensorData.setC3h8(((Double) data.get("MICS_C3H8")));
            sensorData.setC4h10(((Double) data.get("MICS_C4H10")));
            sensorData.setCh4(((Double) data.get("MICS_CH4")));
            sensorData.setH2(((Double) data.get("MICS_H2")));
            sensorData.setC2h5oh(((Double) data.get("MICS_C2H5OH")));
        } catch (JSONException ex) {
            Log.e(TAG, ex.getMessage());
        }

        return sensorData;
    }
}
