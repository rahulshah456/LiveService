package com.example.livewallpaper.resource_wall;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import com.example.livewallpaper.R;

public class ResourceSettings extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.resource_wallpaper_settings);
    }
}
