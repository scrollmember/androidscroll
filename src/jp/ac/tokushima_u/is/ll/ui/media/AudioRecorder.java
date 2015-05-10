package jp.ac.tokushima_u.is.ll.ui.media;

import java.io.File;
import java.io.IOException;

import jp.ac.tokushima_u.is.ll.R;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Chronometer.OnChronometerTickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class AudioRecorder extends Activity {
	private ImageButton optionButton;
	private TextView txtTimePrefix;
	private Chronometer chrono_time;
	private MediaRecorder recorder;
	private String fileprefix = "audio_";
	private File home;

	long elapsedTime = 0;
	String currentTime = "";
	long startTime = SystemClock.elapsedRealtime();
	boolean isRecording = false;
	boolean isCameraButtonDown = false;

	private File filepath;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.audio_recorder);

		Display display = this.getWindowManager().getDefaultDisplay();
		int screenWidth = display.getWidth();

		optionButton = (ImageButton) findViewById(R.id.audio_record_option_btn);
		txtTimePrefix = (TextView) findViewById(R.id.txt_time_prefix);

		chrono_time = (Chronometer) findViewById(R.id.chrono_time);
		chrono_time.setWidth((int) (screenWidth * 0.7));
		// chrono_time.setFormat(timePrefix + "%s");
		chrono_time.setBase(SystemClock.elapsedRealtime());
		
		// 直接ファイルパスを指定する方法を行なってはならない
        // see: https://sites.google.com/a/techdoctranslator.com/jp/android/guide/data-storage
		// home = new File(this.getResources().getString(R.string.audio_file_path));
		
		home = new File(getExternalCacheDir().toString() + getResources().getString(R.string.audio_file_external_path));

		this.updateView();

		optionButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (!isRecording) {
					start();
				}else if (isRecording) {
					stop();
				}
			}
		});

		chrono_time
				.setOnChronometerTickListener(new OnChronometerTickListener() {
					public void onChronometerTick(Chronometer arg0) {
						long hours = ((SystemClock.elapsedRealtime() - chrono_time
								.getBase()) / 1000) / 360;
						long minutes = ((SystemClock.elapsedRealtime() - chrono_time
								.getBase()) / 1000 - hours * 360) / 60;
						long seconds = ((SystemClock.elapsedRealtime() - chrono_time
								.getBase()) / 1000) % 60;
						String strhours = "" + hours + ":";
						String strminutes = "" + minutes;
						String strseconds = "" + seconds;
						if (hours <= 0)
							strhours = "";
						if (minutes < 10)
							strminutes = "0" + minutes;
						if (seconds < 10)
							strseconds = "0" + seconds;
						currentTime = strhours + strminutes + ":" + strseconds;
						arg0.setText(currentTime);
						elapsedTime = SystemClock.elapsedRealtime();
					}
				});

	}


	private void start() {
		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			Toast.makeText(AudioRecorder.this, R.string.info_txt_no_sd_card,
					Toast.LENGTH_SHORT).show();
			return;
		}
		chrono_time.setBase(SystemClock.elapsedRealtime());
		try {
			if(!home.exists())
				home.mkdirs();
			 filepath = File.createTempFile(fileprefix, ".mp3", home);
			initReorder();
			recorder.setOutputFile(filepath.getAbsolutePath());
			recorder.prepare();
			recorder.start();
			chrono_time.start();
			isRecording = true;
		} catch (IOException e) {
			Toast.makeText(AudioRecorder.this,
					R.string.info_voice_record_error, Toast.LENGTH_SHORT)
					.show();
		} catch (IllegalStateException e) {
			Toast.makeText(AudioRecorder.this,
					R.string.info_voice_record_error, Toast.LENGTH_SHORT)
					.show();
		} catch (Exception e) {
			Toast.makeText(AudioRecorder.this,
					R.string.info_audio_resource_is_using,
					Toast.LENGTH_LONG).show();
		}
		updateView();
	}

	private void stop() {
		chrono_time.stop();
		chrono_time.setText(currentTime);
		if (recorder != null) {
			try {
				recorder.stop();
				recorder.release();
				isRecording = false;
			} catch (IllegalStateException e) {
				Toast.makeText(AudioRecorder.this,
						R.string.info_voice_record_error, Toast.LENGTH_SHORT)
						.show();
			}
		}
		updateView();
		Intent data = new Intent();
//		data.putExtra("", value);
		if(this.filepath!=null)
			data.putExtra("filepath", filepath.getAbsolutePath());
		setResult(RESULT_OK, data);
        finish();
	}

	
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Display display = this.getWindowManager().getDefaultDisplay();
		int screenWidth = display.getWidth();
		chrono_time.setWidth((int) (screenWidth * 0.7));
	}

	private void updateView() {
		if (isRecording) {
			txtTimePrefix.setText(R.string.voice_record_prefix_start);
		} else {
			txtTimePrefix.setText(R.string.voice_record_prefix_stop);
		}
	}

	private void initReorder() {
		recorder = new MediaRecorder();
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
	}
}