package com.cruzj6.mha.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.cruzj6.mha.R;
import com.cruzj6.mha.models.Days;

public class PillboxActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pillbox);

    }


    //Called when a day is clicked or all days is clicked
    public void showPillListForDay(View view) {

        int bundleDay = -1;
        switch (view.getId())
        {
            case R.id.button_day_m:
                bundleDay = Days.MONDAY.getNumVal();
                break;

            case R.id.button_day_t:
                bundleDay = Days.TUESDAY.getNumVal();
                break;

            case R.id.button_day_w:
                bundleDay = Days.WEDNESDAY.getNumVal();
                break;

            case R.id.button_day_r:
                bundleDay = Days.THURSDAY.getNumVal();
                break;

            case R.id.button_day_f:
                bundleDay = Days.FRIDAY.getNumVal();
                break;

            case R.id.button_day_sa:
                bundleDay = Days.SATURDAY.getNumVal();
                break;

            case R.id.button_day_s:
                bundleDay = Days.SUNDAY.getNumVal();
                break;

            case R.id.button_day_all:
                bundleDay = -1;
                break;
        }

        //Start the activity sending it which day
        Intent pillListIntent = new Intent(this, PillListActivity.class);
        pillListIntent.putExtra("day", bundleDay);
        startActivity(pillListIntent);

    }
}
