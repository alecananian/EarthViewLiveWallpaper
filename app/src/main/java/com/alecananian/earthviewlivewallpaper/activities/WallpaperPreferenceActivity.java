package com.alecananian.earthviewlivewallpaper.activities;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.alecananian.earthviewlivewallpaper.R;
import com.alecananian.earthviewlivewallpaper.events.WallpaperEvent;
import com.alecananian.earthviewlivewallpaper.events.WallpaperEventType;
import com.alecananian.earthviewlivewallpaper.models.Wallpaper;

import de.greenrobot.event.EventBus;

public class WallpaperPreferenceActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle wallpaperBundle = new Bundle();
        wallpaperBundle.putParcelable("wallpaper", getIntent().getParcelableExtra("wallpaper"));

        WallpaperPreferenceFragment preferenceFragment = new WallpaperPreferenceFragment();
        preferenceFragment.setArguments(wallpaperBundle);

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(android.R.id.content, preferenceFragment);
        fragmentTransaction.commit();
    }

    public static class WallpaperPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences);

            Bundle wallpaperBundle = getArguments();
            if (wallpaperBundle != null) {
                final Wallpaper wallpaper = wallpaperBundle.getParcelable("wallpaper");
                if (wallpaper != null) {
                    // Set address and listener for launch maps button
                    Preference launchMapsPreference = findPreference(getString(R.string.settings_key_launch_maps));
                    launchMapsPreference.setSummary(wallpaper.getLocationString());
                    launchMapsPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            EventBus.getDefault().post(new WallpaperEvent(WallpaperEventType.LAUNCH_MAPS));
                            return true;
                        }
                    });

                    // Set listener for fetch new wallpaper button
                    Preference fetchWallpaperPreference = findPreference(getString(R.string.settings_key_fetch_new));
                    fetchWallpaperPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            EventBus.getDefault().post(new WallpaperEvent(WallpaperEventType.FETCH_NEW));
                            getActivity().onBackPressed();
                            return true;
                        }
                    });

                    // Set attribution
                    findPreference(getString(R.string.settings_key_attribution)).setTitle(wallpaper.attribution);
                } else {
                    // Hide the current wallpaper section
                    getPreferenceScreen().removePreference(findPreference(getString(R.string.settings_key_current_wallpaper)));
                }
            }
        }
    }

}