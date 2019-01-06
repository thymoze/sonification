package mupro.hcm.sonification.fragments;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.lang.ref.WeakReference;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import mupro.hcm.sonification.R;
import mupro.hcm.sonification.database.AppDatabase;
import mupro.hcm.sonification.database.SensorData;
import mupro.hcm.sonification.database.SensorDataDao;
import mupro.hcm.sonification.location.FusedLocationProvider;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment implements
        GoogleMap.OnMarkerClickListener, OnMapReadyCallback, GoogleMap.OnMapClickListener {
    private static final String TAG = MapFragment.class.getName();
    private static final String ARG_DATASET_ID = TAG.concat("dataset_id");

    private SupportMapFragment mSupportMapFragment;
    private GoogleMap mGoogleMap;
    private List<Polyline> polylines;
    private SensorData previousData;

    @BindView(R.id.bottom_sheet_title)
    TextView bottomSheetTitle;

    @BindView(R.id.bottom_sheet_content)
    TextView bottomSheetContent;

    @BindView(R.id.bottom_sheet)
    LinearLayout bottomSheet;

    private long mDataSetId;
    private BottomSheetBehavior mBottomSheetBehavior;

    public MapFragment() {
    }

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
        polylines = new ArrayList<>();
        if (getArguments() != null) {
            mDataSetId = getArguments().getLong(ARG_DATASET_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_map, container, false);

        ButterKnife.bind(this, v);

        // hide bottom sheet at the start
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        // expand bottom sheet on click, not only drag
        bottomSheet.setOnClickListener((listener) -> {
            if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        mSupportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mSupportMapFragment.getMapAsync(this);

        return v;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.setMyLocationEnabled(true);
        googleMap.setOnMarkerClickListener(this);
        googleMap.setOnMapClickListener(this);

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
                .position(new LatLng(data.getLatitude(), data.getLongitude())));
        marker.setTag(data);

        CameraPosition position = new CameraPosition.Builder()
                .target(new LatLng(data.getLatitude(), data.getLongitude()))
                .zoom(15.0f)
                .build();
        mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));

        // add a polyline if there was previous data
        if (previousData != null) {
            PolylineOptions options = new PolylineOptions().add(new LatLng(previousData.getLatitude(), previousData.getLongitude()))
                    .add(new LatLng(data.getLatitude(), data.getLongitude()));
            Polyline line = mGoogleMap.addPolyline(options);
            line.setZIndex(1000);
            polylines.add(line);
        }
        previousData = data;

        Log.i(TAG, "Marker added for " + data.getTimestamp());
        return null;
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        // displaying information in the bottom sheet
        bottomSheetTitle.setText(DateTimeFormatter.ofPattern("dd.MM.yyyy - hh:mm").format(LocalDateTime.ofInstant(((SensorData) marker.getTag()).getTimestamp(), ZoneOffset.UTC)));
        bottomSheetContent.setText(((SensorData) marker.getTag()).toString());
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        return false;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        // bottom sheet should be hidden if the map is clicked
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
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
