package com.cruzj6.mha.dataManagement;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;
import android.util.Log;

import com.cruzj6.mha.models.AppointmentItem;
import com.cruzj6.mha.models.Days;
import com.cruzj6.mha.models.PillItem;

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

    public void deletePill(long pillId)
    {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(DatabaseContract.PillEntry.TABLE_NAME,
                DatabaseContract.PillEntry._ID + "=" + pillId, null);
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

    public long savePill(PillItem pillItem)
    {
        SQLiteDatabase db = getWritableDatabase();

        //Build the entry
        ContentValues newData = new ContentValues();
        newData.put(DatabaseContract.PillEntry.COLUMN_NAME_PILL_NAME, pillItem.getTitle());
        newData.put(DatabaseContract.PillEntry.COLUMN_NAME_INSTR, pillItem.getInstr());

        //So we can iterate through easier
        String[] dayDBContract = {
                DatabaseContract.PillEntry.COLUMN_NAME_TIMES_S,
                DatabaseContract.PillEntry.COLUMN_NAME_TIMES_M,
                DatabaseContract.PillEntry.COLUMN_NAME_TIMES_T,
                DatabaseContract.PillEntry.COLUMN_NAME_TIMES_W,
                DatabaseContract.PillEntry.COLUMN_NAME_TIMES_R,
                DatabaseContract.PillEntry.COLUMN_NAME_TIMES_F,
                DatabaseContract.PillEntry.COLUMN_NAME_TIMES_Sa,
        };

        //Add the times for each day
        for(int i = 0; i < dayDBContract.length; i++)
        {
            long[] timesAsLongs = pillItem.getTimesForDay(i);

            //If there are times for this day
            if(timesAsLongs != null) {
                StringBuilder timesBuilder = new StringBuilder();

                //Build our comma seperated string from the array
                for (long time : timesAsLongs) {
                    String toAppend = time + ",";
                    timesBuilder.append(toAppend);
                }

                //Get rid of hanging comma
                timesBuilder.deleteCharAt(timesBuilder.length() - 1);

                //Finally add it
                newData.put(dayDBContract[i], timesBuilder.toString());
            }
        }

        //Final data
        newData.put(DatabaseContract.PillEntry.COLUMN_NAME_DURATION, pillItem.getDuration());
        newData.put(DatabaseContract.PillEntry.COLUMN_NAME_UNTIL_DATE, pillItem.getUntilDate());
        newData.put(DatabaseContract.PillEntry.COLUMN_NAME_REFILL_DATE, pillItem.getRefillDate());

        if(pillItem.getPillId() == -1)
            //Insert the entry if none exists with that id
            return db.insert(DatabaseContract.PillEntry.TABLE_NAME, null, newData);
        else
            //If already exists update it
            db.update(DatabaseContract.PillEntry.TABLE_NAME, newData,
                    DatabaseContract.PillEntry._ID + "=" + pillItem.getPillId(), null);
        return pillItem.getPillId();
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

    public PillItem loadPillItemById(long id)
    {
        SQLiteDatabase database = getReadableDatabase();
        Cursor c = database.rawQuery("SELECT * FROM " + DatabaseContract.PillEntry.TABLE_NAME
                + " WHERE " + DatabaseContract.PillEntry._ID + "=" + id, null);

        c.moveToFirst();
        PillItem pillItem = pillItemFromCursor(c);

        return pillItem;
    }

    public List<PillItem> loadPillItems()
    {
        List<PillItem> pillItems = new ArrayList<>();

        //Get all of the rows in the table for appointments
        SQLiteDatabase database = getReadableDatabase();
        Cursor c = database.query(DatabaseContract.PillEntry.TABLE_NAME, null, null, null, null, null, null);

        //Go through each row
        c.moveToFirst();
        while(!c.isAfterLast())
        {
            PillItem newItem = pillItemFromCursor(c);
            pillItems.add(newItem);

            c.moveToNext();
        }

        return pillItems;
    }

    private PillItem pillItemFromCursor(Cursor c)
    {
        PillItem newItem;

        int colIndex = c.getColumnIndexOrThrow(DatabaseContract.PillEntry._ID);
        long pk = c.getLong(colIndex);

        colIndex = c.getColumnIndexOrThrow(DatabaseContract.PillEntry.COLUMN_NAME_PILL_NAME);
        String title = c.getString(colIndex);

        colIndex = c.getColumnIndexOrThrow(DatabaseContract.PillEntry.COLUMN_NAME_INSTR);
        String instr = c.getString(colIndex);

        colIndex = c.getColumnIndexOrThrow(DatabaseContract.PillEntry.COLUMN_NAME_DURATION);
        int duration = c.getInt(colIndex);

        colIndex = c.getColumnIndexOrThrow(DatabaseContract.PillEntry.COLUMN_NAME_UNTIL_DATE);
        long untilDate = c.getLong(colIndex);

        colIndex = c.getColumnIndexOrThrow(DatabaseContract.PillEntry.COLUMN_NAME_REFILL_DATE);
        long refillDate = c.getLong(colIndex);

        newItem = new PillItem(title, instr, duration, untilDate, refillDate);

        String[] dayDBContract = {
                DatabaseContract.PillEntry.COLUMN_NAME_TIMES_S,
                DatabaseContract.PillEntry.COLUMN_NAME_TIMES_M,
                DatabaseContract.PillEntry.COLUMN_NAME_TIMES_T,
                DatabaseContract.PillEntry.COLUMN_NAME_TIMES_W,
                DatabaseContract.PillEntry.COLUMN_NAME_TIMES_R,
                DatabaseContract.PillEntry.COLUMN_NAME_TIMES_F,
                DatabaseContract.PillEntry.COLUMN_NAME_TIMES_Sa,
        };

        //Now set up the times to take for each day
        for(int i = 0; i < dayDBContract.length; i++)
        {
            colIndex = c.getColumnIndexOrThrow(dayDBContract[i]);
            String timesToTake = c.getString(colIndex);
            if(timesToTake != null) {
                String[] timesArray = timesToTake.split(",");
                long[] timesAsLongs = new long[timesArray.length];

                //Convert times into longs and store
                for (int j = 0; j < timesArray.length; j++) {
                    timesAsLongs[j] = Long.parseLong(timesArray[j]);
                }

                //Set the times for the item for that day
                newItem.setTimesForDay(i, timesAsLongs);
            }
        }

        newItem.setPillId(pk);

        return newItem;

    }

    public List<PillItem> loadPillsForDay(int day)
    {
        List<PillItem> pills = new ArrayList<>();
        String dbDayName = "";

        //Get the string for the column name we want to query for
        switch(day)
        {
            case 0:
                dbDayName = DatabaseContract.PillEntry.COLUMN_NAME_TIMES_S;
                break;

            case 1:
                dbDayName = DatabaseContract.PillEntry.COLUMN_NAME_TIMES_M;
                break;

            case 2:
                dbDayName = DatabaseContract.PillEntry.COLUMN_NAME_TIMES_T;
                break;

            case 3:
                dbDayName = DatabaseContract.PillEntry.COLUMN_NAME_TIMES_W;
                break;

            case 4:
                dbDayName = DatabaseContract.PillEntry.COLUMN_NAME_TIMES_R;
                break;

            case 5:
                dbDayName = DatabaseContract.PillEntry.COLUMN_NAME_TIMES_F;
                break;

            case 6:
                dbDayName = DatabaseContract.PillEntry.COLUMN_NAME_TIMES_Sa;
                break;
        }

        SQLiteDatabase database = getReadableDatabase();
        Cursor c = database.rawQuery("SELECT * FROM " + DatabaseContract.PillEntry.TABLE_NAME
                + " WHERE " + dbDayName + " IS NOT NULL", null);

        c.moveToFirst();
        while(!c.isAfterLast())
        {
            PillItem pillItem = pillItemFromCursor(c);
            pills.add(pillItem);
            c.moveToNext();
        }

        return pills;

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
        //Create the appointments table
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

        //Create the pills table
        db.execSQL("CREATE TABLE " + DatabaseContract.PillEntry.TABLE_NAME +
                " (" +
                DatabaseContract.PillEntry._ID + " INTEGER PRIMARY KEY," +
                DatabaseContract.PillEntry.COLUMN_NAME_PILL_NAME + " TEXT," +
                DatabaseContract.PillEntry.COLUMN_NAME_DURATION + " INTEGER," +
                DatabaseContract.PillEntry.COLUMN_NAME_UNTIL_DATE + " INTEGER," +
                DatabaseContract.PillEntry.COLUMN_NAME_REFILL_DATE + " INTEGER," +
                DatabaseContract.PillEntry.COLUMN_NAME_TIMES_S + " TEXT," +
                DatabaseContract.PillEntry.COLUMN_NAME_TIMES_M + " TEXT," +
                DatabaseContract.PillEntry.COLUMN_NAME_TIMES_T + " TEXT," +
                DatabaseContract.PillEntry.COLUMN_NAME_TIMES_W + " TEXT," +
                DatabaseContract.PillEntry.COLUMN_NAME_TIMES_R + " TEXT," +
                DatabaseContract.PillEntry.COLUMN_NAME_TIMES_F + " TEXT," +
                DatabaseContract.PillEntry.COLUMN_NAME_TIMES_Sa + " TEXT," +
                DatabaseContract.PillEntry.COLUMN_NAME_INSTR + " TEXT"
                + ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
