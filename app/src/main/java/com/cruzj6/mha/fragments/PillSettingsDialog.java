package com.cruzj6.mha.fragments;


import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cruzj6.mha.R;
import com.cruzj6.mha.adapters.DayEnabledHandler;
import com.cruzj6.mha.adapters.PillTimesPerDayAdapter;
import com.cruzj6.mha.dataManagement.DatabaseManager;
import com.cruzj6.mha.helpers.NotificationItemsManager;
import com.cruzj6.mha.helpers.TimeHelper;
import com.cruzj6.mha.models.Days;
import com.cruzj6.mha.models.ItemSettingsInvokeHandler;
import com.cruzj6.mha.models.PillItem;
import com.cruzj6.mha.models.SettingsTypes;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Joey on 6/20/16.
 */
public class PillSettingsDialog extends DialogFragment implements DatePickerDialog.OnDateSetListener,
        DayEnabledHandler{

    private PillItem item;

    private SettingsTypes mode;
    private EditText pillName;
    private EditText pillInstr;
    private EditText duration;
    private EditText endDate;
    private ListView daysLv;
    private TextView refillDate;
    private TextView allTimes;
    private Button setRefillBtn;
    private RadioGroup radioGroupEnd;
    private CheckBox sameTimesEachDayCheckbox;
    private RadioButton rbTilDate;
    private RadioButton rbDuration;

    private long[] cachedGlobalTimes = {};
    private long cachedEndDate;
    private long cachedRefillDate;

    private List<long[]> timesPerDay;
    private ItemSettingsInvokeHandler handler;
    private PillTimesPerDayAdapter pillTimesAdapter;
    private PillItem cachedItem;

    private final static String TAG = "PillSettingsDialog";
    private final static int SET_END_DATE = 0;
    private final static int SET_REFILL_DATE = 1;
    private final static int SET_ALL_DAYS_TIME = 2;
    private final SimpleDateFormat f = new SimpleDateFormat("MM/dd/yy");

    public static final int PICK_TIMES_GLOBAL = 1;
    public static final int PICK_TIMES_DAY = 0;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inf = getActivity().getLayoutInflater();
        View view = inf.inflate(R.layout.dialog_pill_settings, null);
        pillName = (EditText) view.findViewById(R.id.edittext_pill_title);
        pillInstr = (EditText) view.findViewById(R.id.edittext_instr);
        duration = (EditText)  view.findViewById(R.id.edittext_pill_duration);
        endDate = (EditText) view.findViewById(R.id.edittext_pill_end_date);
        daysLv = (ListView) view.findViewById(R.id.listview_pill_timeday_list);
        radioGroupEnd = (RadioGroup) view.findViewById(R.id.radiogroup_end);
        refillDate = (TextView) view.findViewById(R.id.textview_refill_date);
        setRefillBtn = (Button) view.findViewById(R.id.button_set_refill_date);
        sameTimesEachDayCheckbox = (CheckBox) view.findViewById(R.id.checkbox_same_times_each_day);
        allTimes = (TextView) view.findViewById(R.id.textview_alltimes);
        rbTilDate = (RadioButton) view.findViewById(R.id.radio_tildate);
        rbDuration = (RadioButton) view.findViewById(R.id.radio_duration);
        mode = (SettingsTypes) getArguments().getSerializable("mode");

        final DatabaseManager dbManager = new DatabaseManager(getActivity());
        daysLv.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);

        //No keyboard when clicking into edit text, we open datepicker
        endDate.setKeyListener(null);

        //Refill date set button
        setRefillBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDatePicker(cachedRefillDate, SET_REFILL_DATE);
            }
        });


        //If we are opening an existing appt, vs creating a new one
        if(mode == SettingsTypes.EDIT_EXISTING) {
            final long id = getArguments().getLong("id");
            item = dbManager.loadPillItemById(id);

            //Cache the old one for deleting alarms
            cachedItem = new PillItem(item);
            buildSettingsFields();
        }
        else if(mode == SettingsTypes.NEW_ITEM) {
            item = new PillItem("", "", 0, new Date().getTime()/1000L);
            buildSettingsFields();
        }
        else item = null;

        //When we check "same time each day"
        sameTimesEachDayCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    openGlobalTimesSelect();
                }
            }
        });

        //When user clicks the end date edittext
        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDatePicker(cachedEndDate, SET_END_DATE);
            }
        });

        //Which radio button is selected
        radioGroupEnd.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId)
                {
                    case R.id.radio_duration:
                        endDate.setEnabled(false);
                        duration.setEnabled(true);
                        break;
                    case R.id.radio_tildate:
                        duration.setEnabled(false);
                        endDate.setEnabled(true);

                        //Make sure some date is put here
                        if(endDate.getText().toString().equals(""))
                        {
                            openDatePicker(new Date().getTime()/1000, SET_END_DATE);
                        }
                        break;

                    case R.id.radio_none:
                        endDate.setEnabled(false);
                        duration.setEnabled(false);
                        break;
                }
            }
        });

        //Taps for opening the times selection dialogs
        //When diff times for each day
        daysLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(!sameTimesEachDayCheckbox.isChecked())
                {
                    openDayTimesSelect(Days.values()[position]);
                }
            }
        });

        //When same times each day is checked, and times is tapped
        allTimes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sameTimesEachDayCheckbox.isChecked()) {
                    openGlobalTimesSelect();
                }
            }
        });

        //Set up our dialog itself, cancel and save options
        builder.setView(view).setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //We ignore here, we do it in override so that it doesn't close if no name (below)
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PillSettingsDialog.this.getDialog().cancel();
            }
        });

        //Build and return it
        final AlertDialog d = builder.create();

        d.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {

                Button b = d.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        String medName = pillName.getText().toString();
                        String instr = pillInstr.getText().toString();
                        PillItem pillit = null;

                        //Dont close if they didnt put a name
                        if(medName.replaceAll("\\s+","").equals(""))
                        {
                            Toast t =
                                    Toast.makeText(getContext(),
                                            "Please Enter a Name\nFor the Medication", Toast.LENGTH_SHORT);
                            t.show();
                        }
                        else {
                            switch (radioGroupEnd.getCheckedRadioButtonId()) {
                                case R.id.radio_duration:
                                    int dur = Integer.parseInt(duration.getText().toString());
                                    pillit = new PillItem(medName, instr, dur, cachedRefillDate);
                                    if (mode == SettingsTypes.EDIT_EXISTING)
                                        pillit.setPillId(getArguments().getLong("id"));
                                    break;
                                case R.id.radio_tildate:
                                    long dateUnix = cachedEndDate;
                                    pillit = new PillItem(medName, instr, dateUnix, cachedRefillDate);
                                    if (mode == SettingsTypes.EDIT_EXISTING)
                                        pillit.setPillId(getArguments().getLong("id"));
                                    break;
                                case R.id.radio_none:
                                    pillit = new PillItem(medName, instr, 0, cachedRefillDate);
                                    if (mode == SettingsTypes.EDIT_EXISTING)
                                        pillit.setPillId(getArguments().getLong("id"));
                                    break;
                            }
                            if (pillit != null) {
                                //Set up the times per day in the item
                                for (Days day : Days.values()) {
                                    pillit.setTimesForDay(day, timesPerDay.get(day.getNumVal()));
                                }

                                //Get rid of old alarms so they dont go off
                                if(cachedItem != null)
                                {
                                    NotificationItemsManager
                                            .removeOldPillNotifications(cachedItem, getContext());
                                }
                                long pillId = dbManager.savePill(pillit);
                                NotificationItemsManager.createMedNotification(pillId, getContext());

                                //Trigger handler method
                                handler.onItemSaved();
                            } else {
                                throw new Error("Pill item NULL,  never set!");
                            }
                            d.dismiss();
                        }
                    }
                });
            }
        });

        d.setCanceledOnTouchOutside(false);
        return d;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        pillTimesAdapter.notifyDataSetChanged();
    }


    private void openDatePicker(long startUnixDate, final int dateSetId)
    {
        Date setDate = new Date(startUnixDate*1000);
        SimpleDateFormat f = new SimpleDateFormat("MM");
        int mon = Integer.parseInt(f.format(setDate));

        f.applyPattern("dd");
        int day = Integer.parseInt(f.format(setDate));

        f.applyPattern("yyyy");
        int year = Integer.parseInt(f.format(setDate));

        //If they click cancel then re-check duration radio
        DatePickerDialog dp = new DatePickerDialog(getContext(), this, year, mon - 1, day);
        dp.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_NEGATIVE) {
                    if(dateSetId == SET_END_DATE) {
                        RadioButton rb = (RadioButton) PillSettingsDialog.this.getDialog().findViewById(R.id.radio_duration);
                        rb.setChecked(true);
                    }
                }
            }
        });
        dp.getDatePicker().setId(dateSetId);
        dp.setCancelable(false);
        dp.show();
    }

    private boolean getIsDayEnabled(Days day)
    {
        CheckBox cb =(CheckBox) daysLv.getChildAt(day.getNumVal()).findViewById(R.id.checkbox_day_enabled);
        return cb.isChecked();
    }

    public void setItemSettingsInvokeHandler(ItemSettingsInvokeHandler handler)
    {
        this.handler = handler;
    }

    private void buildTimesPerDayList(List<long[]> timesPerDay)
    {
        //Build the list of times per day for the pill within the dialog
        pillTimesAdapter = new PillTimesPerDayAdapter(getActivity(), 0, timesPerDay, this);
        daysLv.setAdapter(pillTimesAdapter);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        c.set(Calendar.MONTH, monthOfYear);
        c.set(Calendar.YEAR, year);

        switch(view.getId()){
            case SET_END_DATE:
                cachedEndDate = c.getTimeInMillis() / 1000L;
                endDate.setText(f.format(c.getTime()));
                break;
            case SET_REFILL_DATE:
                cachedRefillDate = c.getTimeInMillis() / 1000L;
                refillDate.setText(f.format(c.getTime()));
                break;
        }
    }

    @Override
    public void onDayEnabledChanged(Days day, boolean isEnabled) {
        boolean updated = false;
        //If day enabled and same times each day, give the day the global times
        if(sameTimesEachDayCheckbox.isChecked() && isEnabled){
            item.setTimesForDay(day, cachedGlobalTimes);
            updated = true;
        }
        else if(isEnabled && (item.getTimesForDay(day) == null || item.getTimesForDay(day).length == 0))
        {
            openDayTimesSelect(day);
            updated = true;
        }
        else if(!isEnabled)
        {
            item.setTimesForDay(day, null);
            updated = true;
        }
        if(updated)
            pillTimesAdapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode == Activity.RESULT_OK) {
            int day = data.getIntExtra("day", -1);
            long[] times = data.getLongArrayExtra("times");
            if (requestCode == PICK_TIMES_GLOBAL) {
                setTimesGlobal(times);

            } else {
                item.setTimesForDay(day, times);
            }
        }
        else if(resultCode == Activity.RESULT_CANCELED)
        {
            if(requestCode == PICK_TIMES_GLOBAL) {
                pillTimesAdapter.notifyDataSetChanged();
                sameTimesEachDayCheckbox.setChecked(false);
            }
        }
    }

    private void setTimesGlobal(long[] times)
    {
        //Cache the times for use when a new day is enabled
        setCachedGlobalTimes(times);

        //Loop thru, if the day is enabled, then set it's time
        for(Days day : Days.values())
        {
            //No time if day not enabled
            if(timesPerDay.get(day.getNumVal()) != null)
                timesPerDay.set(day.getNumVal(), cachedGlobalTimes);
        }
    }

    private void openGlobalTimesSelect()
    {
        try {
            Intent timesIntent = new Intent(getContext(), MedTimesSelectionActivity.class);
            timesIntent.putExtra("times", cachedGlobalTimes);
            timesIntent.putExtra("day", -1);
            Toast t = Toast.makeText(getContext(), "Select Times for All Days", Toast.LENGTH_LONG);
            t.show();
            startActivityForResult(timesIntent, PICK_TIMES_GLOBAL);
        }
        catch (Exception e)
        {
            Log.e(TAG, "Error opening time selection activity");
        }
    }

    private void openDayTimesSelect(Days day)
    {
        Intent timesIntent = new Intent(getContext(), MedTimesSelectionActivity.class);
        timesIntent.putExtra("times",
                item.getTimesForDay(day) == null ? item.getTimesForDay(day) : new long[0]);
        timesIntent.putExtra("day", day.getNumVal());
        Toast t = Toast.makeText(getContext(), "Select Times for " + day.getStringName(), Toast.LENGTH_SHORT);
        t.show();
        startActivityForResult(timesIntent, PICK_TIMES_DAY);
    }

    private void buildSettingsFields()
    {
        cachedEndDate = item.getUntilDate();
        cachedRefillDate = item.getRefillDate();
        timesPerDay = item.getTimesPerDay();

        //Build our tiems for each day
        buildTimesPerDayList(timesPerDay);

        //Check if any days have times
        boolean isDayWithTimes = false;
        long[] dayWithTimes = {};
        for(long[] times : item.getTimesPerDay())
        {
            if(times != null && times.length > 0) {
                isDayWithTimes = true;
                dayWithTimes = times;
                break;
            }
        }

        boolean sameTimesEachDay = TimeHelper.checkSameTimesEachDay(item);
        //Check same times each day box, and give the time to the others if it is checked
        sameTimesEachDayCheckbox.setChecked(sameTimesEachDay && mode == SettingsTypes.EDIT_EXISTING);
        if(isDayWithTimes && sameTimesEachDay) {
            setCachedGlobalTimes(dayWithTimes);
        }

        //Set other fields
        pillName.setText(item.getTitle());
        pillInstr.setText(item.getInstr());
        refillDate.setText(f.format(new Date(cachedRefillDate*1000)));

        //End by date or duration
        if(item.getIsEndByDate()) {
            rbTilDate.setChecked(true);
            duration.setEnabled(false);
            long date = item.getUntilDate();
            SimpleDateFormat f = new SimpleDateFormat("MM/dd/yy");
            endDate.setText(f.format(new Date(date * 1000)));
        }
        else
        {
            rbDuration.setChecked(true);
            endDate.setEnabled(false);
            int dura = item.getDuration();
            duration.setText("" + dura);
        }
    }

    public void setCachedGlobalTimes(long[] times)
    {
        this.cachedGlobalTimes = times;
        //Set up label
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat f = new SimpleDateFormat("hh:mmaaa");
        Date d = new Date();
        for(long time : times)
        {
            d.setTime(time*1000);
            sb.append(f.format(d) + ", ");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.deleteCharAt(sb.length() - 1);
        allTimes.setText(sb.toString());
    }
}
