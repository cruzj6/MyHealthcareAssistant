package com.cruzj6.mha.dataManagement;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;
import android.util.Log;

import com.cruzj6.mha.AppointmentItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Joey on 5/23/16.
 */
public class DatabaseManager extends SQLiteOpenHelper{

    public final static String DATABASE_NAME = "MedApp.db";
    public final static int DATABASE_VER = 1;

    public DatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VER);
    }

    public void deleteAppointment(long apptId)
    {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(DatabaseContract.AppointmentEntry.TABLE_NAME,
                DatabaseContract.AppointmentEntry._ID + "=" + apptId, null);
    }

    public long saveAppointment(AppointmentItem apptItem)
    {
        SQLiteDatabase db = getWritableDatabase();

        //Build the entry
        ContentValues newData = new ContentValues();
        newData.put(DatabaseContract.AppointmentEntry.COLUMN_NAME_DR_NAME, apptItem.getAppointmentTitle());
        newData.put(DatabaseContract.AppointmentEntry.COLUMN_NAME_APPT_DATE, apptItem.getApptDate());
        newData.put(DatabaseContract.AppointmentEntry.COLUMN_NAME_REQ_LABWORK, apptItem.getRequiresLabWork());
        newData.put(DatabaseContract.AppointmentEntry.COLUMN_NAME_LABWORK_DAYS_BEFORE, apptItem.getLabworkDaysBefore());
        newData.put(DatabaseContract.AppointmentEntry.COLUMN_NAME_REMIND_DAYS_BEFORE, apptItem.getRemindDaysBefore());
        newData.put(DatabaseContract.AppointmentEntry.COLUMN_NAME_NOTES,apptItem.getNotes());

        if(apptItem.getApptId() == -1)
            //Insert the entry if none exists with that id
            return db.insert(DatabaseContract.AppointmentEntry.TABLE_NAME, null, newData);
        else
            //If already exists update it
            db.update(DatabaseContract.AppointmentEntry.TABLE_NAME, newData,
                        DatabaseContract.AppointmentEntry._ID + "=" + apptItem.getApptId(), null);
        return apptItem.getApptId();
    }

    //Load items from the appointments table in to database into
    //AppointmentItem's and return the list
    public List<AppointmentItem> loadAppointmentItems()
    {
        List<AppointmentItem> loadedItems = new ArrayList<AppointmentItem>();

        //Get all of the rows in the table for appointments
        SQLiteDatabase database = getReadableDatabase();
        Cursor c = database.query(DatabaseContract.AppointmentEntry.TABLE_NAME, null, null, null, null, null, null);

        //Go through each row
        c.moveToFirst();
        while(!c.isAfterLast())
        {
            AppointmentItem newItem = appointmentItemFromCursor(c);
            loadedItems.add(newItem);

            c.moveToNext();
        }

        return loadedItems;
    }

    public AppointmentItem loadAppointmentById(Long id)
    {
        SQLiteDatabase database = getReadableDatabase();
        Cursor c = database.rawQuery("SELECT * FROM " + DatabaseContract.AppointmentEntry.TABLE_NAME
                + " WHERE " + DatabaseContract.AppointmentEntry._ID + "=" + id, null);

        c.moveToFirst();
        AppointmentItem apptItem = appointmentItemFromCursor(c);

        return apptItem;
    }

    private AppointmentItem appointmentItemFromCursor(Cursor c)
    {
        AppointmentItem newItem;

        int colIndex = c.getColumnIndexOrThrow(DatabaseContract.AppointmentEntry._ID);
        long pk = c.getLong(colIndex);
        Log.v("TEST PRIMARY KEY", "" + pk);

        //Get each component
        //Get the drName
        colIndex = c.getColumnIndexOrThrow(DatabaseContract.AppointmentEntry.COLUMN_NAME_DR_NAME);
        String drName = c.getString(colIndex);

        //Get the appointment date (Unix Time)
        colIndex = c.getColumnIndexOrThrow(DatabaseContract.AppointmentEntry.COLUMN_NAME_APPT_DATE);
        long apptDate = c.getLong(colIndex);

        ///Get days before appointment user needs to do lab work
        colIndex = c.getColumnIndexOrThrow(DatabaseContract.AppointmentEntry.COLUMN_NAME_LABWORK_DAYS_BEFORE);
        long labworkDaysBefore = c.getLong(colIndex);

        //Get the notes for the appointment
        colIndex = c.getColumnIndexOrThrow(DatabaseContract.AppointmentEntry.COLUMN_NAME_NOTES);
        String notes = c.getString(colIndex);

        //Get the number of days before the appt to remind the user
        colIndex = c.getColumnIndexOrThrow(DatabaseContract.AppointmentEntry.COLUMN_NAME_REMIND_DAYS_BEFORE);
        long remindDaysBefore = c.getLong(colIndex);

        //Get if requires labwork
        colIndex = c.getColumnIndexOrThrow(DatabaseContract.AppointmentEntry.COLUMN_NAME_REQ_LABWORK);
        String labWork = c.getString(colIndex);
        Log.v("DATABASEMANAGER", "LabWork Req: " + labWork);

        //Check if needs labwork
        if(labWork.equals("1"))
        {
            newItem = new AppointmentItem(drName, apptDate, remindDaysBefore, notes, labworkDaysBefore);
        }
        else
        {
            newItem = new AppointmentItem(drName, apptDate, remindDaysBefore, notes);
        }

        //set id and add to list
        newItem.setApptId(pk);

        return newItem;
    }
    @Override
    public void onCreate(SQLiteDatabase db) {

        //Create the database tables
        db.execSQL("CREATE TABLE " + DatabaseContract.AppointmentEntry.TABLE_NAME +
                " (" +
                DatabaseContract.AppointmentEntry._ID + " INTEGER PRIMARY KEY," +
                DatabaseContract.AppointmentEntry.COLUMN_NAME_DR_NAME + " TEXT," +
                DatabaseContract.AppointmentEntry.COLUMN_NAME_APPT_DATE + " INTEGER," +
                DatabaseContract.AppointmentEntry.COLUMN_NAME_REQ_LABWORK + " TEXT," +
                DatabaseContract.AppointmentEntry.COLUMN_NAME_LABWORK_DAYS_BEFORE + " INTEGER," +
                DatabaseContract.AppointmentEntry.COLUMN_NAME_REMIND_DAYS_BEFORE + " INTEGER," +
                DatabaseContract.AppointmentEntry.COLUMN_NAME_NOTES + " TEXT"
                + ")"
        );
        //TODO: Pills etc
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
