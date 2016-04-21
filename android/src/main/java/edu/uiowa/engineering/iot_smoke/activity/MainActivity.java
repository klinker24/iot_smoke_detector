package edu.uiowa.engineering.iot_smoke.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import java.util.List;

import edu.uiowa.engineering.iot_smoke.R;
import edu.uiowa.engineering.iot_smoke.adapter.RecordAdapter;
import edu.uiowa.engineering.iot_smoke.airQuality.AirQuality;
import edu.uiowa.engineering.iot_smoke.airQuality.model.AirQualityRecord;
import edu.uiowa.engineering.iot_smoke.registration.Registration;
import edu.uiowa.engineering.iot_smoke.util.BaseUtils;

import static edu.uiowa.engineering.iot_smoke.util.Reporting.logError;

public class MainActivity extends GCMRegisterActivity {

    private RecyclerView recyclerView;
    private List<AirQualityRecord> records;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        getData();
    }

    private void getData() {
        final Handler handler = new Handler();

        new Thread(new Runnable() {
            @Override
            public void run() {
                GoogleAccountCredential credential = GoogleAccountCredential
                        .usingAudience(MainActivity.this, "server:client_id:" + BaseUtils.WEB_CLIENT_ID)
                        .setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));

                AirQuality airQuality = new AirQuality.Builder(
                        AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(),
                        credential)
                        .setApplicationName("uiowa-iot-smoke")
                        .build();

                try {
                    records = airQuality.listReadings().execute().getItems();

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            setAdapter();
                        }
                    });
                } catch (Exception e) {
                    logError(e);
                }
            }
        }).start();
    }

    private void setAdapter() {
        recyclerView.setAdapter(new RecordAdapter(records, this));
    }

}
