package mupro.hcm.sonification.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import mupro.hcm.sonification.R;
import mupro.hcm.sonification.database.AppDatabase;
import mupro.hcm.sonification.database.SensorData;
import mupro.hcm.sonification.location.LocationDataReceiver;
import mupro.hcm.sonification.sensors.Sensor;

import static mupro.hcm.sonification.MainActivity.CURRENT_DATASET;

public class MapsBottomSheetFragment extends Fragment {

    private static final String TAG = MapsBottomSheetFragment.class.getName();
    private static final String ARG_SENSORDATA = TAG.concat(".sensordata");

    private SensorData mSensorData;
    private OnDataPointDeleteListener callback;

    @BindView(R.id.bottom_sheet_title)
    TextView bottomSheetTitle;

    @BindView(R.id.card_placeholder)
    LinearLayout cardPlaceholder;

    @BindView(R.id.delete_button)
    ImageButton deleteButton;

    public MapsBottomSheetFragment() {}

    public static MapsBottomSheetFragment newInstance(SensorData sensorData) {
        MapsBottomSheetFragment fragment = new MapsBottomSheetFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_SENSORDATA, sensorData);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnDataPointDeleteListener(OnDataPointDeleteListener callback) {
        this.callback = callback;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mSensorData = (SensorData) getArguments().getSerializable(ARG_SENSORDATA);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maps_bottom_sheet, container, false);
        ButterKnife.bind(this, view);

        bottomSheetTitle.setText(mSensorData.getTimestamp()
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("dd.MM.YYYY HH:mm")));

        // disable delete button if service is running
        if (PreferenceManager.getDefaultSharedPreferences(getContext())
                .getLong(CURRENT_DATASET, -1) != -1) {
            deleteButton.setClickable(false);
            deleteButton.setVisibility(View.INVISIBLE);
        }

        setupDataPresentation();

        return view;
    }

    @OnClick(R.id.delete_button)
    void onDeleteButtonClicked() {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.confirm_sensordata_delete_title)
                .setMessage(R.string.confirm_sensordata_delete_content)
                .setPositiveButton("Ja", (dialog, whichButton) -> {
                    AsyncTask.execute(() -> AppDatabase.getDatabase(getContext()).sensorDataDao().delete(mSensorData));
                    ((MapFragment) getParentFragment()).updateMap();
                    new LocationDataReceiver().calcDistanceDB(getContext(), mSensorData.getDataSetId());

                    callback.onDataPointDelete(mSensorData);

                    Toast.makeText(getContext(), R.string.delete_successful, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Nein", null).show();
    }

    private void setupDataPresentation() {
        Arrays.stream(Sensor.values())
                .sorted(Comparator.comparing(s -> s.getLocalizedName(getContext())))
                .forEachOrdered(s -> {
                    Log.i(TAG, "Added " + s.getId() + ": " + mSensorData.get(s));
                    FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                    GasValueFragment fragment = GasValueFragment.newInstance(s.getId(), mSensorData.get(s));
                    transaction.add(R.id.card_placeholder, fragment, s.getId());
                    transaction.commit();
                });

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public interface OnDataPointDeleteListener {
        void onDataPointDelete(SensorData deleted);
    }
}
