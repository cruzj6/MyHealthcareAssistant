package com.cruzj6.mha.models;


import android.content.Context;

import com.cruzj6.mha.dataManagement.DatabaseManager;
import com.cruzj6.mha.models.RemovableItem;

import java.text.SimpleDateFormat;

/**
 * Created by Joey on 5/23/16.
 */
public class AppointmentItem extends RemovableItem {

    //Publics
    public static final SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    //Privates
    private long remindDaysBefore;
    private long labworkDaysBefore;
    private long apptDate; //UNIX TIME
    private String notes;
    private String appointmentTitle;
    private boolean requiresLabWork;
    private long apptId = -1;

    public AppointmentItem(String title, long apptDate, long remindDaysBefore, String notes)
    {
        this.notes = notes;
        this.apptDate = apptDate;
        this.remindDaysBefore = remindDaysBefore;
        appointmentTitle = title;
    }

    public AppointmentItem(String title, long apptDate, long remindDaysBefore, String notes, long labworkDaysBefore)
    {
        //call basic constructor
        this(title, apptDate, remindDaysBefore, notes);

        //Assign lab work data
        requiresLabWork = true;
        this.labworkDaysBefore = labworkDaysBefore;
    }

    public String getAppointmentTitle()
    {
        return appointmentTitle;
    }

    public boolean getRequiresLabWork()
    {
        return requiresLabWork;
    }

    public long getLabworkDaysBefore()
    {
        return labworkDaysBefore;
    }

    //UNIX TIME
    public long getApptDate()
    {
        return apptDate;
    }

    public String getNotes()
    {
        return notes;
    }

    public long getRemindDaysBefore()
    {
        return remindDaysBefore;
    }

    public long getApptId()
    {
        return apptId;
    }

    public void setApptId(long id)
    {
        apptId = id;
    }

    @Override
    public void removeFromDatabase(Context context) {
        new DatabaseManager(context).deleteAppointment(getApptId());
    }
}
