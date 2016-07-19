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

import com.cruzj6.mha.dataManagement.DatabaseManager;
import com.cruzj6.mha.models.AppointmentItem;
import com.cruzj6.mha.R;
import com.cruzj6.mha.models.PillItem;
import com.cruzj6.mha.models.SimpleTimeItem;
import com.cruzj6.mha.models.TimesPerDayManager;
import com.cruzj6.mha.models.TimesPerDayManagerItem;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Joey on 5/27/16.
 */
public final class NotificationItemsManager extends BroadcastReceiver {

    private final static String TAG = "NotificationItems";
    private final static int NTYPE_APPT = 2;
    private final static int NTYPE_MED = 3;
    private final static String ACTION_WAIT = "w";
    private final static String ACTION_TOOK = "t";
    private final static String ACTION_SKIP = "s";


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
            } else if (act.equals(ACTION_TOOK)) {
                Log.v(TAG, "Took Pill");
            } else if (act.equals(ACTION_WAIT)) {
                Log.v(TAG, "Wait Pill");
            }
            AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                    (int)extras.getLong("item"), intent,PendingIntent.FLAG_UPDATE_CURRENT);
            mgr.cancel(pendingIntent);
            pendingIntent.cancel();
            NotificationManager nmgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            nmgr.cancel((int)extras.getLong("item"));
        }

        switch(ntype){
            case NTYPE_APPT:
                long apptId = intent.getExtras().getLong("item");

                //Get the item from the database
                AppointmentItem theItem = new DatabaseManager(context).loadAppointmentById(apptId);
                SimpleDateFormat f = new SimpleDateFormat("MM/dd hh:mm aaa");

                // build notification
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
                break;

            case NTYPE_MED:
                long pillId = intent.getExtras().getLong("item");
                PillItem pillItem = new DatabaseManager(context).loadPillItemById(pillId);

                //Took it option
                Intent tookReceive = new Intent(context, NotificationItemsManager.class);
                tookReceive.putExtra("item", pillItem.getPillId());
                tookReceive.setAction(ACTION_TOOK);
                PendingIntent pendingIntentTook = PendingIntent.getBroadcast(context,
                        (int)pillItem.getPillId(), tookReceive, PendingIntent.FLAG_UPDATE_CURRENT);

                //Wait it option
                Intent waitReceive = new Intent(context, NotificationItemsManager.class);
                waitReceive.putExtra("item", pillItem.getPillId());
                waitReceive.setAction(ACTION_WAIT);
                PendingIntent pendingIntentWait = PendingIntent.getBroadcast(context,
                        (int)pillItem.getPillId(), waitReceive, PendingIntent.FLAG_UPDATE_CURRENT);

                //Skip it option
                Intent skipReceive = new Intent(context, NotificationItemsManager.class);
                skipReceive.putExtra("item", pillItem.getPillId());
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
        Intent intent = new Intent(context, NotificationItemsManager.class);
        intent.putExtra("item", apptItem.getApptId());
        intent.putExtra("type", NTYPE_APPT);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                (int)apptItem.getApptId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, apptItem.getApptDate()*1000 , pendingIntent);
    }

    public static void createMedNotification(long pillItemId, Context context)
    {
        PillItem pillItem = new DatabaseManager(context).loadPillItemById(pillItemId);

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
        for(TimesPerDayManagerItem dayItem : timesManager.getTimesPerDay())
        {
            //Each time for each day
            for(SimpleTimeItem timeItem : dayItem.getTimesList())
            {
                Bundle extras = new Bundle();
                Intent intent = new Intent(context, NotificationItemsManager.class);

                //Data to send when notifying
                extras.putLong("item", pillItem.getPillId());
                extras.putInt("type", NTYPE_MED);
                extras.putInt("day", dayItem.getDay().getNumVal());
                extras.putSerializable("timeItem", timeItem);
                intent.putExtras(extras);

                //Get the request code
                int reqCode = getUIDReqCodePillAlarm((int)pillItem.getPillId(),
                        dayItem.getDay().getNumVal(), timeItem);

                //If no error make the alarm
                if(reqCode != -1) {
                    //Create our intent with unique request code
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                            reqCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                    //Set the alarm
                    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    alarmManager.set(AlarmManager.RTC_WAKEUP, new Date().getTime(), pendingIntent);
                }
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
