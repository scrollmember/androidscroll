package jp.ac.tokushima_u.is.ll.ui.nav;
/**
 * 
 * @author Kousuke Mouri University of Tokushima,Japan
 * 
 */
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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;

public class CameraNav extends Activity {

	// Declaration
	Context context = this;
	CamView mCamView; // Camera View (Layer) Class
	Compass compass; // Compass View (Layer) Class
	GPSLocation mGPSLocation; // GPS Location
	Button ObjListButton;
	// GetObjectsTask getobject;
	// List<ItemModel> ObjList; // List of the surrounding Objects
	double[] ObjLatList, ObjLngList;
	String[] ObjTitles; // List of the titles of surrounding Objects
	double lat = 0, lng = 0; // Current Location

	boolean AutoPhotoFlag = false; // Flag for Auto Photo Option
	Handler Timerhandler = new Handler(); // Timer Handler for Auto Photo
	Timer updateTimer; // Timer for Auto Photo

	// MultiThreading
	private Handler guiThread; // GUI Thread
	private ExecutorService GetObjectsThread; // Get surrounding objects thread
	private Runnable updateTask;
	private Future GetObjectsPending; // Return results

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

		// Initialize MultitThreading
		initThreading();

		// Get GPS Location
		mGPSLocation = new GPSLocation(CameraNav.this);

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

		// Toast.makeText(this,".........onCreate .........",Toast.LENGTH_LONG).show();

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

	// ----------------------------------------------------------------------
	protected void CreateMenuLayer() {

		// Create Overlay Menu
		final LinearLayout menuLayout;
		menuLayout = new LinearLayout(this);
		// 垂直方向に指定
		menuLayout.setOrientation(LinearLayout.VERTICAL);

		// Create menu buttons
		Button ExitButton = new Button(this);
		ExitButton.setText("   Exit  ");
		ObjListButton = new Button(this);
		ObjListButton.setText("   Objects   ");
		final Button PhotoButton = new Button(this);
		PhotoButton.setText("   Photo   ");
		final Button APhotoButton = new Button(this);
		APhotoButton.setText("   AutoPhoto   ");
		// 追加
		final Button RADARButton = new Button(this);

		RADARButton.setText("   RADAR   ");
//		// 追加
//		 final Button MAPButton = new Button(this);
//		
//		 MAPButton.setText ("   MAP   " );
		// 追加
		// final Button QRButton = new Button(this);
		//
		// QRButton.setText ("   QRCODE   " );

		ObjListButton.setEnabled(false);
		// レイアウトの追加
		menuLayout.addView(ExitButton);
		menuLayout.addView(ObjListButton);
		menuLayout.addView(PhotoButton);
		menuLayout.addView(APhotoButton);
		menuLayout.addView(RADARButton);
//		 menuLayout.addView(MAPButton);
		// menuLayout.addView(QRButton);
		// addContentViewは複数のContentViewを重ね合わせることができる
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

		// ObjListButton.setOnClickListener(new OnClickListener()
		// { @Override
		// public void onClick(View v)
		// {
		// if (ObjList!=null)
		// {
		//
		// final boolean [] checkedItems = new boolean[ObjTitles.length];
		// DialogInterface.OnMultiChoiceClickListener listener = null;
		//
		// // Create Dialog for ObjList
		// new AlertDialog.Builder(CameraNav.this)
		// .setTitle("Surrounding Objects")
		// .setCancelable(true) // Disable back key (or any Cancel action)
		// //.setItems(ObjTitles,
		// .setMultiChoiceItems (ObjTitles, checkedItems,new
		// DialogInterface.OnMultiChoiceClickListener()
		// {
		// @Override
		// public void onClick(DialogInterface dialog, int which,boolean
		// isChecked)
		// {
		// // TODO Auto-generated method stub
		//
		// }} )
		// .setPositiveButton( "OK",
		// new DialogInterface.OnClickListener()
		// {
		// public void onClick(DialogInterface dialog, int arg1)
		// {
		// int sel=0;
		// for (int i=0;i<ObjTitles.length;i++)
		// if (checkedItems[i]!=false) sel++;
		//
		// if (sel>0)
		// { double [] iLat=new double[sel];
		// double [] iLng=new double[sel];
		// String [] iTitle=new String[sel];
		// int j=0;
		// for (int i=0;i<ObjTitles.length;i++)
		// {
		// if (checkedItems[i]!=false)
		// {
		// iLat[j]=ObjLatList[i];
		// iLng[j]=ObjLngList[i];
		// iTitle[j]=ObjTitles[i];
		// j++;
		// }
		//
		// }
		// Intent MapNavi = new Intent(CameraNav.this, llMapNavi.class);
		// MapNavi.putExtra("itemsLat", iLat);
		// MapNavi.putExtra("itemsLng", iLng);
		// MapNavi.putExtra("itemsTitle", iTitle);
		// CameraNav.this.startActivity(MapNavi);
		// }
		// }
		// }) // setItems*/
		// .show();
		// }
		// }
		// });

		PhotoButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { // Take Photo
				mCamView.takePicture(lat, lng);

				// try{
				// writeDB("lat");
				// }catch(Exception e){

				// }
			};
		});

		APhotoButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { // Auto Photo Switch

				if (AutoPhotoFlag) // is on
				{
					AutoPhotoFlag = false;
					APhotoButton.setTextColor(Color.BLACK);
					PhotoButton.setEnabled(true);
					updateTimer.cancel();
					updateTimer.purge();
					updateTimer = null;
				} else if (!AutoPhotoFlag) // is off
				{ // Timer for Auto Photo
					if (updateTimer == null) {
						updateTimer = new Timer("AutoPhoto");
						// Create Dialog for Timer Option
						new AlertDialog.Builder(CameraNav.this)
								.setTitle("Timer")
								.setCancelable(false)
								// Disable back key (or any Cancel action)
								.setItems(R.array.Timer,
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface dialoginterface,
													int i) {
												int mseconds = -1;
												switch (i) {
												case 0:
													mseconds = 60000;
													break;
												case 1:
													mseconds = 300000;
													break;
												case 2:
													mseconds = 600000;
													break;
												} // switch
													// The timer action
												updateTimer
														.scheduleAtFixedRate(
																new TimerTask() {
																	@Override
																	public void run() {
																		TakeAutoPhoto();

																	}
																}, mseconds,
																mseconds); // update
																			// timer

												AutoPhotoFlag = true;
												APhotoButton
														.setTextColor(Color.RED);
												PhotoButton.setEnabled(false);
											} // on click
										}) // setItems
								.show();
					} // If update Timer
				} // else if
			}; // on click
		}); // APhotoButton.setOnClickListener
		// 追加
		RADARButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent CameraModeIntent = new Intent(CameraNav.this,
						radar.class);
				// CameraModeIntent.putExtra("userEmail", userEmail);
				// CameraModeIntent.putExtra("userPassword", userPassword);
				CameraNav.this.startActivity(CameraModeIntent);

			};
		});
