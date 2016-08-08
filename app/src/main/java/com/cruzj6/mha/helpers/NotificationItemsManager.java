package com.cruzj6.mha.helpers;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.cruzj6.mha.activities.RefillRxActivity;
import com.cruzj6.mha.dataManagement.DatabaseManager;
import com.cruzj6.mha.models.AppointmentItem;
import com.cruzj6.mha.R;
import com.cruzj6.mha.models.PillItem;
import com.cruzj6.mha.models.SimpleTimeItem;
import com.cruzj6.mha.models.TimesPerDayManager;
import com.cruzj6.mha.models.TimesPerDayManagerItem;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Joey on 5/27/16.
 */
public final class NotificationItemsManager extends BroadcastReceiver {

    private final static String TAG = "NotificationItems";
    private final static int NTYPE_APPT = 2;
    private final static int NTYPE_MED = 3;
    private final static int NTYPE_MED_REFILL = 4;
    private final static int NTYPE_APPT_B4 = 5;
    private final static int NTYPE_APPT_LABWORK = 6;
    private final static String ACTION_WAIT = "w";
    private final static String ACTION_TOOK = "t";
    private final static String ACTION_SKIP = "s";
    private final static String ACTION_REFILL_NOW = "rn";


    @Override
    public void onReceive(Context context, Intent intent) {
        int ntype = -1;
        Intent intentNew = new Intent(context, NotificationItemsManager.class);
        Bundle extras =  intent.getExtras();
        if (extras != null) ntype = extras.getInt("type");
        String act = intent.getAction();
        if(act != null) {
            if (act.equals(ACTION_SKIP)) {
                Log.v(TAG, "Skip Pill");

                //Add missed medication to the database
                DatabaseManager dbManager = new DatabaseManager(context);
                PillItem thePillItem = dbManager.loadPillItemById(((Long)extras.get("item")).intValue());
                dbManager.addMissedPill(thePillItem);

            } else if (act.equals(ACTION_TOOK)) {
                Log.v(TAG, "Took Pill");
            } else if (act.equals(ACTION_WAIT)) {
                Log.v(TAG, "Wait Pill");
                int reqCode = (int)extras.get("reqCode");
               // PendingIntent pendingIntent = new PendingIntent(context, reqCode, )

                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                //alarmManager.set(AlarmManager.RTC_WAKEUP, millis, pendingIntent);

            } else if(act.equals(ACTION_REFILL_NOW))
            {
                //Open refill activity for the user to refill their meds
                Intent i = new Intent(context.getApplicationContext(), RefillRxActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }
            try {
                AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                        (int) extras.getLong("item"), intent, PendingIntent.FLAG_UPDATE_CURRENT);
                mgr.cancel(pendingIntent);
                pendingIntent.cancel();
                NotificationManager nmgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                nmgr.cancel((int) extras.getLong("item"));
            }
            catch(NullPointerException e)
            {
                Log.e(TAG, "No item sent in bundle for intent notification: " + e.getMessage());
            }
        }

        switch(ntype){
            case NTYPE_APPT:
                long apptId = intent.getExtras().getLong("item");

                //Get the item from the database
                AppointmentItem theItem = new DatabaseManager(context).loadAppointmentById(apptId);
                SimpleDateFormat f = new SimpleDateFormat("MM/dd hh:mm aaa");

                // Build notification
                // the addAction re-use the same intent to keep the example short
                Notification n  = new Notification.BigTextStyle(
                        new Notification.Builder(context)
                                .setContentTitle(context.getString(R.string.mha))
                                .setContentText("APPOINTMENT REMINDER")
                                .setSmallIcon(R.drawable.ic_trash_bin))
                        .bigText("APPOINTMENT REMINDER\n"
                                + theItem.getAppointmentTitle() + "\n" +
                                f.format(new Date(theItem.getApptDate()*1000)) +
                                "\nNotes:\n" +
                                theItem.getNotes())
                        .build();

                NotificationManager notificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify((int)theItem.getApptId(), n);

                //Remove it from the database
                new DatabaseManager(context).deleteAppointment(theItem.getApptId());
                break;

            case NTYPE_MED:
                long pillId = intent.getExtras().getLong("item");
                int day = intent.getExtras().getInt("day");
                SimpleTimeItem timeItem = (SimpleTimeItem) intent.getExtras().getSerializable("timeItem");
                PillItem pillItem = new DatabaseManager(context).loadPillItemById(pillId);

                //Took it option
                Intent tookReceive = new Intent(context, NotificationItemsManager.class);
                tookReceive.putExtra("item", pillItem.getPillId());
                tookReceive.putExtra("reqCode", (int)extras.get("reqCode"));
                tookReceive.setAction(ACTION_TOOK);
                PendingIntent pendingIntentTook = PendingIntent.getBroadcast(context,
                        (int)pillItem.getPillId(), tookReceive, PendingIntent.FLAG_UPDATE_CURRENT);

                //Wait it option
                Intent waitReceive = new Intent(context, NotificationItemsManager.class);
                waitReceive.putExtra("item", pillItem.getPillId());
                waitReceive.putExtra("reqCode", (int)extras.get("reqCode"));
                waitReceive.setAction(ACTION_WAIT);
                PendingIntent pendingIntentWait = PendingIntent.getBroadcast(context,
                        (int)pillItem.getPillId(), waitReceive, PendingIntent.FLAG_UPDATE_CURRENT);

                //Skip it option
                Intent skipReceive = new Intent(context, NotificationItemsManager.class);
                skipReceive.putExtra("item", pillItem.getPillId());
                skipReceive.putExtra("reqCode", (int)extras.get("reqCode"));
                skipReceive.setAction(ACTION_SKIP);
                PendingIntent pendingIntentSkip = PendingIntent.getBroadcast(context,
                        (int)pillItem.getPillId(), skipReceive, PendingIntent.FLAG_UPDATE_CURRENT);

                // build notification
                // the addAction re-use the same intent to keep the example short
                Notification pillNo  = new NotificationCompat.BigTextStyle(
                        new NotificationCompat.Builder(context)
                                .setContentTitle(context.getString(R.string.mha))
                                .setContentText("MEDICATION REMINDER")
                                .setSmallIcon(R.drawable.ic_trash_bin)
                                .addAction(new NotificationCompat.Action(R.drawable.ic_done_24dp, "Took It", pendingIntentTook))
                                .addAction(new NotificationCompat.Action(R.drawable.ic_alarm_add_white_24dp, "Snooze", pendingIntentWait))
                                .addAction(new NotificationCompat.Action(R.drawable.ic_trash_bin, "Skip It", pendingIntentSkip)))
                        .bigText("MEDICATION REMINDER\n"
                                + pillItem.getTitle() + "\n" +
                                "\nNotes:\n" +
                                pillItem.getInstr())
                        .build();

                NotificationManagerCompat nManager =
                        (NotificationManagerCompat) NotificationManagerCompat.from(context);
                nManager.notify((int)pillItem.getPillId(), pillNo);

                //Reset alarm for the next week
                setPillAlarmForTimeDay(context, pillItem, day, timeItem);
                break;

            case NTYPE_APPT_B4:
                AppointmentItem apptItem = new DatabaseManager(context).loadAppointmentById(extras.getLong("item"));
                Notification apptBeforeNo  = new NotificationCompat.BigTextStyle(
                        new NotificationCompat.Builder(context)
                                .setContentTitle(context.getString(R.string.mha) + " Reminder")
                                .setContentText("Reminder about Appointment: " + apptItem.getAppointmentTitle())
                                .setSmallIcon(R.drawable.ic_assignment_24dp))
                        .bigText("Appointment: " + apptItem.getAppointmentTitle() +
                                " in " + apptItem.getRemindDaysBefore() + " days")
                        .build();

                NotificationManagerCompat noManage =
                        (NotificationManagerCompat) NotificationManagerCompat.from(context);
                noManage.notify((int)apptItem.getApptId(), apptBeforeNo);
                break;

            case NTYPE_MED_REFILL:
                PillItem pItem = new DatabaseManager(context).loadPillItemById(extras.getLong("item"));
                //Refill it now action
                Intent refillNow = new Intent(context, NotificationItemsManager.class);
                refillNow.putExtra("item", pItem.getPillId());
                refillNow.setAction(ACTION_REFILL_NOW);
                PendingIntent pendingIntentRefill = PendingIntent.getBroadcast(context,
                        (int)pItem.getPillId(), refillNow, PendingIntent.FLAG_UPDATE_CURRENT);

                Notification refillNo  = new NotificationCompat.BigTextStyle(
                        new NotificationCompat.Builder(context)
                                .setContentTitle(context.getString(R.string.mha))
                                .setContentText("REFILL MEDICATION REMINDER")
                                .setSmallIcon(R.drawable.ic_assignment_24dp)
                                .addAction(new NotificationCompat.Action(R.drawable.ic_done_24dp, "Refill Now", pendingIntentRefill)))
                               .bigText("Reminder to refill Rx: " + pItem.getTitle())
                        .build();

                NotificationManagerCompat nManage =
                        (NotificationManagerCompat) NotificationManagerCompat.from(context);
                nManage.notify((int)pItem.getPillId(), refillNo);
                break;
            case NTYPE_APPT_LABWORK:
                AppointmentItem aItem = new DatabaseManager(context).loadAppointmentById(extras.getLong("item"));
                Notification apptLabNo  = new NotificationCompat.BigTextStyle(
                        new NotificationCompat.Builder(context)
                                .setContentTitle(context.getString(R.string.mha) + " Labwork Reminder")
                                .setContentText("Reminder about Labwork: " + aItem.getAppointmentTitle())
                                .setSmallIcon(R.drawable.ic_assignment_24dp))
                        .bigText("Get Labwork done for: " + aItem.getAppointmentTitle())
                        .build();

                NotificationManagerCompat notManage =
                        (NotificationManagerCompat) NotificationManagerCompat.from(context);
                notManage.notify((int)aItem.getApptId(), apptLabNo);
                break;
        }
    }

