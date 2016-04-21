package edu.uiowa.engineering.iot_smoke.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.EditText;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.util.Base64;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.List;

import javax.crypto.Cipher;

import edu.uiowa.engineering.iot_smoke.R;
import edu.uiowa.engineering.iot_smoke.registration.Registration;
import edu.uiowa.engineering.iot_smoke.registration.model.AccountRecord;
import edu.uiowa.engineering.iot_smoke.registration.model.CollectionResponseAccountRecord;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegistrationUtils extends BaseUtils {

    public static void registerInBackground(final Context context, final Registration registration) {
        new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] params) {

                GoogleCloudMessaging gcm = getGCM(context);

                try {
                    final AccountRecord account = registration
                            .register("Android", gcm.register(SENDER_ID))
                            .execute()
                            .getItems()
                            .get(0);

                    SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

                    sharedPrefs.edit()
                            .putString("pref_auth_token", account.getAuthToken())
                            .commit();

                    log(account.getAuthToken());

                    boolean registeredWithPi = sharedPrefs.getBoolean("registered_with_pi", false);
                    if (registeredWithPi) {
                        return null;
                    }

                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final EditText editText = new EditText(context);

                            new AlertDialog.Builder(context)
                                    .setTitle("Enter Raspberry Pi IP Address")
                                    .setView(editText)
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        sendAuthToRaspberryPi(
                                                                editText.getText().toString(),
                                                                account.getAuthToken()
                                                        );

                                                        PreferenceManager.getDefaultSharedPreferences(context).edit()
                                                                .putBoolean("registered_with_pi", true)
                                                                .commit();
                                                    } catch (Exception e) {
                                                        logError(e);
                                                    }
                                                }
                                            }).start();
                                        }
                                    })
                                    .show();
                        }
                    });
                } catch (Exception e) {
                    logError(e);
                }

                return null;
            }
        }.execute(null, null, null);
    }

    private static void sendAuthToRaspberryPi(String piIpAddress, String authToken) throws Exception {
        String url = "http://" + piIpAddress + ":8889/auth";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .method("GET", null)
                .build();
        Response response = client.newCall(request).execute();

        String publicKey = response.body().string();
        publicKey = publicKey.replaceAll("(-+BEGIN PUBLIC KEY-+\\r?\\n|-+END PUBLIC KEY-+\\r?\\n?)", "");
        byte[] keyBytes = Base64.decodeBase64(publicKey);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PublicKey pk = kf.generatePublic(spec);

        byte[] cipherText;
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, pk);
        cipherText = cipher.doFinal(authToken.getBytes());
        String encodedText = Base64.encodeBase64String(cipherText);

        url = url + "?message=" + encodedText;

        request = new Request.Builder()
                .url(url)
                .method("POST", RequestBody.create(MediaType.parse("application/json"),
                        "{\"message\":\"" + encodedText + "\"}"))
                .header("Content-Length", "0")
                .build();
        client.newCall(request).execute();
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
