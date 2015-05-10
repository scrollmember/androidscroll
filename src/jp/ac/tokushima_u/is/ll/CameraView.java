package jp.ac.tokushima_u.is.ll;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Calendar;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.net.Uri;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.MediaColumns;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraView extends SurfaceView 
	implements SurfaceHolder.Callback, Camera.PictureCallback {
	private SurfaceHolder surfaceHolder;
	private Camera camera;
    private static ContentResolver contentResolver = null;
    
    private static final String SD_CARD = "/sdcard/";

	public CameraView(Context context) {
        super(context);

        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        
		contentResolver = context.getContentResolver();
    }

    @Override
	public void surfaceCreated(SurfaceHolder holder) {
    	camera = Camera.open();
    	setCameraParameters(camera);
        try {
        	camera.setPreviewDisplay(holder);
        } catch (Exception e) {
        	cameraRelease();
        }
    }
    
    private void setCameraParameters(Camera camera) {
        Parameters parameters = camera.getParameters();
        parameters.setPictureSize(480, 320);	//Default:2048x1536
        camera.setParameters(parameters);
    }

    @Override
	public void surfaceDestroyed(SurfaceHolder holder) {
    	cameraRelease();
    }

    @Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {    	
        Parameters parameters = camera.getParameters();
        parameters.setPreviewSize(width, height);
        camera.setParameters(parameters);
        camera.startPreview();
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            camera.takePicture(null, null, this);
            camera.startPreview();
        }
        return true;
    }
    
    @Override
	public void onPictureTaken(byte[] data, Camera camera) {
        try {
        	String dataName = "photo_" + String.valueOf(Calendar.getInstance().getTimeInMillis()) + ".jpg";
        	//saveDataToSdCard(data, dataName);
        	saveDataToURI(data, dataName);
        } catch (Exception e) {
            cameraRelease();
        }
    }

    private void saveDataToSdCard(byte[] data, String dataName) throws Exception {
        FileOutputStream fileOutputStream = null;
        try {
        	fileOutputStream = new FileOutputStream(SD_CARD + dataName);
        	fileOutputStream.write(data);
        } catch (Exception e) {
            cameraRelease();
        } finally {
            if (fileOutputStream != null) {
            	fileOutputStream.close();
            }
        }
    }
        
    private void saveDataToURI(byte[] data, String dataName) {
    	Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
    	ContentValues values = new ContentValues();
    	values.put(MediaColumns.DISPLAY_NAME, dataName);
    	values.put(ImageColumns.DESCRIPTION, "taken with G1");
    	values.put(MediaColumns.MIME_TYPE, "image/jpeg");
    	Uri uri = contentResolver.insert(Media.EXTERNAL_CONTENT_URI, values);
    	try {
    	    OutputStream outStream = contentResolver.openOutputStream(uri);
    	    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outStream);
    	    outStream.close();
    	} catch (Exception e) {
            cameraRelease();
    	}
    }
    
    private void cameraRelease() {
    	if (camera != null) {
        	camera.release();
        	camera = null;
    	}
    }
    
    public void onResume() {
    	cameraRelease();
    }

    public void onPause() {
    	cameraRelease();
    }
    
    
}
