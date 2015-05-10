package jp.ac.tokushima_u.is.ll.ui.nav;

/**
 * 
 * @author Kousuke Mouri University of Tokushima,Japan
 * 
 */

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
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;


public class LightNav extends Activity {
	
	// Declaration
	Context context=this;
	Compass compass; // Compass View (Layer) Class
	GPSLocation mGPSLocation; // GPS Location	
	Button ObjListButton;
//	List<ItemModel> ObjList; // List of the surrounding Objects
	double [] ObjLatList, ObjLngList;
	String[] ObjTitles; // List of the titles of surrounding Objects
	double lat,lng; //Current Location
	ImageView frame;
    
   // MultiThreading
    private Handler guiThread;     // GUI Thread
    private ExecutorService GetObjectsThread;  // Get surrounding objects thread
    private Runnable updateTask;
    private Future GetObjectsPending;  // Return results

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
         setContentView(R.layout.light_nav_layout);
         frame = (ImageView) findViewById(R.id.fame);
       
         String userEmail=null;
         // Receive intent data
         Intent intent = this.getIntent();
     	
 		if(intent!=null)
 		{	
 			userEmail = intent.getStringExtra("userEmail");
 			String userPassword = intent.getStringExtra("userPassword");
 			lat=intent.getDoubleExtra("lat", 0);
 			lng=intent.getDoubleExtra("lng", 0);
 			SharedPreferences.Editor editor;
 			SharedPreferences user = getSharedPreferences("User", Context.MODE_PRIVATE);
 			editor=user.edit();
 			editor.putString("userEmail", userEmail);
 			editor.putString("userPassword", userPassword);
 			editor.commit();
 		}
		else
		{
			finish();
		}
		
		// Open Log File
 
        // Initialize Items Shared References  
    	InitItemsRef();
    	
        // Create Compass and Alert Layer
        compass = new Compass(this);
        
        // Create Menu Layer
        CreateMenuLayer();
        
        //  Initialize MultitThreading
        initThreading();
        
        // Get GPS Location
    	mGPSLocation= new GPSLocation(LightNav.this);
    	
