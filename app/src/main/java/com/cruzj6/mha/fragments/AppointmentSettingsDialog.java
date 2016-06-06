package com.cruzj6.mha.fragments;



import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.cruzj6.mha.R;
import com.cruzj6.mha.activities.AppointmentsActivity;
import com.cruzj6.mha.dataManagement.DatabaseManager;
import com.cruzj6.mha.helpers.NotificationItemsManager;
import com.cruzj6.mha.models.AppointmentItem;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

/**
 * Created by Joey on 5/25/16.
 */
public class AppointmentSettingsDialog extends DialogFragment implements DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener {

    //Set default date
    long setUnixDate = System.currentTimeMillis() / 1000L;

    private static EditText apptTitleEditText;
    private static EditText labworkEditText;
    private static EditText remindDaysEditText;
    private static EditText notesEditText;
    private static CheckBox remindDaysCheckbox;
    private static CheckBox requiresLabworkCheckbox;
    private static Button setDateButton;
    private static Button setTimeButton;
    private static LinearLayout labworkDaysLayout;
    private static TextView selectedTimeTextView;
    private static TextView selectedDateTextView;
    //private static NumberPicker labworkNumberPicker;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //Set up date picker and date refs
        DatePickerDialog dp;

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        //Inflate the view
        View apptSettingsView = inflater.inflate(R.layout.layout_appointment_settings, null);

        //Get our control references
        apptTitleEditText = (EditText) apptSettingsView.findViewById(R.id.editText_appt_title);
        //labworkNumberPicker = (NumberPicker) apptSettingsView.findViewById(R.id.numberpicker_labwork_days);
        labworkEditText = (EditText) apptSettingsView.findViewById(R.id.edittext_labwork_days);
        remindDaysEditText = (EditText) apptSettingsView.findViewById(R.id.edittext_remind_days_appt);
        notesEditText = (EditText)apptSettingsView.findViewById(R.id.edittext_notes);
        remindDaysCheckbox = (CheckBox) apptSettingsView.findViewById(R.id.checkbox_remind_days_appt);
        requiresLabworkCheckbox = (CheckBox) apptSettingsView.findViewById(R.id.checkbox_requires_labwork);
        setDateButton = (Button) apptSettingsView.findViewById(R.id.button_set_date);
        setTimeButton = (Button) apptSettingsView.findViewById(R.id.button_set_time);
        labworkDaysLayout = (LinearLayout) apptSettingsView.findViewById(R.id.linearlayout_labwork_days);
        selectedDateTextView = (TextView) apptSettingsView.findViewById(R.id.textview_selected_date);
        selectedTimeTextView = (TextView) apptSettingsView.findViewById(R.id.textview_selected_time);

        final Long itemId = getArguments().getLong("id");

        //Get the mode from the bundle args, if this is existing or new
        final SettingsTypes mode = (SettingsTypes) getArguments().getSerializable("mode");


        //If we are editing an existing then set up the view
        if(mode == SettingsTypes.EDIT_EXISTING)
        {
            AppointmentItem theItem = new DatabaseManager(getContext()).loadAppointmentById(itemId);
            apptTitleEditText.setText(theItem.getAppointmentTitle());
            labworkEditText.setText(Objects.toString(theItem.getLabworkDaysBefore(), null));
            //labworkNumberPicker.setValue((int)theItem.getLabworkDaysBefore());
            remindDaysEditText.setText(Objects.toString(theItem.getRemindDaysBefore(), null));
            notesEditText.setText(theItem.getNotes());
            remindDaysCheckbox.setChecked(theItem.getRemindDaysBefore() != 0);
            requiresLabworkCheckbox.setChecked(theItem.getRequiresLabWork());

            //Set date and time
            setUnixDate = theItem.getApptDate();
            Date setDate = new Date(setUnixDate*1000);

            //Set up the labels
            setDateTimeLabels(setDate);
        }
        if(mode == SettingsTypes.NEW_APPOINTMENT)
        {
            //Set up default date
            Date setDate = new Date(setUnixDate*1000);
            setDateTimeLabels(setDate);
        }

        //Set conditional diplays based on checks
        remindDaysEditText.setVisibility(remindDaysCheckbox.isChecked() ? View.VISIBLE : View.GONE);
        labworkDaysLayout.setVisibility(requiresLabworkCheckbox.isChecked() ? View.VISIBLE : View.GONE);

