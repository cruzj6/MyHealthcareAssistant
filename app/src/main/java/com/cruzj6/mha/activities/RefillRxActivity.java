package com.cruzj6.mha.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.volley.Response;
import com.cruzj6.mha.R;
import com.cruzj6.mha.dataManagement.WalLandingRespContainer;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Used for controlling the activity where user enters Rx number information for refills
 */
public class RefillRxActivity extends AppCompatActivity {

    private final static int GPS_TIMEOUT_MS = 10000;
    private final static int GPS_REQ_CODE = 3;
    private final static int CAM_REQ_CODE = 4;
    private WalgreensLocationListener locListener;
    private final String TAG = "RefillRxActivity";
    private Button submitWalReqBtn;
    private EditText rxNumEditText;
    private EditText locationEditText;
    private ProgressDialog pd;
    private RadioGroup rgLoc;
    private Timer timerGetLocation;
    private Button scanRxButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refill_rx);

        //gets refs to controls
        rxNumEditText = (EditText) findViewById(R.id.edittext_rx_num);
        locationEditText = (EditText) findViewById(R.id.edittext_zip_code);
        rgLoc = (RadioGroup) findViewById(R.id.radiogroup_location);
        submitWalReqBtn = (Button) findViewById(R.id.button_submit_wgapi);
        scanRxButton = (Button) findViewById(R.id.button_scan_rx);

        //Sets up button for scanning Rx barcode
        scanRxButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int permissionCheck = ContextCompat.checkSelfPermission(RefillRxActivity.this,
                        Manifest.permission.CAMERA);

                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(RefillRxActivity.this,
                            Manifest.permission.CAMERA)) {

                        // No explanation needed, we can request the permission.
                        ActivityCompat.requestPermissions(RefillRxActivity.this,
                                new String[]{Manifest.permission.CAMERA},
                                CAM_REQ_CODE);

                    } else {

                        // No explanation needed, we can request the permission.
                        ActivityCompat.requestPermissions(RefillRxActivity.this,
                                new String[]{Manifest.permission.CAMERA},
                                CAM_REQ_CODE);
                    }
                }

                //Initiate bar code scanning
                else new IntentIntegrator(RefillRxActivity.this).initiateScan();
            }
        });

        rgLoc.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radio_location_zip) {
                    locationEditText.setEnabled(true);
                } else
                    locationEditText.setEnabled(false);
            }
        });

        //Set up button
        submitWalReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Response.Listener<WalLandingRespContainer> listener = new Response.Listener<WalLandingRespContainer>() {
                    @Override
                    public void onResponse(WalLandingRespContainer response) {
                        Log.v(TAG, response.getTemplate() + "|" + response.getToken() +
                                "|" + response.getUpLimit() + "|" + response.getUrl());
                    }
                };

                int id = rgLoc.getCheckedRadioButtonId();
                //User wants to use current location
                switch (id) {
                    case R.id.radio_location_gps:
                        //Easier to ref
                        final Context context = RefillRxActivity.this;

                        //Check if location services enabled
                        LocationManager lm = (LocationManager) RefillRxActivity.this.getSystemService(Context.LOCATION_SERVICE);
                        boolean gps_enabled = false;
                        boolean network_enabled = false;

                        try {
                            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                        } catch (Exception ex) {
                        }

                        try {
                            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                        } catch (Exception ex) {
                        }

                        //If location service is not enabled
                        if (!gps_enabled && !network_enabled) {

                            // Notify user, prompt to be taken to enable
                            final AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                            dialog.setMessage(context.getResources().getString(R.string.loc_not_enabled));
                            dialog.setPositiveButton(context.getResources().getString(R.string.enable_loc), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    context.startActivity(myIntent);
                                }
                            });
                            dialog.setNegativeButton(context.getString(R.string.cancel_loc), new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                                }
                            });
                            dialog.show();
                        } else //We have services, check permissions
                            checkPermissionGPSAndStartWalgreens();
                        break;
                    case R.id.radio_location_zip:
                        final Geocoder geocoder = new Geocoder(RefillRxActivity.this);
                        final String zip = locationEditText.getText().toString();
                        try {
                            List<Address> addresses = geocoder.getFromLocationName(zip, 1);
                            if (addresses != null && !addresses.isEmpty()) {
                                Address address = addresses.get(0);

                                //Run the webview activity
                                Intent webIntent = new Intent(RefillRxActivity.this, RefillWebViewActivity.class);
                                webIntent.putExtra("num", rxNumEditText.getText().toString());
                                webIntent.putExtra("lat", address.getLatitude());
                                webIntent.putExtra("lng", address.getLongitude());
                                startActivity(webIntent);
                            } else {
                                // Display appropriate message when Geocoder services are not available
                                Toast.makeText(RefillRxActivity.this, R.string.cant_geocode_msg, Toast.LENGTH_LONG).show();
                            }
                        } catch (IOException e) {
                            // handle exception
                        }
                        break;
                    default:
                        Log.e(TAG, "Programming Error: No radio id case");
                }
            }
        });
    }

    // Get the results From barcode scan
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                rxNumEditText.setText(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Checks Location permissions, and calls startWalgreensWithGPS() if granted
     */
    private void checkPermissionGPSAndStartWalgreens() {
        //Check our permissions for user locationed
        if (ActivityCompat
                .checkSelfPermission(RefillRxActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(RefillRxActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(RefillRxActivity.this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    GPS_REQ_CODE);
        } else {
            //Should have permissions
            startWalgreensWithGPS();
        }
    }

    /**
     * Start listening for location updates, and start walgreens webview activity once  one is recieved,
     * DO NOT CALL THIS METHOD WITHOUT FIRST GETTING LOCATION PERMISSIONS
     */
    private void startWalgreensWithGPS() {
        //Start listening for location, and provide it in intent bundle
        final LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);

        Intent webIntent = new Intent(RefillRxActivity.this, RefillWebViewActivity.class);
        webIntent.putExtra("num", rxNumEditText.getText().toString());

        //Show user we're getting the location
        pd = new ProgressDialog(this);
        pd.setTitle("Getting Location");
        pd.setCanceledOnTouchOutside(false);
        pd.show();

        try {
            //Request Location updates
            final String bestProvider = lm.getBestProvider(new Criteria(), true);
            locListener = new WalgreensLocationListener(webIntent);
            lm.requestLocationUpdates(lm.getBestProvider(new Criteria(), true), 0, 0, locListener);

            //Init the timer fresh
            timerGetLocation = new Timer();

            //If it takes over timeout to get an update, cancel and alert user
            timerGetLocation.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        lm.removeUpdates(locListener);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pd.dismiss();
                                AlertDialog aTimeOut = new AlertDialog.Builder(RefillRxActivity.this).create();
                                aTimeOut.setTitle(getString(R.string.timeout));
                                aTimeOut.setMessage(getString(R.string.timeout_loc_message));
                                aTimeOut.show();
                            }
                        });
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                }
            }, GPS_TIMEOUT_MS);
        } catch (SecurityException e) {
            Toast.makeText(this,TAG + ": Programming Error, Should have permissions", Toast.LENGTH_SHORT).show();

            //Alert the user about the error
            final AlertDialog ad = new AlertDialog.Builder(RefillRxActivity.this).create();
            ad.setTitle("Error");
            ad.setMessage(getString(R.string.cant_get_location));
            ad.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ad.dismiss();
                }
            });
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        if(timerGetLocation != null) {
            //Cancel the timer to avoid error when user hits back while it's getting location
            timerGetLocation.cancel();
            timerGetLocation.purge();
        }

        super.onBackPressed();
    }

    @Override
    public void onPause() {
        if(locListener != null && ContextCompat.checkSelfPermission(RefillRxActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(RefillRxActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            //Pause location listening for battery
            LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
            lm.removeUpdates(locListener);
        }

        super.onPause();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if(locListener != null&& ContextCompat.checkSelfPermission(RefillRxActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(RefillRxActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locListener);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        if(requestCode == GPS_REQ_CODE)
        {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                startWalgreensWithGPS();
            }
        }
        else if(requestCode == CAM_REQ_CODE)
        {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                new IntentIntegrator(RefillRxActivity.this).initiateScan();
            }
        }
    }

    /**
     * Nested private class used to listen for location events in order to send to the walgreens API,
     * only intended to be used within nesting class (RefillRxActivity)
     */
    private class WalgreensLocationListener implements LocationListener{

        private Intent webIntent;

        public WalgreensLocationListener(Intent webIntent)
        {
            this.webIntent = webIntent;
        }

        @Override
        public void onLocationChanged(Location location) {

            //Cancel the timer for getting location
            timerGetLocation.cancel();
            timerGetLocation.purge();

            //Send location and start
            webIntent.putExtra("lat", location.getLatitude());
            webIntent.putExtra("lng", location.getLongitude());
            if(pd != null && pd.isShowing()) pd.dismiss();
            startActivity(webIntent);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }


}
