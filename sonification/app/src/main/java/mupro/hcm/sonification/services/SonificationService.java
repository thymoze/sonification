package mupro.hcm.sonification.services;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;
import mupro.hcm.sonification.R;
import mupro.hcm.sonification.database.SensorData;
import mupro.hcm.sonification.sensors.Sensor;
import mupro.hcm.sonification.sensors.SensorDataReceiver;
import mupro.hcm.sonification.sound.Direction;
import mupro.hcm.sonification.sound.SoundQueue;

import static mupro.hcm.sonification.MainActivity.ACTION_BROADCAST;

public class SonificationService extends Service {

    private final String TAG = getClass().getName();
    private int counter;
    private SensorDataReceiver mSensorDataReceiver;
    private ArrayList<LinkedList<Double>> medianLists;
    private double oldMedians[];
    private double currentMedians[];
    private SoundQueue soundQueue;

    private int PERCENTAGE = 20; // default value 20
    private int NUM_SAMPLES = 5; // default value 5
    private boolean SLIDING_WINDOW = true; // default value true

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");

        counter = 0;
        medianLists = new ArrayList<>();
        oldMedians = new double[Sensor.values().length];
        currentMedians = new double[Sensor.values().length];
        soundQueue = new SoundQueue(getBaseContext());

        configureServiceWithPreferences();

        // add a list for each sensor
        Arrays.stream(Sensor.values()).forEach(e -> medianLists.add(new LinkedList<>()));

        // check for change in sensors preference
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener((preferences, key) -> {
            if (key.equals("sensors_preference")) {
                counter = 0;
            }
        });

