package mupro.hcm.sonification.preferences;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;
import mupro.hcm.sonification.R;

public class SonificationPreferencesFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_sonification, rootKey);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((PreferencesActivity) getActivity()).setActionBarTitle(getResources().getString(R.string.app_name));
    }
}
