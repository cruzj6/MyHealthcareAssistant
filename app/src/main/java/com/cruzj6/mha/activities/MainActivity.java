package com.cruzj6.mha.activities;


import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.cruzj6.mha.R;

import com.cruzj6.mha.fragments.AppointmentsQueue;

public class MainActivity extends AppCompatActivity {

    //Final vars
    final String TAG = "MainActivity";

    //View components
    static FrameLayout apptFrame;
    static ImageButton pillBoxButton;
    static ImageButton appointmentsButton;
    static ImageButton refillButton;
    static ImageButton otherButton;
    static ActionBar ab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set AB Title
        ab = getSupportActionBar();
        ab.setTitle(R.string.dashboard_header_string);

        //Get button hanldes
        appointmentsButton = (ImageButton) findViewById(R.id.appointments_btn);
        pillBoxButton = (ImageButton) findViewById(R.id.pillBox_btn);
        refillButton = (ImageButton) findViewById(R.id.refill_btn);
        otherButton = (ImageButton) findViewById(R.id.missed_logs_btn);

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
    }

    @Override
    public void onResume()
    {
        super.onResume();
        buildApptQueueFragment();
    }

    public void buildApptQueueFragment()
    {
        Fragment apptFrag = new AppointmentsQueue();
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.appt_queue_frame, apptFrag).commit();
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
        //TODO: Open View
    }
}
