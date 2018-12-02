package mupro.hcm.sonification.helpers;

import android.content.Context;
import android.location.Location;
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
public class FusedLocationProvider {

    private final static String TAG = "FusedLocationProvider";

    public interface LocationGPSCallback {
        void onNewLocationAvailable(GPSCoordinates location);
    }

    public static void requestSingleUpdate(final Context context, final LocationGPSCallback callback) {
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setNumUpdates(1);
        mLocationRequest.setInterval(500);
        mLocationRequest.setFastestInterval(250);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationCallback mLocationCallback = new LocationCallback() {
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    GPSCoordinates loc = new GPSCoordinates(location.getLongitude(), location.getLatitude());
                    Log.i(TAG, loc.toString());
                    callback.onNewLocationAvailable(loc);
                }
            }
        };

        try {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        } catch (SecurityException ex) {
            Log.e(TAG, "Failed to request location update!", ex);
        }
    }
}
