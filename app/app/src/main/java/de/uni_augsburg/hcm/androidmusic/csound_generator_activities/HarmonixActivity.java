package de.uni_augsburg.hcm.androidmusic.csound_generator_activities;

import android.os.Bundle;
import android.support.annotation.Nullable;

import java.io.IOException;

import de.uni_augsburg.hcm.androidmusic.R;
import uni.hcm.music_ga.HarmonixCsoundGenerator;

/**
 * The activity to display a {@link HarmonixCsoundGenerator}.
 */
public class HarmonixActivity extends CsoundGeneratorActivity {

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_harmonix);

        try {
            setUpSequencer(new HarmonixCsoundGenerator(getRawResourceFile(R.raw.sf_gmbank)), 20);
        } catch (final IOException e) {
            showResourceNotFoundAlert(e);
        }
    }
}