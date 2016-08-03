package com.cruzj6.mha.activities;


import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.cruzj6.mha.R;

import com.cruzj6.mha.fragments.AppointmentSettingsDialog;
import com.cruzj6.mha.fragments.AppointmentsQueue;
import com.cruzj6.mha.fragments.PillSettingsDialog;
import com.cruzj6.mha.fragments.PillsQueue;
import com.cruzj6.mha.models.ItemSettingsInvokeHandler;
import com.cruzj6.mha.models.MissedPillContainer;
import com.cruzj6.mha.models.SettingsTypes;

import net.danlew.android.joda.JodaTimeAndroid;

public class MainActivity extends AppCompatActivity implements ItemSettingsInvokeHandler{

    //Final vars
    final String TAG = "MainActivity";

    //View components
    private FrameLayout apptFrame;
    private Button pillBoxButton;
    private Button appointmentsButton;
    private Button refillButton;
    private Button otherButton;
    private ActionBar ab;
    private RelativeLayout quickApptLayout;
    private RelativeLayout quickMedsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Init jodatime library
        JodaTimeAndroid.init(this);

        //Get button hanldes
        appointmentsButton = (Button) findViewById(R.id.appointments_btn);
        pillBoxButton = (Button) findViewById(R.id.pillBox_btn);
        refillButton = (Button) findViewById(R.id.refill_btn);
        otherButton = (Button) findViewById(R.id.missed_logs_btn);
        quickApptLayout = (RelativeLayout) findViewById(R.id.layout_quick_add_appt);
        quickMedsLayout = (RelativeLayout) findViewById(R.id.layout_quick_add_meds);


        //Set click Events
        appointmentsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAppointmentsScreen();
            }
        });

        pillBoxButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPillBoxScreen();
            }
        });

        refillButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openRefillScreen();
            }
        });

        otherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMissedPillsLogScreen();
            }
        });

        quickApptLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Give it the id of this item so it can get it from the db to edit
                AppointmentSettingsDialog dialog = new AppointmentSettingsDialog();
                dialog.setOnSaveHandler(MainActivity.this);
                Bundle args = new Bundle();
                args.putSerializable("mode", SettingsTypes.NEW_ITEM);
                dialog.setArguments(args);
                dialog.show(getSupportFragmentManager(), "New Appointment");
            }
        });

        quickMedsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PillSettingsDialog psd = new PillSettingsDialog();
                Bundle b = new Bundle();
                b.putSerializable("mode", SettingsTypes.NEW_ITEM);
                psd.setItemSettingsInvokeHandler(MainActivity.this);
                psd.setArguments(b);
                psd.show(getSupportFragmentManager(), "New Medication");
            }
        });
    }

    @Override
    public void onResume()
    {
        super.onResume();
        buildApptQueueFragment();
        buildPillQueueFragment();
    }

    public void buildApptQueueFragment()
    {
        Fragment apptFrag = new AppointmentsQueue();
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.appt_queue_frame, apptFrag).commit();
    }

    public void buildPillQueueFragment()
    {
        Fragment pillFrag = new PillsQueue();
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.pill_queue_frame, pillFrag).commit();
    }


    //Called when view has loaded
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

    }

    private void openAppointmentsScreen()
    {
        //Create intent and start activity
        Intent apptIntent = new Intent(this, AppointmentsActivity.class);
        startActivity(apptIntent);
    }

    private void openPillBoxScreen()
    {
        Intent pillboxIntent = new Intent(this, PillboxActivity.class);
        startActivity(pillboxIntent);
    }

    private void openRefillScreen()
    {
        Intent refillIntent = new Intent(this, RefillRxActivity.class);
        startActivity(refillIntent);
    }

    private void openMissedPillsLogScreen()
    {
        Intent missedIntent = new Intent(this, MissMedsActivity.class);
        startActivity(missedIntent);
    }

    @Override
    public void onItemSaved() {
        buildPillQueueFragment();
        buildApptQueueFragment();
    }
}
