package mupro.hcm.sonification.preferences;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import butterknife.ButterKnife;
import mupro.hcm.sonification.R;

public class AboutPreferenceFragment extends Fragment {
    public AboutPreferenceFragment() {
    }

    public static AboutPreferenceFragment newInstance() {
        return new AboutPreferenceFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about_preference, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((PreferencesActivity) getActivity()).setActionBarTitle(getResources().getString(R.string.title_about));
    }
}
