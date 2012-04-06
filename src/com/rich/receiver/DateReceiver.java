package com.rich.receiver;

import com.rich.util.RichUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DateReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		Log.e("date", "Date changed");
		RichUtils.runRootCommand("reboot");
	}

}