//		 MAPButton.setOnClickListener(new OnClickListener()
//		 { @Override
//		 public void onClick(View v)
//		 {
//		 Intent CameraModeIntent = new Intent(CameraNav.this, Map.class);
//		 //CameraModeIntent.putExtra("userEmail", userEmail);
//		 //CameraModeIntent.putExtra("userPassword", userPassword);
//		 CameraNav.this.startActivity(CameraModeIntent);
//		
//		 };
//		 });

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

		/*
		 * // Listener for CurrentLocation change
		 * CurrentLocation.registerOnSharedPreferenceChangeListener(new
		 * SharedPreferences.OnSharedPreferenceChangeListener() { public void
		 * onSharedPreferenceChanged(SharedPreferences sharedPreferences, String
		 * key) { float lat=CurrentLocation.getFloat("lat", 0); float
		 * lng=CurrentLocation.getFloat("lng", 0); if ((lat!=0)&&(lng!=0))
		 * //updateItemRef(); } });
		 */

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
					// GetObjectsTask getObjectsTask = new
					// GetObjectsTask(CameraNav.this,lat,lng);
					// GetObjectsPending =
					// GetObjectsThread.submit(getObjectsTask);
					// Log.v("start new task", "start new task");
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

	// public void FillObjList(List<ItemModel> ItemList, double [] ItemLatList,
	// double [] ItemLngList, String[]items)
	// {
	// ObjList=ItemList;
	// ObjTitles=items;
	//
	// ObjLatList= ItemLatList;
	// ObjLngList= ItemLngList;
	//
	//
	//
	// }

} // end nav class

