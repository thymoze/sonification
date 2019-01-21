package mupro.hcm.sonification.preferences;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import mupro.hcm.sonification.R;
import mupro.hcm.sonification.sound.Direction;
import mupro.hcm.sonification.sound.Sound;
import mupro.hcm.sonification.sound.SoundQueue;

public class SoundsPreferencesFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_sounds, rootKey);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((PreferencesActivity) getActivity()).setActionBarTitle(getResources().getString(R.string.title_sounds));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        ListPreference preference = (ListPreference) findPreference(key);
        if (preference != null) {
            SoundQueue queue = new SoundQueue(getContext());
            queue.playSound(new Sound(preference.getValue(), Direction.UP));
            queue.playSound(new Sound(preference.getValue(), Direction.DOWN));
        }
    }
}