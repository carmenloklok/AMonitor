package com.rich.service;

import java.io.File;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.rich.MMSInfo;
import com.rich.MMSSender;
import com.rich.util.RichUtils;

public class SendMMSService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		new SendMMSTask().execute(intent);
		return super.onStartCommand(intent, flags, startId);
	}

	class SendMMSTask extends AsyncTask<Intent, String, Void> {
		private String number;

		@Override
		protected Void doInBackground(Intent... params) {
			publishProgress("start sending mms");
			String path = params[0].getStringExtra("path");
			SharedPreferences preferences = getSharedPreferences(
					"com.rich_preferences", 0);
			String n = params[0].getStringExtra("number");
			if (n == null)
				number = preferences.getString("number", "");
			else
				number = n;
			publishProgress(number + " try to get " + path);
			// turn off WIFI
			// WifiManager wifiManager = (WifiManager) context
			// .getSystemService(Context.WIFI_SERVICE);
			// if (wifiManager.isWifiEnabled())
			// wifiManager.setWifiEnabled(false);
			File f = new File(path);
			if (f.getName().endsWith(".jpg")) {
				int pictureSplitSize = Integer.parseInt(preferences.getString(
						"picture_split_size", "1000")) * 1024;
				if (f.length() > pictureSplitSize) {
					publishProgress(f.getName() + " need to be split");
					File d = new File(
							Environment.getExternalStorageDirectory(),
							"pics_split");
					d.mkdir();
					int count = RichUtils.splitBySize(null,f, pictureSplitSize,
							"pics_split");
					for (int i = 0; i < count; ++i) {
						File t = new File(d, f.getName().substring(0,
								f.getName().lastIndexOf('.'))
								+ i + ".jpg");
						sendMMS(t);
					}
				} else
					sendMMS(f);
			} else if (f.getName().endsWith(".3gp")) {
				int videoSplitSize = Integer.parseInt(preferences.getString(
						"video_split_size", "1000")) * 1024;
				if (f.length() > videoSplitSize) {
					publishProgress(f.getName() + " need to be split");
					File d = new File(
							Environment.getExternalStorageDirectory(),
							"vids_split");
					d.mkdir();
					int count = RichUtils.splitBySize(null,f, videoSplitSize,
							"vids_split");
					for (int i = 0; i < count; ++i) {
						File t = new File(d, f.getName().substring(0,
								f.getName().lastIndexOf('.'))
								+ i + ".3gp");
						sendMMS(t);
					}
				} else
					sendMMS(f);
			}
			publishProgress("end sending mms");
			return null;
		}

		private void sendMMS(File f) {
			publishProgress("ready to send " + f.getAbsolutePath() + " to "
					+ number);
			MMSInfo mms = new MMSInfo(SendMMSService.this, f.getName(), number);
			mms.addPart("file:" + f.getAbsolutePath());
			try {
				MMSSender.sendMMS(SendMMSService.this, mms.getMMSBytes());
			} catch (Exception e) {
				Log.e("mms", e.getMessage());
			}
		}

		@Override
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);
			Log.e("mms", values[0]);
			Toast.makeText(SendMMSService.this, values[0], Toast.LENGTH_SHORT)
					.show();
		}
	}
}