        registerReceiver();
    }

    private void configureServiceWithPreferences() {
        String sampleSizePreference = android.preference.PreferenceManager.getDefaultSharedPreferences(this).getString("sample_size", null);
        if (sampleSizePreference != null)
            NUM_SAMPLES = Integer.parseInt(sampleSizePreference);

        String percentagePreference = android.preference.PreferenceManager.getDefaultSharedPreferences(this).getString("percentage", null);
        if (percentagePreference != null)
            PERCENTAGE = Integer.parseInt(percentagePreference);

        String typePreference = android.preference.PreferenceManager.getDefaultSharedPreferences(this).getString("type_preference", null);
        if (typePreference != null && typePreference.equals(getString(R.string.frame))) {
            SLIDING_WINDOW = false;
        }

        Log.i(TAG, "Sonification successfully configured.");
        Log.i(TAG, "Sample Size: " + NUM_SAMPLES);
        Log.i(TAG, "Percentage: " + PERCENTAGE);
        Log.i(TAG, "Type: " + (SLIDING_WINDOW ? "Sliding Window" : "Frame"));
        Log.i(TAG, "------------");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mSensorDataReceiver != null) {
            unregisterReceiver(mSensorDataReceiver);
        }
        soundQueue.release();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void registerReceiver() {
        mSensorDataReceiver = new SensorDataReceiver(this::receivedData);
        IntentFilter intentFilter = new IntentFilter(ACTION_BROADCAST);
        registerReceiver(mSensorDataReceiver, intentFilter);
        Log.i(TAG, "Registered SensorDataReceiver");
    }

    private Void receivedData(SensorData sensorData) {
        handleSonification(sensorData);
        return null;
    }

    private void handleSonification(SensorData sensorData) {
        counter++;
        Log.i(TAG, "Counter: " + counter);

        // get selected sensors from the preferences
        Set<Sensor> sensorPreference = PreferenceManager.getDefaultSharedPreferences(this).getStringSet("sensors_preference",
                Arrays.stream(Sensor.values())
                        .map(Sensor::getId)
                        .collect(Collectors.toSet()))
                .stream()
                .map(Sensor::fromId)
                .collect(Collectors.toSet());

        sensorPreference.forEach(sensor -> handleSonificationForSingleSensor(sensor, sensorData));

        // extra sonification handling for particle sensors
        if (sensorPreference.stream().anyMatch(s -> s == Sensor.PM10 || s == Sensor.PM25))
            handleSonificationForParticleSensors();

        // reset counter
        if (counter > NUM_SAMPLES && counter % NUM_SAMPLES == 0) {
            counter = NUM_SAMPLES;
        }
    }

    private void handleSonificationForSingleSensor(Sensor sensor, SensorData sensorData) {
        if (SLIDING_WINDOW)
            handleSonificationForSingleSensorSlidingWindow(sensor, sensorData);
        else
            handleSonificationForSingleSensorNoSlidingWindow(sensor, sensorData);
    }

    private void handleSonificationForParticleSensors() {
        if (SLIDING_WINDOW)
            handleSonificationForParticleSensorsSlidingWindow();
        else
            handleSonificationForParticleSensorsNoSlidingWindow();
    }

    private void handleSonificationForSingleSensorNoSlidingWindow(Sensor sensor, SensorData sensorData) {
        int sensorIndex = sensor.ordinal();

        // add sensorData to the median list
        List<Double> mediansOfCurrentSensor = medianLists.get(sensorIndex);
        mediansOfCurrentSensor.add(sensorData.get(sensor));

        if (counter == NUM_SAMPLES) { // took first NUM_SAMPLES samples to calculate first median
            Collections.sort(mediansOfCurrentSensor);
            oldMedians[sensorIndex] = mediansOfCurrentSensor.get(NUM_SAMPLES / 2);
            mediansOfCurrentSensor.clear();
        } else if (counter > NUM_SAMPLES && counter % NUM_SAMPLES == 0) { // took NUM_SAMPLES samples for comparison with old median
            Collections.sort(mediansOfCurrentSensor);
            currentMedians[sensorIndex] = mediansOfCurrentSensor.get(NUM_SAMPLES / 2);

            // play sound if median increases by PERCENTAGE
            if (sensor != Sensor.PM10 && sensor != Sensor.PM25) {
                playSoundForSensor(oldMedians[sensorIndex], currentMedians[sensorIndex], sensor);

                // set the new median values
                oldMedians[sensorIndex] = currentMedians[sensorIndex];
            }

            // clear median list for next sample set
            mediansOfCurrentSensor.clear();
        }
    }

    private void handleSonificationForSingleSensorSlidingWindow(Sensor sensor, SensorData sensorData) {
        int sensorIndex = sensor.ordinal();

        // add sensorData to the median list
        LinkedList<Double> mediansOfCurrentSensor = medianLists.get(sensorIndex);

        // populate list in the beginning
        if (counter <= NUM_SAMPLES)
            mediansOfCurrentSensor.add(sensorData.get(sensor));

        // Log.i(TAG, "Medians size: " + mediansOfCurrentSensor.size());

        if (counter == NUM_SAMPLES) { // took first NUM_SAMPLES samples to calculate first median
            Collections.sort(mediansOfCurrentSensor);
            oldMedians[sensorIndex] = mediansOfCurrentSensor.get(NUM_SAMPLES / 2);
        } else if (counter > NUM_SAMPLES) { // took NUM_SAMPLES samples for comparison with old median
            mediansOfCurrentSensor.removeFirst();
            mediansOfCurrentSensor.add(sensorData.get(sensor));

            // we need to make a deep copy so the original list is not affected by the sorting
            List<Double> deepCopy = new ArrayList<>(mediansOfCurrentSensor);
            Collections.sort(deepCopy);
            currentMedians[sensorIndex] = deepCopy.get(NUM_SAMPLES / 2);

            // play sound if median increases by PERCENTAGE
            if (sensor != Sensor.PM10 && sensor != Sensor.PM25) {
                playSoundForSensor(oldMedians[sensorIndex], currentMedians[sensorIndex], sensor);

                // set the new median values
                oldMedians[sensorIndex] = currentMedians[sensorIndex];
            }
        }
    }

    private void playSoundForSensor(double oldValue, double currentValue, Sensor sensor) {
        Log.i(TAG, "+++ " + sensor.getId() + " +++");
        Log.i(TAG, "Old median: " + oldValue);
        Log.i(TAG, "Current median:" + currentValue);
        Log.i(TAG, "Change: " + ((currentValue / oldValue) - 1f));
        Log.i(TAG, "------------");

        // play sound if median increases by PERCENTAGE
        if (currentValue / oldValue > (1f + PERCENTAGE / 100f)) {
            // play up sound
            soundQueue.playSoundForSensorWithDirection(sensor, Direction.UP);
        } else if (oldValue / currentValue > (1f + PERCENTAGE / 100f)) {
            // play down sound
            soundQueue.playSoundForSensorWithDirection(sensor, Direction.DOWN);
        }
    }

    private void handleSonificationForParticleSensorsNoSlidingWindow() {
        if (counter > NUM_SAMPLES && counter % NUM_SAMPLES == 0) { // took NUM_SAMPLES samples for comparison with old median
            int indexPM25 = Sensor.PM25.ordinal();
            int indexPM10 = Sensor.PM10.ordinal();

            double averageOld = (oldMedians[indexPM10] + oldMedians[indexPM25]) / 2f;
            double averageCurrent = (currentMedians[indexPM10] + currentMedians[indexPM25]) / 2f;

            playSoundForParticleSensor(averageOld, averageCurrent);

            // set the new median values
            oldMedians[indexPM25] = currentMedians[indexPM25];
            oldMedians[indexPM10] = currentMedians[indexPM10];
        }
    }

    private void handleSonificationForParticleSensorsSlidingWindow() {
        if (counter > NUM_SAMPLES) { // took NUM_SAMPLES samples for comparison with old median
            int indexPM25 = Sensor.PM25.ordinal();
            int indexPM10 = Sensor.PM10.ordinal();

            double averageOld = (oldMedians[indexPM10] + oldMedians[indexPM25]) / 2f;
            double averageCurrent = (currentMedians[indexPM10] + currentMedians[indexPM25]) / 2f;

            playSoundForParticleSensor(averageOld, averageCurrent);

            // set the new median values
            oldMedians[indexPM25] = currentMedians[indexPM25];
            oldMedians[indexPM10] = currentMedians[indexPM10];
        }
    }

    private void playSoundForParticleSensor(double averageOld, double averageCurrent) {
        Log.i(TAG, "+++ Particle Sensors +++");
        Log.i(TAG, "OldAvgMedian: " + averageOld);
        Log.i(TAG, "CurrentAvgMedian: " + averageCurrent);
        Log.i(TAG, "Change: " + ((averageCurrent / averageOld) - 1f));
        Log.i(TAG, "------------");

        // play sound if median increases by PERCENTAGE
        if ((averageCurrent / averageOld) > (1f + PERCENTAGE / 100f)) {
            // play up sound
            soundQueue.playSoundForParticleSensor(Direction.UP);
        } else if ((averageOld / averageCurrent) > (1f + PERCENTAGE / 100f)) {
            // play down sound
            soundQueue.playSoundForParticleSensor(Direction.DOWN);
        }
    }
}
