package com.cruzj6.mha.dataManagement;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;
import android.util.Log;

import com.cruzj6.mha.helpers.NotificationItemsManager;
import com.cruzj6.mha.models.AppointmentItem;
import com.cruzj6.mha.models.Days;
import com.cruzj6.mha.models.MissedPillContainer;
import com.cruzj6.mha.models.PillItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Joey on 5/23/16.
 * Class for managing the database of appointments and medication using the DatabaseContract
 */
public class DatabaseManager extends SQLiteOpenHelper{

    private final static String TAG = "DatabaseManager";
    public final static String DATABASE_NAME = "MedApp.db";
    public final static int DATABASE_VER = 4;
    private Context context;

    public DatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VER);
        this.context = context;
    }

    /**
     * Removes an Appointment from the database
     */
    public void deleteAppointment(long apptId)
    {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(DatabaseContract.AppointmentEntry.TABLE_NAME,
                DatabaseContract.AppointmentEntry._ID + "=" + apptId, null);

        db.close();
    }

    /**
     * Removes a pill from the database
     * @param pillId
     */
    public void deletePill(long pillId)
    {
        PillItem pi = loadPillItemById(pillId);
        //Get rid of the notifications
        NotificationItemsManager.removeOldPillNotifications(pi, context);
        SQLiteDatabase db = getWritableDatabase();

        db.delete(DatabaseContract.PillEntry.TABLE_NAME,
                DatabaseContract.PillEntry._ID + "=" + pillId, null);

        db.close();
    }

    public List<MissedPillContainer> loadAllMissedPills()
    {
        List<MissedPillContainer> loadedItems = new ArrayList<>();

        //Get all of the rows in the table for missed pills/meds
        SQLiteDatabase database = getReadableDatabase();
        Cursor c = database.query(DatabaseContract.MissedPillEntry.TABLE_NAME, null, null, null, null, null, null);

        //Go through each row
        c.moveToFirst();
        while(!c.isAfterLast())
        {
            try {
                MissedPillContainer newItem = missedPillContainerFromCursor(c);
                loadedItems.add(newItem);
            }
            catch(IllegalArgumentException e)
            {
                Log.e(TAG, "Could not load missed pill from DB: loadAppointmentItems(): \n" + e.getMessage());
                e.printStackTrace();
            }

            c.moveToNext();
        }

        database.close();
        return loadedItems;
    }

    private MissedPillContainer missedPillContainerFromCursor(Cursor c)
    {
        MissedPillContainer newItem;

        int colIndex = c.getColumnIndexOrThrow(DatabaseContract.MissedPillEntry._ID);
        long pk = c.getLong(colIndex);

        colIndex = c.getColumnIndexOrThrow(DatabaseContract.MissedPillEntry.COLUMN_NAME_TIME_MISSED);
        long missedDate = c.getLong(colIndex);

        colIndex = c.getColumnIndexOrThrow(DatabaseContract.MissedPillEntry.COLUMN_NAME_PILL_NAME);
        String pillName = c.getString(colIndex);

        newItem = new MissedPillContainer(pillName, pk, missedDate);

        return newItem;
    }

    /**
     * Adds a missed medication to the database (missed time is when this is called)
     * @param missedPill
     * @return Returns DB table id of the missed pill instance (Separate from pillId!!)
     */
    public long addMissedPill(PillItem missedPill)
    {
        SQLiteDatabase db = getWritableDatabase();
        Date missedTime = new Date();

        //Insert into database
        ContentValues newData = new ContentValues();
        newData.put(DatabaseContract.MissedPillEntry.COLUMN_NAME_PILL_NAME, missedPill.getTitle());
        newData.put(DatabaseContract.MissedPillEntry.COLUMN_NAME_TIME_MISSED, missedTime.getTime()/1000);
        long id = db.insert(DatabaseContract.MissedPillEntry.TABLE_NAME, null, newData);
        db.close();

        return id;
    }

    public void removeMissedPill(long missedPillId)
    {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(DatabaseContract.MissedPillEntry.TABLE_NAME,
                DatabaseContract.MissedPillEntry._ID + "=" + missedPillId, null);
        db.close();
    }

    /**
     * Adds(if no id in AppointmentItem) or modifies an appointment in the database
     * @param apptItem
     * @return the appointment's id
     */
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
        db.close();
        return apptItem.getApptId();
    }

    /**
     *Adds(If id in pillItem) or modifies a medication in the database
     * @param pillItem
     * @return id of the medication
     */
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
            else newData.putNull(dayDBContract[i]);
        }

        //Final data
        if(!pillItem.getIsEndByDate()) {
            newData.put(DatabaseContract.PillEntry.COLUMN_NAME_UNTIL_DATE, -1);
            newData.put(DatabaseContract.PillEntry.COLUMN_NAME_DURATION, pillItem.getDuration());
        }
        else {
            newData.put(DatabaseContract.PillEntry.COLUMN_NAME_DURATION, -1);
            newData.put(DatabaseContract.PillEntry.COLUMN_NAME_UNTIL_DATE, pillItem.getUntilDate());
        }
        newData.put(DatabaseContract.PillEntry.COLUMN_NAME_REFILL_DATE, pillItem.getRefillDate());

        if(pillItem.getPillId() == -1)
            //Insert the entry if none exists with that id
            return db.insert(DatabaseContract.PillEntry.TABLE_NAME, null, newData);
        else
            //If already exists update it
            db.update(DatabaseContract.PillEntry.TABLE_NAME, newData,
                    DatabaseContract.PillEntry._ID + "=" + pillItem.getPillId(), null);

        db.close();
        return pillItem.getPillId();
    }

    /**
     *Load items from the appointments table in to database into
     * AppointmentItem's and return the list
     * @return list of all AppointmentItems in DB
     */
    public List<AppointmentItem> loadAppointmentItems()
    {
        List<AppointmentItem> loadedItems = new ArrayList<>();

        //Get all of the rows in the table for appointments
        SQLiteDatabase database = getReadableDatabase();
        Cursor c = database.query(DatabaseContract.AppointmentEntry.TABLE_NAME, null, null, null, null, null, null);

        //Go through each row
        c.moveToFirst();
        while(!c.isAfterLast())
        {
            try {
                AppointmentItem newItem = appointmentItemFromCursor(c);
                loadedItems.add(newItem);
            }
            catch(IllegalArgumentException e)
            {
                Log.e(TAG, "Could not load appointment from DB: loadAppointmentItems(): \n" + e.getMessage());
                e.printStackTrace();
            }

            c.moveToNext();
        }

        database.close();
        return loadedItems;
    }

    /**
     * Gets the AppointmentItem of the appointment with the given id
     * @param id
     * @return The AppointmentItem for the appointment with the id
     */
    public AppointmentItem loadAppointmentById(Long id)
    {
        SQLiteDatabase database = getReadableDatabase();
        Cursor c = database.rawQuery("SELECT * FROM " + DatabaseContract.AppointmentEntry.TABLE_NAME
                + " WHERE " + DatabaseContract.AppointmentEntry._ID + "=" + id, null);

        c.moveToFirst();
        AppointmentItem apptItem = appointmentItemFromCursor(c);
        database.close();
        return apptItem;
    }

    /**
     * Loads a pillItem from DB by its id
     * @param id
     * @return loaded PillItem
     */
    public PillItem loadPillItemById(long id)
    {
        SQLiteDatabase database = getReadableDatabase();
        Cursor c = database.rawQuery("SELECT * FROM " + DatabaseContract.PillEntry.TABLE_NAME
                + " WHERE " + DatabaseContract.PillEntry._ID + "=" + id, null);

        c.moveToFirst();
        try {
            PillItem pillItem = pillItemFromCursor(c);
            database.close();
            return pillItem;
        }
        catch(IllegalArgumentException e)
        {
            Log.e(TAG, "Could not load pillItem from DB: loadPillItemById(): \n" + e.getMessage());
            e.printStackTrace();
        }

        database.close();
        return null;
    }

    /**
     * Loads all PillItems(Medications) from the database
     * @return list of PillItems for all medications in the database
     */
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
            try {
                PillItem newItem = pillItemFromCursor(c);
                pillItems.add(newItem);
            }
            catch(IllegalArgumentException e)
            {
                Log.e(TAG, "Could not load PillItem from DB: loadPillItems(): \n" + e.getMessage());
                e.printStackTrace();
            }

            c.moveToNext();
        }
        database.close();

        return pillItems;
    }

    private PillItem pillItemFromCursor(Cursor c)
    {
        PillItem newItem;

        try {
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

            if (duration == -1)
                newItem = new PillItem(title, instr, untilDate, refillDate);
            else
                newItem = new PillItem(title, instr, duration, refillDate);

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
            for (int i = 0; i < dayDBContract.length; i++) {
                colIndex = c.getColumnIndexOrThrow(dayDBContract[i]);
                String timesToTake = c.getString(colIndex);
                if (timesToTake != null && !timesToTake.equals("")) {
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
        }catch (CursorIndexOutOfBoundsException e)
        {
            e.printStackTrace();
            return null;
        }
        return newItem;
    }

    /**
     * Load all of the medications for a specific day
     * @param day
     * @return List of PillItems for the specified day
     */
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
            try {
                PillItem pillItem = pillItemFromCursor(c);
                pills.add(pillItem);
            }
            catch(IllegalArgumentException e)
            {
                Log.e(TAG, "Could not load PillItem from DB: loadPillsForDay(): \n" + e.getMessage());
                e.printStackTrace();
            }
            c.moveToNext();
        }

        database.close();

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

        //Missed Pills table
        db.execSQL("CREATE TABLE " + DatabaseContract.MissedPillEntry.TABLE_NAME +
            " (" +
                DatabaseContract.MissedPillEntry._ID + " INTEGER PRIMARY KEY," +
                DatabaseContract.MissedPillEntry.COLUMN_NAME_PILL_NAME + " TEXT," +
                DatabaseContract.MissedPillEntry.COLUMN_NAME_TIME_MISSED + " INTEGER" +
            ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if(oldVersion < 4) {
            //Missed Pills table
            //Missed Pills table
            db.execSQL("CREATE TABLE " + DatabaseContract.MissedPillEntry.TABLE_NAME +
                    " (" +
                    DatabaseContract.MissedPillEntry._ID + " INTEGER PRIMARY KEY," +
                    DatabaseContract.MissedPillEntry.COLUMN_NAME_PILL_NAME + " TEXT," +
                    DatabaseContract.MissedPillEntry.COLUMN_NAME_TIME_MISSED + " INTEGER" +
                    ")"
            );
        }
    }
}
