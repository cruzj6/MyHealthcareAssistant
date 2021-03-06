package com.cruzj6.mha.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.android.volley.Response;
import com.cruzj6.mha.R;
import com.cruzj6.mha.dataManagement.WalLandingRespContainer;
import com.cruzj6.mha.dataManagement.WalgreensRequestManager;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

/**
 * Class for controlling webview that loads and displays walgreens API functionality
 */
public class RefillWebViewActivity extends AppCompatActivity {

    WebView webview;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    private ProgressDialog myProgressBar;
    final Activity customWebView = this;
    private Bundle intentExtras;
    private android.app.AlertDialog ad;
    private final static String TAG = "RefillWebViewActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refill_web_view);

        //General purpose alert dialog
        ad = new android.app.AlertDialog.Builder(this).create();
        ad.setButton(DialogInterface.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ad.dismiss();
            }
        });

        intentExtras = getIntent().getExtras();
        final String rxNum = intentExtras.getString("num");
        final double lat = intentExtras.getDouble("lat");
        final double lng = intentExtras.getDouble("lng");

        //Check if we didn't get correct bundled components
        if(rxNum == null || lat == 0.0 || lng == 0.0)
        {
            Log.e(TAG, "Requires \"num\" and \"loc\" extras");
            ad.setTitle("Error: no location or num data passed to activity");
            ad.show();
            ad.setButton(DialogInterface.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ad.dismiss();
                    finish();
                }
            });
            ad.show();
        }

        //Show that we are loading the view
        myProgressBar = ProgressDialog.show(RefillWebViewActivity.this, "Walgreens Rx",
                "Please wait...");

        webview = (WebView) findViewById(R.id.webview);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.setWebViewClient(new WebViewController());
        webview.setVisibility(View.VISIBLE);

        webview.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin,
                                                           android.webkit.GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message,
                                     JsResult result) {
                final JsResult finalRes = result;
                if (myProgressBar != null) {
                    myProgressBar.dismiss();
                }
                new AlertDialog.Builder(view.getContext())
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok,
                                new AlertDialog.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        finalRes.confirm();
                                    }
                                }).setCancelable(false).create().show();
                return true;
            }

            @Override
            public void onProgressChanged(WebView view, int progress) {
                customWebView.setTitle("Rx Refill");
                customWebView.setProgress(progress * 100);
                if (progress == 100)
                    customWebView.setTitle(R.string.app_name);
            }
        });


        WalgreensRequestManager.requestLandingURL(this, new Response.Listener<WalLandingRespContainer>() {
            @Override
            public void onResponse(final WalLandingRespContainer response) {

                final JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("affId", getString(R.string.walgreens_affid));
                    jsonObject.put("token", response.getToken());

                    //Sent location
                    jsonObject.put("lat", "" + lat);
                    jsonObject.put("lng", "" + lng);
                    jsonObject.put("devinf", "Android,5.0");
                    jsonObject.put("appver", "1.0");
                    jsonObject.put("act", "chkExpRx");
                    jsonObject.put("appId", "refillByScan");
                    jsonObject.put("rxNo", rxNum);
                    jsonObject.put("appCallBackScheme", "refillbyscan://handleControl");
                    jsonObject.put("appCallBackAction", "callBackAction");
                    jsonObject.put("trackingId",
                            String.valueOf(System.currentTimeMillis() / 1000L));
                    try {
                        //TODO: please be their end...
                        webview.postUrl(response.getUrl(), jsonObject.toString().getBytes("UTF8"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    };
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
       closeWB();
    }

    private void closeWB() {
        finish();
    }

    public class WebViewController extends WebViewClient {

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (myProgressBar.isShowing()) {
                myProgressBar.dismiss();
            }
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            myProgressBar.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode,
                                     KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_SEARCH
                            && event.getRepeatCount() == 0) {
                        return true;
                    }
                    return false;
                }
            });
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            Uri url1 = Uri.parse(url);
            if (url1 != null
                    && url1.getScheme().equalsIgnoreCase("refillbyscan")) {
                String action = url1.getQueryParameter("callBackAction");
                if (//action.equalsIgnoreCase("back")||
                         action.equalsIgnoreCase("fillAnother")
                        || action.equalsIgnoreCase("close")
                        || action.equalsIgnoreCase("cancel")
                        || action.equalsIgnoreCase("txCancel")
                        || action.equalsIgnoreCase("refillTryAgain")
                    ) {

                    closeWB();
                    return false;
                }
                else {
                    view.loadUrl(url);
                    return true;
                }
            } else {
                view.loadUrl(url);
                return true;
            }
        }
    }

}
