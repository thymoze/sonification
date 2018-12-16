package mupro.hcm.sonification.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import mupro.hcm.sonification.R;
import mupro.hcm.sonification.services.DataService;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    private boolean running = false;
    private SharedPreferences sharedPreferences;

    @BindView(R.id.btnStart)
    Button btnStart;
    @BindView(R.id.btnStop)
    Button btnStop;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        sharedPreferences = getContext().getSharedPreferences("DATA", Context.MODE_PRIVATE);
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, v);

        running = sharedPreferences.getBoolean("SERVICE_RUNNING", false);

        btnStart.setEnabled(!running);
        btnStop.setEnabled(running);

        return v;
    }

    @OnClick(R.id.btnStart)
    public void start() {
        startDataService();
        toggleButtons();
        sharedPreferences.edit().putBoolean("SERVICE_RUNNING", true).apply();
    }

    @OnClick(R.id.btnStop)
    public void stop() {
        stopDataService();
        toggleButtons();
        sharedPreferences.edit().putBoolean("SERVICE_RUNNING", false).apply();
        sharedPreferences.edit().putLong("CURRENT_DATA_ID", -1).apply();
    }

    private void toggleButtons() {
        this.running = !this.running;
        this.btnStart.setEnabled(!this.running);
        this.btnStop.setEnabled(this.running);
    }

    private void startDataService() {
        final Intent intent = new Intent(this.getContext(), DataService.class);
        getContext().startService(intent);
        getContext().startForegroundService(intent);
    }

    private void stopDataService() {
        final Intent intent = new Intent(this.getContext(), DataService.class);
        getContext().stopService(intent);
    }
}
