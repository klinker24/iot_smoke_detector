package edu.uiowa.engineering.iot_smoke.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.Preconditions;

import java.io.IOException;

public class IotAccountCredential extends GoogleAccountCredential {

    public IotAccountCredential(Context context, String scope) {
        super(context, scope);
    }

    public static GoogleAccountCredential usingAudience(Context context, String audience) {
        Preconditions.checkArgument(audience.length() != 0);
        return new GoogleAccountCredential(context, "audience:" + audience);
    }

    @Override
    public String getToken() throws IOException, GoogleAuthException {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        return "";
    }

}
