package de.uni_augsburg.hcm.androidmusic.csound_generator_activities;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import de.uni_augsburg.hcm.androidmusic.R;
import uni.hcm.livesequencer.composable.CsoundComposable;
import uni.hcm.livesequencer.sequencer.CsoundSequencer;

/**
 * A Csound generator activity represents an activity to display a {@link CsoundComposable}.
 */
public abstract class CsoundGeneratorActivity extends AppCompatActivity {

    private CsoundSequencer sequencer;

    /**
     * Sets up and starts a sequencer.
     *
     * @param csoundComposable the {@link CsoundComposable} that composes the desired sequence
     * @param lengthSeconds    the length of one sequence in seconds
     */
    protected void setUpSequencer(final CsoundComposable csoundComposable, final double lengthSeconds) {
        String opcodeDir = getBaseContext().getApplicationInfo().nativeLibraryDir;
        sequencer = new CsoundSequencer(lengthSeconds, opcodeDir);
        sequencer.addMusicGenerator(csoundComposable);
        sequencer.startSequencer();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        if (sequencer != null) {
            sequencer.stop();
        }
        super.onDestroy();
    }

    /**
     * Copies an Android raw resource file to a {@link File} using an {@code id}.
     *
     * @param id the Android resource id
     * @return the raw resource file
     * @throws IOException if the file cannot be found
     */
    protected File getRawResourceFile(final int id) throws IOException {
        final InputStream inputStream = getResources().openRawResource(id);
        final File returnFile = File.createTempFile("res-load", "");
        FileUtils.copyInputStreamToFile(inputStream, returnFile);
        return returnFile;
    }

    /**
     * Show error if a provided file was not found
     *
     * @param e the thrown exception
     */
    protected void showResourceNotFoundAlert(final IOException e) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.file_not_found);
        dialog.setMessage(e.getMessage());
        dialog.setPositiveButton(R.string.ok, null);
        dialog.create().show();
    }

    /**
     * @return the {@link CsoundSequencer} after running {@link #setUpSequencer(CsoundComposable, double)}. If not set up yet, it returns {@code null}.
     */
    protected CsoundSequencer getSequencer() {
        return sequencer;
    }
}