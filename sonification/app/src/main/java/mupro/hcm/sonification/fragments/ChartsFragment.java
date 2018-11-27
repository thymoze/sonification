package mupro.hcm.sonification.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import mupro.hcm.sonification.R;
import mupro.hcm.sonification.database.AppDatabase;
import mupro.hcm.sonification.database.SensorData;
import mupro.hcm.sonification.helpers.JsonReceiver;

import static mupro.hcm.sonification.MainActivity.BROADCAST_ACTION;

public class ChartsFragment extends Fragment {

    private static String TAG = "ChartsFragment";

    @BindView(R.id.chart_part10)
    LineChart chartPart10;

    @BindView(R.id.chart_part25)
    LineChart chartPart25;

    public ChartsFragment() {
        // Required empty public constructor
    }

    public static ChartsFragment newInstance() {
        return new ChartsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private LineDataSet createSet(LineChart chart) {
        LineDataSet set = new LineDataSet(null, "DataSet 1");
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);
        set.setDrawFilled(true);
        set.setDrawCircles(false);
        set.setLineWidth(1.8f);
        set.setCircleRadius(4f);
        set.setCircleColor(Color.WHITE);
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setColor(Color.WHITE);
        set.setFillColor(Color.WHITE);
        set.setFillAlpha(100);
        set.setDrawHorizontalHighlightIndicator(false);
        set.setFillFormatter((dataSet, dataProvider) -> chart.getAxisLeft().getAxisMinimum());

        return set;
    }

    public void addEntryToChart(LineChart chart, float value) {
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

        data.addEntry(new Entry(set.getEntryCount(), value), 0);
        data.notifyDataChanged();

        // let the chart know it's data has changed
        chart.notifyDataSetChanged();

        chart.setVisibleXRangeMaximum(20);
        //chart.setVisibleYRangeMaximum(15, AxisDependency.LEFT);
//
//            // this automatically refreshes the chart (calls invalidate())
        chart.moveViewTo(data.getEntryCount() - 7, 50f, YAxis.AxisDependency.LEFT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_charts, container, false);
        ButterKnife.bind(this, view);

        initChart(chartPart25);
        initChart(chartPart10);

        JsonReceiver jsonReceiver = new JsonReceiver(this::updateCharts);
        IntentFilter intentFilter = new IntentFilter(BROADCAST_ACTION);
        getContext().registerReceiver(jsonReceiver, intentFilter);

        return view;
    }

    private Void updateCharts(JSONObject json) {
        try {
            if (json.has("SDS011_PM2.5"))
                addEntryToChart(chartPart25, ((Double) json.get("SDS011_PM2.5")).floatValue());
            if (json.has("SDS011_PM10")) {
                addEntryToChart(chartPart10, ((Double) json.get("SDS011_PM10")).floatValue());
            }
            //and all the other gases
            //if (json.has("..."))
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }

    private void initChart(LineChart chart) {
        chart.setBackgroundColor(Color.rgb(104, 241, 175));

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

        XAxis x = chart.getXAxis();
        x.setEnabled(false);

        YAxis y = chart.getAxisLeft();
        y.setLabelCount(6, false);
        y.setTextColor(Color.WHITE);
        y.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        y.setDrawGridLines(false);
        y.setAxisLineColor(Color.WHITE);

        chart.getAxisRight().setEnabled(false);
    }
}
