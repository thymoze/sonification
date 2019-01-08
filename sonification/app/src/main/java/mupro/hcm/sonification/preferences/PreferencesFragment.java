package mupro.hcm.sonification.preferences;

import android.os.Bundle;

import java.util.Arrays;

import androidx.preference.MultiSelectListPreference;
import androidx.preference.PreferenceFragmentCompat;
import mupro.hcm.sonification.R;
import mupro.hcm.sonification.sensors.Sensor;

public class PreferencesFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        MultiSelectListPreference chartsPreference = findPreference("sensors_preference");
        chartsPreference.setEntries(Arrays.stream(Sensor.values()).map(s -> s.getLocalizedName(getContext())).toArray(CharSequence[]::new));
        chartsPreference.setEntryValues(Arrays.stream(Sensor.values()).map(Sensor::getId).toArray(CharSequence[]::new));

    }

    @Override
    public void onResume() {
        super.onResume();
        ((PreferencesActivity) getActivity()).setActionBarTitle(getResources().getString(R.string.settings));
    }
}