    /**
     * Create an appointment alarm to be sent with pending intent
     * @param apptItemId id of appt to create alarms for
     * @param context
     */
    public static void createApptNotification(long apptItemId, Context context)
    {
        AppointmentItem apptItem = new DatabaseManager(context).loadAppointmentById(apptItemId);

        //Apointment at time of appt
        Intent intent = new Intent(context, NotificationItemsManager.class);
        intent.putExtra("item", apptItem.getApptId());
        intent.putExtra("type", NTYPE_APPT);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                (int)apptItem.getApptId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, apptItem.getApptDate()*1000 , pendingIntent);


        if(apptItem.getRemindDaysBefore() > 0) {
            //Now do days before reminder
            Intent intentB4 = new Intent(context, NotificationItemsManager.class);
            intentB4.putExtra("item", apptItem.getApptId());
            intentB4.putExtra("type", NTYPE_APPT_B4);

            //Intent for remind days before
            PendingIntent pIntB4 = PendingIntent.getBroadcast(context,
                    (int) (apptItem.getApptId() * apptItem.getRemindDaysBefore()), intentB4,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            //Get the date however many days before
            DateTime dt = new DateTime(new Date(apptItem.getApptDate() * 1000));
            dt = dt.plusDays((int) (-(apptItem.getRemindDaysBefore())));

            //Set the alarm
            alarmManager.set(AlarmManager.RTC_WAKEUP, dt.getMillis(), pIntB4);
        }

        if(apptItem.getLabworkDaysBefore() > 0) {
            //Now do Labwork reminder
            Intent intentLab = new Intent(context, NotificationItemsManager.class);
            intentLab.putExtra("item", apptItem.getApptId());
            intentLab.putExtra("type", NTYPE_APPT_LABWORK);

            //Intent for labwork notification
            PendingIntent pIntLab = PendingIntent.getBroadcast(context,
                    (int) -(apptItem.getApptId() * apptItem.getLabworkDaysBefore()), intentLab,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            //Get the date however many days before the labwork shoule be done
            DateTime dt = new DateTime(new Date(apptItem.getApptDate() * 1000));
            dt = dt.plusDays((int) (-(apptItem.getLabworkDaysBefore())));

            //Set the alarm
            alarmManager.set(AlarmManager.RTC_WAKEUP, dt.getMillis(), pIntLab);
        }
    }

    /**
     * Create notifications for the upcoming weeks pills based on the pillItem the id is given for
     * @param pillItemId
     * @param context
     */
    public static void createMedNotification(long pillItemId, Context context)
    {
        PillItem pillItem = new DatabaseManager(context).loadPillItemById(pillItemId);

        //Set alarm for the refill date
        Intent intent = new Intent(context, NotificationItemsManager.class);
        intent.putExtra("item", pillItem.getPillId());
        intent.putExtra("type", NTYPE_MED_REFILL);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                (int)pillItem.getPillId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, pillItem.getRefillDate()*1000, pendingIntent);

        //Set the alarms for the week
        pillsSetAlarmsUpcomingWeek(context, pillItem);
    }

