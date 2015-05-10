package jp.ac.tokushima_u.is.ll.ui.media;

import jp.ac.tokushima_u.is.ll.R;
import jp.ac.tokushima_u.is.ll.ui.LogListActivity;
import jp.ac.tokushima_u.is.ll.util.ApiConstants;
import jp.ac.tokushima_u.is.ll.util.BitmapUtil;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

public class ShowPhoto extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.mr_photo_player);

		Intent intent = this.getIntent();
		Uri fileUri = intent.getData();

		ImageView imageView = (ImageView)findViewById(R.id.PhotoPreviewView);
//		imageView.setImageURI(fileUri);
		
		Bitmap drawable = null;
		try{
			String path = fileUri.toString();
			drawable = BitmapUtil.getURLBitmap(ShowPhoto.this, path);
//			if(path!=null){
//				String filename = path.substring(ApiConstants.Image_Server_Url.length());
//				drawable = BitmapUtil.getBitmap(ShowPhoto.this, filename);
//			}
		}catch(Exception e){
			
		}
		
//		Bitmap drawable = BitmapUtil.getURLBitmap(fileUri.toString());
//		Bitmap drawable = BitmapUtil
//		.getBitmap(ShowPhoto.this, fileUri, ApiConstants.SmallestSizePostfix);
		if(drawable==null){
			drawable = BitmapFactory.decodeFile(fileUri.getPath());
		}
		imageView.setImageBitmap(drawable);
		imageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ShowPhoto.this.finish();
			}
		});
	}
}
