package com.cruzj6.mha.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Response;
import com.cruzj6.mha.R;
import com.cruzj6.mha.dataManagement.WalLandingRespContainer;
import com.cruzj6.mha.dataManagement.WalgreensRequestManager;

/**
 * Used for controlling the activity where user enteres Rx number information for refills
 */
public class RefillRxActivity extends AppCompatActivity {

    private final String TAG = "RefillRxActivity";
    private Button submitWalReqBtn;
    private EditText rxNumEditText;
    private EditText locationEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refill_rx);

        rxNumEditText = (EditText)findViewById(R.id.edittext_rx_num);
        locationEditText = (EditText) findViewById(R.id.edittext_zip_code);

        submitWalReqBtn = (Button) findViewById(R.id.button_submit_wgapi);
        submitWalReqBtn.setOnClickListener(new View.OnClickListener() {
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
                webIntent.putExtra("num", rxNumEditText.getText().toString());
                startActivity(webIntent);
            }
        });
    }


}
