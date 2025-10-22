package com.xuxing.autogray;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import java.util.HashSet;
import java.util.Set;

public class GrayscaleAccessibilityService extends AccessibilityService {
    private static final String TAG = "GrayscaleAccessibility";
    private static final String PREFS_NAME = "GrayscaleControllerPrefs";
    private static final String KEY_PACKAGE_LIST = "package_list";

    private static final String DISPLAY_DALTONIZER_ENABLED = "accessibility_display_daltonizer_enabled";
    private static final String DISPLAY_DALTONIZER = "accessibility_display_daltonizer";

    // Add this as a class member
    private static final Set<String> IGNORED_PACKAGES = new HashSet<>();

    // Initialize in onCreate() or static block
    static {
        IGNORED_PACKAGES.add("com.android.systemui");
        IGNORED_PACKAGES.add("miui.systemui.plugin");
        IGNORED_PACKAGES.add("com.sohu.inputmethod.sogou.xiaomi");
        IGNORED_PACKAGES.add("li.songe.gkd");
        IGNORED_PACKAGES.add("com.miui.personalassistant");
    }

    private static GrayscaleAccessibilityService instance;

    private Set<String> monitoredPackages;
    private String currentPackage = "";
    private long lastEventTime = 0;
    private boolean isGrayscaleActive = false;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        loadMonitoredPackages();
        Log.d(TAG, "Accessibility service created");
    }

    @Override
    public void onDestroy() {
        instance = null;
        setGrayscale(false); // Reset to color when service stops
        super.onDestroy();
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.notificationTimeout = 100;
        setServiceInfo(info);

        Log.d(TAG, "Accessibility service connected");
        loadMonitoredPackages();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (event.getPackageName() != null) {
                String className = event.getClassName().toString();
                String packageName = event.getPackageName().toString();
                long eventTime = event.getEventTime();
                Log.d(TAG, "Receive event: " + packageName + "/" + className + ";" + eventTime);


                // Ignore system UI and our own app
                if (IGNORED_PACKAGES.contains(packageName) || packageName.equals(getPackageName())) {
                    return;
                }

                // When switch back from recent tasks, there have a wrong event
                // 'window change to Launcher' may be receive.
                // We filter out here if delay < 1s and package name is Launcher :(
                if((eventTime - lastEventTime) < 1000 && packageName.equals("com.miui.home")){
                    Log.d(TAG, "Filter out event: " + packageName + "/" + className + ";" + eventTime);
                    return;
                }

                if (!packageName.equals(currentPackage)) {
                    currentPackage = packageName;
                    lastEventTime = eventTime;
                    Log.d(TAG, "Window changed to: " + currentPackage + "/" + className);

                    // Reload packages in case they were updated
                    // loadMonitoredPackages();

                    boolean shouldBeGrayscale = monitoredPackages.contains(currentPackage);

                    if (shouldBeGrayscale != isGrayscaleActive) {
                        setGrayscale(shouldBeGrayscale);
                        isGrayscaleActive = shouldBeGrayscale;
                        Log.d(TAG, "Grayscale " + (shouldBeGrayscale ? "enabled" : "disabled") +
                                " for " + currentPackage);
                    }
                }
            } else {
                Log.w(TAG, "AccessibilityEvent packagename is null!");
            }
        }
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "Accessibility service interrupted");
    }

    private void loadMonitoredPackages() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        monitoredPackages = new HashSet<>(prefs.getStringSet(KEY_PACKAGE_LIST, new HashSet<>()));
        Log.d(TAG, "Loaded packages: " + monitoredPackages);
    }

    private void setGrayscale(boolean grayscale) {
        try {
            ContentResolver contentResolver = getContentResolver();
            Secure.putInt(contentResolver, DISPLAY_DALTONIZER_ENABLED, grayscale ? 1 : 0);
            Secure.putInt(contentResolver, DISPLAY_DALTONIZER, grayscale ? 0 : -1);
        } catch (SecurityException e) {
            Log.e(TAG, "Failed to set grayscale. Missing WRITE_SECURE_SETTINGS permission", e);
        }
    }

    public static boolean isRunning() {
        return instance != null;
    }

    public void reloadPackages() {
        loadMonitoredPackages();
    }
}