       	// Get object list id lan and lng are known
    	if ((lat!=0)&&(lng!=0))
    	{
    		queueUpdate(0);  //in millisecond
    	}
    	
       
    } //end onCreate
 
    
    //----------------------------------------------------------------------   
    @Override
    protected void onPause() {
    	// Cancel previous thread if it hasn't started yet
        guiThread.removeCallbacks(updateTask);

        mGPSLocation.CloseGPS();
        
       super.onPause();
    }
    
    //----------------------------------------------------------------------   
    @Override
    protected void onResume() {
    	// Cancel previous thread if it hasn't started yet
        guiThread.removeCallbacks(updateTask);
 
    	mGPSLocation.OpenGPSListener();
       super.onResume();
    }
    
    
    //----------------------------------------------------------------------   
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
 
       super.onDestroy();
    }
 
  //----------------------------------------------------------------------
    protected void CreateMenuLayer()
    {	
    	// Create Overlay Menu
    	final LinearLayout menuLayout;
    	menuLayout = new LinearLayout(this);
    	menuLayout.setOrientation(LinearLayout.VERTICAL);
    	
    	// Create menu buttons
    	Button ExitButton = new Button(this); 
        ExitButton.setText("   Exit  " ); 
        ObjListButton = new Button(this); 
        ObjListButton.setText ("   Objects   " );
        ObjListButton.setEnabled(false);
       	
        menuLayout.addView(ExitButton);
       	menuLayout.addView(ObjListButton);
       	
        addContentView(menuLayout, new LayoutParams (LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        menuLayout.setVisibility(View.INVISIBLE); // Hide it
    
        // Menu buttons Lister
        ExitButton.setOnClickListener(new OnClickListener() 
        {  @Override
	        public void onClick(View v)     	
	        {   
        			// Terminate extra threads here
					GetObjectsThread.shutdownNow();    	
        			// Close Orientation Sensor Listener
		        	compass.FinishSensor();
	        		// Close GPS Listener
	            	mGPSLocation.CloseGPS();
	            	
	            	
	            	// CLose the log file

	            	// Close the activity
	            	finish();     	
	         };   
        });
        
        ObjListButton.setOnClickListener(new OnClickListener() 
        {  @Override
        	public void onClick(View v)     	
	        {   
//        		if 	(ObjList!=null)
//        		{	
        			
        			final boolean [] checkedItems = new boolean[ObjTitles.length];
        	        DialogInterface.OnMultiChoiceClickListener listener = null;
        	        
        			// Create Dialog for ObjList
        			new AlertDialog.Builder(LightNav.this)
        			.setTitle("Surrounding Objects")
        			.setCancelable(true) // Disable back key (or any Cancel action)
        			//.setItems(ObjTitles,
        			.setMultiChoiceItems (ObjTitles, checkedItems,new DialogInterface.OnMultiChoiceClickListener()
        			{
        				@Override
						public void onClick(DialogInterface dialog, int which,boolean isChecked) 
        				{
							// TODO Auto-generated method stub
							
					}} ) 
        			.setPositiveButton( "OK", 
        			new DialogInterface.OnClickListener() 
        			{
        				@Override
						public void onClick(DialogInterface dialog,	int arg1) 
    	       			{
        					int sel=0;
        					for (int i=0;i<ObjTitles.length;i++)
        							if (checkedItems[i]!=false) sel++; 
        					
        					if (sel>0)
        					{	double [] iLat=new double[sel];
        						double [] iLng=new double[sel];
        						String [] iTitle=new String[sel];
        						int j=0;
        						for (int i=0;i<ObjTitles.length;i++)
        						{	
	        						if (checkedItems[i]!=false)
	        						{
	        							iLat[j]=ObjLatList[i];
	        							iLng[j]=ObjLngList[i];
	        							iTitle[j]=ObjTitles[i];

	        							j++;
	        						}
	        						
	        					}
	        					Intent MapNavi = new Intent(LightNav.this, llMapNavi.class);
	        					MapNavi.putExtra("itemsLat", iLat);
	        					MapNavi.putExtra("itemsLng", iLng);
	        					MapNavi.putExtra("itemsTitle", iTitle);
	        					LightNav.this.startActivity(MapNavi);
        					}	        					
    	       			}
        			}) // setItems*/
        			.show();
        		}
		    }
    	//}
        );
        
      
       	// Display or Hide the menu
        frame.setOnClickListener(new OnClickListener() 
       {    @Override  
            public void onClick(View v) 
       		{   
	         	if ((menuLayout.getVisibility())==View.INVISIBLE)
	            {
	            	menuLayout.setVisibility(View.VISIBLE);
	           	}
	            else if ((menuLayout.getVisibility())==View.VISIBLE) 
	            {	
	           		menuLayout.setVisibility(View.INVISIBLE);           		
	           	}         	
    		};   
        });  //mCamView.setOnClickListener	
    } // CreateMenuLayer
  //----------------------------------------------------------------------
    
  
    void InitItemsRef()
    {
    	int mode = Context.MODE_PRIVATE;
    	SharedPreferences.Editor editor;
    	
    	SharedPreferences TrueNorthItem = getSharedPreferences("TrueNorthItem",mode);
    	editor = TrueNorthItem.edit();
    	editor.clear();
    	editor.commit();
    	
    	SharedPreferences TrueEastItem = getSharedPreferences("TrueEastItem",mode);
    	editor = TrueEastItem.edit();
    	editor.clear();
    	editor.commit();
    	
    	SharedPreferences TrueSouthItem = getSharedPreferences("TrueSouthItem",mode);
    	editor = TrueSouthItem.edit();
    	editor.clear();
    	editor.commit();
    	
    	SharedPreferences TrueWestItem = getSharedPreferences("TrueWestItem",mode);
    	editor = TrueWestItem.edit();
    	editor.clear();
    	editor.commit();
 		
		// Initialize Item change flag in the SharedPreferences
		SharedPreferences updateRef = getSharedPreferences("ItemRefUpdated",mode);
		editor = updateRef.edit();
		editor.putBoolean("update", false);
		editor.commit();
		
	   	
     	// Initialize current location in the SharedPreferences
    	final SharedPreferences CurrentLocation = getSharedPreferences("CurrentLocation",mode);
		editor = CurrentLocation.edit();
		editor.clear();
		editor.commit();
		

    }
    
    private void initThreading() 
    {
       guiThread = new Handler();
       GetObjectsThread = Executors.newSingleThreadExecutor();
       updateTask = new Runnable() 
       { 
          @Override
		public void run() 
          {
              // Cancel previous GetObjects task if there was one
              if (GetObjectsPending != null)
            	  GetObjectsPending.cancel(true); 
        	  
        	  try 
              {
//            	  GetObjectsTask getObjectsTask = new GetObjectsTask(LightNav.this,lat,lng); 
//	              GetObjectsPending = GetObjectsThread.submit(getObjectsTask); 
//	              Log.v("start new task", "start new task");
	          }
              catch (RejectedExecutionException e) 
	          {
	              // Unable to start new task
	              Log.v("Unable to start new task", "Unable to start new task");
	          }
          }
     };
	
    }  
    // Start GetObjectsThread after a short delay 
    public void queueUpdate(long delayMillis) 
    {  // Cancel previous thread if it hasn't started yet
       guiThread.removeCallbacks(updateTask);
       // Start the thread if nothing happens after a few milliseconds
       guiThread.postDelayed(updateTask, delayMillis);
    }
    // Update the Alerts after GetObjects
    public void UpdateAlert() 
    {
        guiThread.post(new Runnable() 
        {
        	@Override
			public void run() 
        	{
        	   compass.updateGrid();
	           	if (ObjTitles.length>0)
	        	{
	        	  	ObjListButton.setEnabled(true);
	        	}
	        	else
	        	{
	        	  	ObjListButton.setEnabled(false);
	        	}
        			
        	}
        });
     }
    
    
//    public void FillObjList(List<ItemModel> ItemList, double [] ItemLatList, double [] ItemLngList, String[]items)
//    {
//    	ObjList=ItemList;
//    	ObjTitles=items;
//    	
//    	ObjLatList= ItemLatList;
//    	ObjLngList= ItemLngList;
//    	
//
//    }
    

} //end nav class

