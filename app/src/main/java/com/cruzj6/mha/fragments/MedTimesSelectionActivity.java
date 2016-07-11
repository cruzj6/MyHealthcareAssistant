package com.cruzj6.mha.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.cruzj6.mha.R;
import com.cruzj6.mha.activities.ListActivity;
import com.cruzj6.mha.adapters.PillTimeItemAdapter;
import com.cruzj6.mha.helpers.TimeHelper;
import com.cruzj6.mha.models.Days;
import com.cruzj6.mha.models.PillItem;
import com.cruzj6.mha.models.PillTimeItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Joey on 6/24/16.
 */
public class MedTimesSelectionActivity extends ListActivity implements TimePickerDialog.OnTimeSetListener{

    private ListView timesLv;
    private long[] times;
    private int day;
    private List<PillTimeItem> pillTimes;
    private PillTimeItemAdapter adapter;

    private static final int TIME_PICK_NEW = 0;
    private static final int TIME_PICK_EDIT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_med_times);
        times = getIntent().getExtras().getLongArray("times");
        day = getIntent().getExtras().getInt("day");
        if(times != null) buildPillTimes();
        else pillTimes = new ArrayList<>();
        timesLv = (ListView) findViewById(R.id.listview_pill_times);
        adapter = new PillTimeItemAdapter(this, 0, pillTimes);
        timesLv.setAdapter(adapter);
        String dayString = "Select times for ";
        if (day == -1) dayString += "All Days";
        else dayString += Days.values()[day].getStringName();
        Snackbar sb = Snackbar.make(findViewById(android.R.id.content), dayString,
                Snackbar.LENGTH_INDEFINITE)
                .setAction("Done", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        sb.setActionTextColor(Color.YELLOW);
        sb.show();
    }

    @Override
    public void onStop()
    {
        super.onStop();
    }

    @Override
    public void onBackPressed()
    {
        long[] finalTimes = new long[pillTimes.size()];
        int i = 0;
        for(PillTimeItem timeItem : pillTimes)
        {
            finalTimes[i] = TimeHelper.getTimeUnix(timeItem.hourOfDay, timeItem.minute, 0);
            i++;
        }
        Intent data = new Intent();
        data.putExtra("times", finalTimes);
        data.putExtra("day", day);

        if(finalTimes.length > 0) setResult(RESULT_OK, data);
        else setResult(RESULT_CANCELED);
        super.onBackPressed();
        finish();
    }

    private void buildPillTimes()
    {
        int hour;
        int minute;
        SimpleDateFormat hf = new SimpleDateFormat("HH");
        SimpleDateFormat mf = new SimpleDateFormat("mm");
        Date d = new Date();
        pillTimes = new ArrayList<>();
        for(long time : times)
        {
            d.setTime(time*1000);
            hour = Integer.parseInt(hf.format(d));
            minute = Integer.parseInt(mf.format(d));
            pillTimes.add(new PillTimeItem(hour, minute));
        }
    }

    @Override
    protected void onRemoveModeStart() {
        adapter.startRemoveMode();
    }

    @Override
    protected void onAddClick() {
        if(pillTimes.size() >= 7)
        {
            Toast t = Toast.makeText(this, "Cannot add more Times", Toast.LENGTH_SHORT);
            t.show();
        }
        else {
            TimePickerDialog tpd = new TimePickerDialog(this, this, 12, 0, false);
            tpd.show();
        }
    }

    @Override
    protected void onConfirmRemove() {

        //Remove from the list, we do not do anything in the database yet,
        //so this will send it to the pillsettingsdialog
        List<PillTimeItem> toRemove = new ArrayList<>();
        for(PillTimeItem item : pillTimes)
        {
            if(item.getShouldRemove())
            {
                toRemove.add(item);
            }
        }
        for(PillTimeItem item : toRemove)
        {
            pillTimes.remove(item);
        }
        adapter.endRemoveMode(true);
    }

    @Override
    protected void onRemoveModeEnd() {
        adapter.endRemoveMode(false);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        pillTimes.add(new PillTimeItem(hourOfDay, minute));
        adapter.notifyDataSetChanged();
    }
}
