package mupro.hcm.sonification;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;
import mupro.hcm.sonification.database.AppDatabase;
import mupro.hcm.sonification.database.DataSet;
import mupro.hcm.sonification.database.SensorData;
import mupro.hcm.sonification.fragments.ChartsFragment;
import mupro.hcm.sonification.fragments.MapFragment;
import mupro.hcm.sonification.location.LocationDataReceiver;
import mupro.hcm.sonification.preferences.PreferencesActivity;
import mupro.hcm.sonification.sensors.SensorDataReceiver;

import static mupro.hcm.sonification.MainActivity.ACTION_BROADCAST;
import static mupro.hcm.sonification.MainActivity.CURRENT_DATASET;
import static mupro.hcm.sonification.MainActivity.EXTRA_DATASETID;

public class DataActivity extends AppCompatActivity implements MapFragment.OnDataPointDeleteListener {
    private static final String TAG = DataActivity.class.getName();

    @BindView(R.id.data_activity_toolbar)
    Toolbar toolbar;
    @BindView(R.id.data_activity_toolbar_title)
    TextView toolbarTitle;
    @BindView(R.id.data_activity_appbar)
    AppBarLayout appBarLayout;
    @BindView(R.id.data_activity_tablayout)
    TabLayout tabLayout;
    @BindView(R.id.data_activity_container)
    ViewPager viewPager;

    private ChartsFragment mChartsFragment;
    private MapFragment mMapFragment;
    private SensorDataReceiver mSensorDataReceiver;
    private LocationDataReceiver mLocationDataReceiver;

    private DataSet mDataSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);
        ButterKnife.bind(this);

        postponeEnterTransition();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        Intent intent = getIntent();
        AsyncTask.execute(() -> {
            mDataSet = AppDatabase.getDatabase(this).dataSetDao()
                    .getById(intent.getLongExtra(EXTRA_DATASETID, -1));
            toolbarTitle.setText(mDataSet.getName());

            long currDataSetId = PreferenceManager.getDefaultSharedPreferences(this)
                    .getLong(CURRENT_DATASET, -1);

            if (mDataSet.getId() == currDataSetId) {
                registerReceivers();
            }
        });

        tabLayout.setupWithViewPager(viewPager);
        viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    default:
                        if (mChartsFragment == null)
                            mChartsFragment = ChartsFragment.newInstance(mDataSet.getId());
                        return mChartsFragment;
                    case 1:
                        if (mMapFragment == null) {
                            mMapFragment = MapFragment.newInstance(mDataSet.getId());
                            mMapFragment.setOnDataPointDeleteListener(DataActivity.this);
                        }
                        return mMapFragment;
                }
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case 0: return getResources().getString(R.string.title_charts);
                    case 1: return getResources().getString(R.string.title_map);
                    default: return null;
                }
            }

            @Override
            public int getCount() {
                return 2;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, R.id.settings, Menu.NONE, R.string.settings);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finishAfterTransition();
                return true;
            case R.id.settings:
                Intent intent = new Intent(this, PreferencesActivity.class);
                startActivity(intent);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mSensorDataReceiver != null) {
            unregisterReceiver(mSensorDataReceiver);
        }

        if (mLocationDataReceiver != null) {
            unregisterReceiver(mLocationDataReceiver);
        }
    }

    private void registerReceivers() {
        IntentFilter intentFilter = new IntentFilter(ACTION_BROADCAST);

        mSensorDataReceiver = new SensorDataReceiver(this::receivedData);
        registerReceiver(mSensorDataReceiver, intentFilter);
        Log.i(TAG, "Registered SensorDataReceiver");
    }

    private Void receivedData(SensorData sensorData) {
        Log.i(TAG, "Updating Charts and Map...");
        mChartsFragment.updateCharts(sensorData);
        mMapFragment.addMarker(sensorData);
        Log.i(TAG, "Charts and Map updated.");
        return null;
    }

    @Override
    public void onDataPointDelete(SensorData deleted) {
        Log.i(TAG, "Removing entry from charts...");
        mChartsFragment.updateCharts(deleted);
    }
}
