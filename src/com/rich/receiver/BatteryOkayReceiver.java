package com.rich.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.SmsManager;

public class BatteryOkayReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				"com.rich_preferences", 0);
		String number = sharedPreferences.getString("number", "");
		if (!number.equals("")) {
			SmsManager smsManager = SmsManager.getDefault();
			smsManager.sendTextMessage(number, null, "Battery Okay!", null,
					null);
		}
	}

}
