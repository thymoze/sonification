package de.uni_augsburg.hcm.androidmusic.csound_generator_activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.SeekBar;

import com.csounds.bindings.ui.CsoundUI;

import de.uni_augsburg.hcm.androidmusic.R;
import uni.hcm.music_ga.OscCsoundGenerator;

/**
 * The activity to display a {@link OscCsoundGenerator}.
 */
public class OscListenerActivity extends CsoundGeneratorActivity {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_osclistener_example);

        final SeekBar amplitudeSeekBar = findViewById(R.id.amplitudeSeekBar);

        setUpSequencer(new OscCsoundGenerator(), 100);

        final CsoundUI csoundUI = new CsoundUI(getSequencer().getCsoundObj());
        csoundUI.addSlider(amplitudeSeekBar, "amplitude", 0d, 10000d);
    }
}