        //Set up the click events for buttons and others
        remindDaysCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Only show if the box is checked pertaining to it
                remindDaysEditText.setVisibility(remindDaysCheckbox.isChecked() ? View.VISIBLE : View.GONE);
            }
        });

        requiresLabworkCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Only show if the box is checked pertaining to it
                labworkDaysLayout.setVisibility(requiresLabworkCheckbox.isChecked() ? View.VISIBLE : View.GONE);
            }
        });

        setDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date setDate = new Date(setUnixDate*1000);

                SimpleDateFormat f = new SimpleDateFormat("MM");
                int mon = Integer.parseInt(f.format(setDate));

                f = new SimpleDateFormat("dd");
                int day = Integer.parseInt(f.format(setDate));

                f = new SimpleDateFormat("yyyy");
                int year = Integer.parseInt(f.format(setDate));

                //Deploy the date picker, this as its listener
                DatePickerDialog dp = new DatePickerDialog(getContext(), AppointmentSettingsDialog.this, year, mon - 1, day);
                dp.show();
            }
        });

        setTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date setDate = new Date(setUnixDate*1000);

                SimpleDateFormat f = new SimpleDateFormat("HH");
                int hour = Integer.parseInt(f.format(setDate));

                f = new SimpleDateFormat("mm");
                int minutes = Integer.parseInt(f.format(setDate));

                //Deploy time picker
                TimePickerDialog tp = new TimePickerDialog(getContext(), AppointmentSettingsDialog.this, hour, minutes, false);
                tp.show();
            }
        });

        //Set the layout for the dialog
        builder.setView(apptSettingsView)
                // Add action buttons
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String title = apptTitleEditText.getText().toString();
                        String notes = notesEditText.getText().toString();
                        long apptDate = setUnixDate;
                        long remindDaysBefore =
                                remindDaysCheckbox.isChecked() ?
                                        Long.parseLong(remindDaysEditText.getText().toString()) : 0;

                        //Check if we need labwork object or not
                        AppointmentItem apptItem = null;
                        if(requiresLabworkCheckbox.isChecked())
                        {
                            long labworkTime = Long.parseLong(labworkEditText.getText().toString());
                            //long labworkTime = labworkNumberPicker.getValue();
                            apptItem = new AppointmentItem(title, apptDate, remindDaysBefore, notes, labworkTime);
                        }
                        else
                        {
                            apptItem = new AppointmentItem(title, apptDate, remindDaysBefore, notes);
                        }

                        //If we are editing an existing one, tack on the ID
                        if(mode == SettingsTypes.EDIT_EXISTING) {
                            apptItem.setApptId(itemId);
                        }

                        //Push it into the database
                        long apptItemId = new DatabaseManager(getContext()).saveAppointment(apptItem);

                        //Notification setup
                        NotificationItemsManager.createApptNotification(apptItemId, getContext());

                        //Trigger reload for the appointments activity
                        ((AppointmentsActivity)getContext()).buildApptsList();


                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        AppointmentSettingsDialog.this.getDialog().cancel();
                    }
                });

        return builder.create();
    }

    private void setDateTimeLabels(Date setDate)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(setDate);
        SimpleDateFormat f = new SimpleDateFormat("MM/dd/yy");
        selectedDateTextView.setText(f.format(setDate));
        f = new SimpleDateFormat("hh:mm aaa");
        selectedTimeTextView.setText(f.format(setDate));
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        Date setDate = new Date(setUnixDate * 1000);
        Calendar c = Calendar.getInstance();
        c.setTime(setDate);
        c.set(Calendar.YEAR, year);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        c.set(Calendar.MONTH, monthOfYear);

        //Set unix date global
        setUnixDate = c.getTimeInMillis() / 1000L;

        //Change the labels
        setDateTimeLabels(c.getTime());
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Date setDate = new Date(setUnixDate * 1000);
        Calendar c = Calendar.getInstance();
        c.setTime(setDate);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.HOUR_OF_DAY, hourOfDay);

        //Set unix date global
        setUnixDate = c.getTimeInMillis() / 1000L;

        //Set the labels
        setDateTimeLabels(c.getTime());
    }

    public enum SettingsTypes
    {
        EDIT_EXISTING,
        NEW_APPOINTMENT
    }
}
