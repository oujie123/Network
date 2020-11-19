package com.gacrnd.gcs.networkmodule;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.gacrnd.gcs.network.TencentNetworkApi;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        TencentNetworkApi.getInstance().getService()
    }
}
