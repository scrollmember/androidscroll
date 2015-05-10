package jp.ac.tokushima_u.is.ll.ui.media;

import jp.ac.tokushima_u.is.ll.R;
import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;

public class VideoPlayer extends Activity {
	
	private VideoView videoView = null;
	private MediaController mc = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.mr_video_player);

		Intent intent = this.getIntent();
		Uri fileUri = intent.getData();

		videoView = (VideoView) findViewById(R.id.VideoView01);
		videoView.requestFocus();

		mc = new MediaController(VideoPlayer.this);
		videoView.setMediaController(mc);
		videoView.setVideoURI(fileUri);

//		videoView.requestFocus();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(mc!=null&&videoView!=null){
			mc.show(2000);
			videoView.start();
		}
	}



	@Override
	protected void onPause() {
		super.onPause();
		if(videoView!=null&&videoView.isPlaying()){
			videoView.pause();
		}
	}
}
