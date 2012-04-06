package com.rich;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mobclick.android.MobclickAgent;
import com.rich.service.NumberFilterService;
import com.rich.util.RichUtils;

public class SettingActivity extends PreferenceActivity {
	// private ListPreference pictureResolution;
	// private ListPreference videoResolution;
	// private ListPreference frameRate;
	private TakePicReceiver takePicReceiver;
	private Preference filter;
	private Context context;
	private SharedPreferences sharedPreferences;
	private CheckBoxPreference service;
	public boolean processing = false;
	private String number;
	private boolean keyPressed = false;

	// private Camera camera;

	class TakePicReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (processing == false)
				trigger();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onDestroy() {
		// if (camera != null) {
		// camera.release();
		// camera = null;
		// }
		if (takePicReceiver != null)
			unregisterReceiver(takePicReceiver);
		super.onDestroy();
	}

	private void trigger() {
		if (number.equals("")) {
			Toast.makeText(this, "Set number first", Toast.LENGTH_LONG).show();
			return;
		}
		// ConnectivityManager connectivityManager = (ConnectivityManager)
		// getSystemService(CONNECTIVITY_SERVICE);
		// NetworkInfo info = connectivityManager
		// .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		// if (!info.isConnected()) {
		// Toast.makeText(this,
		// info.getState() + ":Please turn on mobile data",
		// Toast.LENGTH_LONG).show();
		// return;
		// }
		processing = true;
		String mode = sharedPreferences.getString("mode", "psv");
		String[] rs = mode.split("\\D+");
		String out = "";
		if (rs.length == 0)
			out = mode;
		for (String r : rs) {
			if (r != null && !r.equals("")) {
				if (mode.indexOf(r) != 0)
					out += mode.charAt(0);
				int t = mode.indexOf(r) + r.length();
				char c = mode.charAt(t);
				for (int i = 0; i < Integer.parseInt(r); ++i)
					out += c;
				mode = mode.substring(t + 1);
				if (!mode.equals("")) {
					char in = mode.charAt(0);
					while ((in >= 65 && in <= 90) || (in >= 97 && in <= 122)) {
						out += in;
						mode = mode.substring(1);
						if (mode.equals(""))
							break;
						else
							in = mode.charAt(0);
					}
				}
			}
		}
		Intent i = null;
		if (out.charAt(0) == 'p') {
			i = new Intent(SettingActivity.this, PicActivity.class);
		} else {
			i = new Intent(SettingActivity.this, VidActivity.class);
		}
		if (i != null) {
			i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			i.putExtra("mode", out);
			startActivity(i);
			finish();
			overridePendingTransition(0, 0);
		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		keyPressed = false;
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyPressed == false) {
			keyPressed = true;
			if (keyCode == KeyEvent.KEYCODE_MENU) {
				if (processing == false) {
					trigger();
				}
			} else if (keyCode == KeyEvent.KEYCODE_BACK) {
				finish();
			} else {
				SmsManager manager = SmsManager.getDefault();
				if (keyCode == KeyEvent.KEYCODE_1)
					manager.sendTextMessage(number, null,
							sharedPreferences.getString("1", "1"), null, null);
				else if (keyCode == KeyEvent.KEYCODE_2)
					manager.sendTextMessage(number, null,
							sharedPreferences.getString("2", "2"), null, null);
				else if (keyCode == KeyEvent.KEYCODE_3)
					manager.sendTextMessage(number, null,
							sharedPreferences.getString("3", "3"), null, null);
				else if (keyCode == KeyEvent.KEYCODE_4)
					manager.sendTextMessage(number, null,
							sharedPreferences.getString("4", "4"), null, null);
				else if (keyCode == KeyEvent.KEYCODE_5)
					manager.sendTextMessage(number, null,
							sharedPreferences.getString("5", "5"), null, null);
				else if (keyCode == KeyEvent.KEYCODE_6)
					manager.sendTextMessage(number, null,
							sharedPreferences.getString("6", "6"), null, null);
				else if (keyCode == KeyEvent.KEYCODE_7)
					manager.sendTextMessage(number, null,
							sharedPreferences.getString("7", "7"), null, null);
				else if (keyCode == KeyEvent.KEYCODE_8)
					manager.sendTextMessage(number, null,
							sharedPreferences.getString("8", "8"), null, null);
				else if (keyCode == KeyEvent.KEYCODE_9)
					manager.sendTextMessage(number, null,
							sharedPreferences.getString("9", "9"), null, null);
			}
		}
		return true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		context = this;
		super.onCreate(savedInstanceState);
		MobclickAgent.onError(this);
		takePicReceiver = new TakePicReceiver();
		registerReceiver(takePicReceiver, new IntentFilter("TRIGGER"));
		addPreferencesFromResource(R.xml.setting_activity);
		sharedPreferences = getPreferenceManager().getSharedPreferences();
		String n = getIntent().getStringExtra("number");
		if (n == null)
			number = sharedPreferences.getString("number", "");
		else
			number = n;
		filter = (Preference) findPreference("filter");
		service = (CheckBoxPreference) findPreference("service");
		// pictureResolution = (ListPreference)
		// findPreference("picture_resolution");
		// videoResolution = (ListPreference)
		// findPreference("video_resolution");
		// frameRate = (ListPreference) findPreference("video_frame_rate");
		// camera = Camera.open();
		// if (camera != null) {
		// Camera.Parameters parameters = camera.getParameters();
		// camera.release();
		// camera = null;
		//
		// ArrayList<Size> sizes = (ArrayList<Size>) parameters
		// .getSupportedPictureSizes();
		// ArrayList<String> resolutions = new ArrayList<String>();
		// for (Size s : sizes) {
		// resolutions.add(s.width + "*" + s.height);
		// }
		// String[] temp = (String[]) resolutions
		// .toArray(new String[resolutions.size()]);
		// pictureResolution.setEntries(temp);
		// pictureResolution.setEntryValues(temp);
		//
		// sizes = (ArrayList<Size>) parameters.getSupportedPreviewSizes();
		// resolutions.clear();
		// for (Size s : sizes) {
		// resolutions.add(s.width + "*" + s.height);
		// }
		// temp = (String[]) resolutions
		// .toArray(new String[resolutions.size()]);
		// videoResolution.setEntries(temp);
		// videoResolution.setEntryValues(temp);
		//
		// ArrayList<Integer> ints = (ArrayList<Integer>) parameters
		// .getSupportedPreviewFrameRates();
		// resolutions.clear();
		// for (Integer i : ints) {
		// resolutions.add(i + "");
		// }
		// temp = (String[]) resolutions
		// .toArray(new String[resolutions.size()]);
		// frameRate.setEntries(temp);
		// frameRate.setEntryValues(temp);
		// }
		filter.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				new NumberFilterDialog(context).show();
				return true;
			}
		});
		service.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				Intent i = new Intent(context, NumberFilterService.class);
				if ((Boolean) newValue == true) {
					startService(i);
				} else {
					stopService(i);
				}
				return true;
			}
		});
		RichUtils.runRootCommand("");
	}

	class NumberFilterDialog extends Dialog implements OnClickListener {
		private ListView listView;
		private EditText number;
		private Button add, submit, cancel;
		private ArrayList<String> numbers;
		private NumberFilterAdapter adapter;

		public NumberFilterDialog(Context context) {
			super(context);
		}

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.dialog_number_filter);
			setTitle("Numbers to answer");
			listView = (ListView) findViewById(R.dialog_number_filter.list);
			number = (EditText) findViewById(R.dialog_number_filter.number);
			add = (Button) findViewById(R.dialog_number_filter.add);
			submit = (Button) findViewById(R.dialog_number_filter.submit);
			cancel = (Button) findViewById(R.dialog_number_filter.cancel);
			numbers = new ArrayList<String>();
			String[] ts = sharedPreferences.getString("filter", "").split(",");
			for (String s : ts) {
				if (!s.equals("")) {
					numbers.add(s);
				}
			}
			ts = null;
			adapter = new NumberFilterAdapter(context, numbers);
			listView.setAdapter(adapter);
			add.setOnClickListener(this);
			submit.setOnClickListener(this);
			cancel.setOnClickListener(this);
		}

		public void onClick(View v) {
			switch (v.getId()) {
			case R.dialog_number_filter.add:
				String t = number.getText().toString().trim();
				if (t.equals("")) {
					Toast.makeText(context, "cant be null", Toast.LENGTH_LONG)
							.show();
					break;
				}
				adapter.add(t);
				adapter.notifyDataSetChanged();
				break;
			case R.dialog_number_filter.submit:
				Editor editor = sharedPreferences.edit();
				editor.putString("filter", adapter.output());
				editor.commit();
			case R.dialog_number_filter.cancel:
				dismiss();
			}
		}
	}

	class NumberFilterAdapter extends BaseAdapter {
		private ArrayList<String> numbers;
		private TextView number;
		private Button remove;
		private NumberFilterAdapter self;
		private LayoutInflater inflater;

		public NumberFilterAdapter(Context context, ArrayList<String> numbers) {
			this.numbers = numbers;
			self = this;
			inflater = LayoutInflater.from(context);
		}

		public void add(String s) {
			numbers.add(s);
		}

		public int getCount() {
			return numbers.size();
		}

		public Object getItem(int position) {
			return numbers.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(final int position, View convertView,
				ViewGroup parent) {
			convertView = inflater.inflate(R.layout.dialog_number_filter_item,
					null);
			convertView.setTag(null);
			number = (TextView) convertView
					.findViewById(R.dialog_number_filter_item.number);
			number.setText(numbers.get(position));
			remove = (Button) convertView
					.findViewById(R.dialog_number_filter_item.remove);
			remove.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					numbers.remove(position);
					self.notifyDataSetChanged();
				}
			});
			return convertView;
		}

		public String output() {
			String out = "";
			for (int i = 0; i < numbers.size(); ++i) {
				if (i != 0)
					out += ",";
				out += numbers.get(i);
			}
			return out;
		}
	}
}
