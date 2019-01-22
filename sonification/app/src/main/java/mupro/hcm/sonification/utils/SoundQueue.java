package mupro.hcm.sonification.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.stream.Collectors;

import mupro.hcm.sonification.sensors.Sensor;

public class SoundQueue extends MediaPlayer implements MediaPlayer.OnCompletionListener {

    private final String TAG = "SoundQueue";
    private Context context;
    private LinkedList<Sound> playlist;

    public SoundQueue(Context context) {
        super();
        playlist = new LinkedList<>();
        this.context = context;
        setOnCompletionListener(this);
    }

    /**
     * Plays a single sound file given the fileName.
     *
     * @param sound the sound file name
     */
    public void playSound(final Sound sound) {
        String fileName = "sounds/" + sound.getInstrument().toLowerCase() + "-" + sound.getDirection().getId() + ".mp3";
        if (!isPlaying()) {
            try {
                Log.i(TAG, "Playing: " + fileName);
                AssetFileDescriptor afd = context.getAssets().openFd(fileName);
                setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                prepare();
                setLooping(false);
                start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Queued: " + fileName);
            playlist.add(sound);
        }
    }

    public void playSoundForSensorWithDirection(Sensor sensor, Direction direction) {
        String instrument = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(sensor.getId().toLowerCase() + "_preference", null);
        if (instrument != null) {
            playSound(new Sound(instrument, direction));
        }
    }

    public void playSoundForParticleSensor(Direction direction) {
        String instrument = PreferenceManager.getDefaultSharedPreferences(context)
                .getString("pm_preference", null);
        if (instrument != null) {
            playSound(new Sound(instrument, direction));
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        reset();

        // Play the rest of the sounds currently queued up
        if (playlist.size() > 0) {
            playSound(playlist.removeFirst());
        }
    }
}
