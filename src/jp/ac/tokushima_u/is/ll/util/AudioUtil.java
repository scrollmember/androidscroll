package jp.ac.tokushima_u.is.ll.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;

import jp.ac.tokushima_u.is.ll.R;
import android.content.Context;
import android.util.Log;

//import com.actionbarsherlock.R;

public class AudioUtil {
	private static final String TAG = "AudioRetrieve";

    public static File getPronounceAudio(Context context, String content,
			String code) {
		if(content==null||content.length()==0)
			return null;
		
		// 直接ファイルパスを指定する方法を行なってはならない
		// see: https://sites.google.com/a/techdoctranslator.com/jp/android/guide/data-storage
		// String filepath = context.getResources().getString(R.string.cache_audio_path);
		String filepath = context.getExternalCacheDir().toString() + context.getResources().getString(R.string.cache_audio_external_path);
		
		File dir = new File(filepath);
		if (!dir.exists())
			dir.mkdirs();
		
	      File nomedia = new File(context.getExternalCacheDir().toString() + "/.nomedia");
	        
	        if(!nomedia.exists()) {
	            try {
	                nomedia.createNewFile();
	            }catch(IOException e) {
	                Log.e(TAG, "nomedia create error", e);
	            }
	        }

		filepath = filepath + content + "_" + code + ".mp3";
		File file = new File(filepath);
		if (!file.exists()) {
			String purl = ApiConstants.Pronounce_URI + "?ie=UTF-8&lang=" + code
					+ "&text=";
			FileOutputStream fout = null;

			InputStream in = null;

			try {
				purl = purl + URLEncoder.encode(content, "UTF-8");
				URL url = new URL(purl);// 获得文件的路径
				in = url.openStream();// 打开到此 URL 的连接并返回一个用于从该连接读入的 InputStream。
				fout = new FileOutputStream(file);// 生成文件的位置

				byte[] b = new byte[1024];// 缓冲区
				int length = 0;
				int a;
				while ((a = in.read(b)) > 0) {
					length = a + length;
					fout.write(b);
				}
				fout.close();
				in.close();
				if(length==0)
					file.deleteOnExit();
			} catch (Exception e) {
			}
		}
		return file;

	}
}
