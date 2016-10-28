package com.felixtian.uwgpa;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import java.util.Date;

public class GradeNotificationReceiver extends WakefulBroadcastReceiver {
    public GradeNotificationReceiver() {
    }
    public static void initialize(Context context,long quartersToTrigger) {
        Intent notificationIntent = new Intent(context, GradeNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime(),quartersToTrigger*AlarmManager.INTERVAL_FIFTEEN_MINUTES,pendingIntent);
    }
    public static void cancel(Context context){
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent notificationIntent = new Intent(context, GradeNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Intent serviceIntent = null;
        Log.i("Receiver", "onReceive received");
        serviceIntent = GradeNotificationService.prepareIntentPoll(context);
        if (serviceIntent != null) {
            startWakefulService(context, serviceIntent);
        }
    }
}
