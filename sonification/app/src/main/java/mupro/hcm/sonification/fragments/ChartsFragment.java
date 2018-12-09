package mupro.hcm.sonification.fragments;

import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import mupro.hcm.sonification.R;
import mupro.hcm.sonification.database.SensorData;
import mupro.hcm.sonification.helpers.SensorDataHelper;
import mupro.hcm.sonification.helpers.SensorDataReceiver;
import mupro.hcm.sonification.helpers.Sensor;

import static mupro.hcm.sonification.MainActivity.BROADCAST_ACTION;

public class ChartsFragment extends Fragment {

    private static String TAG = "ChartsFragment";
    private SensorDataReceiver sensorDataReceiver;

    @BindView(R.id.charts_container)
    LinearLayout charts_container;

    public ChartsFragment() {
    }

    public static ChartsFragment newInstance() {
        return new ChartsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_charts, container, false);
        ButterKnife.bind(this, view);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        for (Sensor s : Sensor.values()) {
            Fragment fragment = ChartCardFragment.newInstance(s);
            transaction.add(R.id.charts_container, fragment, s.getId());
        }
        transaction.commit();

        sensorDataReceiver = new SensorDataReceiver(this::updateCharts);
        IntentFilter intentFilter = new IntentFilter(BROADCAST_ACTION);
        getContext().registerReceiver(sensorDataReceiver, intentFilter);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Objects.requireNonNull(getContext()).unregisterReceiver(sensorDataReceiver);
    }

    private Void updateCharts(SensorData data) {
        FragmentManager fragmentManager = getFragmentManager();
        for (Sensor s : Sensor.values()) {
            if (fragmentManager != null) {
                ChartCardFragment fragment = (ChartCardFragment) fragmentManager.findFragmentByTag(s.getId());

                if (fragment != null) {
                    Double val = data.get(s);
                    if (val != null) {
                        float x = (float) data.getId();
                        float y = val.floatValue();
                        Log.i(TAG, String.format("(%f, %f)", x, y));
                        fragment.addEntryToChart(x, y);
                    }
                }
            }
        }

        return null;
    }
}
