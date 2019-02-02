package mupro.hcm.sonification.fragments;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.time.Instant;
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
    private static final String ARG_DATASET_ID = TAG.concat(".dataset_id");

    @BindView(R.id.charts_container)
    LinearLayout charts_container;
    @BindView(R.id.no_charts_text)
    TextView no_charts_text;

    private TreeSet<String> mSensors;
    private long mDataSetId;
    private int initCounter;

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

        mSensors = new TreeSet<>(Comparator.comparing(s -> Sensor.fromId(s).getLocalizedName(getContext())));

        //setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateFragments();
    }

    @Override
    public void startPostponedEnterTransition() {
        if (++initCounter == mSensors.size()) {
            Log.i(TAG, "Starting postponed enter transition");
            getActivity().startPostponedEnterTransition();
            super.startPostponedEnterTransition();
        } else {
            Log.i(TAG, "loadFromDb " + initCounter + " finished");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_charts, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    private void updateFragments() {
        mSensors.clear();
        mSensors.addAll(PreferenceManager.getDefaultSharedPreferences(getContext())
                .getStringSet("sensors_preference", Arrays.stream(Sensor.values())
                        .map(Sensor::getId)
                        .collect(Collectors.toSet())));

        no_charts_text.setVisibility(mSensors.isEmpty() ? View.VISIBLE : View.GONE);

        for (Sensor sensor : Sensor.values()) {
            Fragment f = getChildFragmentManager().findFragmentByTag(sensor.getId());
            if (f != null) {
                getFragmentManager().beginTransaction().remove(f).commit();
            }
        }

        for (String s : mSensors) {
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            ChartCardFragment fragment = ChartCardFragment.newInstance(s, mDataSetId);
            transaction.add(R.id.charts_container, fragment, s);
            transaction.commit();
        }
    }

    public Void updateCharts(SensorData data) {
        FragmentManager fragmentManager = getChildFragmentManager();
        for (String s : mSensors) {
            ChartCardFragment fragment = (ChartCardFragment) fragmentManager.findFragmentByTag(s);

            if (fragment != null) {
                Double val = data.get(Sensor.fromId(s));
                if (val != null) {
                    Instant x = data.getTimestamp();
                    float y = val.floatValue();
                    if (fragment.chartContainsEntry(x)) {
                        Log.i(TAG, "updateCharts remove");
                        boolean removed = fragment.removeEntryFromChart(x);
                        Log.i(TAG, removed ? "Datapoint removed" : "Removing failed.");
                    } else {
                        Log.i(TAG, "updateCharts insert");
                        fragment.addEntryToChart(x, y);
                    }
                } else {
                    Log.e(TAG, "val is null: " + s);
                }
            } else {
                Log.i(TAG, "ChartsCardFragment " + s + " is null");
            }
        }

        return null;
    }
}
