package mupro.hcm.sonification;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;

import com.github.mikephil.charting.charts.LineChart;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import mupro.hcm.sonification.fragments.ChartsFragment;
import mupro.hcm.sonification.fragments.HomeFragment;
import mupro.hcm.sonification.fragments.MapFragment;

public class MainActivity extends AppCompatActivity  {

    private static final int PORT = 7777;
    private static final String TAG = "ChartsFragment";

    private static AsyncTask<Void, JSONObject, Void> server;
    private boolean running = false;

    private enum Navigation {
        HOME, CHARTS, MAP;
    }
    private HomeFragment homeFragment;
    private ChartsFragment chartsFragment;
    private MapFragment mapFragment;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        switchFragment(Navigation.HOME);
                        return true;
                    case R.id.navigation_dashboard:
                        switchFragment(Navigation.CHARTS);
                        return true;
                    case R.id.navigation_notifications:
                        switchFragment(Navigation.MAP);
                        return true;
                }
                return false;
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        switchFragment(Navigation.HOME);

        running = true;
        runServer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        running = false;
        server.cancel(true);
    }

    @SuppressLint("StaticFieldLeak")
    private void runServer() {
        server = new AsyncTask<Void, JSONObject, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                byte[] msg = new byte[4096];
                DatagramPacket dp = new DatagramPacket(msg, msg.length);

                try(DatagramSocket ds = new DatagramSocket(PORT)) {
                    while (running) {
                        ds.receive(dp);

                        try {
                            publishProgress(new JSONObject(new String(msg, 0, dp.getLength())));
                        } catch (JSONException e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }

                return null;
            }

            @Override
            protected void onProgressUpdate(JSONObject... values) {
                try {
                    for (JSONObject json : values) {
                        if (json.has("F25")) {
                            LineChart chart = findViewById(R.id.chart_part25);
                            if (chartsFragment != null && chart != null) {
                                chartsFragment.addEntryToChart(chart, ((Double) json.get("F25")).floatValue());
                            }
                        }
                        if (json.has("F10")) {
                            LineChart chart = findViewById(R.id.chart_part10);
                            if (chartsFragment != null && chart != null) {
                                chartsFragment.addEntryToChart(chart, ((Double) json.get("F10")).floatValue());
                            }
                        }
                        //and all the other gases
                        //if (json.has("..."))
                    }
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        };
        server.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void switchFragment(Navigation navigation) {
        Fragment fragment;
        switch (navigation) {
            case CHARTS:
                if (chartsFragment == null)
                    chartsFragment = ChartsFragment.newInstance();
                fragment = chartsFragment;
                break;
            case MAP:
                if (mapFragment == null)
                    mapFragment = MapFragment.newInstance();
                fragment = mapFragment;
                break;
            case HOME:
            default:
                if (homeFragment == null)
                    homeFragment = HomeFragment.newInstance();
                fragment = homeFragment;
                break;
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
