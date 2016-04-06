package edu.uiowa.engineering.iot_smoke.activity;

import android.os.Bundle;

import edu.uiowa.engineering.iot_smoke.R;

public class MainActivity extends GCMRegisterActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
