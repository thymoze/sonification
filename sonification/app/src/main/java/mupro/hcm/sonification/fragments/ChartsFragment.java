package mupro.hcm.sonification.fragments;

import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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

    private SharedPreferences sharedPreferences;
    private SensorDataReceiver sensorDataReceiver;
    private Set<String> sensors;

    @BindView(R.id.charts_container)
    LinearLayout charts_container;

    @BindView(R.id.no_charts_text)
    TextView no_charts_text;

    public ChartsFragment() {
    }

    public static ChartsFragment newInstance() {
        return new ChartsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getContext().getSharedPreferences("CHARTS", Context.MODE_PRIVATE);
        sensors = sharedPreferences.getStringSet("CHART_LIST",
                Arrays.stream(Sensor.values())
                        .map(Sensor::getId)
                        .collect(Collectors.toSet()));
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_charts, container, false);
        ButterKnife.bind(this, view);

        no_charts_text.setVisibility(sensors.isEmpty() ? View.VISIBLE : View.GONE);
        for (String s : sensors) {
            addFragment(s);
        }

        sensorDataReceiver = new SensorDataReceiver(this::updateCharts);
        IntentFilter intentFilter = new IntentFilter(BROADCAST_ACTION);
        getContext().registerReceiver(sensorDataReceiver, intentFilter);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Arrays.stream(Sensor.values())
                .sorted(Comparator.comparing(s -> s.getLocalizedName(getContext())))
                .forEach(s -> {
                    menu.add(Menu.NONE, s.ordinal() + 1, Menu.NONE, s.getLocalizedName(getContext()))
                            .setCheckable(true)
                            .setChecked(sensors.contains(s.getId()));
                });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String id = Sensor.values()[item.getItemId() - 1].getId();

        if (item.isChecked()) {
            item.setChecked(false);
            sensors.remove(id);
            removeFragment(id);
        } else {
            item.setChecked(true);
            sensors.add(id);
            addFragment(id);
        }

        no_charts_text.setVisibility(sensors.isEmpty() ? View.VISIBLE : View.GONE);

        sharedPreferences.edit()
                .putStringSet("CHART_LIST", sensors)
                .apply();

        //updateFragments();

        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContext().unregisterReceiver(sensorDataReceiver);
    }

    private void addFragment(String tag) {
        FragmentTransaction transaction = requireFragmentManager().beginTransaction();
        transaction.add(R.id.charts_container, ChartCardFragment.newInstance(tag), tag);
        transaction.commit();
    }

    private void removeFragment(String tag) {
        FragmentManager fragmentManager = requireFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        Fragment fragment = fragmentManager.findFragmentByTag(tag);
        if (fragment != null)
            transaction.remove(fragment);
        transaction.commit();
    }

    private Void updateCharts(SensorData data) {
        FragmentManager fragmentManager = getFragmentManager();
        for (String s : sensors) {
            if (fragmentManager != null) {
                ChartCardFragment fragment = (ChartCardFragment) fragmentManager.findFragmentByTag(s);

                if (fragment != null) {
                    Double val = data.get(s);
                    if (val != null) {
                        float x = (float) data.getId();
                        float y = val.floatValue();
                        Log.i(TAG, String.format("(%f, %f)", x, y));
                        fragment.addEntryToChart(x, y);
                    } else {
                        Log.e(TAG, "val is null: " + s);
                    }
                }
            }
        }

        return null;
    }
}
