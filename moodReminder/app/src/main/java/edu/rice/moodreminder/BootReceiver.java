package edu.rice.moodreminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Automatically re-sets the alarm after device reboot.
 * Requires BOOT_COMPLETED permission for obvious reasons.
 *
 * @author Kevin Lin
 * @since 10/23/2014
 */
public class BootReceiver extends BroadcastReceiver {
    AlarmReceiver alarm = new AlarmReceiver();
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
        {
            alarm.setAlarm(context);
        }
    }
}