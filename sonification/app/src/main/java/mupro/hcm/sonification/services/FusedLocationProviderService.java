package mupro.hcm.sonification.services;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;


/**
 * This service gets the current GPS location using the FusedLocationProvider.
 * <p>
 * See: https://developers.google.com/location-context/fused-location-provider/
 */
public class FusedLocationProviderService extends IntentService{

    private final String TAG = "FusedLocProvService";

    // components needed for FusedLocationService
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private ResultReceiver receiver;

    // constants
    private final int INTERVAL_SECONDS = 20;
    private final int LOCATION_INTERVAL = INTERVAL_SECONDS * 1000;
    private final int LOCATION_DISTANCE = 25; // 25 meters
    public static final int LOCATION_SUCCESS = 2;
    public static final int LOCATION_ERROR = 3;

    public FusedLocationProviderService() {
        super("FusedLocationProviderService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        receiver = intent.getParcelableExtra("receiver");
        doStuffWithLocation();
    }

    private void doStuffWithLocation() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationRequest = setupLocationRequest();

        mLocationCallback = new LocationCallback() {
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    Log.i(TAG, "Lat: " + location.getLatitude() + " - Long: " + location.getLongitude());
                    sendLocation(location);
                }
            }
        };
        try {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        } catch (SecurityException ex) {
            Log.e(TAG, "Failed to request location update!", ex);
        }
    }

    private void sendLocation(Location location) {
        if (location != null) {
            Bundle bundle = new Bundle();
            bundle.putDouble("longitude", location.getLongitude());
            bundle.putDouble("latitude", location.getLatitude());
            receiver.send(LOCATION_SUCCESS, bundle);
        } else {
            receiver.send(LOCATION_ERROR, Bundle.EMPTY);
        }
    }

    private LocationRequest setupLocationRequest() {
        LocationRequest request = new LocationRequest();
        request.setInterval(LOCATION_INTERVAL);
        request.setFastestInterval(LOCATION_INTERVAL / 2);
        // request.setSmallestDisplacement(LOCATION_DISTANCE);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return request;
    }
}
