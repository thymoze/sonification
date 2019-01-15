package mupro.hcm.sonification.fragments;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

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
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
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

    private long mDataSetId;
    private BottomSheetBehavior mBottomSheetBehavior;
    private Marker mCurrentMarker;
    private LinkedList<Marker> markers;

    @BindView(R.id.bottom_sheet_placeholder)
    FrameLayout bottomSheet;

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
        markers = new LinkedList<>();
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

        SensorData data = new SensorData();
        data.setC2h5oh(2);
        data.setC3h8(5);
        data.setCo(3);
        data.setC4h10(1);
        data.setH2(7);
        data.setHumidity(0.4);
        data.setNh3(4);
        data.setCh4(3);
        data.setPm25(21);
        data.setPm10(22);
        data.setNo2(22);
        data.setTimestamp(Instant.now());
        data.setLatitude(48.3);
        data.setLongitude(10.898);
        data.setTemperatureBMP(23);
        data.setTemperatureSHT(22);
        addMarker(data);

        SensorData data2 = new SensorData();
        data2.setC2h5oh(2);
        data2.setC3h8(5);
        data2.setC4h10(1);
        data2.setH2(7.0);
        data2.setHumidity(0.4);
        data2.setNh3(4.0);
        data2.setCh4(3.0);
        data2.setPm25(21.0);
        data2.setPm10(22.0);
        data2.setNo2(22.0);
        data2.setTimestamp(Instant.now());
        data2.setLatitude(48.4);
        data2.setLongitude(10.85);
        data2.setTemperatureBMP(23);
        data2.setTemperatureSHT(22);
        data2.setCo(200000);
        addMarker(data2);

        SensorData data3 = new SensorData();
        data3.setC2h5oh(2);
        data3.setC3h8(5);
        data3.setC4h10(1);
        data3.setH2(7.0);
        data3.setHumidity(0.4);
        data3.setNh3(4.0);
        data3.setCh4(3.0);
        data3.setPm25(21.0);
        data3.setPm10(22.0);
        data3.setNo2(22.0);
        data3.setTimestamp(Instant.now());
        data3.setLatitude(48.5);
        data3.setLongitude(10.898);
        data3.setTemperatureBMP(23);
        data3.setTemperatureSHT(22);
        data3.setCo(200000);
        addMarker(data3);

    }

    private void initializeMarkers() {
        new loadFromDbTask(this).execute(mDataSetId);
    }

    public Marker addMarker(SensorData data) {
        Marker marker = mGoogleMap.addMarker(new MarkerOptions()
                .position(new LatLng(data.getLatitude(), data.getLongitude())));
        marker.setTag(data);

        CameraPosition position = new CameraPosition.Builder()
                .target(new LatLng(data.getLatitude(), data.getLongitude()))
                .zoom(15.0f)
                .build();
        mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));

        addPolyline(markers.peekLast(), marker);
        markers.add(marker);

        Log.i(TAG, "Marker added for " + data.getTimestamp());
        return marker;
    }

    private void addPolyline(Marker start, Marker end) {
        // add a polyline if there was previous data
        if (start != null && end != null) {
            PolylineOptions options = new PolylineOptions().add(start.getPosition())
                    .add(end.getPosition());
            Polyline line = mGoogleMap.addPolyline(options);
            line.setZIndex(1000);
            polylines.add(line);
        }
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        mCurrentMarker = marker;

        // add a new bottom sheet with the marker information (replacing the previous one)
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        MapsBottomSheetFragment fragment = MapsBottomSheetFragment.newInstance(((SensorData) marker.getTag()));
        transaction.replace(R.id.bottom_sheet_placeholder, fragment);
        transaction.commit();

        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        // expand bottom sheet on click, not only drag
        bottomSheet.setOnClickListener((listener) -> {
            if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        return false;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        // bottom sheet should be hidden if the map is clicked
        if (mBottomSheetBehavior != null)
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

    public void updateMap() {
        // remove old polylines from that point
        List<Polyline> adjLines = polylines.stream()
                .flatMap(lines -> Stream.of(lines.getPoints())
                        .filter(latLng -> latLng.get(0).equals(mCurrentMarker.getPosition()) || latLng.get(1).equals(mCurrentMarker.getPosition()))
                        .limit(2)
                        .map(marker -> lines)).collect(Collectors.toList());

        Log.i(TAG, "Adj: " + adjLines.size());
        Log.i(TAG, "All: " + polylines.size());
        polylines.removeAll(adjLines);
        adjLines.forEach(Polyline::remove);

        int index = markers.indexOf(mCurrentMarker);

        // add new polyline only if marker is not start or end
        if (index > 0 && index < markers.size() - 1) {
            Marker prev = markers.get(index - 1);
            Marker next = markers.get(index + 1);

            addPolyline(prev, next);
        }

        // remove marker
        markers.remove(index);
        mCurrentMarker.remove();

        // TODO: recalculate distance

        // bottom sheet should be hidden if the map is clicked
        if (mBottomSheetBehavior != null)
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }
}
