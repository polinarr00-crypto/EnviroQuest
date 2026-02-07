package com.example.enviroquest;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        SwitchMaterial switchDarkMode = findViewById(R.id.switch_dark_mode);
        findViewById(R.id.btn_back_settings).setOnClickListener(v -> finish());

        SharedPreferences sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean("DarkMode", false);
        switchDarkMode.setChecked(isDarkMode);

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                sharedPreferences.edit().putBoolean("DarkMode", true).apply();
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                sharedPreferences.edit().putBoolean("DarkMode", false).apply();
            }
        });
    }
}