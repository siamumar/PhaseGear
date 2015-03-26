package edu.rice.moodreminder;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.content.WakefulBroadcastReceiver;

import java.util.Calendar;

/**
 * Receiver that controls behavior of the application after the alarm goes off.
 *
 * @author Kevin Lin
 * @since 10/23/2014
 */
public class AlarmReceiver extends WakefulBroadcastReceiver {

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    /**
     * Starts the AlarmService when it receives the alarm.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, AlarmService.class);
        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, service);
    }

    /**
     * Sets an alarm for 8 PM daily. Called from MainActivity.class.
     *
     * @param context: application context
     */
    public void setAlarm(Context context) {
        alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        // Currently, the alarm is set for *precisely* 8 PM. The tradeoff is potentially higher battery consumption.
        // Use setInexactRepeating() for an alarm that will go off at *approximately* 8 PM.
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, getCalendarAtTime(Config.NOTIFICATION_HOUR, Config.NOTIFICATION_MINUTE).getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);

        // Re-set the alarm after device reboot.
        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    /**
     * Creates a Calendar object at the given hour and minute.
     *
     * @param hour: Integer representing the hour for the Calendar (24-hour time)
     * @param minute: Integer representing the minute for the Calendar
     * @return Calendar object at hour and time
     */
    public Calendar getCalendarAtTime(int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        return calendar;
    }
}

