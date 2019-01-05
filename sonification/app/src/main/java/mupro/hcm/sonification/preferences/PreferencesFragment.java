package mupro.hcm.sonification.preferences;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;
import mupro.hcm.sonification.R;

public class PreferencesFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
}
