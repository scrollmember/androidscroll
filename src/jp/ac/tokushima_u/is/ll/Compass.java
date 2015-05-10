package jp.ac.tokushima_u.is.ll;


///////////////////////////////////////////

//Written By Dr.Moushir EL-Bishouty (2010)

///////////////////////////////////////////

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

public class Compass {
	
	public float deviceDegreeToNorth;
	Button Button12, Button21, Button23,Button32;
	AlertDialog.Builder adB12,adB21, adB23, adB32;  
	SensorManager mSensorManager;
	CompassView mCompassView;
	Context mContext;
	String DeviceNorthItem,DeviceEastItem,DeviceSouthItem,DeviceWestItem; 
	
	Compass(Context context)
	{	
		deviceDegreeToNorth=-1f;
		mContext=context;
		// Get Compass Sensor
		mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
		mSensorManager.registerListener(SENSOR_ORIENTATION_Listener, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_NORMAL);
		// Create Overlay CompassView
		mCompassView = new CompassView(context);
	   ((Activity) context).addContentView(mCompassView, new LayoutParams	(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	   
	   // Register ItemsRefListener
	   ItemsRefListener();
	}
	
	SensorEventListener SENSOR_ORIENTATION_Listener = new SensorEventListener()
	{
		@Override
		public void onAccuracyChanged(Sensor arg0, int i)
		{
		}
		@Override
		public void onSensorChanged(SensorEvent evt)
		{	
			if (evt.sensor.getType()==Sensor.TYPE_ORIENTATION)
			{	
				float vals[] = evt.values;   
		    	deviceDegreeToNorth=vals[0];
		    	if (deviceDegreeToNorth<=270)
				{	
					deviceDegreeToNorth=270-deviceDegreeToNorth;
				}
				else
				{
					deviceDegreeToNorth=360+(270-deviceDegreeToNorth);
				}
		    	mCompassView.invalidate();
		    	updateGrid();		// update and rotate the Tags 
			 }
		 }
	};	
	public void FinishSensor()
	{
		mSensorManager.unregisterListener(SENSOR_ORIENTATION_Listener);
	}
	void ItemsRefListener()
	{
		int mode = Context.MODE_PRIVATE;
    	SharedPreferences NorthItem = mContext.getSharedPreferences("NorthItem",mode);
    	NorthItem.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() 
    	   {	@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) 
    	   		{	
    		   		updateGrid();	
    	   		}
    		});
	};	
	class CompassView extends View 
	{	
		float CenterX=240;
		float CenterY=135;
		float radius=40;
		float CircleWidth=10;
		float CharWidth=2;
		float CharSize=15;
		Paint paint = new Paint();
		RectF rect;
		CompassView(Context context)
		{
			super(context);
			InitGrid(context); // Create Tags grid
		}
		@Override
		protected void onDraw(Canvas canvas) 
		{
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(CircleWidth);
			paint.setColor(Color.BLACK);
			paint.setAlpha(30);
			paint.setAntiAlias(true);
			canvas.drawCircle(CenterX, CenterY, radius, paint); //Draw Compass Circle shape
			
			if (deviceDegreeToNorth>=0f)
			{	// Show Degree
				paint.setStrokeWidth(1);
				paint.setTextSize(15);
				paint.setColor(Color.WHITE);
				String NorthDegree="N:"+deviceDegreeToNorth;
				//canvas.drawText(NorthDegree, 400, 65, paint);
			
				//rotate the compass indicator
				canvas.rotate(deviceDegreeToNorth, CenterX, CenterY);
				// Draw Direction letters
				paint.setStrokeWidth(CharWidth);
				paint.setTextSize(CharSize);
				paint.setColor(Color.WHITE);
				canvas.drawText("N", CenterX, CenterY-radius+CircleWidth/2,paint);
				canvas.drawText("E", CenterX+radius, CenterY,paint);
				canvas.drawText("S", CenterX, CenterY+radius+CircleWidth/2,paint);
				canvas.drawText("W", CenterX-radius, CenterY,paint);
			}
				super.onDraw(canvas);
		}
	}// CompassView Class

//------------------------------ CompassView Class ---------------------------------------------
	
