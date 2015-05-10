/**
 * 
 * @author 徳島大学　Kousuke Mouri
 * 
 */
package jp.ac.tokushima_u.is.ll.ui.navTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import java.lang.Math;
import android.view.MotionEvent;

import jp.ac.tokushima_u.is.ll.R;

public class Compass extends CameraTask {

	public float deviceDegreeToNorth;
	public float predeviceDegreeToNorth = -1f;

	Button Button12, Button21, Button23, Button32;
	AlertDialog.Builder adB12, adB21, adB23, adB32;
	SensorManager mSensorManager;
	CompassView mCompassView;
	Context mContext;
	String DeviceNorthItem, DeviceEastItem, DeviceSouthItem, DeviceWestItem;
	double[] ObjLatList, ObjLngList;
	public float CenterX;
	public float CenterY;
	public float radius;
	public int gridWidth;
	public int gridHeight;

	public Compass(Context context) {
		deviceDegreeToNorth = -1f;
		mContext = context;

		SharedPreferences screen = context.getSharedPreferences("screen",
				Context.MODE_PRIVATE);
		int width = screen.getInt("width", 0);
		;
		int height = screen.getInt("height", 0);
		if (height == 0) {
			Display display = ((WindowManager) context
					.getSystemService(Context.WINDOW_SERVICE))
					.getDefaultDisplay();
			width = display.getWidth();
			height = display.getHeight();
			SharedPreferences.Editor e;
			e = screen.edit();
			e.putInt("height", height);
			e.putInt("width", width);
			e.commit();

		}

		CenterX = width / 2;
		CenterY = (height - 50) / 2;
		// 追加

		radius = 40;

		gridWidth = width;
		gridHeight = height - 50;
		// 追加　8->6
		radius = height / 6;

		// Get Compass Sensor
		mSensorManager = (SensorManager) context
				.getSystemService(Context.SENSOR_SERVICE);
		mSensorManager.registerListener(SENSOR_ORIENTATION_Listener,
				mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
				SensorManager.SENSOR_DELAY_NORMAL);
		// Create Overlay CompassView
		mCompassView = new CompassView(context);
		((Activity) context).addContentView(mCompassView, new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

	}

	SensorEventListener SENSOR_ORIENTATION_Listener = new SensorEventListener() {

		@Override
		public void onAccuracyChanged(Sensor arg0, int i) {
		}

		@Override
		public void onSensorChanged(SensorEvent evt) {
			if (evt.sensor.getType() == Sensor.TYPE_ORIENTATION) {
				float vals[] = evt.values;
				deviceDegreeToNorth = vals[0];
				float accuracy = Math.abs(deviceDegreeToNorth
						- predeviceDegreeToNorth);
				if ((predeviceDegreeToNorth == -1f) || (accuracy >= 2f)) {
					predeviceDegreeToNorth = deviceDegreeToNorth;
					if (deviceDegreeToNorth <= 270) {
						deviceDegreeToNorth = 270 - deviceDegreeToNorth;
					} else {
						deviceDegreeToNorth = 360 + (270 - deviceDegreeToNorth);

					}
					mCompassView.invalidate();
					updateGrid();
					// update and rotate the Tags

				}
			}

		}
	};

	public void FinishSensor() {
		mSensorManager.unregisterListener(SENSOR_ORIENTATION_Listener);
	}

	class CompassView extends View {
		public DrawOnTop mDraw;
		// 線の太さ
		Compass compass;
		float CircleWidth = 1;
		float CharWidth = 2;
		float CharSize = 15;
		Paint paint = new Paint();
		RectF rect;
		// itemballの定義
		private Paint myPaint = new Paint();
		private Bitmap myBitmap;

		CompassView(Context context) {
			super(context);
			setKeepScreenOn(true);
			InitGrid(context); // Create Tags grid

		}

		@Override
		public boolean onTouchEvent(MotionEvent motion) {
			int x = (int) motion.getX();
			int y = (int) motion.getY();
			int mode = Activity.MODE_PRIVATE;
			SharedPreferences.Editor editor;
			SharedPreferences TOUCH = this.getContext().getSharedPreferences(
					"TOUCH", mode);
			editor = TOUCH.edit();
			editor.putInt("TouchX", x);
			editor.putInt("TouchY", y);
			editor.commit();

			return false;
		}

		@Override
		// protected void onDraw(Canvas canvas)
		public void onDraw(Canvas canvas) {
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(CircleWidth);
			paint.setColor(Color.BLACK);
			paint.setAlpha(30);
			paint.setAntiAlias(true);
			canvas.drawCircle(CenterX, CenterY, radius, paint); // Draw Compass

			SharedPreferences screen = this.getContext().getSharedPreferences(
					"screen", Context.MODE_PRIVATE);
			int width = screen.getInt("width", 0);
			int height = screen.getInt("height", 0);
			SharedPreferences touch = this.getContext().getSharedPreferences(
					"TOUCH", Context.MODE_PRIVATE);
			int touchx = touch.getInt("TouchX", 0);
			int touchy = touch.getInt("TouchY", 0);
			int X = width / 2;
			int Y = (height - 50) / 2;

			// Circle shape
			// 追加

			paint.setColor(Color.RED);
			canvas.drawCircle(CenterX, CenterY, 5, paint);

			// ここまで
			if (deviceDegreeToNorth >= 0f) { // Show Degreefloat gpsdistance[];
				Resources res = this.getContext().getResources();
				/* 画像の読み込み(res/drawable/gclue.png) */
				myBitmap = BitmapFactory.decodeResource(res,
						R.drawable.itemball);

				paint.setStrokeWidth(1);
				paint.setTextSize(15);
				paint.setColor(Color.WHITE);
				// String NorthDegree="N:"+deviceDegreeToNorth;

				SharedPreferences DATA = this.getContext()
						.getSharedPreferences("DATALATANDLNG",
								Activity.MODE_PRIVATE);

				SharedPreferences CurrentLocation = this.getContext()
						.getSharedPreferences("GPSDATA", Activity.MODE_PRIVATE);
				int count = DATA.getInt("COUNT", 0);
				float[] gpsdistance = new float[count];
				float[] houkou1 = new float[count];
				float[] houkou2 = new float[count];
				float[] objectX = new float[count];
				float[] objectY = new float[count];
				int i = 0;

				// rotate the compass indicator
				canvas.rotate(deviceDegreeToNorth, CenterX, CenterY);
				// Draw Direction letters
				paint.setStrokeWidth(CharWidth);
				paint.setTextSize(CharSize);
				paint.setColor(Color.WHITE);
				canvas.drawText("N", CenterX, CenterY - radius + CircleWidth
						/ 2, paint);
				canvas.drawText("E", CenterX + radius, CenterY, paint);
				canvas.drawText("S", CenterX, CenterY + radius + CircleWidth
						/ 2, paint);
				canvas.drawText("W", CenterX - radius, CenterY, paint);

				for (i = 0; i < count; i++) {
					double datafiled = 0;
					SharedPreferences radarvalues = this.getContext()
							.getSharedPreferences("RADARVALUES",
									Activity.MODE_PRIVATE);
					int sakutekihani = radarvalues.getInt("distance", 0);
					int results = radarvalues.getInt("hantei", 0);
					if (results == 1) {

						datafiled = 1 / ((double) sakutekihani / 100);
						gpsdistance[i] = CurrentLocation.getFloat("gpsdistance"
								+ i, 0);
						houkou1[i] = CurrentLocation.getFloat("cos" + i, 0);
						houkou2[i] = CurrentLocation.getFloat("sin" + i, 0);
						objectX[i] = gpsdistance[i] * houkou1[i]
								* (float) datafiled;
						objectY[i] = gpsdistance[i] * houkou2[i]
								* (float) datafiled;
						if (objectX[i] < 10 && objectY[i] < 10
								&& objectX[i] > -10 && objectY[i] > -10) {
							paint.setColor(Color.GREEN);
							canvas.drawBitmap(myBitmap, CenterX
									+ (int) objectX[i], CenterY
									+ (int) objectY[i], myPaint);

							if (touchx == (X + (int) objectX[i])
									&& touchy == (Y + (int) objectY[i])) {
								int a = 10;
								finish();
							}

						}
					} else if (results == 2) {

						datafiled = 1 / ((double) sakutekihani / 100);
						gpsdistance[i] = CurrentLocation.getFloat("gpsdistance"
								+ i, 0);
						houkou1[i] = CurrentLocation.getFloat("cos" + i, 0);
						houkou2[i] = CurrentLocation.getFloat("sin" + i, 0);
						objectX[i] = gpsdistance[i] * houkou1[i]
								* (float) datafiled;
						objectY[i] = gpsdistance[i] * houkou2[i]
								* (float) datafiled;

						if (objectX[i] < 20 && objectY[i] < 20
								&& objectX[i] > -20 && objectY[i] > -20) {
							paint.setColor(Color.GREEN);
							canvas.drawBitmap(myBitmap, CenterX
									+ (int) objectX[i], CenterY
									+ (int) objectY[i], myPaint);

							if (touchx == (X + (int) objectX[i])
									&& touchy == (Y + (int) objectY[i])) {
								int a = 10;
								finish();
							}
						}
					} else if (results == 3) {

						datafiled = 1 / ((double) sakutekihani / 100);
						gpsdistance[i] = CurrentLocation.getFloat("gpsdistance"
								+ i, 0);
						houkou1[i] = CurrentLocation.getFloat("cos" + i, 0);
						houkou2[i] = CurrentLocation.getFloat("sin" + i, 0);
						objectX[i] = gpsdistance[i] * houkou1[i]
								* (float) datafiled;
						objectY[i] = gpsdistance[i] * houkou2[i]
								* (float) datafiled;
						if (objectX[i] < 30 && objectY[i] < 30
								&& objectX[i] > -30 && objectY[i] > -30) {
							paint.setColor(Color.GREEN);
							canvas.drawBitmap(myBitmap, CenterX
									+ (int) objectX[i], CenterY
									+ (int) objectY[i], myPaint);
							// canvas.drawCircle(CenterX + (int) objectX[i],
							// CenterY + (int) objectY[i], 3, paint);

						}
					} else if (results == 4) {

						datafiled = 1 / ((double) sakutekihani / 100);
						gpsdistance[i] = CurrentLocation.getFloat("gpsdistance"
								+ i, 0);
						houkou1[i] = CurrentLocation.getFloat("cos" + i, 0);
						houkou2[i] = CurrentLocation.getFloat("sin" + i, 0);
						objectX[i] = gpsdistance[i] * houkou1[i]
								* (float) datafiled;
						objectY[i] = gpsdistance[i] * houkou2[i]
								* (float) datafiled;

						if (objectX[i] < 40 && objectY[i] < 40
								&& objectX[i] > -40 && objectY[i] > -40) {
							paint.setColor(Color.GREEN);
							canvas.drawBitmap(myBitmap, CenterX
									+ (int) objectX[i], CenterY
									+ (int) objectY[i], myPaint);
							// canvas.drawCircle(CenterX + (int) objectX[i],
							// CenterY + (int) objectY[i], 3, paint);
							int s = touchx;
							int s1 = touchy;
							int d = X + (int) objectX[i];
							int d1 = Y + (int) objectY[i];
							if (touchx == (X + (int) objectX[i])
									&& touchy == (Y + (int) objectY[i])) {
								int a = 10;
								finish();
							}
						}
					} else if (results == 5) {

						datafiled = 1 / ((double) sakutekihani / 100);
						gpsdistance[i] = CurrentLocation.getFloat("gpsdistance"
								+ i, 0);
						houkou1[i] = CurrentLocation.getFloat("cos" + i, 0);
						houkou2[i] = CurrentLocation.getFloat("sin" + i, 0);
						objectX[i] = gpsdistance[i] * houkou1[i]
								* (float) datafiled;
						objectY[i] = gpsdistance[i] * houkou2[i]
								* (float) datafiled;
						if (objectX[i] < 50 && objectY[i] < 50
								&& objectX[i] > -50 && objectY[i] > -50) {
							paint.setColor(Color.GREEN);
							canvas.drawBitmap(myBitmap, CenterX
									+ (int) objectX[i], CenterY
									+ (int) objectY[i], myPaint);
							// canvas.drawCircle(CenterX + (int) objectX[i],
							// CenterY + (int) objectY[i], 3, paint);

						}
					} else if (results == 6) {

						datafiled = 1 / ((double) sakutekihani / 100);
						gpsdistance[i] = CurrentLocation.getFloat("gpsdistance"
								+ i, 0);
						houkou1[i] = CurrentLocation.getFloat("cos" + i, 0);
						houkou2[i] = CurrentLocation.getFloat("sin" + i, 0);
						objectX[i] = gpsdistance[i] * houkou1[i]
								* (float) datafiled;
						objectY[i] = gpsdistance[i] * houkou2[i]
								* (float) datafiled;
						if (objectX[i] < 60 && objectY[i] < 60
								&& objectX[i] > -60 && objectY[i] > -60) {
							paint.setColor(Color.GREEN);
							canvas.drawBitmap(myBitmap, CenterX
									+ (int) objectX[i], CenterY
									+ (int) objectY[i], myPaint);
							// canvas.drawCircle(CenterX + (int) objectX[i],
							// CenterY + (int) objectY[i], 3, paint);

						}
					} else if (results == 7) {

						datafiled = 1 / ((double) sakutekihani / 100);
						gpsdistance[i] = CurrentLocation.getFloat("gpsdistance"
								+ i, 0);
						houkou1[i] = CurrentLocation.getFloat("cos" + i, 0);
						houkou2[i] = CurrentLocation.getFloat("sin" + i, 0);
						objectX[i] = gpsdistance[i] * houkou1[i]
								* (float) datafiled;
						objectY[i] = gpsdistance[i] * houkou2[i]
								* (float) datafiled;
						if (objectX[i] < 70 && objectY[i] < 70
								&& objectX[i] > -70 && objectY[i] > -70) {
							paint.setColor(Color.GREEN);
							canvas.drawBitmap(myBitmap, CenterX
									+ (int) objectX[i], CenterY
									+ (int) objectY[i], myPaint);
							// canvas.drawCircle(CenterX + (int) objectX[i],
							// CenterY + (int) objectY[i], 3, paint);

						}
					} else if (results == 8) {

						datafiled = 1 / ((double) sakutekihani / 100);
						gpsdistance[i] = CurrentLocation.getFloat("gpsdistance"
								+ i, 0);
						houkou1[i] = CurrentLocation.getFloat("cos" + i, 0);
						houkou2[i] = CurrentLocation.getFloat("sin" + i, 0);
						objectX[i] = gpsdistance[i] * houkou1[i]
								* (float) datafiled;
						objectY[i] = gpsdistance[i] * houkou2[i]
								* (float) datafiled;
						if (objectX[i] < 80 && objectY[i] < 80
								&& objectX[i] > -80 && objectY[i] > -80) {
							paint.setColor(Color.GREEN);
							canvas.drawBitmap(myBitmap, CenterX
									+ (int) objectX[i], CenterY
									+ (int) objectY[i], myPaint);
							// canvas.drawCircle(CenterX + (int) objectX[i],
							// CenterY + (int) objectY[i], 3, paint);

						}
					} else if (results == 9) {

						datafiled = 1 / ((double) sakutekihani / 100);
						gpsdistance[i] = CurrentLocation.getFloat("gpsdistance"
								+ i, 0);
						houkou1[i] = CurrentLocation.getFloat("cos" + i, 0);
						houkou2[i] = CurrentLocation.getFloat("sin" + i, 0);
						objectX[i] = gpsdistance[i] * houkou1[i]
								* (float) datafiled;
						objectY[i] = gpsdistance[i] * houkou2[i]
								* (float) datafiled;
						if (objectX[i] < 90 && objectY[i] < 90
								&& objectX[i] > -90 && objectY[i] > -90) {
							paint.setColor(Color.GREEN);
							canvas.drawBitmap(myBitmap, CenterX
									+ (int) objectX[i], CenterY
									+ (int) objectY[i], myPaint);
							// canvas.drawCircle(CenterX + (int) objectX[i],
							// CenterY + (int) objectY[i], 3, paint);

						}
					} else if (results == 10) {

						datafiled = 1 / ((double) sakutekihani / 100);
						gpsdistance[i] = CurrentLocation.getFloat("gpsdistance"
								+ i, 0);
						houkou1[i] = CurrentLocation.getFloat("cos" + i, 0);
						houkou2[i] = CurrentLocation.getFloat("sin" + i, 0);
						objectX[i] = gpsdistance[i] * houkou1[i]
								* (float) datafiled;
						objectY[i] = gpsdistance[i] * houkou2[i]
								* (float) datafiled;
						if (objectX[i] < 100 && objectY[i] < 100
								&& objectX[i] > -100 && objectY[i] > -100) {
							paint.setColor(Color.GREEN);
							canvas.drawBitmap(myBitmap, CenterX
									+ (int) objectX[i], CenterY
									+ (int) objectY[i], myPaint);
							// canvas.drawCircle(CenterX + (int) objectX[i],
							// CenterY + (int) objectY[i], 3, paint);

						}
					} else if (results == 11) {
						datafiled = 1 / ((double) sakutekihani / 100);
						gpsdistance[i] = CurrentLocation.getFloat("gpsdistance"
								+ i, 0);
						houkou1[i] = 0;
						houkou2[i] = 0;
						// objectX[i] = gpsdistance[i] * houkou1[i]
						// * (float) datafiled;
						// objectY[i] = gpsdistance[i] * houkou2[i]
						// * (float) datafiled;
						// if (objectX[i] < 100 && objectY[i] < 100
						// && objectX[i] > -100 && objectY[i] > -100) {
						// paint.setColor(Color.GREEN);
						// canvas.drawCircle(CenterX + (int) objectX[i],
						// CenterY + (int) objectY[i], 3, paint);
						//
						// }
					}

					else {
						datafiled = 1;
					}

				}

			}
			super.onDraw(canvas);
		}
	}

	// CompassView Class



	void InitGrid(final Context context) {
		TableLayout grid; // Create Grid of Buttons 3x3 overly Camera View
		grid = new TableLayout(context);
		grid.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));
		grid.setOrientation(LinearLayout.VERTICAL);
		grid.setPadding(0, 0, 0, 0);

