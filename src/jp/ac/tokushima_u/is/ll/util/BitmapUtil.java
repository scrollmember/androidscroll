package jp.ac.tokushima_u.is.ll.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import jp.ac.tokushima_u.is.ll.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;

public class BitmapUtil {
	private static String TAG = "BitmapRetrieve";


	public static Bitmap getURLBitmap(Context context, String url) {
		if(url==null)
			return null;
		String filename = url.substring(ApiConstants.Image_Server_Url.length());
		return BitmapUtil.getBitmap(context, filename);
	}
	
	public static Bitmap getBitmap(Context context, String filename) {
		Bitmap bitmap = null;
		
		if(filename == null)
			return bitmap;

		// 直接ファイルパスを指定する方法を行なってはならない
		// see: https://sites.google.com/a/techdoctranslator.com/jp/android/guide/data-storage
		// String filepath = context.getResources().getString(R.string.cache_image_path);
		String filepath = context.getExternalCacheDir().toString() + context.getResources().getString(R.string.cache_image_external_path);
		
		
		File dir = new File(filepath);
		if(!dir.exists())
			dir.mkdirs();
		
		File nomedia = new File(context.getExternalCacheDir().toString() + "/.nomedia");
		
		if(!nomedia.exists()) {
		    try {
		        nomedia.createNewFile();
		    }catch(IOException e) {
		        Log.e(TAG, "nomedia create error", e);
		    }
		}
				
		filepath = filepath + filename ;
		File file = new File(filepath);
		try {
			if (file.exists()) {
				bitmap = BitmapFactory.decodeFile(filepath);
			}

			if (bitmap == null) {
				String url = ApiConstants.Image_Server_Url + filename;
				bitmap = getURLBitmap(url);
				if (bitmap != null)
					bitmap.compress(CompressFormat.PNG, 100,
							new FileOutputStream(file));
			}
		} catch (Exception e) {
			Log.d(TAG, "Exception occurred", e);
		}
		return bitmap;
	}
	
	public static Bitmap getBitmap(Context context, String filename,
			String post_fix) {
		return getBitmap(context, filename+post_fix);
	}
	
	public static Bitmap getURLBitmap(String url){
		URL imageUrl = null;
		Bitmap bitmap = null;
		HttpURLConnection conn = null;
		try{
			imageUrl = new URL(url);
			conn = (HttpURLConnection) imageUrl.openConnection();
			conn.connect();
			InputStream is = conn.getInputStream();
			bitmap = BitmapFactory.decodeStream(is);
			is.close();
		}catch(Exception e){
		}
		return bitmap;
	}
	
	public static Bitmap resizeBitmap(String url, int maxwidth, int maxheight){
        Bitmap photoOrg1 = getURLBitmap(url);
        if(photoOrg1 == null)
        	return null;
        return zoomImage(photoOrg1, maxwidth, maxheight);
    }
	
	public static Bitmap zoomImage(Bitmap image, int maxwidth, int maxheight){
		int width = image.getWidth();
		int height = image.getHeight();
//		if(width<maxwidth&&height<maxheight)
//			return image;
		Matrix matrix = new Matrix();
		float scaleWidth = ((float)maxwidth/width);
		float scaleHeight = ((float)maxheight/height);
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap bitmap = Bitmap.createBitmap(image, 0, 0, width, height,
                matrix, true);
		return bitmap;
	}
}
