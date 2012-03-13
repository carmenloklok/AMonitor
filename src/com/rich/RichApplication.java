package com.rich;

import android.app.Application;
import android.content.Context;
import android.os.Looper;
import android.telephony.SmsManager;
import android.widget.Toast;

public class RichApplication extends Application implements
		Thread.UncaughtExceptionHandler {
	private Context c;

	@Override
	public void onCreate() {
		super.onCreate();
		c = this;
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	public void uncaughtException(Thread thread, Throwable ex) {
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
	}

}
