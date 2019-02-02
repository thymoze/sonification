package mupro.hcm.sonification;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
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
import com.google.android.material.snackbar.Snackbar;
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
import mupro.hcm.sonification.preferences.PreferencesActivity;
import mupro.hcm.sonification.services.DataService;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();
    private static final String PACKAGE = MainActivity.class.getPackage().getName();

    // CONSTANTS
    public static final String ACTION_BROADCAST = PACKAGE.concat(".broadcast");
    public static final String EXTRA_SENSORDATA = PACKAGE.concat(".sensor_data");
    public static final String EXTRA_UDPRECEIVER = PACKAGE.concat(".receiver");
    public static final String EXTRA_DATASETID = PACKAGE.concat(".dataset_id");
    public static final String CURRENT_DATASET = "CURRENT_DATASET";

    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

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
    private Snackbar mItemDeletedSnackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        setSupportActionBar(toolbar);

        setupRecyclerView();

        checkPermissions();
    }

    private void setupRecyclerView() {
        dataSetList.setHasFixedSize(true);

        mDataSetListLayoutManager = new LinearLayoutManager(this);
        dataSetList.setLayoutManager(mDataSetListLayoutManager);

        mDataSetListAdapter = new DataSetListAdapter(this);
        dataSetList.setAdapter(mDataSetListAdapter);

        mDataSetViewModel = ViewModelProviders.of(this).get(DataSetViewModel.class);
        mDataSetViewModel.getAllDataSets().observe(this, dataSets -> mDataSetListAdapter.setDataSets(dataSets));

        dataSetList.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                int px = (int) getResources().getDimension(R.dimen.dataset_card_margin);
                outRect.top = parent.getChildAdapterPosition(view) == 0 ? px : 0;
                outRect.bottom = px;
            }
        });

        setupItemTouchHelper();
    }

    private void setupItemTouchHelper() {
        ItemTouchHelper.Callback callback = new ItemTouchHelper.Callback() {
            Drawable trash = getDrawable(R.drawable.ic_delete_white_24dp);
            int background = getResources().getColor(R.color.error, getTheme());
            float margin = getResources().getDimension(R.dimen.dataset_card_delete_margin);
            float radius = getResources().getDimension(R.dimen.dataset_card_radius);

            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                long currDataSetId = PreferenceManager.getDefaultSharedPreferences(MainActivity.this)
                        .getLong(CURRENT_DATASET, -1);

                if (mDataSetListAdapter.getItem(viewHolder.getAdapterPosition()).getId() == currDataSetId) {
                    return 0;
                } else {
                    return makeMovementFlags(0, ItemTouchHelper.START | ItemTouchHelper.END);
                }
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();

                mItemDeletedSnackbar = Snackbar.make(dataSetList, String.format(getResources().getString(R.string.dataset_removed_message),
                            mDataSetListAdapter.getItem(viewHolder.getAdapterPosition()).getName()), Snackbar.LENGTH_LONG)
                        .setAction(R.string.undo, v -> mDataSetListAdapter.cancelRemoval());

                mItemDeletedSnackbar.addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        // only delete from db if the snackbar timed out or was actively dismissed by the user
                        if ((event & (DISMISS_EVENT_TIMEOUT | DISMISS_EVENT_SWIPE | DISMISS_EVENT_CONSECUTIVE)) != 0) {
                            mDataSetViewModel.delete(position);
                        }
                    }
                });

                mDataSetListAdapter.pendingRemoval(position);
                mItemDeletedSnackbar.show();
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;

                // not sure why, but this method get's called for viewholder that are already swiped away
                if (viewHolder.getAdapterPosition() == -1) {
                    // not interested in those
                    return;
                }

                int itemHeight = itemView.getBottom() - itemView.getTop();
                int trashWidth = trash.getIntrinsicWidth();
                int trashHeight = trash.getIntrinsicWidth();

                int trashTop = itemView.getTop() + (itemHeight - trashHeight)/2;
                int trashBottom = trashTop + trashHeight;

                float trashLeft = 0;
                float trashRight = 0;

                Paint p = new Paint();
                p.setColor(background);
                if (dX > 0) {
                    // swipe to the right
                    c.drawRoundRect(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + dX + margin, itemView.getBottom(),  radius, radius, p);

                    trashLeft = itemView.getLeft() + margin;
                    trashRight = itemView.getLeft() + margin + trashWidth;
                } else if (dX < 0) {
                    // swipe to the left
                    c.drawRoundRect(itemView.getRight() + dX - margin, itemView.getTop(), itemView.getRight(), itemView.getBottom(), radius, radius, p);

                    trashRight = itemView.getRight() - margin;
                    trashLeft = itemView.getRight() - margin - trashWidth;
                }

                //background.draw(c);
                trash.setBounds((int) trashLeft, trashTop, (int) trashRight, trashBottom);
                trash.draw(c);

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(dataSetList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (PreferenceManager.getDefaultSharedPreferences(this)
                .getLong(CURRENT_DATASET, -1) != -1) {
            menu.add(Menu.NONE, R.id.stop, Menu.NONE, R.string.stop)
                    .setIcon(R.drawable.ic_stop_black_24dp)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }

        menu.add(Menu.NONE, R.id.settings, Menu.NONE, R.string.settings);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.stop:
                stopDataService();
                return true;
            case R.id.settings:
                Intent intent = new Intent(this, PreferencesActivity.class);
                startActivity(intent);
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
            button_pos.setOnClickListener(v -> {
                String name = input.getText().toString();
                if (!name.isEmpty()) {
                    new insertNewDataSetTask(this).execute(name);
                    dialog.dismiss();
                } else {
                    inputLayout.setError(getResources().getString(R.string.enter_name));
                }
            });
        });

        // Automatically open keyboard
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        dialog.show();
        input.requestFocus();
    }

    private void startDataService() {
        final Intent intent = new Intent(this, DataService.class);
        startForegroundService(intent);

        invalidateOptionsMenu();
        startNewButton.setVisibility(View.GONE);
    }

    private void stopDataService() {
        final Intent intent = new Intent(this, DataService.class);
        stopService(intent);

        PreferenceManager.getDefaultSharedPreferences(this).edit().remove(CURRENT_DATASET).apply();

        invalidateOptionsMenu();
        startNewButton.setVisibility(View.VISIBLE);

        mDataSetListAdapter.notifyDataSetChanged();
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
                    Toast.makeText(getApplicationContext(), getString(R.string.thanks), Toast.LENGTH_SHORT).show();
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
        task.addOnSuccessListener(this, locationSettingsResponse -> Toast.makeText(getApplicationContext(), getString(R.string.thanks), Toast.LENGTH_SHORT).show());

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
                PreferenceManager.getDefaultSharedPreferences(context)
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
                intent.putExtra(EXTRA_DATASETID, id);
                context.startActivity(intent);
                context.startDataService();
            }
        }
    }
}