   void InitGrid(final Context context)
   {
	   TableLayout  grid; // Create Grid of Buttons 3x3 overly Camera View 
	   grid= new TableLayout(context);
	   grid.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
	   grid.setOrientation(LinearLayout.VERTICAL);
	   grid.setPadding(0, 0, 0, 0);

	   int gridWidth=480;
	   int gridHeight=270;
	   //Color color = new Color();
	   int BKcolor = Color.argb(50, 135, 206, 250);

	   // Row 1
	   TableRow row1=new TableRow(context);
	   row1.setPadding(0, 0, 0, 0);

	   Button Button11 = new Button(context); 
	   Button11.setPadding(0, 0, 0, 0);
	   Button11.setWidth(gridWidth/3);
	   Button11.setHeight(gridHeight/3);
	   row1.addView(Button11);
	   Button11.setVisibility(View.INVISIBLE);
	   
	   Button12 = new Button(context); // Device North Direction
	   Button12.setPadding(0, 0, 0, 0);
	   Button12.setWidth(gridWidth/3);
	   Button12.setHeight(gridHeight/3);
	   Button12.setBackgroundColor(BKcolor);
	   Button12.setTextColor(Color.WHITE);
	   Button12.setVisibility(View.INVISIBLE);
	   Button12.setTextSize(20);
	   row1.addView(Button12);
	   Button12.setOnClickListener(new OnClickListener() 
	   {   @Override
		   public void onClick(View v)     	
	   	   {
		   		// Get Item data
		   		int mode = Context.MODE_PRIVATE;
		   		SharedPreferences Item = mContext.getSharedPreferences(DeviceNorthItem,mode);
		   		final float lat=Item.getFloat("lat", 0);
		   		final float lng=Item.getFloat("lng", 0);
		   		String title=Item.getString("Title", "");
		   		
		   		// Create Item Dialog
		   		adB12 = new AlertDialog.Builder(context);
		   		adB12.setCancelable(true);
		   		adB12.setTitle(title);
		   		adB12.setPositiveButton ("Map",new DialogInterface.OnClickListener()
	       		{	@Override
				public void onClick(DialogInterface dialog,	int arg1) 
	       			{
	       				Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse("geo:"+lat+","+lng+"?z="+18));
	  		   			mContext.startActivity(intent); 
	       			}
	       		});
		   		adB12.setNegativeButton("Close",new DialogInterface.OnClickListener()
	       		{	@Override
				public void onClick(DialogInterface dialog,	int arg1) 
	       			{}
	       		});		
	       		adB12.show();
	       	}
	    });

	   Button Button13 = new Button(context); 
	   Button13.setPadding(0, 0, 0, 0);
	   Button13.setWidth(gridWidth/3);
	   Button13.setHeight(gridHeight/3);
	   Button13.setVisibility(View.INVISIBLE);
	   row1.addView(Button13);
	   grid.addView(row1);

	   // Row 2
	   TableRow row2=new TableRow(context);
	   row2.setPadding(0, 0, 0, 0);
	   
