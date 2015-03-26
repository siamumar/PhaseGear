package com.riceucla.mobilelogger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StartMyServiceAtBootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) 
	{
		if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) 
		{
            System.out.println("LiveLab Received BootCompleted");
			Intent serviceIntent = new Intent(context, MainService.class);
            context.startService(serviceIntent);
        }
	}

}