		int BKcolor = Color.argb(50, 135, 206, 250);

		// Row 1
		TableRow row1 = new TableRow(context);
		row1.setPadding(0, 0, 0, 0);

		Button Button11 = new Button(context);
		Button11.setPadding(0, 0, 0, 0);
		Button11.setWidth(gridWidth / 3);
		Button11.setHeight(gridHeight / 3);
		row1.addView(Button11);
		Button11.setVisibility(View.INVISIBLE);

		Button12 = new Button(context); // Device North Direction
		Button12.setPadding(0, 0, 0, 0);
		Button12.setWidth(gridWidth / 3);
		Button12.setHeight(gridHeight / 3);
		Button12.setBackgroundColor(BKcolor);
		Button12.setTextColor(Color.WHITE);
		Button12.setVisibility(View.INVISIBLE);
		Button12.setTextSize(20);
		row1.addView(Button12);
		Button12.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Get Item data
				int mode = Activity.MODE_PRIVATE;
				SharedPreferences Item = mContext.getSharedPreferences(
						DeviceNorthItem, mode);
				String title = "Forward Object";
				String dis = "Distance: "
						+ Math.round((Item.getFloat("Distance", 0)) * 1000)
						+ " Meter";
				StringBuilder sb = new StringBuilder();
				sb.append("English  Title: ");
				sb.append(Item.getString("EnTitle", ""));
				sb.append('\n');
				sb.append("Japanese Title: ");
				sb.append(Item.getString("JpTitle", ""));
				sb.append('\n');
				sb.append("Chinese Title: ");
				sb.append(Item.getString("ZhTitle", ""));
				sb.append('\n');
				sb.append(dis);
				String info = sb.toString();

