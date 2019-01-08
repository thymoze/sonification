package mupro.hcm.sonification.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import mupro.hcm.sonification.R;
import mupro.hcm.sonification.database.SensorData;

public class MapsBottomSheetFragment extends Fragment {

    private static final String TAG = MapsBottomSheetFragment.class.getName();
    private static final String ARG_SENSORDATA = TAG.concat("sensordata");

    private SensorData mSensorData;

    @BindView(R.id.bottom_sheet_title)
    TextView bottomSheetTitle;

    @BindView(R.id.bottom_sheet_content)
    TextView bottomSheetContent;

    public MapsBottomSheetFragment() {}

    public static MapsBottomSheetFragment newInstance(SensorData sensorData) {
        MapsBottomSheetFragment fragment = new MapsBottomSheetFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_SENSORDATA, sensorData);
        fragment.setArguments(args);
        return fragment;
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

        bottomSheetTitle.setText(DateTimeFormatter.ofPattern("dd.MM.yyyy - hh:mm").format(LocalDateTime.ofInstant(mSensorData.getTimestamp(), ZoneOffset.UTC)));
        bottomSheetContent.setText(mSensorData.toString());

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
