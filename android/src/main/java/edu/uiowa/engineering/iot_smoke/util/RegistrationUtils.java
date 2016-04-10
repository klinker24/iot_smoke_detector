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
import java.util.List;

import edu.uiowa.engineering.iot_smoke.registration.Registration;
import edu.uiowa.engineering.iot_smoke.registration.model.AccountRecord;
import edu.uiowa.engineering.iot_smoke.registration.model.CollectionResponseAccountRecord;

public class RegistrationUtils extends BaseUtils {

    public static void registerInBackground(final Context context, final Registration registration) {
        new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] params) {

                GoogleCloudMessaging gcm = getGCM(context);

                try {
                    AccountRecord account = registration
                            .register("Android", gcm.register(SENDER_ID))
                            .execute()
                            .getItems()
                            .get(0);

                    // todo: do something with this account, that contains the auth token
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
}
