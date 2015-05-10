/**
 * 
 * @author 徳島大学　Kousuke Mouri
 * 
 */
package jp.ac.tokushima_u.is.ll.ui.navTask;//////////////////////////////////////////////////////////////////////////////////////////////

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import jp.ac.tokushima_u.is.ll.ui.nav.Reflect;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.net.Uri;

import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

class CamView extends SurfaceView implements SurfaceHolder.Callback 
{
	SurfaceHolder mHolder;
    protected Camera mCamera;
    Context mContext;
    double lat,lng;
    
    CamView(Context context) 
    {
    	super(context);
    	mContext=context;
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }
    private Size size;
    @Override
	public void surfaceCreated(SurfaceHolder holder) 
    {   // The Surface has been created, acquire the camera and tell it where to draw.
        mCamera = Camera.open();
        try 
        {
        	Camera.Parameters params = mCamera.getParameters();  
        	//Reflect.getSupportedPreviewSizes(params)でSizeリストは大きい順に戻ってくる
                   List<Size> supportedSizes = Reflect.getSupportedPreviewSizes(params);  
                     
        	           if (supportedSizes != null && supportedSizes.size() > 0) {  
                         
                      size = supportedSizes.get(0);
                      Log.v("カメラサイズ","カメラサイズ"+size.width+"と"+size.height);
                      //パラメータのセット
                      params.setPreviewSize(size.width, size.height);  
                  mCamera.setParameters(params);  
                 }  
                  // プレビュー設定  
                  mCamera.setPreviewDisplay(holder);  

        } catch (IOException exception) {
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
	public void surfaceDestroyed(SurfaceHolder holder) 
    {   // Surface will be destroyed when we return, so stop the preview.
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    @Override
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) 
    {  
         Camera.Parameters params = mCamera.getParameters(); 
          params.setPreviewSize(size.width, size.height);  
    	       mCamera.setParameters(params);  
          // プレビュー開始  
          mCamera.startPreview();  
          
    }
    
    

	public void takePicture(double lat, double lng) 
    {
    	this.lat=lat;
    	this.lng=lng;
    	//shutterCallbackはシャッターが押されたときに呼ばれるコールバックを指定している
    	//rawCallbackはRawイメージ生成後に呼ばれる
    	//JPEGイメージは生成後に呼ばれる
		mCamera.takePicture(shutterCallback, rawCallback, jpegCallback); 
    	
    }

    ShutterCallback shutterCallback = new ShutterCallback() 
    {
    	@Override
		public void onShutter() 
	    {   
	    }
      };
        
    PictureCallback rawCallback = new PictureCallback() 
    {
    	@Override
		public void onPictureTaken(byte[] _data, Camera _camera) 
    	{
    	}
     };

     PictureCallback jpegCallback = new PictureCallback() 
     {
    	 //写真撮影完了時に呼ばれる
        @Override
		public void onPictureTaken(byte[] _data, Camera _camera) 
        {//113参照
        	StoreByteImage(mContext, _data, 100);
        	//プレビュー再開　これはサーフィスイベント時に停止しているから
        	mCamera.startPreview();
        }
      };    
      
     // Store JPG image on SD card
      public boolean StoreByteImage(Context mContext, byte[] imageData, int quality) 
      {	// Format the current time stamp. 　SimpleDateFormatはyは年、Mは月、ｄは日であり、Hとｍとｓで一日の時間帯を表している
      	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
      	Date currentTime = new Date();
      	String dateStamp = formatter.format(currentTime)+".jpg"; // file name
     	//このクラスはcontentモデルへのアプリケーションアクセスを提供します。
      	ContentResolver contentResolver= mContext.getContentResolver();
      	Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
      	//This class is used to store a set of values that the ContentResolver can process. 
      	//このクラスは、ContentResolverが処理できる1セットの値を保存するのに使用されます。
    	ContentValues values = new ContentValues(5);
    	values.put(ImageColumns.LATITUDE, lat);
    	values.put(ImageColumns.LONGITUDE, lng);
    	values.put(MediaColumns.DISPLAY_NAME, dateStamp);
    	values.put(ImageColumns.DESCRIPTION, "LL Nav");
    	values.put(MediaColumns.MIME_TYPE, "image/jpeg");
    	Uri uri = contentResolver.insert(Media.EXTERNAL_CONTENT_URI, values);
    	try {//bitmapをバイトに変換
    	    OutputStream outStream = contentResolver.openOutputStream(uri);
    	    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outStream);
    	    outStream.close();
    	  		
  		} catch (IOException e) {
  			Toast.makeText(((ContextWrapper) mContext).getBaseContext(), "Photo can not be saved!!!",Toast.LENGTH_SHORT).show();
  			return false;
  		}
  		Toast.makeText(((ContextWrapper) mContext).getBaseContext(),"Photo is Saved",Toast.LENGTH_SHORT).show();
  		return true;
  	}
} // end CamView


