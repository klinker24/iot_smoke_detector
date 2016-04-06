package edu.uiowa.engineering.iot_smoke.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import java.io.IOException;

import edu.uiowa.engineering.iot_smoke.registration.Registration;

public class RegistrationUtils extends BaseUtils {

    public static void registerInBackground(final Context context, final Registration registration) {
        new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] params) {

                GoogleCloudMessaging gcm = getGCM(context);

                try {
                    registration.register(gcm.register(SENDER_ID)).execute();
                } catch (IOException e) {
                    logError(e);
                }

                return null;
            }
        }.execute(null, null, null);
    }

    public static void unregisterInBackground(final Context context, final Registration registration) {

        new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] params) {

                try {
                    registration.unregister().execute();
                } catch (IOException e) {
                    logError(e);
                }

                return null;
            }
        }.execute(null, null, null);
    }

    public static void storeRegistrationId(Activity a, String regId) {
        final SharedPreferences prefs = getSharedPreferences(a);
        int appVersion = getAppVersion(a);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        Log.i(TAG, "regId: " + regId);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    public static void removeRegistrationId(Activity a) {
        final SharedPreferences prefs = getSharedPreferences(a);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(PROPERTY_REG_ID);
        editor.remove(PROPERTY_APP_VERSION);
        editor.commit();
    }

    public static void removeRegistrationSharedPrefs(Context c) {
        SharedPreferences sp = BaseUtils.getSharedPreferences(c);
        SharedPreferences.Editor e = sp.edit();

        e.putBoolean("registered_device", false);
        e.putString("name", "");
        e.putString("device", "");

        e.commit();
    }
}
