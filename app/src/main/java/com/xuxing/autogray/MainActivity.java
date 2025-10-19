package com.xuxing.autogray;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "GrayscaleControllerPrefs";
    private static final String KEY_PACKAGE_LIST = "package_list";

    private TextView statusText;
    private EditText packageInput;
    private Button addButton;
    private Button openAccessibilityButton;
    private RecyclerView packageListView;
    private PackageListAdapter adapter;

    private SharedPreferences prefs;
    private Set<String> packageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        packageList = new HashSet<>(prefs.getStringSet(KEY_PACKAGE_LIST, new HashSet<>()));

        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermissions();
    }

    private void initViews() {
        statusText = findViewById(R.id.statusText);
        packageInput = findViewById(R.id.packageInput);
        addButton = findViewById(R.id.addButton);
        openAccessibilityButton = findViewById(R.id.openAccessibilityButton);
        packageListView = findViewById(R.id.packageListView);

        adapter = new PackageListAdapter(packageList, this::removePackage);
        packageListView.setLayoutManager(new LinearLayoutManager(this));
        packageListView.setAdapter(adapter);

        addButton.setOnClickListener(v -> addPackage());
        openAccessibilityButton.setOnClickListener(v -> openAccessibilitySettings());
    }

    private void checkPermissions() {
        boolean hasAccessibility = isAccessibilityServiceEnabled();
        boolean hasSecureSettings = hasSecureSettingsPermission();

        StringBuilder status = new StringBuilder("Permission Status:\n");
        status.append("Accessibility Service: ").append(hasAccessibility ? "✓ Enabled" : "✗ Disabled").append("\n");
        status.append("Secure Settings: ").append(hasSecureSettings ? "✓ Granted" : "✗ Not Granted").append("\n");

        if (!hasAccessibility) {
            status.append("\nClick the button below to enable accessibility service");
            openAccessibilityButton.setVisibility(View.VISIBLE);
        } else {
            openAccessibilityButton.setVisibility(View.GONE);
        }

        if (!hasSecureSettings) {
            status.append("\n\nTo grant Secure Settings permission, run:\n");
            status.append("adb shell pm grant com.example.grayscalecontroller android.permission.WRITE_SECURE_SETTINGS");
        }

        statusText.setText(status.toString());
    }

    private boolean isAccessibilityServiceEnabled() {
        String service = getPackageName() + "/" + GrayscaleAccessibilityService.class.getCanonicalName();
        try {
            String enabledServices = Settings.Secure.getString(
                    getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            );
            return enabledServices != null && enabledServices.contains(service);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean hasSecureSettingsPermission() {
        return checkSelfPermission("android.permission.WRITE_SECURE_SETTINGS")
                == android.content.pm.PackageManager.PERMISSION_GRANTED;
    }

    private void openAccessibilitySettings() {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivity(intent);
        Toast.makeText(this, "Please enable Grayscale Controller in the list", Toast.LENGTH_LONG).show();
    }

    private void addPackage() {
        String packageName = packageInput.getText().toString().trim();
        if (!packageName.isEmpty()) {
            packageList.add(packageName);
            savePackageList();
            adapter.updateList(packageList);
            packageInput.setText("");
            Toast.makeText(this, "Added: " + packageName, Toast.LENGTH_SHORT).show();
        }
    }

    private void removePackage(String packageName) {
        packageList.remove(packageName);
        savePackageList();
        adapter.updateList(packageList);
        Toast.makeText(this, "Removed: " + packageName, Toast.LENGTH_SHORT).show();
    }

    private void savePackageList() {
        prefs.edit().putStringSet(KEY_PACKAGE_LIST, packageList).apply();
    }
}
