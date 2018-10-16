package de.uni_augsburg.hcm.androidmusic.csound_generator_activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.RatingBar;

import java.io.File;
import java.io.IOException;

import de.uni_augsburg.hcm.androidmusic.R;
import uni.hcm.music_ga.GaCsoundGenerator;
import uni.hcm.music_ga.SequenceFinishedListener;

/**
 * The activity to display a {@link GaCsoundGenerator}.
 */
public class GaGeneratorActivity extends CsoundGeneratorActivity implements SequenceFinishedListener {

    private static final float NEUTRAL_RATING = 2.5f;
    private RatingBar ratingBar;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_gagenerator);

        ratingBar = findViewById(R.id.ratingBar);
        ratingBar.setRating(NEUTRAL_RATING);

        try {
            final File soundFontFile = getRawResourceFile(R.raw.sf_gmbank);
            final File gaconfig = getRawResourceFile(R.raw.music);
            setUpSequencer(new GaCsoundGenerator(soundFontFile, this, gaconfig), 5);
        } catch (IOException e) {
            showResourceNotFoundAlert(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double onSequenceFinished() {
        final double returnRating = ratingBar.getRating();
        ratingBar.setRating(NEUTRAL_RATING);
        return returnRating;
    }
}
