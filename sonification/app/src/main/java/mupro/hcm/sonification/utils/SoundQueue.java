package mupro.hcm.sonification.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;
import java.util.LinkedList;

public class SoundQueue implements MediaPlayer.OnCompletionListener {

    private final String TAG = "SoundQueue";
    private Context context;
    private LinkedList<String> playlist;
    private boolean playing = false;

    public SoundQueue(Context context) {
        super();
        playlist = new LinkedList<>();
        this.context = context;
    }

    /**
     * Plays a single sound file given the fileName.
     * @param fileName the sound file name
     */
    public void playSound(final String fileName) {
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
            playlist.add(fileName);
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
