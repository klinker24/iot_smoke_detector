package edu.uiowa.engineering.iot_smoke.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import edu.uiowa.engineering.iot_smoke.Manifest;
import edu.uiowa.engineering.iot_smoke.R;
import edu.uiowa.engineering.iot_smoke.registration.Registration;
import edu.uiowa.engineering.iot_smoke.util.BaseUtils;
import edu.uiowa.engineering.iot_smoke.util.RegistrationUtils;

public abstract class GCMRegisterActivity extends AppCompatActivity {

    private static final String PREF_ACCOUNT_NAME = "pref_account_name";
    private static final int REQUEST_ACCOUNT_PICKER = 2;
    private static final int REQUEST_PERMISSIONS = 3;

    SharedPreferences settings;
    GoogleAccountCredential credential;
    Registration service;
    String accountName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = BaseUtils.getSharedPreferences(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{android.Manifest.permission.READ_CONTACTS},
                    REQUEST_PERMISSIONS
            );
        } else {
            register();
        }
    }

    private void register() {
        setService();
        if (credential.getSelectedAccountName() != null) {
            registerWithBackend();
        } else {
            chooseAccount();
        }
    }

    private void setService() {
        credential = GoogleAccountCredential.usingAudience(this, "server:client_id:" + BaseUtils.WEB_CLIENT_ID);
        setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));

        Registration.Builder builder = new Registration.Builder(
                AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(),
                credential)
                .setApplicationName("uiowa-iot-smoke");
        service = builder.build();
    }

    private void registerWithBackend() {
        if (BaseUtils.checkPlayServices(this) && BaseUtils.hasRegistered(this)) {
            String regid = BaseUtils.getRegistrationId(this);

            if (regid.isEmpty()) {
                RegistrationUtils.registerInBackground(this, service);
            }
        } else if (!BaseUtils.hasRegistered(this)) {
            RegistrationUtils.registerInBackground(this, service);
        }
    }

    private void setSelectedAccountName(String accountName) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PREF_ACCOUNT_NAME, accountName);
        editor.commit();
        credential.setSelectedAccountName(accountName);
        this.accountName = accountName;
    }

    private void chooseAccount() {
        startActivityForResult(credential.newChooseAccountIntent(),
                REQUEST_ACCOUNT_PICKER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ACCOUNT_PICKER:
                if (data != null && data.getExtras() != null) {
                    String accountName =
                            data.getExtras().getString(
                                    AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        setSelectedAccountName(accountName);
                        setService();
                        registerWithBackend();
                    }
                }
                break;
            case REQUEST_PERMISSIONS:

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(this, MainActivity.class);

                    finish();
                    startActivity(intent);

                    overridePendingTransition(0,0);
                }
                break;
        }
    }
}