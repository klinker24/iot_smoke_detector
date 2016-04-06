package edu.uiowa.engineering.iot_smoke.util;


import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

public class Reporting {
    private static String TAG = "IoTSmokeError";

    public static void log(String message) {
        Log.v(TAG, message);
    }

    public static void logError(Exception e) {
        e.printStackTrace();
    }
}
