/**
 * 
 * @author 徳島大学　Kousuke Mouri
 * 
 */
package jp.ac.tokushima_u.is.ll.ui.navTask;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

import jp.ac.tokushima_u.is.ll.R;

//import jp.ac.tokushima_u.is.ll.ws.service.model.ItemModel;
import android.content.DialogInterface;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class CameraTask extends Activity {

	// Declaration
	Context context = this;
	CamView mCamView;
	Compass compass;
	TaskGPS mGPSLocation;
	Button ObjListButton;
	double[] ObjLatList, ObjLngList;
	String[] ObjTitles;
	double lat = 0, lng = 0;

	boolean AutoPhotoFlag = false;
	Handler Timerhandler = new Handler();
	Timer updateTimer;
	private Handler guiThread; // GUI Thread
	private ExecutorService GetObjectsThread; // Get surrounding objects thread
	private Runnable updateTask;
	private Future GetObjectsPending; // Return results
	private final int WC1 = ViewGroup.LayoutParams.WRAP_CONTENT; 
	private final int FP=ViewGroup.LayoutParams.FILL_PARENT;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String userEmail = null;
		// Receive intent data
		Intent intent = this.getIntent();

		if (intent != null) {
			userEmail = intent.getStringExtra("userEmail");
			String userPassword = intent.getStringExtra("userPassword");
			lat = intent.getDoubleExtra("lat", 0);
			lng = intent.getDoubleExtra("lng", 0);
			SharedPreferences.Editor editor;
			SharedPreferences user = getSharedPreferences("User",
					Context.MODE_PRIVATE);
			editor = user.edit();
			editor.putString("userEmail", userEmail);
			editor.putString("userPassword", userPassword);
			editor.commit();
		} else {
			finish();
		}

		// Open Log File

		// Get Camera View and set it as the content of our activity.
		mCamView = new CamView(this);

		setContentView(mCamView);

		// Initialize Items Shared References
		InitItemsRef();

		// Create Compass and Alert Layer
		compass = new Compass(this);

		// Create Menu Layer
		CreateMenuLayer();

		// Task_display
		Task_display();
		
		// Initialize MultitThreading
		initThreading();

		// Get GPS Location
		mGPSLocation = new TaskGPS(CameraTask.this);

		// Get object list id lan and lng are known
		if ((lat != 0) && (lng != 0)) {

			int mode = Context.MODE_PRIVATE;
			SharedPreferences.Editor editor;
			SharedPreferences CurrentLocation = getSharedPreferences(
					"CurrentLocation", mode);
			editor = CurrentLocation.edit();
			editor.putFloat("lat", (float) lat);
			editor.putFloat("lng", (float) lng);
			editor.commit();
			// queueUpdate(0); //in millisecond

		}

	} // end onCreate

	// ----------------------------------------------------------------------
	@Override
	protected void onPause() {
		// Cancel previous thread if it hasn't started yet
		guiThread.removeCallbacks(updateTask);

		mGPSLocation.CloseGPS();
		super.onPause();

		// Toast.makeText(this,".........onPause .........",Toast.LENGTH_LONG).show();
	}

	// ----------------------------------------------------------------------
	@Override
	protected void onResume() {
		// Cancel previous thread if it hasn't started yet
		guiThread.removeCallbacks(updateTask);

		mGPSLocation.OpenGPSListener();
		super.onResume();

		// Toast.makeText(this,".........onResume .........",Toast.LENGTH_LONG).show();
	}

	// ----------------------------------------------------------------------
	@Override
	protected void onDestroy() {
		// Terminate extra threads here
		GetObjectsThread.shutdownNow();
		// CLose the log file

		// Close Orientation Sensor Listener
		compass.FinishSensor();
		// Close GPS Listener
		mGPSLocation.CloseGPS();
		// Close the Auto Photo timer
		if (updateTimer != null) {
			updateTimer.cancel();
			updateTimer.purge();
			updateTimer = null;
		}

		super.onDestroy();

		// Toast.makeText(this,".........onDestroy .........",Toast.LENGTH_LONG).show();

	}

	public void Task_display(){
		WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
		Display disp = wm.getDefaultDisplay(); 
		int width = disp.getWidth();
		int height = disp.getHeight();
		if(width<500&&height<810){
			AbsoluteLayout absoluteLayout = new AbsoluteLayout(this); 
			Button Taskbutton = new Button(this);   
			Taskbutton.setText("スーパーへ大根、ちくわ、おでんスープを買いましょう\nLet's you buy daikon,tikuwa and oden in the supermarket.");
			absoluteLayout.addView(Taskbutton, new AbsoluteLayout.LayoutParams(150,50,80,height-100));  
			addContentView(absoluteLayout, new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT));
		}
		else{
		AbsoluteLayout absoluteLayout = new AbsoluteLayout(this); 
		Button Taskbutton = new Button(this);   
		Taskbutton.setBackgroundColor(Color.WHITE);
		Taskbutton.setText("スーパーへ大根、ちくわ、おでんスープを買いましょう\nLet's you buy daikon,tikuwa and oden in the supermarket.");
		absoluteLayout.addView(Taskbutton, new AbsoluteLayout.LayoutParams(width-200,FP,120,height-150));  
		addContentView(absoluteLayout, new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		}
		

	}
	
	
	// ----------------------------------------------------------------------
	protected void CreateMenuLayer() {

		// Create Overlay Menu
		final LinearLayout menuLayout;
		menuLayout = new LinearLayout(this);
		// 垂直方向に指定
		menuLayout.setOrientation(LinearLayout.VERTICAL);

		// Create menu buttons
		final Button PhotoButton = new Button(this);
		PhotoButton.setText("   Photo   ");
		final Button NextScriptButton = new Button(this);
		NextScriptButton.setText("   Next Task   ");
		Button ExitButton = new Button(this);
		ExitButton.setText("   Exit  ");
		final Button TaskdisplayButton=new Button(this);

		// ObjListButton.setEnabled(false);
		// レイアウトの追加

		// menuLayout.addView(ObjListButton);
		menuLayout.addView(PhotoButton);
		menuLayout.addView(NextScriptButton);
		menuLayout.addView(ExitButton);
		addContentView(menuLayout, new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		menuLayout.setVisibility(View.INVISIBLE); // Hide it

		// Menu buttons Lister
		ExitButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Terminate extra threads here
				GetObjectsThread.shutdownNow();
				// Close Orientation Sensor Listener
				compass.FinishSensor();
				// Close GPS Listener
				mGPSLocation.CloseGPS();
				// Close the Auto Photo timer
				if (updateTimer != null) {
					updateTimer.cancel();
					updateTimer.purge();
					updateTimer = null;
				}

				// Close the activity
				finish();
			};
		});

		PhotoButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { // Take Photo
				mCamView.takePicture(lat, lng);

			};
		});

		// Display or Hide the menu
		mCamView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if ((menuLayout.getVisibility()) == View.INVISIBLE) {
					menuLayout.setVisibility(View.VISIBLE);
				} else if ((menuLayout.getVisibility()) == View.VISIBLE) {
					menuLayout.setVisibility(View.INVISIBLE);
				}
			};
		}); // mCamView.setOnClickListener
	} // CreateMenuLayer

	// ----------------------------------------------------------------------

	// Take Photo when the Timer fire
	private void TakeAutoPhoto() {
		Timerhandler.post(new Runnable() {
			@Override
			public void run() {
				if (hasWindowFocus()) // Only take Photo when the application is
										// on Focus
				{
					mCamView.takePicture(lat, lng);

				}
			}
		}); // Timerhandler.post
	} // TakeAutoPhoto

	// ----------------------------------------------------------------------
	void InitItemsRef() {
		int mode = Context.MODE_PRIVATE;
		SharedPreferences.Editor editor;

		SharedPreferences TrueNorthItem = getSharedPreferences("TrueNorthItem",
				mode);
		editor = TrueNorthItem.edit();
		// ぷリファレンスのキーと値をすべて削除
		editor.clear();
		// データの保存はeditor.commit();
		editor.commit();

		SharedPreferences TrueEastItem = getSharedPreferences("TrueEastItem",
				mode);
		editor = TrueEastItem.edit();
		editor.clear();
		editor.commit();

		SharedPreferences TrueSouthItem = getSharedPreferences("TrueSouthItem",
				mode);
		editor = TrueSouthItem.edit();
		editor.clear();
		editor.commit();

		SharedPreferences TrueWestItem = getSharedPreferences("TrueWestItem",
				mode);
		editor = TrueWestItem.edit();
		editor.clear();
		editor.commit();

		// Initialize Item change flag in the SharedPreferences
		SharedPreferences updateRef = getSharedPreferences("ItemRefUpdated",
				mode);
		editor = updateRef.edit();
		editor.putBoolean("update", false);
		editor.commit();

		// Initialize current location in the SharedPreferences
		final SharedPreferences CurrentLocation = getSharedPreferences(
				"CurrentLocation", mode);
		editor = CurrentLocation.edit();
		editor.clear();
		editor.commit();

	}

	// ----------------------------------------------------------------------
	// Initialize multiThreading. There are two threads: 1) The main
	// graphical user interface thread already started by Android,
	// and 2) The GetObjects thread, which we start using an executor.
	private void initThreading() {
		guiThread = new Handler();
		// Executorsクラスで必要なクラスを作成　newSingleThreadExecutorで単一のスレッドを作成
		GetObjectsThread = Executors.newSingleThreadExecutor();
		updateTask = new Runnable() {
			@Override
			public void run() {
				// Cancel previous GetObjects task if there was one
				if (GetObjectsPending != null)
					// タスクの実行の取り消しを試みます
					GetObjectsPending.cancel(true);

				try {

				} catch (RejectedExecutionException e) {
					// Unable to start new task
					Log.v("Unable to start new task",
							"Unable to start new task");
				}
			}
		};

	}

	// Start GetObjectsThread after a short delay
	public void queueUpdate(long delayMillis) { // Cancel previous thread if it
												// hasn't started yet
		guiThread.removeCallbacks(updateTask);
		// Start the thread if nothing happens after a few milliseconds
		guiThread.postDelayed(updateTask, delayMillis);

		// Empty last location objects

	}

	// Update the Alerts after GetObjects
	public void UpdateAlert() {
		guiThread.post(new Runnable() {
			@Override
			public void run() {
				compass.updateGrid();

				if (ObjTitles.length > 0) {
					ObjListButton.setEnabled(true);
				} else {
					ObjListButton.setEnabled(false);
				}
			}
		});
	}

} // end nav class

