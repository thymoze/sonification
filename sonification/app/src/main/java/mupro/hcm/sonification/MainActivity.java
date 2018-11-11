package mupro.hcm.sonification;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.Manifest;
import android.arch.persistence.room.Room;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import android.util.Log;
import android.widget.EditText;

import com.github.mikephil.charting.charts.LineChart;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import java.util.Arrays;
import java.util.List;

import mupro.hcm.sonification.database.AppDatabase;
import mupro.hcm.sonification.fragments.ChartsFragment;
import mupro.hcm.sonification.fragments.HomeFragment;
import mupro.hcm.sonification.fragments.MapFragment;
import mupro.hcm.sonification.helpers.FusedLocationProvider;
import mupro.hcm.sonification.services.DataService;
import mupro.hcm.sonification.services.UdpService;

public class MainActivity extends AppCompatActivity {

    private enum Navigation {
        HOME, CHARTS, MAP
    }

    private final String TAG = "SonificationMain";
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    public static final String BROADCAST_ACTION = "mupro.hcm.sonification.broadcast_action";

    private HomeFragment homeFragment;
    private ChartsFragment chartsFragment;
    private MapFragment mapFragment;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        switch (item.getItemId()) {
            case R.id.navigation_home:
                switchFragment(Navigation.HOME);
                return true;
            case R.id.navigation_dashboard:
                switchFragment(Navigation.CHARTS);
                return true;
            case R.id.navigation_notifications:
                switchFragment(Navigation.MAP);
                return true;
        }
        return false;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        switchFragment(Navigation.HOME);

        checkAndStart();
    }

    private void checkAndStart() {
        if(((LocationManager) getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER) && permissionsAreGranted())
            startDataService();

        if (!((LocationManager) getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER))
            requestGPSSettings();

        if (!permissionsAreGranted())
            requestPermissions();
    }

    private void startDataService() {
        final Intent intent = new Intent(this.getApplication(), DataService.class);
        this.getApplication().startService(intent);
        this.getApplication().startForegroundService(intent);
    }

    private boolean permissionsAreGranted() {
        int[] permissions = {
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION),
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION),
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE),
                ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
        };

        return Arrays.stream(permissions).noneMatch(perm -> perm != PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermissions() {
        Dexter.withActivity(MainActivity.this).withPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET
        ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                // check if all permissions are granted
                if (report.areAllPermissionsGranted()) {
                    // All good
                    Toast.makeText(getApplicationContext(), "Thanks man!", Toast.LENGTH_SHORT).show();
                    startDataService();
                }
                // check for permanent denial of any permission
                if (report.isAnyPermissionPermanentlyDenied()) {
                    openSettings();
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                token.continuePermissionRequest();
            }
        }).check();
    }

    private void requestGPSSettings() {
        Task<LocationSettingsResponse> task = createLocationSettingsTask();
        task.addOnSuccessListener(this, locationSettingsResponse -> Toast.makeText(getApplicationContext(), "Thanks man!", Toast.LENGTH_SHORT).show());

        task.addOnFailureListener(this, e -> {
            if (e instanceof ResolvableApiException) {
                // Location settings are not satisfied
                try {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException sendEx) {
                    Log.e(TAG, "Couldn't send intent for permissions!", sendEx);
                }
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void switchFragment(Navigation navigation) {
        Fragment fragment;
        switch (navigation) {
            case CHARTS:
                if (chartsFragment == null)
                    chartsFragment = ChartsFragment.newInstance();
                fragment = chartsFragment;
                break;
            case MAP:
                if (mapFragment == null)
                    mapFragment = MapFragment.newInstance();
                fragment = mapFragment;
                break;
            case HOME:
            default:
                if (homeFragment == null)
                    homeFragment = HomeFragment.newInstance();
                fragment = homeFragment;
                break;
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private Task<LocationSettingsResponse> createLocationSettingsTask() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        return client.checkLocationSettings(builder.build());
    }

    private void openSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void updateCharts(JSONObject json) {
        try {
            if (json.has("F25")) {
                LineChart chart = findViewById(R.id.chart_part25);
                if (chartsFragment != null && chart != null) {
                    chartsFragment.addEntryToChart(chart, ((Double) json.get("F25")).floatValue());
                }
            }
            if (json.has("F10")) {
                LineChart chart = findViewById(R.id.chart_part10);
                if (chartsFragment != null && chart != null) {
                    chartsFragment.addEntryToChart(chart, ((Double) json.get("F10")).floatValue());
                }
            }
            //and all the other gases
            //if (json.has("..."))
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }
}
