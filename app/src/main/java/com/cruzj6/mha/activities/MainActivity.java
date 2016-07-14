package com.cruzj6.mha.activities;


import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.cruzj6.mha.R;

import com.cruzj6.mha.fragments.AppointmentsQueue;
import com.cruzj6.mha.fragments.PillsQueue;

public class MainActivity extends AppCompatActivity {

    //Final vars
    final String TAG = "MainActivity";

    //View components
    private FrameLayout apptFrame;
    private Button pillBoxButton;
    private Button appointmentsButton;
    private Button refillButton;
    private Button otherButton;
    private ActionBar ab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get button hanldes
        appointmentsButton = (Button) findViewById(R.id.appointments_btn);
        pillBoxButton = (Button) findViewById(R.id.pillBox_btn);
        refillButton = (Button) findViewById(R.id.refill_btn);
        otherButton = (Button) findViewById(R.id.missed_logs_btn);

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
        //TODO: Open View
    }
}
