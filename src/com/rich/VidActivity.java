package com.rich;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnInfoListener;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.rich.service.SendMMSService;

public class VidActivity extends Activity implements SurfaceHolder.Callback {
	private MediaRecorder recorder = null;
	private SurfaceView surfaceView = null;
	private SurfaceHolder holder = null;
	private String mode = null;
	private File file;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mode = getIntent().getStringExtra("mode");
		super.onCreate(savedInstanceState);
		surfaceView = new SurfaceView(this);
		holder = surfaceView.getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		setContentView(surfaceView);
	}

	private void release() {
		if (recorder != null) {
			try {
				recorder.stop();
			} catch (Exception e) {
				Log.e("preview", "stop failed " + e.getMessage());
			}
			try {
				recorder.reset();
			} catch (Exception e) {
				Log.e("preview", "stop failed " + e.getMessage());
			}
			recorder.release();
			recorder = null;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		record();
	}

	private void record() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
				"yyyy_MM_dd_HH_mm_ss");
		final String date = simpleDateFormat.format(Calendar.getInstance()
				.getTime());
		SharedPreferences s = getSharedPreferences("com.rich_preferences", 0);
		String[] rs = s.getString("video_resolution", "854*480").split("\\*");
		File d = new File(Environment.getExternalStorageDirectory(), "vids");
		d.mkdir();
		file = new File(d, date + ".3gp");
		recorder = new MediaRecorder();
		recorder.setPreviewDisplay(holder.getSurface());
		recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
		recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
		recorder.setVideoSize(Integer.parseInt(rs[0]), Integer.parseInt(rs[1]));
		recorder.setVideoFrameRate(Integer.parseInt(s.getString(
				"video_frame_rate", "15")));
		recorder.setVideoEncodingBitRate(Integer.parseInt(s.getString(
				"video_bit_rate", "5500")) * 1024);
		recorder.setMaxDuration((Integer.parseInt(s.getString("video_length",
				"5")) * 1000));
		recorder.setOnInfoListener(new OnInfoListener() {
			public void onInfo(MediaRecorder mr, int what, int extra) {
				if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
					release();
					Toast.makeText(VidActivity.this, "end recording",
							Toast.LENGTH_SHORT).show();
					if (mode.length() >= 2)
						mode = mode.substring(1);
					else
						mode = "";
					if (mode.equals("")) {
						Intent i = new Intent(VidActivity.this,
								SettingActivity.class);
						i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
						startActivity(i);
						finish();
						overridePendingTransition(0, 0);
					} else {
						char c = mode.charAt(0);
						if (c == 's') {
							Intent i = new Intent(getApplicationContext(),
									SendMMSService.class);
							i.putExtra("path", file.getAbsolutePath());
							startService(i);
							if (mode.length() >= 2)
								mode = mode.substring(1);
							else
								mode = "";
						}
						if (mode.equals("")) {
							Intent i = new Intent(VidActivity.this,
									SettingActivity.class);
							i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
							startActivity(i);
							finish();
							overridePendingTransition(0, 0);
						} else {
							c = mode.charAt(0);
							if (c == 'p') {
								Intent i = new Intent(VidActivity.this,
										PicActivity.class);
								i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
								i.putExtra("mode", mode);
								startActivity(i);
								finish();
								overridePendingTransition(0, 0);
							} else {
								record();
							}
						}
					}
				}
			}
		});
		recorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
		recorder.setOutputFile(file.getAbsolutePath());
		try {
			recorder.prepare();
		} catch (IllegalStateException e) {
		} catch (IOException e) {
		}
		Toast.makeText(this, "start recording", Toast.LENGTH_SHORT).show();
		recorder.start();
	}

	public void surfaceCreated(SurfaceHolder holder) {

	}

	public void surfaceDestroyed(SurfaceHolder holder) {
	}
}
