package com.rich;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import com.mobclick.android.MobclickAgent;
import com.rich.service.SendMMSService;

public class PicActivity extends Activity implements SurfaceHolder.Callback {
	private SurfaceHolder holder = null;
	private SurfaceView surfaceView;
	private SharedPreferences preferences;

	private int picResolutionWidth, picResolutionHeight, picQuality,
			sampleSize;

	private Camera camera;
	private int times = 0;
	private String mode = null;

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
	public void onCreate(Bundle savedInstanceState) {
		mode = getIntent().getStringExtra("mode");
		preferences = getSharedPreferences("com.rich_preferences", 0);
		String[] rs = preferences.getString("picture_resolution", "3264*2448")
				.split("\\*");
		picResolutionWidth = Integer.parseInt(rs[0]);
		picResolutionHeight = Integer.parseInt(rs[1]);
		picQuality = Integer.parseInt(preferences.getString("picture_quality",
				"100"));
		sampleSize = Integer
				.parseInt(preferences.getString("sample_size", "1"));
		WindowManager.LayoutParams params = getWindow().getAttributes();
		params.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
		getWindow().setAttributes(params);
		super.onCreate(savedInstanceState);
		surfaceView = new SurfaceView(this);
		setContentView(surfaceView);
		holder = surfaceView.getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	private void init() {
		camera = Camera.open();
		Camera.Parameters parameters = camera.getParameters();
		parameters.setPictureFormat(PixelFormat.JPEG);
		parameters.setPictureSize(picResolutionWidth, picResolutionHeight);
		parameters.setJpegQuality(picQuality);
		camera.setParameters(parameters);
		try {
			camera.setPreviewDisplay(holder);
		} catch (IOException e) {
			camera.release();
			camera = null;
		}
		camera.startPreview();
	}

	public void release() {
		if (camera != null) {
			camera.stopPreview();
			camera.release();
			camera = null;
		}
	}

	private AutoFocusCallback autoFocus = new AutoFocusCallback() {

		public void onAutoFocus(boolean success, Camera camera) {
			if (success) {
				if (camera != null) {
					camera.takePicture(null, null, pic);
				}
			} else {
				times++;
				if (times > 5) {
					times = 0;
					release();
					init();
				}
				camera.autoFocus(autoFocus);
			}
		}
	};

	public Camera.PictureCallback pic = new Camera.PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			new CameraTask().execute(data);
		}
	};

	class CameraTask extends AsyncTask<byte[], Void, Void> {
		@Override
		protected Void doInBackground(byte[]... params) {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
					"yyyy_MM_dd_HH_mm_ss");
			String date = simpleDateFormat.format(Calendar.getInstance()
					.getTime());
			if (mode.length() >= 2)
				mode = mode.substring(1);
			else
				mode = "";
			boolean send = false;
			if (mode.equals("")) {
				new SaveDataTask().execute(params[0], date, send);
				release();
				Intent i = new Intent(PicActivity.this, SettingActivity.class);
				i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
				startActivity(i);
				finish();
				overridePendingTransition(0, 0);
			} else {
				char c = mode.charAt(0);
				if (c == 's') {
					send = true;
					if (mode.length() >= 2)
						mode = mode.substring(1);
					else
						mode = "";
				}
				new SaveDataTask().execute(params[0], date, send);
				if (mode.equals("")) {
					Intent i = new Intent(PicActivity.this,
							SettingActivity.class);
					i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
					startActivity(i);
					release();
					finish();
					overridePendingTransition(0, 0);
				} else {
					c = mode.charAt(0);
					if (c == 'p') {
						camera.startPreview();
						camera.autoFocus(autoFocus);
					} else {
						release();
						Intent i = new Intent(PicActivity.this,
								VidActivity.class);
						i.putExtra("mode", mode);
						i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
						startActivity(i);
						finish();
						overridePendingTransition(0, 0);
					}
				}
			}
			return null;
		}
	}

	class SaveDataTask extends AsyncTask<Object, Integer, Void> {
		@Override
		protected Void doInBackground(Object... params) {
			publishProgress(0);
			File d = new File(Environment.getExternalStorageDirectory(), "pics");
			d.mkdir();
			File f = new File(d, params[1] + ".jpg");
			try {
				BitmapFactory.Options opts = new BitmapFactory.Options();
				opts.inSampleSize = sampleSize;
				opts.inPurgeable = true;
				Bitmap mBitmap = BitmapFactory.decodeByteArray(
						(byte[]) params[0], 0, ((byte[]) params[0]).length,
						opts);
				BufferedOutputStream os = new BufferedOutputStream(
						new FileOutputStream(f));
				mBitmap.compress(Bitmap.CompressFormat.JPEG, picQuality, os);
				os.flush();
				os.close();
				mBitmap.recycle();
				mBitmap = null;
			} catch (FileNotFoundException e) {
				Log.e("save", e.getMessage());
			} catch (IOException e) {
				Log.e("save", e.getMessage());
			} catch (Exception e) {
				Log.e("save", e.getMessage());
			}
			publishProgress(1);
			if ((Boolean) params[2]) {
				Intent i = new Intent(getApplicationContext(),
						SendMMSService.class);
				i.putExtra("path", f.getAbsolutePath());
				startService(i);
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			if (values[0] == 0)
				Toast.makeText(PicActivity.this, "start saving pic data",
						Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(PicActivity.this, "end saving pic data",
						Toast.LENGTH_SHORT).show();
		}
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		camera.autoFocus(autoFocus);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		init();
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
	}
}