    /**
     * Sets the alarms for the upcoming week for a pill item
     * @param context
     * @param pillItem
     */
    private static void pillsSetAlarmsUpcomingWeek(Context context, PillItem pillItem)
    {
        //Get times manager for the item
        TimesPerDayManager timesManager = pillItem.getTimesManager();

        //Alarm for each day's times
        if(timesManager != null) {
            for (TimesPerDayManagerItem dayItem : timesManager.getTimesPerDay()) {
                //Each time for each day
                for (SimpleTimeItem timeItem : dayItem.getTimesList()) {
                    //Set the alarm for next day/time
                    setPillAlarmForTimeDay(context, pillItem, dayItem.getDay().getNumVal(), timeItem);
                }
            }
        }
    }

    /**
     * Sets an alarm for the given nearest day and time that match the day and time items passed
     * @param context
     * @param day Item containing info about the day of the week to set for
     * @param timeItem Item containing info about the time of the day to set for
     */
    private static void setPillAlarmForTimeDay(Context context, PillItem pillItem,
                                               int day, SimpleTimeItem timeItem)
    {
        Bundle extras = new Bundle();
        Intent intent = new Intent(context, NotificationItemsManager.class);

        //Get the request code
        int reqCode = getUIDReqCodePillAlarm((int)pillItem.getPillId(),
                day, timeItem);

        //Data to send when notifying
        extras.putLong("item", pillItem.getPillId());
        extras.putInt("type", NTYPE_MED);
        extras.putInt("day", day);
        extras.putInt("reqCode", reqCode);
        extras.putSerializable("timeItem", timeItem);
        intent.putExtras(extras);

        //If no error make the alarm
        if(reqCode != -1) {
            //Create our intent with unique request code
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                    reqCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            int jodaDay = day == 0 ? 7 : day;
            DateTime now = new DateTime(new Date());

            //M = 1, Sun = 7 for JodaTime library
            DateTime wDay = now.withDayOfWeek(jodaDay);

            //If day/time already happened this week, add a week
            if(jodaDay < new LocalDate().getDayOfWeek() || timeItem.getHour24() < wDay.getHourOfDay()
                    || wDay.getHourOfDay() == timeItem.getHour24() &&
                    wDay.getMinuteOfHour() >= timeItem.getMins())
            {
                wDay = wDay.plusWeeks(1);
            }

            //Set up the hour and mins
            wDay = wDay.withHourOfDay(timeItem.getHour24());
            wDay = wDay.withMinuteOfHour(timeItem.getMins());

            //Set the alarm
            long millis = wDay.getMillis();
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, millis, pendingIntent);
        }
    }

    public static void removeApptNotifications(AppointmentItem item, Context context)
    {
        //Cancel appt notification
        AlarmManager am = (AlarmManager) context.getSystemService(context.getApplicationContext().ALARM_SERVICE);
        Intent i = new Intent(context.getApplicationContext(), NotificationItemsManager.class);
        PendingIntent p = PendingIntent.getBroadcast(context.getApplicationContext(), (int)item.getApptId(), i, 0);
        am.cancel(p);
        p.cancel();

        //Cancel days before reminder
        p = PendingIntent.getBroadcast(context.getApplicationContext(),
                (int)(item.getRemindDaysBefore() * item.getApptDate()), i, 0);
        am.cancel(p);
        p.cancel();

        //Cancel Labwork reminder
        p = PendingIntent.getBroadcast(context.getApplicationContext(),
                (int)(item.getRemindDaysBefore() * item.getLabworkDaysBefore()), i, 0);
        am.cancel(p);
        p.cancel();
    }

    /**
     *  Deletes a pending alarm by passing it the alarms old parameters, use when a time is removed
     * @param pillItem
     * @param context
     */
    public static void removeOldPillNotifications(PillItem pillItem, Context context)
    {
        for(TimesPerDayManagerItem dayItem : pillItem.getTimesManager().getTimesPerDay()) {
            for(SimpleTimeItem timeItem : dayItem.getTimesList()) {
                int oldReqCode = getUIDReqCodePillAlarm((int)pillItem.getPillId(),
                        dayItem.getDay().getNumVal(), timeItem);

                AlarmManager am = (AlarmManager) context.getSystemService(context.getApplicationContext().ALARM_SERVICE);
                Intent i = new Intent(context.getApplicationContext(), NotificationItemsManager.class);
                PendingIntent p = PendingIntent.getBroadcast(context.getApplicationContext(), oldReqCode, i, 0);
                am.cancel(p);
                p.cancel();
            }
        }

    }

    /**
     * Get a request code for the given pill, day and time for creating an alarm/notification
     * @param pillId
     * @param day
     * @param timeItem
     * @return returns request code or -1 on error
     */
    private static int getUIDReqCodePillAlarm(int pillId, int day, SimpleTimeItem timeItem)
    {
        //Build request code as string
        String reqCode = pillId + "";
        reqCode += day + "";
        reqCode += timeItem.getHour24() + "" + timeItem.getMins();

        try {
            int reqCodeInt = Integer.parseInt(reqCode);
            return reqCodeInt;
        }
        catch(NumberFormatException e)
        {
            Log.e(TAG, "getUIDReqCodePillAlarm(): " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }
}
