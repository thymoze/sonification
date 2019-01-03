package mupro.hcm.sonification;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Rect;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.lang.ref.WeakReference;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import mupro.hcm.sonification.database.AppDatabase;
import mupro.hcm.sonification.database.DataSet;
import mupro.hcm.sonification.database.DataSetDao;
import mupro.hcm.sonification.dataset.DataSetListAdapter;
import mupro.hcm.sonification.dataset.DataSetViewModel;
import mupro.hcm.sonification.services.DataService;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();

    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    public static final String BROADCAST_ACTION = TAG.concat("broadcast_action");
    public static final String CURRENT_DATASET = "CURRENT_DATASET";

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.app_bar)
    AppBarLayout appBarLayout;
    @BindView(R.id.dataset_list)
    RecyclerView dataSetList;
    @BindView(R.id.start_new_btn)
    FloatingActionButton startNewButton;

    private DataSetListAdapter mDataSetListAdapter;
    private RecyclerView.LayoutManager mDataSetListLayoutManager;
    private DataSetViewModel mDataSetViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        dataSetList.setHasFixedSize(true);

        mDataSetListLayoutManager = new LinearLayoutManager(this);
        dataSetList.setLayoutManager(mDataSetListLayoutManager);

        mDataSetListAdapter = new DataSetListAdapter(this);
        mDataSetListAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                for (int i = positionStart; i < positionStart + itemCount; i++)
                    mDataSetViewModel.delete(i);
            }
        });
        dataSetList.setAdapter(mDataSetListAdapter);

        mDataSetViewModel = ViewModelProviders.of(this).get(DataSetViewModel.class);
        mDataSetViewModel.getAllDataSets().observe(this, dataSets -> mDataSetListAdapter.setDataSets(dataSets));

        dataSetList.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                int px = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12,
                                getResources().getDisplayMetrics()));
                outRect.top = parent.getChildAdapterPosition(view) == 0 ? px : 0;
                outRect.bottom = px;
            }
        });

        ItemTouchHelper ith = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                return makeMovementFlags(0, ItemTouchHelper.START | ItemTouchHelper.END);
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                mDataSetListAdapter.onItemDismiss(viewHolder.getAdapterPosition());
            }

            @Override
            public boolean isItemViewSwipeEnabled() {
                return true;
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }
        });

        ith.attachToRecyclerView(dataSetList);

        checkPermissions();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (getSharedPreferences("DATA", MODE_PRIVATE)
                .getLong(CURRENT_DATASET, -1) != -1) {
            getMenuInflater().inflate(R.menu.stop_menu, menu);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.stop:
                stopDataService();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @OnClick(R.id.start_new_btn)
    public void startNewDataSet() {
        View view = LayoutInflater.from(this).inflate(R.layout.dataset_dialog, dataSetList, false);
        final EditText input = view.findViewById(R.id.input);
        final TextInputLayout inputLayout = view.findViewById(R.id.input_layout);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.new_dataset))
                .setView(view)
                .setPositiveButton(R.string.add, null)
                .setNegativeButton(R.string.abort, null)
                .create();

        dialog.setOnShowListener(dia -> {
            Button button_pos = ((AlertDialog) dia).getButton(AlertDialog.BUTTON_POSITIVE);
            Button button_neg = ((AlertDialog) dia).getButton(AlertDialog.BUTTON_NEGATIVE);
            button_pos.setTextColor(Color.parseColor("#3B3B3B"));
            button_neg.setTextColor(Color.parseColor("#3B3B3B"));
            button_pos.setOnClickListener(v -> {
                String name = input.getText().toString();
                if (!name.isEmpty()) {
                    new insertNewDataSetTask(this).execute(name);
                    dialog.dismiss();
                } else {
                    inputLayout.setError(getResources().getString(R.string.enter_name));
                }
            });
            input.requestFocus();
        });

        dialog.show();
    }

    private void startDataService() {
        final Intent intent = new Intent(this, DataService.class);
        startService(intent);
        startForegroundService(intent);

        invalidateOptionsMenu();
        startNewButton.setVisibility(View.INVISIBLE);
    }

    private void stopDataService() {
        final Intent intent = new Intent(this, DataService.class);
        stopService(intent);

        getSharedPreferences("DATA", MODE_PRIVATE)
                .edit()
                .remove(CURRENT_DATASET)
                .apply();
        invalidateOptionsMenu();
        startNewButton.setVisibility(View.VISIBLE);
    }

    private void checkPermissions() {
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
        Dexter.withActivity(this).withPermissions(
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
                    checkPermissions();
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
                    resolvable.startResolutionForResult(this, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException sendEx) {
                    Log.e(TAG, "Couldn't send intent for permissions!", sendEx);
                }
            }
        });
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

    private static class insertNewDataSetTask extends AsyncTask<String, Void, Long> {
        private WeakReference<MainActivity> mContext;
        private DataSetDao mDataSetDao;

        insertNewDataSetTask(MainActivity context) {
            mContext = new WeakReference<>(context);
            mDataSetDao = AppDatabase.getDatabase(context).dataSetDao();
        }

        @Override
        protected Long doInBackground(String... names) {
            DataSet current = new DataSet(names[0], Instant.now());
            long id = mDataSetDao.insert(current);
            MainActivity context = mContext.get();
            if (context != null) {
                context.getSharedPreferences("DATA", MODE_PRIVATE)
                        .edit()
                        .putLong(CURRENT_DATASET, id)
                        .apply();
            }
            return id;
        }

        @Override
        protected void onPostExecute(Long id) {
            MainActivity context = mContext.get();
            if (context != null) {
                Intent intent = new Intent(context, DataActivity.class);
                intent.putExtra("DATASET_ID", id);
                context.startActivity(intent);
                context.startDataService();
            }
        }
    }
}
