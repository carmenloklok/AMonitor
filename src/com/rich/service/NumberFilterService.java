package com.rich.service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.RemoteException;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.ITelephony;

public class NumberFilterService extends Service {
	private TelephonyManager m_telephonyManager;
	private ITelephony m_telephonyService;
	// private AudioManager m_audioManager;
	private MyPhoneStateListener listener;
	KeyguardLock myLock;
	Context context;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void onCreate() {
		super.onCreate();
		KeyguardManager myKeyGuard = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
		myLock = myKeyGuard.newKeyguardLock("");
		myLock.disableKeyguard();
		m_telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		Class c;
		try {
			c = Class.forName(m_telephonyManager.getClass().getName());
			Method m = c.getDeclaredMethod("getITelephony");
			m.setAccessible(true);
			m_telephonyService = (ITelephony) m.invoke(m_telephonyManager);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// m_audioManager = (AudioManager)
		// getSystemService(Context.AUDIO_SERVICE);
		listener = new MyPhoneStateListener(this);
		m_telephonyManager.listen(listener,
				PhoneStateListener.LISTEN_CALL_STATE);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (m_telephonyManager != null)
			m_telephonyManager.listen(listener, PhoneStateListener.LISTEN_NONE);
		if (myLock != null)
			myLock.reenableKeyguard();
	}

	class MyPhoneStateListener extends PhoneStateListener {
		private Context context;

		public MyPhoneStateListener(Context c) {
			context = c;
		}

		public void onCallStateChanged(int state, String incomingNumber) {
			// m_audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
			switch (state) {
			case TelephonyManager.CALL_STATE_IDLE:
				// m_audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				// CALL_STATE_OFFHOOK;
				break;
			case TelephonyManager.CALL_STATE_RINGING:
				// if incomingNumber need to be blocked ex:if num is "33",block
				// it
				// must be function "equals", no "=="
				SharedPreferences sharedPreferences = context
						.getSharedPreferences("com.rich_preferences", 0);
				String f = sharedPreferences.getString("filter", "");
				String[] nums = f.split(",");
				for (int i = 0; i < nums.length; ++i) {
					Log.e("NUMBER", "num:" + nums[i] + "incoming:"
							+ incomingNumber);
					if (!nums[i].equals("")
							&& (nums[i].endsWith(incomingNumber) || incomingNumber
									.endsWith(nums[i]))) {
						try {
							m_telephonyService.answerRingingCall();
						} catch (RemoteException e) {
							e.printStackTrace();
						}
						return;
					}
				}
				try {
					m_telephonyService.endCall();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
