package edu.uiowa.engineering.iot_smoke.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.telephony.SmsManager;
import android.text.Html;
import android.util.Log;

import edu.uiowa.engineering.iot_smoke.R;
import edu.uiowa.engineering.iot_smoke.activity.MainActivity;
import edu.uiowa.engineering.iot_smoke.receiver.PushNotificationReceiver;


public class PushNotificationService extends IntentService {

    private static final String TWITTER_INTERACTIONS_GROUP = "twitter_interactions_group";
    private static final String TWITTER_QUOTE_GROUP = "twitter_quotes_group";

    public PushNotificationService() {
        super("PushNotificationService");
    }

    private SharedPreferences sharedPreferences;
    int currentId = 0;

    @Override
    protected void onHandleIntent(Intent intent) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        currentId = sharedPreferences.getInt("current_noti_id", 0);

        try {
            Bundle extras = intent.getExtras();

            String message = extras.getString("message");
            message = java.net.URLDecoder.decode(message, "UTF-8");

            Intent main = new Intent(this, MainActivity.class);
            PendingIntent pi = PendingIntent.getActivity(this, getPiRequestCode(), main, 0);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                    .setContentIntent(pi)
                    .setContentTitle("Smoke Alert")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setColor(0x607d8b) // blue grey color
                    .setContentText(message)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message));

            NotificationManagerCompat notificationManager =
                    NotificationManagerCompat.from(this);

            notificationManager.notify(currentId, mBuilder.build());

        } catch (Exception e) {
            e.printStackTrace();
        }

        sharedPreferences.edit().putInt("current_noti_id", currentId + 1).commit();

        PushNotificationReceiver.completeWakefulIntent(intent);
    }

    private int getPiRequestCode() {
        int code = sharedPreferences.getInt("pi_request_code", 1);
        sharedPreferences.edit().putInt("pi_request_code", code < 100 ? code + 1 : 1).commit();
        return code;
    }
}