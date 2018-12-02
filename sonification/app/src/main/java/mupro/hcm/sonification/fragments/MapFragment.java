package mupro.hcm.sonification.fragments;

import android.annotation.SuppressLint;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import mupro.hcm.sonification.R;
import mupro.hcm.sonification.database.SensorData;
import mupro.hcm.sonification.helpers.FusedLocationProvider;
import mupro.hcm.sonification.helpers.SensorDataReceiver;

import static mupro.hcm.sonification.MainActivity.BROADCAST_ACTION;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment implements
        GoogleMap.OnMarkerClickListener,
        OnMapReadyCallback {

    SupportMapFragment mSupportMapFragment;
    GoogleMap googleMap;

    private final String TAG = "MapFragment";

    public MapFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        SensorDataReceiver sensorDataReceiver = new SensorDataReceiver(this::addMarker);
        IntentFilter intentFilter = new IntentFilter(BROADCAST_ACTION);
        getContext().registerReceiver(sensorDataReceiver, intentFilter);

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_map, container, false);

        mSupportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mSupportMapFragment.getMapAsync(this);

        return v;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.setMyLocationEnabled(true);
        googleMap.setOnMarkerClickListener(this);

        FusedLocationProvider.requestSingleUpdate(getContext(), (location -> {
            CameraPosition position = new CameraPosition.Builder()
                    .target(new LatLng(location.latitude, location.longitude))
                    .zoom(15.0f)
                    .build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));
        }));

        this.googleMap = googleMap;
    }

    private Void addMarker(SensorData data) {
        Marker marker = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(data.getLatitude(), data.getLongitude()))
                .title(data.getTimestamp().toString()));
        marker.setTag(data);

        CameraPosition position = new CameraPosition.Builder()
                .target(new LatLng(data.getLatitude(), data.getLongitude()))
                .zoom(15.0f)
                .build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));

        Log.i(TAG, "Marker added for " + data.getTimestamp());
        return null;
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        Toast.makeText(getContext(), marker.getTitle(), Toast.LENGTH_SHORT).show();

        return false;
    }
}