	   Button21 = new Button(context); // Device West Direction
	   Button21.setPadding(0, 0, 0, 0);
	   Button21.setWidth(gridWidth/3);
	   Button21.setHeight(gridHeight/3);
	   Button21.setBackgroundColor(BKcolor);
	   Button21.setTextColor(Color.WHITE);
	   Button21.setVisibility(View.INVISIBLE);
	   Button21.setTextSize(20);
	   row2.addView(Button21);
	   Button21.setOnClickListener(new OnClickListener() 
	   {   @Override
	   public void onClick(View v)     	
   	   {
	   		// Get Item data
	   		int mode = Context.MODE_PRIVATE;
	   		SharedPreferences Item = mContext.getSharedPreferences(DeviceWestItem,mode);
	   		final float lat=Item.getFloat("lat", 0);
	   		final float lng=Item.getFloat("lng", 0);
	   		String title=Item.getString("Title", "");
	   		
	   		// Create Item Dialog
	   		adB21 = new AlertDialog.Builder(context);
	   		adB21.setCancelable(true);
	   		adB21.setTitle(title);
	   		adB21.setPositiveButton ("Map",new DialogInterface.OnClickListener()
       		{	@Override
			public void onClick(DialogInterface dialog,	int arg1) 
       			{
       				Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse("geo:"+lat+","+lng+"?z="+18));
  		   			mContext.startActivity(intent); 
       			}
       		});
	   		adB21.setNegativeButton("Close",new DialogInterface.OnClickListener()
       		{	@Override
			public void onClick(DialogInterface dialog,	int arg1) 
       			{}
       		});		
       		adB21.show();
       	}
    });


	   Button Button22 = new Button(context); 
	   Button22.setPadding(0, 0, 0, 0);
	   Button22.setWidth(gridWidth/3);
	   Button22.setHeight(gridHeight/3);
	   Button22.setVisibility(View.INVISIBLE);
	   row2.addView(Button22);

	   Button23 = new Button(context); // Device East Direction
	   Button23.setPadding(0, 0, 0, 0);
	   Button23.setWidth(gridWidth/3);
	   Button23.setHeight(gridHeight/3);
	   Button23.setBackgroundColor(BKcolor);
	   Button23.setTextColor(Color.WHITE);
	   Button23.setVisibility(View.INVISIBLE);
	   Button23.setTextSize(20);
	   row2.addView(Button23);
	   grid.addView(row2);
	   Button23.setOnClickListener(new OnClickListener() 
	   {   @Override
	   public void onClick(View v)     	
   	   {
	   		// Get Item data
	   		int mode = Context.MODE_PRIVATE;
	   		SharedPreferences Item = mContext.getSharedPreferences(DeviceEastItem,mode);
	   		final float lat=Item.getFloat("lat", 0);
	   		final float lng=Item.getFloat("lng", 0);
	   		String title=Item.getString("Title", "");
	   		
	   		// Create Item Dialog
	   		adB23 = new AlertDialog.Builder(context);
	   		adB23.setCancelable(true);
	   		adB23.setTitle(title);
	   		adB23.setPositiveButton ("Map",new DialogInterface.OnClickListener()
       		{	@Override
			public void onClick(DialogInterface dialog,	int arg1) 
       			{
       				Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse("geo:"+lat+","+lng+"?z="+18));
  		   			mContext.startActivity(intent); 
       			}
       		});
	   		adB23.setNegativeButton("Close",new DialogInterface.OnClickListener()
       		{	@Override
			public void onClick(DialogInterface dialog,	int arg1) 
       			{}
       		});		
       		adB23.show();
       	}
    });
	   
	   // Row 3
	   TableRow row3=new TableRow(context);
	   row3.setPadding(0, 0, 0, 0);

	   Button Button31 = new Button(context); 
	   Button31.setPadding(0, 0, 0, 0);
	   Button31.setWidth(gridWidth/3);
	   Button31.setHeight(gridHeight/3);
	   Button31.setVisibility(View.INVISIBLE);
	   row3.addView(Button31);

	   Button32 = new Button(context); // Device South Direction
	   Button32.setPadding(0, 0, 0, 0);
	   Button32.setWidth(gridWidth/3);
	   Button32.setHeight(gridHeight/3);
	   Button32.setBackgroundColor(BKcolor);
	   Button32.setTextColor(Color.WHITE);
	   Button32.setVisibility(View.INVISIBLE);
	   Button32.setTextSize(20);
	   row3.addView(Button32);
	   Button32.setOnClickListener(new OnClickListener() 
	   {   @Override
	   public void onClick(View v)     	
   	   {
	   		// Get Item data
	   		int mode = Context.MODE_PRIVATE;
	   		SharedPreferences Item = mContext.getSharedPreferences(DeviceSouthItem,mode);
	   		final float lat=Item.getFloat("lat", 0);
	   		final float lng=Item.getFloat("lng", 0);
	   		String title=Item.getString("Title", "");
	   		
	   		// Create Item Dialog
	   		adB32 = new AlertDialog.Builder(context);
	   		adB32.setCancelable(true);
	   		adB32.setTitle(title);
	   		adB32.setPositiveButton ("Map",new DialogInterface.OnClickListener()
       		{	@Override
			public void onClick(DialogInterface dialog,	int arg1) 
       			{
       				Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse("geo:"+lat+","+lng+"?z="+18));
  		   			mContext.startActivity(intent); 
       			}
       		});
	   		adB32.setNegativeButton("Close",new DialogInterface.OnClickListener()
       		{	@Override
			public void onClick(DialogInterface dialog,	int arg1) 
       			{}
       		});		
       		adB32.show();
       	}
    });

	   Button Button33 = new Button(context); 
	   Button33.setPadding(0, 0, 0, 0);
	   Button33.setWidth(gridWidth/3);
	   Button33.setHeight(gridHeight/3);
	   Button33.setVisibility(View.INVISIBLE);
	   row3.addView(Button33);
	   grid.addView(row3);
	     
	   ((Activity) context).addContentView(grid, new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));	   
   
   } // InitGrid
   
