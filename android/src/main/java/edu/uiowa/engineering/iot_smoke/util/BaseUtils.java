package edu.uiowa.engineering.iot_smoke.util;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class BaseUtils extends Reporting {

    protected static final String TAG = "IoTSmokeUtils";

    public static final String PROPERTY_REG_ID = "registration_id";
    public static final String PROPERTY_APP_VERSION = "appVersion";
    protected final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    public static final String WEB_CLIENT_ID = "846797458679-hh9rprrj2gocpo4sbn9sd8iskro6jcj3.apps.googleusercontent.com";
    public static final String ANDROID_CLIENT_ID = "846797458679-v5iff1k3i0oaosoln0pobb77mks75nt8.apps.googleusercontent.com";
    protected static final String SENDER_ID = "846797458679";

    public static GoogleCloudMessaging getGCM(Context c) {
        return GoogleCloudMessaging.getInstance(c);
    }

    public static boolean hasRegistered(Activity a) {
        SharedPreferences sp = getSharedPreferences(a);
        return sp.getBoolean("registered_device", false);
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    public static boolean checkPlayServices(Activity a) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(a);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, a,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            return false;
        }
        return true;
    }

    public static String getRegistrationId(Context context) {
        final SharedPreferences prefs = getSharedPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }

        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    public static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    protected static int getAppVersion(Context a) {
        try {
            PackageInfo packageInfo = a.getPackageManager()
                    .getPackageInfo(a.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

}
