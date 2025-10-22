package com.example.wastewizard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class AboutFragment extends Fragment {

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_about, container, false);

        ((TextView) v.findViewById(R.id.txtAppName)).setText(getString(R.string.app_name));
        ((TextView) v.findViewById(R.id.txtVersion)).setText("1.0.0");
        ((TextView) v.findViewById(R.id.txtModelVersion)).setText(readModelVersion());

        v.findViewById(R.id.btnLicenses).setOnClickListener(view -> showLicenses());
        return v;
    }

    private String readModelVersion() {
        try {
            // Put a file named model_version.txt in assets (e.g., "1.0.0")
            java.io.InputStream is = requireContext().getAssets().open("model_version.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String ver = br.readLine();
            br.close();
            return ver != null ? ver.trim() : "unknown";
        } catch (Exception e) {
            return "unknown";
        }
    }

    private void showLicenses() {
        View content = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_licenses, null, false);
        WebView web = content.findViewById(R.id.webLicenses);
        web.loadUrl("file:///android_res/raw/licenses.html");

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.open_source_licenses)
                .setView(content)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}
