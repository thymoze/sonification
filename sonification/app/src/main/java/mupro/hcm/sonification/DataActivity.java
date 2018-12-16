package mupro.hcm.sonification;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;

import java.time.format.DateTimeFormatter;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.LiveData;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;
import mupro.hcm.sonification.database.AppDatabase;
import mupro.hcm.sonification.database.DataSet;
import mupro.hcm.sonification.database.SensorData;
import mupro.hcm.sonification.fragments.ChartsFragment;
import mupro.hcm.sonification.fragments.MapFragment;
import mupro.hcm.sonification.sensors.SensorDataReceiver;

import static mupro.hcm.sonification.NavbarActivity.BROADCAST_ACTION;

public class DataActivity extends AppCompatActivity {
    private static final String TAG = DataActivity.class.getName();

    @BindView(R.id.data_activity_toolbar)
    Toolbar toolbar;
    @BindView(R.id.data_activity_appbar)
    AppBarLayout appBarLayout;
    @BindView(R.id.data_activity_tablayout)
    TabLayout tabLayout;
    @BindView(R.id.data_activity_container)
    ViewPager viewPager;

    private ChartsFragment mChartsFragment;
    private MapFragment mMapFragment;
    private SensorDataReceiver mSensorDataReceiver;

    private DataSet mDataSet;
    private LiveData<List<SensorData>> mSensorData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        AsyncTask.execute(() -> {
            mDataSet = AppDatabase.getDatabase(this).dataSetDao()
                    .getById(intent.getLongExtra("DATASET_ID", -1));
            toolbar.setTitle(mDataSet.getName());
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
                        if (mMapFragment == null)
                            mMapFragment = MapFragment.newInstance(mDataSet.getId());
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
    protected void onStart() {
        super.onStart();

        long currDataSetId = getSharedPreferences("DATA", MODE_PRIVATE)
                .getLong("CURRENT_DATASET", -1);

        if (mDataSet.getId() == currDataSetId) {
            registerReceivers();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mSensorDataReceiver != null) {
            unregisterReceiver(mSensorDataReceiver);
        }
    }

    private void registerReceivers() {
        mSensorDataReceiver = new SensorDataReceiver(this::receivedData);
        IntentFilter intentFilter = new IntentFilter(BROADCAST_ACTION);
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
}
