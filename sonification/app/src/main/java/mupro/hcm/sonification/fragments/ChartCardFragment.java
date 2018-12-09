package mupro.hcm.sonification.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import mupro.hcm.sonification.R;
import mupro.hcm.sonification.database.AppDatabase;
import mupro.hcm.sonification.database.SensorData;
import mupro.hcm.sonification.helpers.Sensor;


public class ChartCardFragment extends Fragment {
    private static final String TAG = "ChartCardFragment";
    private static final String ARG_SENSOR_NAME = "_sensor_name";

    private String sensor;

    @BindView(R.id.chart) LineChart chart;
    @BindView(R.id.label) TextView label;

    public ChartCardFragment() {}

    public static ChartCardFragment newInstance(String sensor) {
        ChartCardFragment fragment = new ChartCardFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SENSOR_NAME, sensor);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            sensor = getArguments().getString(ARG_SENSOR_NAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chart_card, container, false);
        ButterKnife.bind(this, view);

        label.setText(Sensor.fromId(sensor).getLocalizedName(getContext()));
        initChart();
        loadFromDb();

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

    private void loadFromDb() {
        List<SensorData> last30 = AppDatabase.getDatabase(getContext()).sensorDataDao().getAll();
        Double val;
        for (SensorData data : last30)
            if ((val = data.get(sensor)) != null)
                addEntryToChart((float) data.getId(), val.floatValue());
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


        IAxisValueFormatter xAxisFormatter = (value, axis) -> {
            Instant ts = AppDatabase.getDatabase(getContext()).sensorDataDao().getTimestampById((int) value - 1);
            if (ts == null) {
                Log.w(TAG, "Timestamp is null");
                return "";
            } else {
                ZonedDateTime z = ZonedDateTime.ofInstant(ts, ZoneId.systemDefault());
                return z.format(DateTimeFormatter.ofPattern("dd/MM HH:mm"));
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
        y.setAxisMinimum(-5);
        y.setAxisMaximum(5);

        chart.getAxisRight().setEnabled(false);
    }

    private LineDataSet createSet(LineChart chart) {
        LineDataSet set = new LineDataSet(null, Sensor.fromId(sensor).getLocalizedName(getContext()));
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);
        set.setDrawFilled(true);
        set.setDrawCircles(true);
        set.setLineWidth(1.8f);
        set.setDrawValues(false);
        set.setCircleRadius(1f);
        set.setCircleColor(Color.BLACK);
        //set.setHighLightColor(Color.BLUE);
        set.setColor(Color.rgb(104, 241, 175));
        set.setFillColor(Color.rgb(104, 241, 175));
        set.setFillAlpha(255);
        set.setDrawHorizontalHighlightIndicator(false);
        set.setFillFormatter((dataSet, dataProvider) -> chart.getAxisLeft().getAxisMinimum());

        return set;
    }

    public void addEntryToChart(float x, float y) {
        LineData data = chart.getData();

        if (data == null) {
            data = new LineData();
            chart.setData(data);
        }

        ILineDataSet set = data.getDataSetByIndex(0);
        // set.addEntry(...); // can be called as well

        if (set == null) {
            set = createSet(chart);
            data.addDataSet(set);
        }

        data.addEntry(new Entry(x, y), 0);
        data.notifyDataChanged();

        // let the chart know it's data has changed
        chart.notifyDataSetChanged();

        //chart.setVisibleXRangeMaximum(20);
        //chart.setVisibleYRangeMaximum(15, AxisDependency.LEFT);

        // this automatically refreshes the chart (calls invalidate())
        int offset = set.getEntryCount() - 7;
        if (offset < 0) offset = 0;
        float xx = set.getEntryForIndex(offset).getX();
        Log.i(TAG, String.format("%f, %d, %f", x, offset, xx));
        chart.moveViewTo(xx, 0f, YAxis.AxisDependency.LEFT);
    }

}
