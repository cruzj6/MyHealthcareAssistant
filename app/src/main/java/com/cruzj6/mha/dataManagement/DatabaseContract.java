package com.cruzj6.mha.dataManagement;

import android.provider.BaseColumns;

/**
 * Created by Joey on 5/24/16.
 */
public final class DatabaseContract {
    public DatabaseContract()
    {

    }

    /**
     *Contract with the database for each appointment row
     */
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

    /**
     *     Contract with the database for each pill row
     */
    public static abstract class PillEntry implements BaseColumns
    {
        //Basic info
        public static final String TABLE_NAME = "pills";
        public static final String COLUMN_NAME_PILL_NAME = "name";
        public static final String COLUMN_NAME_INSTR = "instructions";

        //Times for each day, comma separated
        public static final String COLUMN_NAME_TIMES_S = "timessun";
        public static final String COLUMN_NAME_TIMES_M = "timesmon";
        public static final String COLUMN_NAME_TIMES_T = "timestue";
        public static final String COLUMN_NAME_TIMES_W = "timeswed";
        public static final String COLUMN_NAME_TIMES_R = "timesthu";
        public static final String COLUMN_NAME_TIMES_F = "timesfri";
        public static final String COLUMN_NAME_TIMES_Sa = "timessat";

        //Dates and times
        public static final String COLUMN_NAME_DURATION = "takeduration";
        public static final String COLUMN_NAME_UNTIL_DATE = "untildate";
        public static final String COLUMN_NAME_REFILL_DATE = "refilldate";
    }

    public static abstract class MissedPillEntry implements BaseColumns
    {
        public static final String TABLE_NAME = "missedpills";

        public static final String COLUMN_NAME_PILL_NAME = "name";
        public static final String COLUMN_NAME_TIME_MISSED = "timemissed";
    }
}
