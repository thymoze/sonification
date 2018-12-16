package mupro.hcm.sonification.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.stream.Collectors;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import butterknife.BindView;
import butterknife.ButterKnife;
import mupro.hcm.sonification.R;
import mupro.hcm.sonification.database.SensorData;
import mupro.hcm.sonification.sensors.Sensor;

public class ChartsFragment extends Fragment {

    private static final String TAG = ChartsFragment.class.getName();
    private static final String ARG_DATASET_ID = TAG.concat("dataset_id");

    @BindView(R.id.charts_container)
    LinearLayout charts_container;
    @BindView(R.id.no_charts_text)
    TextView no_charts_text;

    private SharedPreferences sharedPreferences;

    private TreeSet<String> mSensors;
    private long mDataSetId;

    public ChartsFragment() {
    }

    public static ChartsFragment newInstance(long dataSetId) {
        ChartsFragment fragment = new ChartsFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_DATASET_ID, dataSetId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mDataSetId = getArguments().getLong(ARG_DATASET_ID);
        }

        sharedPreferences = getContext().getSharedPreferences("CHARTS", Context.MODE_PRIVATE);
        mSensors = new TreeSet<>(Comparator.comparing(s -> Sensor.fromId(s).getLocalizedName(getContext())));
        mSensors.addAll(sharedPreferences.getStringSet("CHART_LIST",
                                                       Arrays.stream(Sensor.values())
                                                             .map(Sensor::getId)
                                                             .collect(Collectors.toSet())));

        setHasOptionsMenu(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_charts, container, false);
        ButterKnife.bind(this, view);

        no_charts_text.setVisibility(mSensors.isEmpty() ? View.VISIBLE : View.GONE);
        //new FragmentLoader(this).execute();
        updateFragments();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Arrays.stream(Sensor.values())
                .parallel()
                .sorted(Comparator.comparing(s -> s.getLocalizedName(getContext())))
                .forEachOrdered(s -> {
                    menu.add(99, s.ordinal() + 1, Menu.NONE, s.getLocalizedName(getContext()))
                            .setCheckable(true)
                            .setChecked(mSensors.contains(s.getId()));
                });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getGroupId() == 99) {
            String id = Sensor.values()[item.getItemId() - 1].getId();

            if (item.isChecked()) {
                item.setChecked(false);
                mSensors.remove(id);
            } else {
                item.setChecked(true);
                mSensors.add(id);
            }
            //new FragmentLoader(this).execute();
            updateFragments();

            no_charts_text.setVisibility(mSensors.isEmpty() ? View.VISIBLE : View.GONE);

            sharedPreferences.edit()
                    .putStringSet("CHART_LIST", mSensors)
                    .apply();
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateFragments() {
        for (Sensor sensor : Sensor.values()) {
            Fragment f = getFragmentManager().findFragmentByTag(sensor.getId());
            if (f != null) {
                getFragmentManager().beginTransaction().remove(f).commit();
            }
        }

        mSensors.parallelStream()
                .sorted(Comparator.comparing(s -> Sensor.fromId(s).getLocalizedName(getContext())))
                .forEachOrdered(s -> {
                    FragmentTransaction transaction = requireFragmentManager().beginTransaction();
                    transaction.add(R.id.charts_container, ChartCardFragment.newInstance(s, mDataSetId), s);
                    transaction.commit();
                });
    }

    public Void updateCharts(SensorData data) {
        FragmentManager fragmentManager = getFragmentManager();
        for (String s : mSensors) {
            if (fragmentManager != null) {
                ChartCardFragment fragment = (ChartCardFragment) fragmentManager.findFragmentByTag(s);

                if (fragment != null) {
                    Double val = data.get(Sensor.fromId(s));
                    if (val != null) {
                        fragment.addEntryToChart(data.getTimestamp(), val.floatValue());
                    } else {
                        Log.e(TAG, "val is null: " + s);
                    }
                } else {
                    Log.i(TAG, "ChartsCardFragment " + s + " is null");
                }
            }
        }

        return null;
    }
}
