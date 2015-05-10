package jp.ac.tokushima_u.is.ll.ui.media;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import jp.ac.tokushima_u.is.ll.R;
import jp.ac.tokushima_u.is.ll.util.Constants;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class AudioPlayer extends Activity implements OnClickListener {
	private Button mPreviousButton = null;
	private Button mStartButton = null;
	private Button mNextButton = null;
	private SeekBar mSeekBar;
	private TextView mPalyTime;
	private TextView mPalyTimePrefix;
	private TextView mPalyTimeStart;
	private TextView mPalyTimeEnd;
	private int forwardSeconds;

	Timer mTimer = new Timer();
	TimerTask mCurrentTimeTask;

	public MediaPlayer mPlayer = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.mr_sound_player);

		try {
			forwardSeconds = Integer.valueOf(getString(R.string.forward_seconds));
		} catch (NumberFormatException e1) {
			forwardSeconds = 5;
		}

		mStartButton = (Button) this.findViewById(R.id.playback_pause);
		mPreviousButton = (Button) this.findViewById(R.id.playback_previous);
		mNextButton = (Button) this.findViewById(R.id.playback_next);
		mSeekBar = (SeekBar) this.findViewById(R.id.playback_seek);
		mPalyTime = (TextView) this.findViewById(R.id.txt_playtime);
		mPalyTimePrefix = (TextView) this.findViewById(R.id.txt_playtimeprefix);
		mPalyTimeStart = (TextView) this.findViewById(R.id.txt_playtimestart);
		mPalyTimeEnd = (TextView) this.findViewById(R.id.txt_pplaytimeend);

		Display display = this.getWindowManager().getDefaultDisplay();
		int width = display.getWidth();

		Intent intent = this.getIntent();
		Uri fileUri = intent.getData();
		try {
			mPlayer = new MediaPlayer();
			mPlayer.setOnCompletionListener(mCompletionListener);
			mPlayer.setDataSource(AudioPlayer.this, fileUri);
			mPlayer.prepare();
			mPlayer.start();
		} catch (IOException e) {
			if (e != null)
				Log.d(Constants.DEBUG_TAG, "IOException occured", e);
		}
		((LinearLayout)findViewById(R.id.audio_player_wrapper)).setLayoutParams(new LinearLayout.LayoutParams(width-200, LinearLayout.LayoutParams.WRAP_CONTENT));
		mStartButton.setOnClickListener(this);
		mPreviousButton.setOnClickListener(this);
		mNextButton.setOnClickListener(this);
		mPalyTimeStart.setText(formatTime(0));
		if (mPlayer != null)
			mPalyTimeEnd.setText(formatTime(mPlayer.getDuration()));

		mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			public void onProgressChanged(SeekBar bar, int progress,
					boolean fromUser) {
				if (fromUser && mPlayer != null){
					mPlayer.seekTo(mPlayer.getDuration() * progress / 1024);
					if(mPlayer.getCurrentPosition()>mPlayer.getDuration()){
						mPlayer.seekTo(mPlayer.getDuration());
					}
				}
			}

			public void onStartTrackingTouch(SeekBar arg0) {
				if (mPlayer != null && mPlayer.isPlaying()) {
					mPlayer.pause();
					updateView();
				}
			}

			public void onStopTrackingTouch(SeekBar bar) {
				if (mPlayer != null) {
					mPlayer.start();
					updateView();
				}
			}

		});

		updateView();
	}

	OnCompletionListener mCompletionListener = new OnCompletionListener() {
		public void onCompletion(MediaPlayer mp) {
			updateView();
		}
	};

	@Override
	protected void onPause() {
		super.onStop();
		if (mCurrentTimeTask != null) {
			mCurrentTimeTask.cancel();
		}
		if (mPlayer != null&&mPlayer.isPlaying()) {
			mPlayer.pause();
		}
//		this.finish();
	}



	@Override
	protected void onDestroy() {
		super.onStop();
		if (mPlayer != null) {
			mPlayer.stop();
			mPlayer.release();
		}
	}



	@Override
	protected void onResume() {
		super.onResume();
		mCurrentTimeTask = new TimerTask() {
			public void run() {
				AudioPlayer.this.runOnUiThread(new Runnable() {
					public void run() {
						if (mPlayer != null) {
							updateProgressDisplay();
						}
					}
				});
			}
		};
		mTimer.scheduleAtFixedRate(mCurrentTimeTask, 0, 100);
		updateView();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Display display = this.getWindowManager().getDefaultDisplay();
		int width = display.getWidth();
		((LinearLayout)findViewById(R.id.audio_player_wrapper)).setLayoutParams(new LinearLayout.LayoutParams(width-200, LinearLayout.LayoutParams.WRAP_CONTENT));
	}

	void updateProgressDisplay() {
		if (mPlayer != null) {
			mPalyTime.setText(formatTime(mPlayer.getCurrentPosition()));
			mSeekBar.setProgress((1024 * mPlayer.getCurrentPosition())
					/ mPlayer.getDuration());
		}
	}

	private String formatTime(int mseconds) {
		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss", Locale.US);
		format.setTimeZone(TimeZone.getTimeZone( "GMT-0 "));
		Date date = new Date(mseconds);
		return format.format(date);
	}

	@Override
	public void onClick(View v) {
		if (v.equals(mStartButton)) {
			if (mPlayer != null) {
				if (mPlayer.isPlaying()) {
					mPlayer.pause();
				} else {
					mPlayer.start();
				}
			}
		} else if (v.equals(mPreviousButton)) {
			if(mPlayer!=null){
				mPlayer.seekTo(mPlayer.getCurrentPosition()-forwardSeconds*1000);
			}
		} else if (v.equals(mNextButton)) {
			if(mPlayer!=null){
				mPlayer.seekTo(mPlayer.getCurrentPosition()+forwardSeconds*1000);
			}
		}
		updateView();
	}

	private void updateView() {
		if (mPlayer != null && mPlayer.isPlaying()) {
			mStartButton.setCompoundDrawablesWithIntrinsicBounds(null, null,
					null, getResources().getDrawable(R.drawable.mr_pause));
			this.mPalyTimePrefix.setText(R.string.txt_playtime_prefix_on);
		} else if (mPlayer != null && !mPlayer.isPlaying()) {
			mStartButton.setCompoundDrawablesWithIntrinsicBounds(null, null,
					null, getResources().getDrawable(R.drawable.mr_playing));
			this.mPalyTimePrefix.setText(R.string.txt_playtime_prefix_off);
		}
	}

}
