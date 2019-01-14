package mupro.hcm.sonification.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Locale;

import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import mupro.hcm.sonification.R;
import mupro.hcm.sonification.sensors.Sensor;

public class GasValueFragment extends Fragment {

    private static final String TAG = MapsBottomSheetFragment.class.getName();
    private static final String ARG_SENSOR = TAG.concat("sensor");
    private static final String ARG_VALUE = TAG.concat("value");

    private String mSensorId;
    private double value;

    @BindView(R.id.gas_card_title)
    TextView gasCardTitle;

    @BindView(R.id.gas_card_value)
    TextView gasCardValue;

    public GasValueFragment() {
    }

    public static GasValueFragment newInstance(String sensor, double value) {
        GasValueFragment fragment = new GasValueFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SENSOR, sensor);
        args.putDouble(ARG_VALUE, value);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSensorId = getArguments().getString(ARG_SENSOR);
            value = getArguments().getDouble(ARG_VALUE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.gas_value_fragment, container, false);
        ButterKnife.bind(this, view);

        gasCardTitle.setText((Sensor.fromId(mSensorId).getLocalizedName(getContext())));
        gasCardValue.setText(String.format(Locale.GERMAN, "%.3f", value));

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
