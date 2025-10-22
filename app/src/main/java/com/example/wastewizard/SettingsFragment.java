package com.example.wastewizard;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.prefs_settings, rootKey);

        // Username
        EditTextPreference usernamePref = findPreference("pref_username");
        if (usernamePref != null) {
            usernamePref.setText(AppThemeManager.getUsername());
            usernamePref.setSummaryProvider(preference -> AppThemeManager.getUsername());
            usernamePref.setOnPreferenceChangeListener((pref, newValue) -> {
                String name = String.valueOf(newValue);
                AppThemeManager.setUsername(name);
                requireActivity().invalidateOptionsMenu();
                return true;
            });
        }

        // Theme mode
        ListPreference themePref = findPreference("pref_theme");
        if (themePref != null) {
            themePref.setValue(AppThemeManager.getThemeMode());
            themePref.setOnPreferenceChangeListener((preference, newValue) -> {
                AppThemeManager.setThemeMode(String.valueOf(newValue));
                requireActivity().recreate(); // apply
                return true;
            });
        }

        // Dynamic color
        SwitchPreferenceCompat dynamicPref = findPreference("pref_dynamic_color");
        if (dynamicPref != null) {
            boolean canUseDynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S;
            dynamicPref.setEnabled(canUseDynamic);
            dynamicPref.setChecked(canUseDynamic && AppThemeManager.isDynamicEnabled());
            dynamicPref.setOnPreferenceChangeListener((preference, newValue) -> {
                AppThemeManager.setDynamicEnabled((boolean) newValue, requireActivity().getApplication());
                requireActivity().recreate();
                return true;
            });
        }

        // Real-time classification
        SwitchPreferenceCompat realtime = findPreference("pref_realtime");
        if (realtime != null) {
            realtime.setChecked(AppThemeManager.isRealtimeEnabled());
            realtime.setOnPreferenceChangeListener((p, v) -> {
                AppThemeManager.setRealtimeEnabled((Boolean) v);
                return true;
            });
        }

        // Auto-classify
        SwitchPreferenceCompat autoCls = findPreference("pref_auto_classify");
        if (autoCls != null) {
            autoCls.setChecked(AppThemeManager.isAutoClassifyEnabled());
            autoCls.setOnPreferenceChangeListener((p, v) -> {
                AppThemeManager.setAutoClassifyEnabled((Boolean) v);
                return true;
            });
        }

        // Clear history
        Preference clearHistoryPref = findPreference("pref_clear_history");
        if (clearHistoryPref != null) {
            clearHistoryPref.setOnPreferenceClickListener(pref -> {
                new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.clear_history_title)
                    .setMessage(R.string.clear_history_message)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(R.string.clear, (d, w) -> {
                        GameManager gm = new GameManager(requireContext());
                        gm.clearScanHistory();
                        android.widget.Toast.makeText(requireContext(), R.string.history_cleared, android.widget.Toast.LENGTH_SHORT).show();
                    })
                    .show();
                return true;
            });
        }

        // About
        Preference aboutPref = findPreference("pref_about");
        if (aboutPref != null) {
            aboutPref.setOnPreferenceClickListener(p -> {
                requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.fragmentContainer, new AboutFragment())
                    .addToBackStack("about")
                    .commit();
                return true;
            });
        }
    }

    @Override
    public void onViewCreated(@NonNull android.view.View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        androidx.recyclerview.widget.RecyclerView rv = getListView();
        int bg = com.google.android.material.color.MaterialColors.getColor(
                requireContext(), com.google.android.material.R.attr.colorSurface, 0);
        rv.setBackgroundColor(bg);
        view.setBackgroundColor(bg);
        
        // Add proper padding to avoid bottom navigation overlap
        int pad = getResources().getDimensionPixelSize(R.dimen.space_md);
        
        // Calculate bottom navigation height + some extra margin
        int bottomNavHeight = getResources().getDimensionPixelSize(android.R.dimen.app_icon_size) + 
                             getResources().getDimensionPixelSize(android.R.dimen.app_icon_size) + 32; // Icon + text + padding
        int bottomPad = pad + bottomNavHeight;
        
        rv.setPadding(pad, pad, pad, bottomPad);
        rv.setClipToPadding(false);
        
        // Ensure the RecyclerView can scroll to show all content
        rv.setNestedScrollingEnabled(true);
    }
}