//--------------------------------------------------------------------------------------------------------------
   void updateGrid()
   {
   		
	    
	    Button12.setText("");
	    Button21.setText("");
	    Button23.setText("");
	    Button32.setText("");
	    Button12.setVisibility(View.INVISIBLE);
	    Button21.setVisibility(View.INVISIBLE);
	    Button23.setVisibility(View.INVISIBLE);
	    Button32.setVisibility(View.INVISIBLE);
	
	    
	    if (((0<=deviceDegreeToNorth) && (deviceDegreeToNorth <=45)) || ((315<deviceDegreeToNorth) && (deviceDegreeToNorth <=360)))
	    {
	    	DeviceNorthItem="TrueNorthItem";
	    	DeviceEastItem="TrueEastItem";
	    	DeviceSouthItem="TrueSouthItem";
	    	DeviceWestItem="TrueWestItem";
	    }
	    else if ((45<deviceDegreeToNorth) && (deviceDegreeToNorth<=135))
	    {
	    	DeviceNorthItem="TrueWestItem";
	    	DeviceEastItem="TrueNorthItem";
	    	DeviceSouthItem="TrueEastItem";
	    	DeviceWestItem="TrueSouthItem";
	    }
	    else if ((135<deviceDegreeToNorth) && (deviceDegreeToNorth <=225))
	    {
	    	DeviceNorthItem="TrueSouthItem";
	    	DeviceEastItem="TrueWestItem";
	    	DeviceSouthItem="TrueNorthItem";
	    	DeviceWestItem="TrueEastItem";
	    }
	    else 
	    {
	    	DeviceNorthItem="TrueEastItem";
	    	DeviceEastItem="TrueSouthItem";
	    	DeviceSouthItem="TrueWestItem";
	    	DeviceWestItem="TrueNorthItem";
	    }
	   
	   	int mode = Context.MODE_PRIVATE;
	    
	    SharedPreferences DeviceNorthItemRef = mContext.getSharedPreferences(DeviceNorthItem,mode);
	    String DeviceNorthItemTitle = DeviceNorthItemRef.getString("Title",null);
	    
	    SharedPreferences DeviceEastItemRef = mContext.getSharedPreferences(DeviceEastItem,mode);
	    String DeviceEastItemTitle = DeviceEastItemRef.getString("Title",null);
	    
	    SharedPreferences DeviceSouthItemRef = mContext.getSharedPreferences(DeviceSouthItem,mode);
	    String DeviceSouthItemTitle = DeviceSouthItemRef.getString("Title",null);
	    
	    SharedPreferences DeviceWestItemRef = mContext.getSharedPreferences(DeviceWestItem,mode);
	    String DeviceWestItemTitle = DeviceWestItemRef.getString("Title",null);
	    
	 	if (DeviceNorthItemTitle!=null)
	 	{
	 		Button12.setText(DeviceNorthItemTitle);
	    	Button12.setVisibility(View.VISIBLE);
	    }
	    if (DeviceEastItemTitle!=null)
	 	{
	 		Button23.setText(DeviceEastItemTitle);
	    	Button23.setVisibility(View.VISIBLE);
	    	//adB23.setTitle(DeviceEastItemTitle);
	    }
	    if (DeviceSouthItemTitle!=null)
	 	{
	 		Button32.setText(DeviceSouthItemTitle);
	    	Button32.setVisibility(View.VISIBLE);
	    	//adB32.setTitle(DeviceSouthItemTitle);
	    }
	    if (DeviceWestItemTitle!=null)
	 	{
	 		Button21.setText(DeviceWestItemTitle);
	    	Button21.setVisibility(View.VISIBLE);
	    	//adB21.setTitle(DeviceWestItemTitle);
	    }
   } // updateGrid
   
} // Compass Class

