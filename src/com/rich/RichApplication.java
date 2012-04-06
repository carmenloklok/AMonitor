package com.rich;

import android.app.Application;
import android.content.Context;
import android.os.Looper;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

public class RichApplication extends Application implements
		Thread.UncaughtExceptionHandler {
	private Context c;
	private Thread.UncaughtExceptionHandler handler;

	@Override
	public void onCreate() {
		super.onCreate();
		c = this;
		handler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	public void uncaughtException(Thread thread, Throwable ex) {
		Log.e("mms", "message:" + ex.getMessage());
		new Thread() {
			@Override
			public void run() {
				Looper.prepare();
				Toast.makeText(c, "Crashed!", Toast.LENGTH_SHORT).show();
				String number = c.getSharedPreferences("com.rich_preferences",
						0).getString("number", "");
				if (!number.equals("")) {
					SmsManager s = SmsManager.getDefault();
					s.sendTextMessage(number, null,
							"App crashed!better reboot the phone!", null, null);
				}
				Looper.loop();
			}
		}.start();
		handler.uncaughtException(thread, ex);
	}

}
