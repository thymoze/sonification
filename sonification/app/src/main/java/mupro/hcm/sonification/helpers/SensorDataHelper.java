package mupro.hcm.sonification.helpers;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;


import mupro.hcm.sonification.database.SensorData;

public class SensorDataHelper {

    private static final String TAG = "SensorDataHelper";

    public static SensorData createSensorDataObjectFromValues(GPSCoordinates location, JSONObject data) {
        SensorData sensorData = new SensorData();

        // create timestamp
        sensorData.setTimestamp(Instant.now().toString());

        // set gps data
        sensorData.setLongitude(location.longitude);
        sensorData.setLatitude(location.latitude);

        // get all sensor values
        try {
            sensorData.setPm25(((Double) data.get("SDS011_PM2.5")).floatValue());
            sensorData.setPm10(((Double) data.get("SDS011_PM10")).floatValue());
            sensorData.setHumidity(((Double) data.get("SHT_Humidity")).floatValue());
            sensorData.setTemperatureSHT(((Double) data.get("SHT_Temperature")).floatValue());
            sensorData.setPressure(((Double) data.get("BPM_Pressure")).floatValue());
            sensorData.setTemperatureBMP(((Double) data.get("BMP_Temperature")).floatValue());

            sensorData.setCo(((Double) data.get("MICS_CO")).floatValue());
            sensorData.setNo2(((Double) data.get("MICS_NO2")).floatValue());
            sensorData.setNh3(((Double) data.get("MICS_NH3")).floatValue());
            sensorData.setC3h8(((Double) data.get("MICS_C3H8")).floatValue());
            sensorData.setC4h10(((Double) data.get("MICS_C4H10")).floatValue());
            sensorData.setCh4(((Double) data.get("MICS_CH4")).floatValue());
            sensorData.setH2(((Double) data.get("MICS_H2")).floatValue());
            sensorData.setC2h5oh(((Double) data.get("MICS_C2h5OH")).floatValue());
        } catch (JSONException ex) {
            Log.e(TAG, ex.getMessage());
        }

        return sensorData;
    }
}
