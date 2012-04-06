package com.rich.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.rich.SettingActivity;
import com.rich.service.NumberFilterService;
import com.rich.service.RegisterSMSService;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
//		Intent service1 = new Intent(context, RegisterSMSService.class);
//		context.startService(service1);
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				"com.rich_preferences", 0);
		boolean app = sharedPreferences.getBoolean("app", false);
		boolean service = sharedPreferences.getBoolean("service", false);
		if (service) {
			Intent i = new Intent(context.getApplicationContext(),
					NumberFilterService.class);
			context.startService(i);
		}
		if (app) {
			Intent i = new Intent(context, SettingActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(i);
		}
	}

}
