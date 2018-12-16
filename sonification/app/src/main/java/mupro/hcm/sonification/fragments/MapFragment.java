package mupro.hcm.sonification.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
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

import java.lang.ref.WeakReference;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import mupro.hcm.sonification.R;
import mupro.hcm.sonification.database.AppDatabase;
import mupro.hcm.sonification.database.SensorData;
import mupro.hcm.sonification.database.SensorDataDao;
import mupro.hcm.sonification.location.FusedLocationProvider;
import mupro.hcm.sonification.sensors.Sensor;
import mupro.hcm.sonification.sensors.SensorDataReceiver;

import static mupro.hcm.sonification.NavbarActivity.BROADCAST_ACTION;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment implements
        GoogleMap.OnMarkerClickListener, OnMapReadyCallback {
    private static final String TAG = MapFragment.class.getName();
    private static final String ARG_DATASET_ID = TAG.concat("dataset_id");

    private SupportMapFragment mSupportMapFragment;
    private GoogleMap mGoogleMap;

    private long mDataSetId;

    public MapFragment() {}

    public static MapFragment newInstance(long dataSetId) {
        MapFragment fragment = new MapFragment();
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

        this.mGoogleMap = googleMap;
        initializeMarkers();
    }

    private void initializeMarkers() {
        new loadFromDbTask(this).execute(mDataSetId);
    }

    public Void addMarker(SensorData data) {
        Marker marker = mGoogleMap.addMarker(new MarkerOptions()
                .position(new LatLng(data.getLatitude(), data.getLongitude()))
                .title(DateTimeFormatter.ofPattern("dd.MM.yyyy - hh:mm").format(LocalDateTime.ofInstant(data.getTimestamp(), ZoneOffset.UTC))));
        marker.setTag(data);

        CameraPosition position = new CameraPosition.Builder()
                .target(new LatLng(data.getLatitude(), data.getLongitude()))
                .zoom(15.0f)
                .build();
        mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));

        Log.i(TAG, "Marker added for " + data.getTimestamp());
        return null;
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        Toast.makeText(getContext(), marker.getTag().toString(), Toast.LENGTH_SHORT).show();

        return false;
    }

    private static class loadFromDbTask extends AsyncTask<Long, SensorData, Void> {

        private SensorDataDao mSensorDataDao;
        private WeakReference<MapFragment> mContext;

        loadFromDbTask(MapFragment context) {
            mContext = new WeakReference<>(context);
            mSensorDataDao = AppDatabase.getDatabase(context.getContext()).sensorDataDao();
        }

        @Override
        protected Void doInBackground(Long... dataSetIds) {
            mSensorDataDao.getSensorDataForDataSet(dataSetIds[0])
                    .forEach(this::publishProgress);
            return null;
        }

        @Override
        protected void onProgressUpdate(SensorData... values) {
            MapFragment fragment = mContext.get();
            if (fragment != null)
                fragment.addMarker(values[0]);
        }
    }
}