				// Create Item Dialog
				adB12 = new AlertDialog.Builder(context);
				adB12.setCancelable(true);
				adB12.setTitle(title);
				adB12.setMessage(info);
				adB12.setPositiveButton("Map",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int arg1) {
								GetItemMap(DeviceNorthItem);
							}
						});
				adB12.setNegativeButton("Close",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int arg1) {
							}
						});
				adB12.show();
			}
		});

		Button Button13 = new Button(context);
		Button13.setPadding(0, 0, 0, 0);
		Button13.setWidth(gridWidth / 3);
		Button13.setHeight(gridHeight / 3);
		Button13.setVisibility(View.INVISIBLE);
		row1.addView(Button13);
		grid.addView(row1);

		// Row 2
		TableRow row2 = new TableRow(context);
		row2.setPadding(0, 0, 0, 0);

		Button21 = new Button(context); // Device West Direction
		Button21.setPadding(0, 0, 0, 0);
		Button21.setWidth(gridWidth / 3);
		Button21.setHeight(gridHeight / 3);
		Button21.setBackgroundColor(BKcolor);
		Button21.setTextColor(Color.WHITE);
		Button21.setVisibility(View.INVISIBLE);
		Button21.setTextSize(20);
		row2.addView(Button21);
		Button21.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Get Item data
				int mode = Activity.MODE_PRIVATE;
				SharedPreferences Item = mContext.getSharedPreferences(
						DeviceWestItem, mode);
				String title = "Right Object";
				String dis = "Distance: "
						+ Math.round((Item.getFloat("Distance", 0)) * 1000)
						+ " Meter";
				StringBuilder sb = new StringBuilder();
				sb.append("English  Title: ");
				sb.append(Item.getString("EnTitle", ""));
				sb.append('\n');
				sb.append("Japanese Title: ");
				sb.append(Item.getString("JpTitle", ""));
				sb.append('\n');
				sb.append("Chinese Title: ");
				sb.append(Item.getString("ZhTitle", ""));
				sb.append('\n');
				sb.append(dis);
				String info = sb.toString();

				// Create Item Dialog
				adB21 = new AlertDialog.Builder(context);
				adB21.setCancelable(true);
				adB21.setTitle(title);
				adB21.setMessage(info);
				adB21.setPositiveButton("Map",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int arg1) {
								GetItemMap(DeviceWestItem);
							}

						});
				adB21.setNegativeButton("Close",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int arg1) {
							}
						});
				adB21.show();
			}
		});

		Button Button22 = new Button(context);
		Button22.setPadding(0, 0, 0, 0);
		Button22.setWidth(gridWidth / 3);
		Button22.setHeight(gridHeight / 3);
		Button22.setVisibility(View.INVISIBLE);
		row2.addView(Button22);

		Button23 = new Button(context); // Device East Direction
		Button23.setPadding(0, 0, 0, 0);
		Button23.setWidth(gridWidth / 3);
		Button23.setHeight(gridHeight / 3);
		Button23.setBackgroundColor(BKcolor);
		Button23.setTextColor(Color.WHITE);
		Button23.setVisibility(View.INVISIBLE);
		Button23.setTextSize(20);
		row2.addView(Button23);
		grid.addView(row2);
		Button23.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Get Item data
				int mode = Activity.MODE_PRIVATE;
				SharedPreferences Item = mContext.getSharedPreferences(
						DeviceEastItem, mode);
				String title = "Left Object";
				String dis = "Distance: "
						+ Math.round((Item.getFloat("Distance", 0)) * 1000)
						+ " Meter";
				StringBuilder sb = new StringBuilder();
				sb.append("English  Title: ");
				sb.append(Item.getString("EnTitle", ""));
				sb.append('\n');
				sb.append("Japanese Title: ");
				sb.append(Item.getString("JpTitle", ""));
				sb.append('\n');
				sb.append("Chinese Title: ");
				sb.append(Item.getString("ZhTitle", ""));
				sb.append('\n');
				sb.append(dis);
				String info = sb.toString();

				// Create Item Dialog
				adB23 = new AlertDialog.Builder(context);
				adB23.setCancelable(true);
				adB23.setTitle(title);
				adB23.setMessage(info);
				adB23.setPositiveButton("Map",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int arg1) {
								GetItemMap(DeviceEastItem);
							}

						});
				adB23.setNegativeButton("Close",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int arg1) {
							}
						});
				adB23.show();
			}
		});

		// Row 3
		TableRow row3 = new TableRow(context);
		row3.setPadding(0, 0, 0, 0);

		Button Button31 = new Button(context);
		Button31.setPadding(0, 0, 0, 0);
		Button31.setWidth(gridWidth / 3);
		Button31.setHeight(gridHeight / 3);
		Button31.setVisibility(View.INVISIBLE);
		row3.addView(Button31);

		Button32 = new Button(context); // Device South Direction
		Button32.setPadding(0, 0, 0, 0);
		Button32.setWidth(gridWidth / 3);
		Button32.setHeight(gridHeight / 3);
		Button32.setBackgroundColor(BKcolor);
		Button32.setTextColor(Color.WHITE);
		Button32.setVisibility(View.INVISIBLE);
		Button32.setTextSize(20);
		row3.addView(Button32);
		Button32.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Get Item data
				int mode = Activity.MODE_PRIVATE;
				SharedPreferences Item = mContext.getSharedPreferences(
						DeviceSouthItem, mode);
				String title = "Backward Object";
				String dis = "Distance: "
						+ Math.round((Item.getFloat("Distance", 0)) * 1000)
						+ " Meter";
				StringBuilder sb = new StringBuilder();
				sb.append("English  Title: ");
				sb.append(Item.getString("EnTitle", ""));
				sb.append('\n');
				sb.append("Japanese Title: ");
				sb.append(Item.getString("JpTitle", ""));
				sb.append('\n');
				sb.append("Chinese Title: ");
				sb.append(Item.getString("ZhTitle", ""));
				sb.append('\n');
				sb.append(dis);
				String info = sb.toString();

				// Create Item Dialog
				adB32 = new AlertDialog.Builder(context);
				adB32.setCancelable(true);
				adB32.setTitle(title);
				adB32.setMessage(info);
				adB32.setPositiveButton("Map",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int arg1) {
								GetItemMap(DeviceSouthItem);
							}
						});
				adB32.setNegativeButton("Close",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int arg1) {
							}
						});
				adB32.show();
			}
		});

		Button Button33 = new Button(context);
		Button33.setPadding(0, 0, 0, 0);
		Button33.setWidth(gridWidth / 3);
		Button33.setHeight(gridHeight / 3);
		Button33.setVisibility(View.INVISIBLE);
		row3.addView(Button33);
		grid.addView(row3);

		((Activity) context).addContentView(grid, new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

	} // InitGrid

	// --------------------GetItemMap--------------------------------------------------------------------------------

	public void GetItemMap(String item) {
		SharedPreferences DeviceItemRef = mContext.getSharedPreferences(item,
				Activity.MODE_PRIVATE);
		float lat = DeviceItemRef.getFloat("lat", 0);
		float lng = DeviceItemRef.getFloat("lng", 0);
		String title = DeviceItemRef.getString("JpTitle", "") + " "
				+ DeviceItemRef.getString("EnTitle", "") + " "
				+ DeviceItemRef.getString("ZhTitle", "");
		double[] iLat = { lat };
		double[] iLng = { lng };
		String[] iTitle = { title };
		// Intent MapNavi = new Intent(mContext, llMapNavi.class);
		// MapNavi.putExtra("itemsLat", iLat);
		// MapNavi.putExtra("itemsLng", iLng);
		// MapNavi.putExtra("itemsTitle", iTitle);
		// mContext.startActivity(MapNavi);
	}

	// --------------------------------------------------------------------------------------------------------------
	void updateGrid() {

		Log.v("updateGrid:", "updateGrid");

		Button12.setText("");
		Button21.setText("");
		Button23.setText("");
		Button32.setText("");
		Button12.setVisibility(View.INVISIBLE);
		Button21.setVisibility(View.INVISIBLE);
		Button23.setVisibility(View.INVISIBLE);
		Button32.setVisibility(View.INVISIBLE);

		if (((0 <= deviceDegreeToNorth) && (deviceDegreeToNorth <= 45))
				|| ((315 < deviceDegreeToNorth) && (deviceDegreeToNorth <= 360))) {
			DeviceNorthItem = "TrueNorthItem";
			DeviceEastItem = "TrueEastItem";
			DeviceSouthItem = "TrueSouthItem";
			DeviceWestItem = "TrueWestItem";
		} else if ((45 < deviceDegreeToNorth) && (deviceDegreeToNorth <= 135)) {
			DeviceNorthItem = "TrueWestItem";
			DeviceEastItem = "TrueNorthItem";
			DeviceSouthItem = "TrueEastItem";
			DeviceWestItem = "TrueSouthItem";
		} else if ((135 < deviceDegreeToNorth) && (deviceDegreeToNorth <= 225)) {
			DeviceNorthItem = "TrueSouthItem";
			DeviceEastItem = "TrueWestItem";
			DeviceSouthItem = "TrueNorthItem";
			DeviceWestItem = "TrueEastItem";
		} else {

			DeviceNorthItem = "TrueEastItem";
			DeviceEastItem = "TrueSouthItem";
			DeviceSouthItem = "TrueWestItem";
			DeviceWestItem = "TrueNorthItem";
		}

		int mode = Activity.MODE_PRIVATE;

		// Reset Update Item change flag in the SharedPreferences
		SharedPreferences updateRef = mContext.getSharedPreferences(
				"ItemRefUpdated", mode);
		SharedPreferences.Editor editor;
		editor = updateRef.edit();
		editor.putBoolean("update", false);
		editor.commit();

		// Get new Items
		SharedPreferences DeviceNorthItemRef = mContext.getSharedPreferences(
				DeviceNorthItem, mode);
		String DeviceNorthItemTitle = DeviceNorthItemRef.getString("JpTitle",
				"");
		if ((DeviceNorthItemTitle.length() == 0)
				|| (DeviceNorthItemTitle == null))
			DeviceNorthItemTitle = DeviceNorthItemRef.getString("EnTitle", "");

		SharedPreferences DeviceEastItemRef = mContext.getSharedPreferences(
				DeviceEastItem, mode);
		String DeviceEastItemTitle = DeviceEastItemRef.getString("JpTitle", "");
		if ((DeviceEastItemTitle.length() == 0)
				|| (DeviceEastItemTitle == null))
			DeviceEastItemTitle = DeviceEastItemRef.getString("EnTitle", "");

		SharedPreferences DeviceSouthItemRef = mContext.getSharedPreferences(
				DeviceSouthItem, mode);
		String DeviceSouthItemTitle = DeviceSouthItemRef.getString("JpTitle",
				"");
		if ((DeviceSouthItemTitle.length() == 0)
				|| (DeviceSouthItemTitle == null))
			DeviceSouthItemTitle = DeviceSouthItemRef.getString("EnTitle", "");

		SharedPreferences DeviceWestItemRef = mContext.getSharedPreferences(
				DeviceWestItem, mode);
		String DeviceWestItemTitle = DeviceWestItemRef.getString("JpTitle", "");
		if ((DeviceWestItemTitle.length() == 0)
				|| (DeviceWestItemTitle == null))
			DeviceWestItemTitle = DeviceWestItemRef.getString("EnTitle", "");

		if (DeviceNorthItemTitle != null) {
			if (DeviceNorthItemTitle.trim().length() > 0) {
				Button12.setText(DeviceNorthItemTitle);
				Button12.setVisibility(View.VISIBLE);
				// Log.v("DeviceNorthItemTitle:", DeviceNorthItemTitle);

			}
		}
		if (DeviceEastItemTitle != null) {

			if (DeviceEastItemTitle.trim().length() > 0) {
				Button23.setText(DeviceEastItemTitle);
				Button23.setVisibility(View.VISIBLE);
				// adB23.setTitle(DeviceEastItemTitle);
				// Log.v("DeviceEastItemTitle:", DeviceEastItemTitle);
			}
		}

		if (DeviceSouthItemTitle != null) {

			if (DeviceSouthItemTitle.trim().length() > 0) {
				Button32.setText(DeviceSouthItemTitle);
				Button32.setVisibility(View.VISIBLE);
				// adB32.setTitle(DeviceSouthItemTitle);
				// Log.v("DeviceSouthItemTitle:", DeviceSouthItemTitle);
			}
		}
		if (DeviceWestItemTitle != null) {

			if (DeviceWestItemTitle.trim().length() > 0) {
				Button21.setText(DeviceWestItemTitle);
				Button21.setVisibility(View.VISIBLE);
				// adB21.setTitle(DeviceWestItemTitle);
				// Log.v("DeviceWestItemTitle:", DeviceWestItemTitle);
			}
		}
	} // updateGrid

} // Compass Class

