package mupro.hcm.sonification.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import mupro.hcm.sonification.R;
import mupro.hcm.sonification.services.DataService;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    private boolean running = false;
    private Button btnStart;
    private Button btnStop;

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
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_home, container, false);

        btnStart = v.findViewById(R.id.btnStart);
        btnStart.setEnabled(!running);
        btnStart.setOnClickListener(v1 -> {
            startDataService();
            toggleButtons();
        });

        btnStop = v.findViewById(R.id.btnStop);
        btnStop.setEnabled(running);
        btnStop.setOnClickListener(v1 -> {
            stopDataService();
            toggleButtons();
        });

        return v;
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
