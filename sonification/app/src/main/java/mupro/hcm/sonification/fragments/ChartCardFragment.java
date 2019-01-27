package mupro.hcm.sonification.fragments;


import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.lang.ref.WeakReference;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import butterknife.BindColor;
import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import mupro.hcm.sonification.R;
import mupro.hcm.sonification.database.AppDatabase;
import mupro.hcm.sonification.database.SensorDataDao;
import mupro.hcm.sonification.sensors.Sensor;

public class ChartCardFragment extends Fragment {
    private static final String TAG = ChartCardFragment.class.getName();
    private static final String ARG_SENSOR_ID = TAG.concat("sensor_name");
    private static final String ARG_DATASET_ID = TAG.concat("dataset_id");

    @BindView(R.id.chart)
    LineChart chart;
    @BindView(R.id.label)
    TextView label;

    @BindColor(R.color.error)
    int chart_empty_color;
    @BindColor(R.color.secondary_variant)
    int chart_color;

    private String mSensorId;
    private long mDataSetId;

    public ChartCardFragment() {}

    public static ChartCardFragment newInstance(String sensor, long dataSetId) {
        ChartCardFragment fragment = new ChartCardFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SENSOR_ID, sensor);
        args.putLong(ARG_DATASET_ID, dataSetId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSensorId = getArguments().getString(ARG_SENSOR_ID);
            mDataSetId = getArguments().getLong(ARG_DATASET_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chart_card, container, false);
        ButterKnife.bind(this, view);

        label.setText(Sensor.fromId(mSensorId).getLocalizedName(getContext()));
        initChart();
        loadFromDb();

        return view;
    }

    public long getDataSetId() {
        return mDataSetId;
    }

    public String getSensorId() {
        return mSensorId;
    }

    public void loadFromDb() {
        new loadFromDbTask(this).execute(Pair.create(mSensorId, mDataSetId));
    }

    // -------------------------------------

    private void initChart() {
        chart.setBackgroundColor(Color.WHITE);

        // no description text
        chart.getDescription().setEnabled(false);

        // enable touch gestures
        chart.setTouchEnabled(true);

        // enable scaling and dragging
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(false);

        chart.setDrawGridBackground(false);
        chart.setMaxHighlightDistance(300);

        chart.getLegend().setEnabled(false);

        chart.setNoDataText(getResources().getString(R.string.no_chart_data));
        chart.setNoDataTextColor(chart_empty_color);

        chart.setBackgroundResource(R.color.surface);

        IAxisValueFormatter xAxisFormatter = new IAxisValueFormatter() {
            private DateTimeFormatter mFormat = DateTimeFormatter.ofPattern("HH:mm");

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                int bits = Float.floatToIntBits(value);
                Instant time = Instant.ofEpochSecond(bits);
                ZonedDateTime z = ZonedDateTime.ofInstant(time, ZoneId.systemDefault());
                return mFormat.format(z);
            }
        };

        XAxis x = chart.getXAxis();
        x.setLabelCount(3, false);
        x.setEnabled(true);
        x.setPosition(XAxis.XAxisPosition.TOP);
        x.setDrawGridLines(false);
        x.setDrawAxisLine(false);
        x.setValueFormatter(xAxisFormatter);

        YAxis y = chart.getAxisLeft();
        y.setLabelCount(5, false);
        y.setTextColor(Color.BLACK);
        y.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        y.setDrawAxisLine(false);
        //y.setAxisMinimum(-5);
        //y.setAxisMaximum(5);

        chart.getAxisRight().setEnabled(false);
    }

    private LineDataSet createSet(LineChart chart) {
        LineDataSet set = new LineDataSet(null, Sensor.fromId(mSensorId).getLocalizedName(getContext()));
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);
        set.setDrawFilled(true);
        set.setDrawCircles(true);
        set.setLineWidth(1.8f);
        set.setDrawValues(false);
        set.setCircleRadius(1f);
        set.setCircleColor(Color.BLACK);
        set.setHighlightEnabled(false);
        set.setColor(chart_color);
        set.setFillColor(chart_color);
        set.setFillAlpha(255);
        set.setDrawHorizontalHighlightIndicator(false);
        set.setFillFormatter((dataSet, dataProvider) -> chart.getAxisLeft().getAxisMinimum());

        return set;
    }

    public boolean chartContainsEntry(Instant x) {
        float xx = Float.intBitsToFloat((int) x.getEpochSecond());

        LineData data = chart.getData();

        if (data == null) {
            return false;
        }

        LineDataSet set = (LineDataSet) data.getDataSetByIndex(0);

        if (set == null)
            return false;

        return !set.getEntriesForXValue(xx).isEmpty();
    }

    public boolean removeEntryFromChart(Instant x) {
        float xx = Float.intBitsToFloat((int) x.getEpochSecond());

        LineData data = chart.getData();

        if (data == null) {
            return false;
        }

        LineDataSet set = (LineDataSet) data.getDataSetByIndex(0);

        if (set == null)
            return false;

        boolean removed = set.removeEntryByXValue(xx);
        if (set.getEntryCount() == 0) {
            data.removeDataSet(0);
            chart.setData(null);
        }
        chart.invalidate();
        return removed;
    }

    public void addEntryToChart(Instant x, float y) {
        LineData data = chart.getData();

        if (data == null) {
            data = new LineData();
            chart.setData(data);
        }

        LineDataSet set = (LineDataSet) data.getDataSetByIndex(0);
        // set.addEntry(...); // can be called as well

        if (set == null) {
            set = createSet(chart);
            data.addDataSet(set);
        }

        float xx = Float.intBitsToFloat((int) x.getEpochSecond());
        data.addEntry(new Entry(xx, y), 0);
        data.notifyDataChanged();

        // let the chart know it's data has changed
        chart.notifyDataSetChanged();

        //chart.setVisibleXRangeMaximum(20);
        //chart.setVisibleYRangeMaximum(15, AxisDependency.LEFT);

        chart.invalidate();
    }

    private static class loadFromDbTask extends AsyncTask<Pair<String, Long>, Pair<Instant, Double>, Void> {

        private SensorDataDao mSensorDataDao;
        private WeakReference<ChartCardFragment> mContext;

        loadFromDbTask(ChartCardFragment context) {
            mContext = new WeakReference<>(context);
            mSensorDataDao = AppDatabase.getDatabase(context.getContext()).sensorDataDao();
        }

        @Override
        protected Void doInBackground(Pair<String, Long>... pairs) {
            List<Double> sensorData = mSensorDataDao.getSensorForDataSet(Sensor.fromId(pairs[0].first), pairs[0].second);
            List<Instant> timeData = mSensorDataDao.getTimestampForDataSet(pairs[0].second);

            if (sensorData.size() == timeData.size()) {
                for (int i = 0; i < sensorData.size(); i++) {
                    publishProgress(new Pair<>(timeData.get(i), sensorData.get(i)));
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Pair<Instant, Double>... values) {
            ChartCardFragment fragment = mContext.get();
            if (fragment != null)
                fragment.addEntryToChart(values[0].first, values[0].second.floatValue());
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mContext.get().getParentFragment().startPostponedEnterTransition();
        }
    }
}
