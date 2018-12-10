package mupro.hcm.sonification;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import mupro.hcm.sonification.fragments.ChartsFragment;
import mupro.hcm.sonification.fragments.HomeFragment;
import mupro.hcm.sonification.fragments.MapFragment;
import mupro.hcm.sonification.services.DataService;

public class MainActivity extends AppCompatActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener {

    private final String TAG = "SonificationMain";
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    public static final String BROADCAST_ACTION = "mupro.hcm.sonification.broadcast_action";

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.navigation)
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        switchFragment(HomeFragment.newInstance());

        checkEverything();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.shutdown:
                // It would be good to be able to shut down the box, so the sd card and our source of truth for logs don't get corrupted.
                Toast.makeText(this, "TODO: shutdown", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void checkEverything() {
        if (!((LocationManager) getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER))
            requestGPSSettings();

        if (!permissionsAreGranted())
            requestPermissions();
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
                    checkEverything();
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.navigation_home:
                switchFragment(HomeFragment.newInstance());
                return true;
            case R.id.navigation_dashboard:
                switchFragment(ChartsFragment.newInstance());
                return true;
            case R.id.navigation_notifications:
                switchFragment(MapFragment.newInstance());
                return true;
        }
        return false;
    }

    private void switchFragment(Fragment fragment) {
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
}
