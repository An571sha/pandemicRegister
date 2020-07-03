package com.animesh.notfallapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Configuration;
import android.os.Bundle;

import com.animesh.notfallapp.R;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    public static final String KEY_PREF_LANGUAGE = "pref_language";
    public static final String KEY_PREF_UI = "pref_ui";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Locale localeEN = new Locale("en");
        setLocale(localeEN);

        Locale localeDE = new Locale("de");
        setLocale(localeDE);

        Locale localeHI = new Locale("hi");
        setLocale(localeHI);
    }

    public static void setLocale(Locale locale) {
        Locale.setDefault(locale);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


}
