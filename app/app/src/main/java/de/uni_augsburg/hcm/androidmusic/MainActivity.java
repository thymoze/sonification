package de.uni_augsburg.hcm.androidmusic;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import de.uni_augsburg.hcm.androidmusic.csound_generator_activities.GaGeneratorActivity;
import de.uni_augsburg.hcm.androidmusic.csound_generator_activities.HarmonixActivity;
import de.uni_augsburg.hcm.androidmusic.csound_generator_activities.MidiImportActivity;
import de.uni_augsburg.hcm.androidmusic.csound_generator_activities.OscListenerActivity;

/**
 * Activity to list all implemented Csound generators.
 */
public class MainActivity extends AppCompatActivity {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String[] testNames = new String[]{getString(R.string.harmonix_name), getString(R.string.osc_listener_name), getString(R.string.midi_import_name), "GA Generator"};
        final Class[] activities = new Class[]{HarmonixActivity.class, OscListenerActivity.class, MidiImportActivity.class, GaGeneratorActivity.class};


        setContentView(R.layout.activity_main);
        final ListView listView = findViewById(R.id.list_view);

        listView.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                testNames));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(final AdapterView<?> arg0, final View arg1,
                                    final int position, final long arg3) {
                startActivity(new Intent(MainActivity.this,
                        activities[position]));
            }
        });
    }
}