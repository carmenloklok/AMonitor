package com.rich.service;

import java.io.File;

import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.rich.MMSInfo;
import com.rich.MMSSender;
import com.rich.util.RichUtils;

public class SendMMSService extends Service {
	private Context context;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		context = getApplicationContext();
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
			//
			ConnectivityManager conManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			conManager.startUsingNetworkFeature(
					ConnectivityManager.TYPE_MOBILE, "mms");
			SharedPreferences s = context.getSharedPreferences(
					"com.rich_preferences", 0);
			String mmscUrl = s.getString("mmsc_url", "");
			String mmsProxy = s.getString("mms_proxy", "");
			String mmsPort = s.getString("mms_port", "");
			if (mmscUrl == null || mmscUrl.equals("")) {
			}
			DefaultHttpClient client = null;
			// Make sure to use a proxy which supports CONNECT.
			// client = HttpConnector.buileClient(context);

			HttpParams httpParams = new BasicHttpParams();
			if (!mmsProxy.equals("") && !mmsPort.equals("")) {
				HttpHost httpHost = new HttpHost(mmsProxy,
						Integer.parseInt(mmsPort));
				httpParams
						.setParameter(ConnRouteParams.DEFAULT_PROXY, httpHost);
			}
			HttpConnectionParams.setConnectionTimeout(httpParams, 0);

			client = new DefaultHttpClient(httpParams);
			HttpParams params1 = client.getParams();
			HttpProtocolParams.setContentCharset(params1, "UTF-8");
			HttpPost post = new HttpPost(mmscUrl);
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
					int count = RichUtils.splitBySize(context, f,
							pictureSplitSize, "pics_split");
					for (int i = 0; i < count; ++i) {
						File t = new File(d, f.getName().substring(0,
								f.getName().lastIndexOf('.'))
								+ i + ".jpg");
						sendMMS(t, client, post);
					}
				} else
					sendMMS(f, client, post);
			} else if (f.getName().endsWith(".3gp")) {
				int videoSplitSize = Integer.parseInt(preferences.getString(
						"video_split_size", "1000")) * 1024;
				if (f.length() > videoSplitSize) {
					publishProgress(f.getName() + " need to be split");
					File d = new File(
							Environment.getExternalStorageDirectory(),
							"vids_split");
					d.mkdir();
					int count = RichUtils.splitBySize(context, f,
							videoSplitSize, "vids_split");
					for (int i = 0; i < count; ++i) {
						File t = new File(d, f.getName().substring(0,
								f.getName().lastIndexOf('.'))
								+ i + ".3gp");
						sendMMS(t, client, post);
					}
				} else
					sendMMS(f, client, post);
			}
			if (f.getName().endsWith(".txt")) {
				sendMMS(f, client, post);
			}
			publishProgress("end sending mms");
			client.getConnectionManager().shutdown();
			return null;
		}

		private void sendMMS(File f, DefaultHttpClient client, HttpPost post) {
			publishProgress("ready to send " + f.getAbsolutePath() + " to "
					+ number);
			MMSInfo mms = new MMSInfo(SendMMSService.this, f.getName(), number);
			mms.addPart("file:" + f.getAbsolutePath());
			try {
				MMSSender.sendMMS(SendMMSService.this, mms.getMMSBytes(),
						client, post);
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
