package de.uni_augsburg.hcm.androidmusic.csound_generator_activities;

import android.os.Bundle;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.IOException;

import de.uni_augsburg.hcm.androidmusic.R;
import uni.hcm.music_ga.MidiCsoundGenerator;

/**
 * The activity to display a {@link MidiCsoundGenerator}.
 */
public class MidiImportActivity extends CsoundGeneratorActivity {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_midi_file_example);

        try {
            final File midiFile = getRawResourceFile(R.raw.pastoral);
            setUpSequencer(new MidiCsoundGenerator(midiFile, getRawResourceFile(R.raw.sf_gmbank)), 200);
        } catch (final IOException e) {
            showResourceNotFoundAlert(e);
        }
    }
}
