package com.cruzj6.mha.dataManagement;

import android.provider.BaseColumns;

/**
 * Created by Joey on 5/24/16.
 */
public final class DatabaseContract {
    public DatabaseContract()
    {

    }

    //Contract with the database for each appointment row
    public static abstract class AppointmentEntry implements BaseColumns
    {
        public static final String TABLE_NAME = "appointments";
        public static final String COLUMN_NAME_DR_NAME = "doctorname";
        public static final String COLUMN_NAME_APPT_DATE = "apptdate";
        public static final String COLUMN_NAME_REQ_LABWORK = "haslabwork";
        public static final String COLUMN_NAME_LABWORK_DAYS_BEFORE = "labworkdate";
        public static final String COLUMN_NAME_NOTES = "notes";
        public static final String COLUMN_NAME_REMIND_DAYS_BEFORE = "daysbeforeremind";

    }
    //TODO: For pills etc

    public static abstract class PillEntry implements BaseColumns
    {
        public static final String TABLE_NAME = "pills";
        public static final String COLUMN_NAME_DR_NAME = "doctorname";
        public static final String COLUMN_NAME_APPT_DATE = "apptdate";
        public static final String COLUMN_NAME_REQ_LABWORK = "haslabwork";
        public static final String COLUMN_NAME_LABWORK_DAYS_BEFORE = "labworkdate";
        public static final String COLUMN_NAME_NOTES = "notes";
        public static final String COLUMN_NAME_REMIND_DAYS_BEFORE = "daysbeforeremind";
    }
}
