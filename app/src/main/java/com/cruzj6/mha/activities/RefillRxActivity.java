package com.cruzj6.mha.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.android.volley.Response;
import com.cruzj6.mha.R;
import com.cruzj6.mha.dataManagement.WalLandingRespContainer;

public class RefillRxActivity extends AppCompatActivity {

    private static final String TAG = "RefillRxActivity";
    private static Button testWalReqBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refill_rx);

        testWalReqBtn = (Button) findViewById(R.id.button_test_wgapi);

        testWalReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Response.Listener<WalLandingRespContainer> listener = new Response.Listener<WalLandingRespContainer>() {
                    @Override
                    public void onResponse(WalLandingRespContainer response) {
                        Log.v(TAG, response.getTemplate() + "|" + response.getToken() +
                                "|" + response.getUpLimit() + "|" +  response.getUrl());
                    }
                };

                //WalgreensRequestManager.requestLandingURL(RefillRxActivity.this, listener);

                Intent webIntent = new Intent(RefillRxActivity.this, RefillWebViewActivity.class);
                startActivity(webIntent);
            }
        });
    }


}
