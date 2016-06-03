package com.cruzj6.mha.dataManagement;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cruzj6.mha.AppointmentItem;
import com.cruzj6.mha.R;

import java.util.Date;

/**
 * Created by Joey on 5/27/16.
 */
public final class NotificationItemsManager extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intentNew = new Intent(context, NotificationItemsManager.class);

        long apptId = intent.getExtras().getLong("item");
        //Get the item from the database
        AppointmentItem theItem =
                new DatabaseManager(context).loadAppointmentById(apptId);

        //Get the pendring intent
        PendingIntent pIntent = PendingIntent.getActivity(context, (int)theItem.getApptId(), intent, 0);

        // build notification
        // the addAction re-use the same intent to keep the example short
        Notification n  = new Notification.BigTextStyle(
                new Notification.Builder(context)
                .setContentTitle(context.getString(R.string.mha))
                .setContentText("APPOINTMENT REMINDER")
                .setSmallIcon(R.drawable.ic_trash_bin))
                .bigText("APPOINTMENT REMINDER\n" + theItem.getAppointmentTitle() + "\n" +
                new Date(theItem.getApptDate()*1000).toString() +
                        "\nNotes:\n" +
                        theItem.getNotes())
                .build();


        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify((int)theItem.getApptId(), n);

    }

    public static void createApptNotification(long apptItemId, Context context)
    {
        AppointmentItem apptItem = new DatabaseManager(context).loadAppointmentById(apptItemId);
        Intent intent = new Intent(context, NotificationItemsManager.class);
        intent.putExtra("item", apptItem.getApptId());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                (int)apptItem.getApptId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, apptItem.getApptDate()*1000 , pendingIntent);
    }
}
