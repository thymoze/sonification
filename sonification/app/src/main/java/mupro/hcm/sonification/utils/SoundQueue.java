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

public class SoundQueue implements MediaPlayer.OnCompletionListener {

    private final String TAG = "SoundQueue";
    private Context context;
    private LinkedList<Sound> playlist;
    private boolean playing = false;

    public SoundQueue(Context context) {
        super();
        playlist = new LinkedList<>();
        this.context = context;
    }

    /**
     * Plays a single sound file given the fileName.
     *
     * @param sound the sound file name
     */
    public void playSound(final Sound sound) {
        String fileName = "sounds/" + sound.getInstrument().toLowerCase() + "-" + sound.getDirection().getId() + ".mp3";
        if (!playing) {
            playing = true;
            new Thread(() -> {
                try {
                    Log.i(TAG, "Playing: " + fileName);
                    MediaPlayer player = new MediaPlayer();
                    player.setOnCompletionListener(this);
                    AssetFileDescriptor afd = context.getAssets().openFd(fileName);
                    player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                    player.prepare();
                    player.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
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
        playing = false;
        mp.stop();
        mp.release();

        // Play the rest of the sounds currently queued up
        if (playlist.size() > 0) {
            playSound(playlist.removeFirst());
        }
    }
}
