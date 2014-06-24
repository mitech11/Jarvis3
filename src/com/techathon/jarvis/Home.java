package com.techathon.jarvis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import com.android.internal.telephony.ITelephony;
import com.techathon.jarvis.data.DatabaseHelper;
import com.techathon.jarvis.data.LocationData;

import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.provider.Settings.System;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.database.Cursor;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.DrawerLayout;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Home extends Activity implements OnItemClickListener, android.view.View.OnClickListener{	
	
	public static final String ARG_SECTION_NUMBER = "section_number";
	
	private static final int SETTINGS_ACTIVITY = 1;
	private boolean gpsStarted;
	private int speed;
	private double latitude;
	private double longitude;
	private String unit;
	private String profileStatus;
	private int nightStartHour;
	
	public static int speedLimit;
	public static int speedLimitMPH;
	public static boolean sendSMS;
	public static String lastIncomingNumber;
	public static int lastIncomingCallTime;
	public static int lastLogTime;
	public static String smsText;
	public static boolean manualOverride;
	
	private RelativeLayout backgroundLayout;
	private TextView speedLabel;
	private TextView profileLabel;
	private TextView speedDigit1;
	private TextView speedDigit2;
	private TextView speedDigit3;
	private TextView profile;
	private ImageView GPSStatus;
	private TextView kmph;
	private TextView mph;
	//private TextView locationLabel;
	private TextView location;
	private AudioManager mAudioManager;
	private TelephonyManager incomingCallManager;
	private PhoneStateListener  incomingCallListener;
	
	//abhilasha
	//Azhar start
	private LocationManager locationManager;
	private ConnectivityManager connectivityManager;
	private Intent proximityService;
	private SharedPreferences preferences;
	private SharedPreferences.Editor editor;
	boolean isLogsEnabled;
	private double mLat,mLong;
	//Azhar end 
	private ActionBar actionBar;
	private DrawerLayout drawerLayout;
	private ListView jarvisMenu;
	private ActionBarDrawerToggle actionBarDrawerToggle;
	//MIB: Remove driving logs for 1st release.
//	private String[] drawerListItems = {"Home","Find My Car", "Settings", "Driving Logs"};
	private String[] drawerListItems = {"Home","Find My Car", "Settings"};
	//abhilasha end
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		closeNotification();
		//abhilasha
		//create database on app launch
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.getWritableDatabase();
        dbHelper.close();
        actionBar = getActionBar();
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        jarvisMenu = (ListView)findViewById(R.id.left_drawer);
        jarvisMenu.setAdapter(new ArrayAdapter<String>(getApplicationContext(), R.layout.drawer_list_item, drawerListItems));
        jarvisMenu.setOnItemClickListener(this);
        setActionBarDrawerToggle();
        showJarvisNotification();
		
		backgroundLayout = (RelativeLayout) findViewById(R.id.background_layout);
		speedLabel = (TextView) findViewById(R.id.speed_label);
		profileLabel = (TextView) findViewById(R.id.profile_label);
		speedDigit1 = (TextView) findViewById(R.id.speed_first_digit);
		speedDigit2 = (TextView) findViewById(R.id.speed_second_digit);
		speedDigit3 = (TextView) findViewById(R.id.speed_third_digit);
		profile 	= (TextView) findViewById(R.id.profile);	
		GPSStatus 	= (ImageView) findViewById(R.id.GPS_status);
		kmph = (TextView) findViewById(R.id.kmph);
		mph = (TextView) findViewById(R.id.mph);	
		//locationLabel = (TextView) findViewById(R.id.location_label);
		location = (TextView) findViewById(R.id.location);

		nightStartHour = 19; //in 24h format
		if(savedInstanceState == null) {
			speed = 0;
			unit = "kmph";
			profileStatus = "normal";
			gpsStarted = false;
			manualOverride = false;
			lastLogTime = 0;
			Home.this.location.setVisibility(Home.this.findViewById(android.R.id.content).GONE);
        	//Home.this.locationLabel.setVisibility(Home.this.findViewById(android.R.id.content).GONE);
			loadSettings();		
			setSpeed();
		}
		
		dayOrNight();
		
		loadSettings();

		checkGPS(savedInstanceState);

		Typeface myTypeface = Typeface.createFromAsset(this.getAssets(),
				"fonts/digi.TTF");
		Typeface myTypeface2 = Typeface.createFromAsset(this.getAssets(),
				"fonts/profile.TTF");

		speedDigit1.setTypeface(myTypeface);
		speedDigit2.setTypeface(myTypeface);
		speedDigit3.setTypeface(myTypeface);
		profile.setTypeface(myTypeface2);
		//speedLabel.setTypeface(myTypeface2);
		//profileLabel.setTypeface(myTypeface2);

		//TextView speed_label = (TextView) findViewById(R.id.speed_label);
		//speedLabel.setTypeface(myTypeface2);
		//TextView profile_label = (TextView) findViewById(R.id.profile_label);
		//profileLabel.setTypeface(myTypeface2);
		
		mAudioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
		checkProfileStatus();
		BroadcastReceiver ringerModeChangeReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				checkProfileStatus();
			}
		};
		IntentFilter intentFilterRingerMode = new IntentFilter(
				AudioManager.RINGER_MODE_CHANGED_ACTION);
		this.registerReceiver(ringerModeChangeReceiver, intentFilterRingerMode);

		GPSStatus.setOnClickListener(new Button.OnClickListener() {  
        public void onClick(View v)
            {
        		buildAlertMessageNoGps(v);
            }
         });
		
		speedDigit1.setOnClickListener(new Button.OnClickListener() {  
        public void onClick(View v)
            {
        		changeUnit(v);
            }
         });
		speedDigit2.setOnClickListener(new Button.OnClickListener() {  
	    public void onClick(View v)
	        {
	        	changeUnit(v);
	        }
	    });
		speedDigit3.setOnClickListener(new Button.OnClickListener() {  
	    public void onClick(View v)
	        {
	        	changeUnit(v);
	        }
	    });
		TextView kmph = (TextView) findViewById(R.id.kmph);
		TextView mph = (TextView) findViewById(R.id.mph);
		kmph.setOnClickListener(new Button.OnClickListener() {  
	    public void onClick(View v)
	        {
	        	convertToKmph(v);
	        }
	    });
		mph.setOnClickListener(new Button.OnClickListener() {  
	    public void onClick(View v)
	        {
	        	convertToMph(v);
	        }
	    });
		profile.setOnClickListener(new Button.OnClickListener() {  
	        public void onClick(View v)
            {
	        	changeProfileManualPopUp(v);
            }
		});			
		onCreateByAzhar();
	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.try1, menu);
//		return true;
//	}
	
	private void dayOrNight() {
		GregorianCalendar dateToday = new GregorianCalendar();
		//Calendar now = Calendar.getInstance();
		//SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		//String timeString = dateFormat.format(now.getTime());
		
		int hourOfday = dateToday.get(Calendar.HOUR_OF_DAY);
		//showToast("Current hour of day is: " + hourOfday);
		if(hourOfday < nightStartHour) {
			//day time
			backgroundLayout.setBackgroundColor(getResources().getColor(R.color.white));
			speedLabel.setTextColor(getResources().getColor(R.color.black));
			profileLabel.setTextColor(getResources().getColor(R.color.black));
			if(unit == "kmph") {
    			kmph.setTextColor(getResources().getColor(R.color.black));
    			mph.setTextColor(getResources().getColor(R.color.gray));
    		}
    		else if(unit == "mph"){
    			mph.setTextColor(getResources().getColor(R.color.black));
    			kmph.setTextColor(getResources().getColor(R.color.gray));
    		}	
			//locationLabel.setTextColor(getResources().getColor(R.color.black));
		}
		else {
			//night time
			backgroundLayout.setBackgroundColor(getResources().getColor(R.color.black));
			speedLabel.setTextColor(getResources().getColor(R.color.white));
			profileLabel.setTextColor(getResources().getColor(R.color.white));
			if(unit == "kmph") {
    			kmph.setTextColor(getResources().getColor(R.color.white));
    			mph.setTextColor(getResources().getColor(R.color.gray));
    		}
    		else if(unit == "mph"){
    			mph.setTextColor(getResources().getColor(R.color.white));
    			kmph.setTextColor(getResources().getColor(R.color.gray));
    		}
			//locationLabel.setTextColor(getResources().getColor(R.color.white));
		}
	}
	
	private void convertToKmph(View view) {						
		if(unit.equalsIgnoreCase("mph"))
		{
			long speed1 = Math.round((speed/0.621371));
			speed= (int) speed1;
			unit="kmph";
			dayOrNight();			
			setSpeed();
		}		
	 }
	
	private void convertToMph(View view) {		
		if(unit.equalsIgnoreCase("kmph"))
		{
			long speed1 = Math.round((speed * 0.621371));
			speed= (int) speed1;
			unit="mph";
			dayOrNight();
			setSpeed();
		}		
	}
	
	private void trackSpeed() {	
		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		
//		final LocationListener gpsLocationListener = new LocationListener() {
		final LocationListener locationListener = new LocationListener() {

	        @Override
	        public void onStatusChanged(String provider, int status, Bundle extras) {
	            switch (status) {
	            case LocationProvider.AVAILABLE:
	            	showToast("Located you successfully.");
	                //textView.setText(textView.getText().toString() + "GPS available again\n");
	                break;
	            case LocationProvider.OUT_OF_SERVICE:
	            	showToast("Location fixing service seems out of service.");
	                //textView.setText(textView.getText().toString() + "GPS out of service\n");
	                break;
	            case LocationProvider.TEMPORARILY_UNAVAILABLE:
	            	showToast("Problem locating you, will keep trying.");
	                //textView.setText(textView.getText().toString() + "GPS temporarily unavailable\n");
	                break;
	            }
	        }

	        @Override
	        public void onProviderEnabled(String provider) {
	            //textView.setText(textView.getText().toString() + "GPS Provider Enabled\n");
	        	gpsStarted = true;
		    	GPSStatus.setVisibility(View.GONE);	        	
	        }

	        @Override
	        public void onProviderDisabled(String provider) {
	            //textView.setText(textView.getText().toString() + "GPS Provider Disabled\n");
	        	gpsStarted = false;
		    	GPSStatus.setVisibility(View.VISIBLE);
	        }

	        @Override
	        public void onLocationChanged(Location location) {
//	            locationManager.removeUpdates(networkLocationListener);
//	        	if(location.getProvider().equals(LocationManager.GPS_PROVIDER)){
//	                //here Gpsi
//	            } else if(location.getProvider().equals(LocationManager.NETWORK_PROVIDER)){
//	                //here Network
//	            }
	        	
	        	latitude = location.getLatitude();
        		longitude = location.getLongitude();
	        	
	        	Geocoder locationArea = new Geocoder(getApplicationContext(), Locale.getDefault());
	        	Home.this.location.setVisibility(Home.this.findViewById(android.R.id.content).VISIBLE);
	        	
	        	
	        	
	        	//Home.this.locationLabel.setVisibility(Home.this.findViewById(android.R.id.content).VISIBLE);	        	
	        	try {
	        		if(locationArea.isPresent()) {
	        			List<Address> addresses = null;
	        			addresses = locationArea.getFromLocation(latitude, longitude, 1);
	        			if(addresses.size() > 0) {
	        				Address address = addresses.get(0);
	        				if(address.getSubLocality() != "null" && address.getLocality() != "null") {
	        					Home.this.location.setText(address.getSubLocality() + ", " + address.getLocality());
	        					Home.this.location.setTextColor(getResources().getColor(R.color.mobile_blue));
	        				}
	        				else {
	        					Home.this.location.setText("Locating you ...");
	        					Home.this.location.setTextColor(getResources().getColor(R.color.orange));
	        				}
	        				
	        			}
	        		}
	        		else {
	        			Home.this.location.setText("Lat:" + String.format("%9.1f", latitude) + " Long: " +  String.format("%9.1f", longitude));
        				Home.this.location.setTextColor(getResources().getColor(R.color.orange));	        			
	        		}
	        		
	        	}
	        	catch(Exception e) {
	        		Home.this.location.setVisibility(Home.this.findViewById(android.R.id.content).GONE);
		        	//Home.this.locationLabel.setVisibility(Home.this.findViewById(android.R.id.content).GONE);
    				Home.this.location.setTextColor(getResources().getColor(R.color.mobile_blue));	
	        	}
	        	
	        	try {	  
	        		if(!location.hasSpeed())
	        			return;	        			        		
	        		
	        		if(unit.equalsIgnoreCase("kmph"))
	        			speed= (int) (Math.round(location.getSpeed() * 3.6));
	        		else
	        			if(unit.equalsIgnoreCase("mph"))
	        				speed= (int) (Math.round((location.getSpeed() * 3.6 * 0.621371)));
	        	
	        		setSpeed();
	        		
	        		if(manualOverride) {
	        			DontlistenForIncomingCalls();
	        			return;
	        		}
	        	
	        		if(speed >0) {	        	
	        			if(isLogsEnabled) {	        				
	                		GregorianCalendar date = new GregorianCalendar();
	                		int currentTime = date.get(Calendar.HOUR_OF_DAY) * 100 + date.get(Calendar.MINUTE);
	                		if((currentTime - lastLogTime) > 4) {
	                			DatabaseHelper db = new DatabaseHelper(Home.this);
	                			db.getWritableDatabase();
	                			try {
	            	        		if(locationArea.isPresent()) {
	            	        			List<Address> addresses = null;
	            	        			addresses = locationArea.getFromLocation(latitude, longitude, 1);
	            	        			if(addresses.size() > 0) {
	            	        				Address address = addresses.get(0);
	            	        				if(address.getSubLocality() != "null" && address.getLocality() != "null") {
	            	        					Calendar now = Calendar.getInstance();
	            	        					SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	            	        					String timeString = dateFormat.format(now.getTime());
	            	        					String[] s = timeString.split(" ");
	            	        					LocationData locData = new LocationData(address.getSubLocality(), address.getLocality(), latitude, longitude, s[0], s[1], speed);
	            	                			db.addLogs(locData);
	            	                			lastLogTime = currentTime;
	            	        				}
	            	        				else {
	            	        					//do not add location to log
	            	        				}
	            	        				
	            	        			}
	            	        		}
	            	        		else {
	            	        			//do not add location to log	        			
	            	        		}
	            	        		
	            	        	}
	            	        	catch(Exception e) {
	            	        		//do not add location to log	
	            	        	}            	     
	                		}  
	        			}
	        			
	        			if(unit.equalsIgnoreCase("kmph"))
	        				if(speed >= speedLimit) {
	        					makeProfileSilent();
	        					listenForIncomingCalls();
	        				}
	        				else if(speed < speedLimit) {
	        					makeProfileNormal();      
	        					DontlistenForIncomingCalls();
	        				}
	        		
	        			if (unit.equalsIgnoreCase("mph"))
	        				if(speed >= speedLimitMPH) {
	        					makeProfileSilent();
	        					listenForIncomingCalls();
	        				}
	        				else if(speed < speedLimitMPH) {
	        					makeProfileNormal();	
	        					DontlistenForIncomingCalls();
	        				}
	        		}
	        	}
	        	catch (Exception e) {
	        		showToast("Tracking your speed seems hard, but will keep trying.");
	    	    	e.printStackTrace();
	        	}	        	
	        	
//	        	checkProfileStatus();	        	
//	        	TextView pp = (TextView) findViewById(R.id.speed_label);
//	        	pp.setText("Latitude: " + String.format("%9.6f", latitude));
//	        	TextView ppp = (TextView) findViewById(R.id.profile_label);
//	        	ppp.setText("Longitude: " + String.format("%9.6f", longitude));

	        }
	    };

//	    final LocationListener networkLocationListener = new LocationListener() {
//
//	        @Override
//	        public void onStatusChanged(String provider, int status, Bundle extras) {
//	            switch (status) {
//	            case LocationProvider.AVAILABLE:
//	                //textView.setText(textView.getText().toString() + "Network location available again\n");
//	                break;
//	            case LocationProvider.OUT_OF_SERVICE:
//	                //textView.setText(textView.getText().toString() + "Network location out of service\n");
//	                break;
//	            case LocationProvider.TEMPORARILY_UNAVAILABLE:
//	                //textView.setText(textView.getText().toString() + "Network location temporarily unavailable\n");
//	                break;
//	            }
//	        }
//
//	        @Override
//	        public void onProviderEnabled(String provider) {
//	            //textView.setText(textView.getText().toString() + "Network Provider Enabled\n");
//
//	        }
//
//	        @Override
//	        public void onProviderDisabled(String provider) {
//	            //textView.setText(textView.getText().toString() + "Network Provider Disabled\n");
//	        }
//
//	        @Override
//	        public void onLocationChanged(Location location) {
//	        	if(!location.hasSpeed())
//	        		return;
//	        	latitude = location.getLatitude();
//	        	longitude = location.getLongitude();
//	        	//speed = 0;
//	        	if(unit.equalsIgnoreCase("kmph"))
//	        		speed= (int) (Math.round(location.getSpeed() * 3.6));
//	        	else
//	        		if(unit.equalsIgnoreCase("mph"))
//	        			speed= (int) (Math.round((location.getSpeed() * 3.6 * 0.621371)));
//	        			
//	        	setSpeed();
//	        	
//	        	if(speed >0) {
//	        		if(unit.equalsIgnoreCase("kmph"))
//	        			if(speed > speedLimit) {
//	        				makeProfileSilent();
//	        				listenForIncomingCalls();
//	        			}
//	        			else if(speed <= speedLimit) {
//	        				makeProfileNormal();      
//	        				DontlistenForIncomingCalls();
//	        			}
//	        		
//	        		if (unit.equalsIgnoreCase("mph"))
//	        			if(speed > speedLimitMPH) {
//	        				makeProfileSilent();
//	        				listenForIncomingCalls();
//	        			}
//	        			else if(speed <= speedLimitMPH) {
//	        				makeProfileNormal();	
//	        				DontlistenForIncomingCalls();
//	        			}
//	        	}
//	        	
////	        	checkProfileStatus(); 	
////	        	TextView pp = (TextView) getActivity().findViewById(R.id.speed_label);
////	        	pp.setText(String.format("%9.6f", latitude));
////	        	TextView ppp = (TextView) getActivity().findViewById(R.id.profile_label);
////	        	ppp.setText(String.format("%9.6f", longitude));
//
//	        }
//	    };   
	    
	    try {
//	    	Criteria criteria = new Criteria();
////		    criteria.setAccuracy(Criteria.ACCURACY_FINE);
//		    criteria.setAltitudeRequired(false);
//		    criteria.setBearingRequired(false);
//		    criteria.setSpeedRequired(true);
//		    criteria.setSpeedAccuracy(Criteria.ACCURACY_HIGH);
////		    criteria.setCostAllowed(true);
//		    criteria.setPowerRequirement(Criteria.POWER_LOW);
//	    	String bestProvider = locationManager.getBestProvider(criteria, true);
//	    	locationManager.requestLocationUpdates(bestProvider, 0, 2, locationListener);
	    	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10, locationListener); 
	    	try {
		    	locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 10, locationListener);
		    }
		    catch(Exception e) {
		    	e.printStackTrace();
		    }
	    }
	    catch (Exception e)
	    {	    	
	    	showToast("Oops, error encountered while tracking your speed.");
	    	e.printStackTrace();
	    }
	    
	}
	

	private void setSpeed() {
		int digit1, digit2, digit3;
		if (speed <= 0)
		{
			speedDigit1.setText("0");
			speedDigit1.setTextColor(getResources().getColor(R.color.not_available));
			speedDigit2.setText("0");
			speedDigit2.setTextColor(getResources().getColor(R.color.not_available));
			speedDigit3.setText("0");			
			speedDigit3.setTextColor(getResources().getColor(R.color.speed_zero));
			return;
		}
		
		digit3 = speed % 10;
		digit2 = (speed/10) % 10;
		digit1 = (speed/100) % 10;		
		
		speedDigit1.setText(String.valueOf(digit1));		
		speedDigit2.setText(String.valueOf(digit2));		
		speedDigit3.setText(String.valueOf(digit3));			
		
		int color = 0;    		
    	if(unit.equalsIgnoreCase("kmph"))
    		if(speed >= speedLimit)
    			color = R.color.speed_exceed;
    		else if(speed < speedLimit)
    			color = R.color.mobile_blue;
    	
    	if (unit.equalsIgnoreCase("mph"))
    		if(speed >= speedLimitMPH)
    			color = R.color.speed_exceed;
    		else if(speed < speedLimitMPH)
    			color = R.color.mobile_blue;
    	
    	if(color==0)
    		return;
		
		if(digit1 == 0)
			speedDigit1.setTextColor(getResources().getColor(R.color.not_available));
		else
			speedDigit1.setTextColor(getResources().getColor(color));
		
		if(digit1 == 0 && digit2 == 0 )
			speedDigit2.setTextColor(getResources().getColor(R.color.not_available));
		else
			speedDigit2.setTextColor(getResources().getColor(color));
		
		speedDigit3.setTextColor(getResources().getColor(color));
	}
	
	private void changeUnit(View view) {		
		if(unit.equalsIgnoreCase("kmph"))
			convertToMph(view);
		else if(unit.equalsIgnoreCase("mph"))
			convertToKmph(view);
	}
	
	private void checkProfileStatus() {		
		int ringermode = mAudioManager.getRingerMode();
        if (ringermode == AudioManager.RINGER_MODE_SILENT || ringermode == AudioManager.RINGER_MODE_VIBRATE) {
            profileStatus = "silent";
            profile.setText(R.string.profile_silent);
            profile.setTextColor(getResources().getColor(R.color.profile_silent));
        }
        else
        {
        	profileStatus = "normal";
        	profile.setText(R.string.profile_general);
            profile.setTextColor(getResources().getColor(R.color.profile_general));
        }       
	}
	
	private void makeProfileSilent() {
		mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
		//mAudioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
	}
	
	private void makeProfileNormal() {
		mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);		
	}
	
	private void listenForIncomingCalls() {
		incomingCallManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
		incomingCallListener = new PhoneStateListener () {
	        @Override
	        public void onCallStateChanged(int state, String incomingNumber) {
	            switch (state) {
	                case TelephonyManager.CALL_STATE_RINGING:
	                	if (profileStatus.equalsIgnoreCase("normal"))
	                		break;
	                	// called when someone is ringing to this phone
	                	Class c=null;
	                	try {
	                		c = Class.forName(incomingCallManager.getClass().getName());
	                	} catch (ClassNotFoundException e) {
	                		// TODO Auto-generated catch block
	                		e.printStackTrace();
	                	}
	                	Method m=null;
	                	try {
	                		m = c.getDeclaredMethod("getITelephony");
	                	} catch (SecurityException e) {
	                		// TODO Auto-generated catch block
	                		e.printStackTrace();
	                	} catch (NoSuchMethodException e) {
	                		// TODO Auto-generated catch block
	                		e.printStackTrace();
	                	}
	                		m.setAccessible(true);
	                		ITelephony telephonyService=null;
	                		try {
	                			telephonyService = (ITelephony) m.invoke(incomingCallManager);
	                		} catch (IllegalArgumentException e) {
	                			// TODO Auto-generated catch block
	                			e.printStackTrace();
	                		} catch (IllegalAccessException e) {
	                			// TODO Auto-generated catch block
	                			e.printStackTrace();
	                		} catch (InvocationTargetException e) {
	                			// TODO Auto-generated catch block
	                			e.printStackTrace();
	                		}
	                		//Bundle bundle = intent.getExtras();
	                		telephonyService.endCall();
	                		
	                		boolean smsSent = false;
	                		GregorianCalendar date = new GregorianCalendar();
	                		int currentTime = date.get(Calendar.HOUR) * 100 + date.get(Calendar.MINUTE);
	                		if(lastIncomingNumber.equalsIgnoreCase(incomingNumber) && (currentTime - lastIncomingCallTime) < 9)
	                			smsSent = true;
	                		else {
	                			smsSent = false;
	                			lastIncomingCallTime = currentTime;
	                			lastIncomingNumber = incomingNumber;
	                		}
	                		
	                		if (sendSMS && !smsSent)
	                			sendSMS(incomingNumber);
	                		break;
	            }
	        }	        
	    };
	    
	    incomingCallManager.listen(incomingCallListener, PhoneStateListener.LISTEN_CALL_STATE);
	}
	
	private void DontlistenForIncomingCalls() {	    
	    incomingCallManager.listen(incomingCallListener, PhoneStateListener.LISTEN_NONE);
	}
	
	private void sendSMS(String incomingNumber) {
		String name = "";
		String messageText = smsText;
		try {
			// define the columns I want the query to return
			String[] projection = new String[] {
			        ContactsContract.PhoneLookup.DISPLAY_NAME,
			        ContactsContract.PhoneLookup._ID};
			// encode the phone number and build the filter URI
			Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(incomingNumber));
			// query
			Cursor cursor = this.getContentResolver().query(contactUri, projection, null, null, null);
			if (cursor.moveToFirst())
			    name = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
			else
				name = "";
			
			int i = -1;
			i = smsText.indexOf("Hi!");
			if(i==-1)
				i = smsText.indexOf("Hi ");
			if(i==-1)
				i = smsText.indexOf("Hey");
			
			if(i != -1 && !name.equalsIgnoreCase(""))
				messageText = smsText.substring(0, i+2) + " " + name + " " + smsText.substring(i+3, smsText.length());
			
			messageText = messageText + " Sent by Jarvis :)";
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			SmsManager sms = SmsManager.getDefault();
			sms.sendTextMessage(incomingNumber, null, messageText, null, null);
			ContentValues values = new ContentValues();             
		    values.put("address", incomingNumber); 		              
		    values.put("body", messageText); 		              
		    this.getContentResolver().insert(Uri.parse("content://sms/sent"), values);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	  super.onActivityResult(requestCode, resultCode, data);
	  switch(requestCode) {
	    case (SETTINGS_ACTIVITY) : {
	      if (resultCode == Activity.RESULT_OK) {
//	    	  if (data.hasExtra("UserPref")) {
//	    		  String dataReturned = data.getExtras().getString("UserPref");
//	          }
	    	  loadSettings();	    	
	      }
	      break;
	    } 
	  }
	}
	
	private void loadSettings() {
		try {
		    BufferedReader inputReader = new BufferedReader(new InputStreamReader(
		            this.openFileInput("jarvisSettings")));
		    String inputString;
		    StringBuffer stringBuffer = new StringBuffer();                
		    while ((inputString = inputReader.readLine()) != null) {
		        stringBuffer.append(inputString + "\n");
		    }
		    int delimiter = 1;
		    int newStartPos = 0;
		    for(int i=0; i<stringBuffer.toString().length(); i++) {
		    	if(stringBuffer.toString().charAt(i) == '~') {
		    		switch(delimiter) {
		    		case 1:
		    			try {
		    				speedLimit = Integer.parseInt(stringBuffer.toString().substring(newStartPos, i));
		    				speedLimitMPH = (int) (Math.round((speedLimit * 0.621371)));
		    			} catch(Exception ex) {
		    				//do nothing
		    			}
		    			delimiter++;
		    			newStartPos = i+1;
		    			break;
		    		case 2:
		    			if(stringBuffer.toString().substring(newStartPos, i).equalsIgnoreCase("true"))
		    				sendSMS = true;
		    			else
		    				sendSMS = false;
		    			delimiter++;
		    			newStartPos = i+1;
		    			break;
		    		case 3:
		    			if(!sendSMS) {
		    				break;
		    			}
		    			smsText = stringBuffer.toString().substring(newStartPos, i);
		    			delimiter++;
		    			newStartPos = i+1;
		    			break;
		    		}
		    	}
		    }	    
		} 
		catch (IOException e) {
			speedLimit = 50;
			speedLimitMPH = (int) (Math.round((speedLimit * 0.621371)));
			sendSMS = true;
			smsText = getResources().getString(R.string.default_SMS_text);			
		    e.printStackTrace();
		}
		GregorianCalendar date = new GregorianCalendar();
		lastIncomingCallTime = date.get(Calendar.HOUR) * 100 + date.get(Calendar.MINUTE);
		lastIncomingNumber = "";
		
		//loading settings from shared preferences
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		isLogsEnabled = settings.getBoolean("pref_logs", true);
		try {
			speedLimit = (Integer.parseInt(settings.getString("pref_maxSpeedLimit", "50")));
		}
		catch (Exception e) {
			speedLimit = 50;
		}
		speedLimitMPH = (int) (Math.round((speedLimit * 0.621371)));
		boolean iscustomSMSEnabled = settings.getBoolean("pref_readSMS", true);
		sendSMS = settings.getBoolean("pref_autodecline_summary", true);
		smsText	= settings.getString("pref_custom_sms", getResources().getString(R.string.default_SMS_text));
		//smsText = getResources().getString(R.string.default_SMS_text);		
	}
	
	private void checkGPS(Bundle savedInstanceState) {
		final LocationManager manager = (LocationManager) this.getSystemService( Context.LOCATION_SERVICE );

	    if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
	    	gpsStarted = false;
	    	GPSStatus.setVisibility(View.VISIBLE);
	    	if(savedInstanceState == null) {
	    		buildAlertMessageNoGps(this.findViewById(android.R.id.content));
	    	}	        
	    }
	    else {
	    	gpsStarted = true;
	    	GPSStatus.setVisibility(View.GONE);
	    }
	}
	
	private void buildAlertMessageNoGps(View view) {
		GPSStatus.setImageResource(R.drawable.gpsoffglow);
	    final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, android.R.style.Theme_Holo));
	    builder.setMessage("Your GPS seems to be disabled, do you want to enable it ?")
	           .setCancelable(false)
	           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	               public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
	                   startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
	                   GPSStatus.setImageResource(R.drawable.gpsoff);
	               }
	           })
	           .setNegativeButton("No", new DialogInterface.OnClickListener() {
	               public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
	            	   GPSStatus.setImageResource(R.drawable.gpsoff);
	            	   dialog.cancel();
	               }
	           })
	           .setTitle("No Signal")
	           .setIcon(R.drawable.ic_no_gps_dark);
	    final AlertDialog alert = builder.create();
	    alert.show();
	}
	
	private void changeProfileManualPopUp(View view) {
		if(speed <= speedLimit)
			return;
		final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, android.R.style.Theme_Holo));
		if(profileStatus.equalsIgnoreCase("normal")) {
			builder
	           .setCancelable(false)
	           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	               public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
	            	   makeProfileSilent();
	            	   manualOverride = false;
	            	   showToast("Keep driving, iDriveSafe is back in charge.");
	               }
	           })
	           .setNegativeButton("No", new DialogInterface.OnClickListener() {
	               public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
	                    dialog.cancel();
	               }
	           })
	           .setTitle("Activate iDriveSafe ?")
	           .setIcon(android.R.drawable.ic_lock_silent_mode);
			final AlertDialog alert = builder.create();
			alert.show();
		}
		else if(profileStatus.equalsIgnoreCase("silent")) {
			builder
	           .setCancelable(false)
	           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	               public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
	            	   makeProfileNormal();
	            	   manualOverride = true;
	            	   showToast("Deactivated.");
	               }
	           })
	           .setNegativeButton("No", new DialogInterface.OnClickListener() {
	               public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
	                    dialog.cancel();
	               }
	           })
	           .setTitle("Override?")
	           .setIcon(android.R.drawable.ic_lock_silent_mode_off);
			final AlertDialog alert = builder.create();
			alert.show();
		}		
	}	
	
	private void showToast(String message) {
		Context context = this.getApplicationContext();
		CharSequence text = message;
		int duration = Toast.LENGTH_LONG;
		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
	}
	
	//after integration from abhilasha
	@Override
    protected void onResume() {
    	// TODO Auto-generated method stub
    	super.onResume();
    	loadSettings();
    	dayOrNight();
    }
    
    
    private void showJarvisNotification(){
    	NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    	Intent intent = new Intent(this, Home.class);
    	PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, Notification.FLAG_ONGOING_EVENT);
    	NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
    	builder.setContentTitle("Jarvis");
    	builder.setContentIntent(pendingIntent);
    	builder.setContentText("Jarvis is listening...");
    	builder.setSmallIcon(R.drawable.ic_launcher);
    	notificationManager.notify(1234, builder.build());
    }
    private void setActionBarDrawerToggle(){
    	actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_closed);
    	drawerLayout.setDrawerListener(actionBarDrawerToggle);
    	actionBar.setDisplayHomeAsUpEnabled(true);
    	actionBar.setHomeButtonEnabled(true);
    	
    }
    
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
    	// TODO Auto-generated method stub
    	super.onPostCreate(savedInstanceState);
    	actionBarDrawerToggle.syncState();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	// TODO Auto-generated method stub
    	super.onConfigurationChanged(newConfig);
    	actionBarDrawerToggle.onConfigurationChanged(newConfig);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	// TODO Auto-generated method stub
    	if(actionBarDrawerToggle.onOptionsItemSelected(item)){
    		return true;
    	}
    	return super.onOptionsItemSelected(item);
    }

	
	

    //Code by Azahr start
	
	private boolean isConnected(){
    	if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isAvailable()){
    		return true;
    	}else if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isAvailable()){
    		return true;
    	}else{
    		return false;
    	}
    }

    private void showMyDialog() {
		// TODO Auto-generated method stub
		AlertDialog.Builder  dialog = new AlertDialog.Builder(Home.this);
		dialog.setTitle("Enable GPS");
		dialog.setMessage("Please turn on your GPS");
		
		
		dialog.setPositiveButton("OK", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivity(intent);
				arg0.dismiss();
			}
		});
		
		dialog.setNegativeButton("Cancel", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				arg0.dismiss();
			}
		});
		
		dialog.show();
	}
    
    private void showMyMobileDataDialog() {
		// TODO Auto-generated method stub
		AlertDialog.Builder  dialog = new AlertDialog.Builder(Home.this);
		dialog.setTitle("Enable Internet");
		dialog.setMessage("Please turn on your mobile data");
		
		
		dialog.setPositiveButton("OK", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
				startActivity(intent);
				arg0.dismiss();
			}
		});
		
		dialog.setNegativeButton("Cancel", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				arg0.dismiss();
			}
		});
		
		dialog.show();
	}
    
    public void startService(){
    	startService(proximityService);
    }

    public void stopService(){
    	stopService(proximityService);
    }
    
//    public void showParkedLocation(View view){
//    	Intent i = new Intent(getApplicationContext(), ShowParkedCar.class);
//    	startActivity(i);
//    }
    
//    public void saveParkedLocation(View view){
//    	editor.putLong(Holder.LATTITUDE, (long) mLat);
//    	editor.putLong(Holder.LONGITUDE, (long) mLong);
//    	editor.commit();
//    	
//    	showToast("Parking location saved!!! " + "lat : " + mLat + " long : " + mLong);
//    }
    
    //private void showToast(String message){
	//	Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
	//}


//	@Override
//	public void onLocationChanged(Location location) {
//		// TODO Auto-generated method stub
//		mLat = location.getLatitude();
//		mLong = location.getLongitude();
//	}
//
//
//	@Override
//	public void onProviderDisabled(String arg0) {
//		// TODO Auto-generated method stub
//		
//	}
//
//
//	@Override
//	public void onProviderEnabled(String arg0) {
//		// TODO Auto-generated method stub
//		
//	}
//
//
//	@Override
//	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
//		// TODO Auto-generated method stub
//		
//	}
	
	//Azhar oncreate code
    private void onCreateByAzhar(){
    	Settings.System.putInt(getContentResolver(), System.SCREEN_BRIGHTNESS_MODE, System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        
        connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        preferences = this.getSharedPreferences(Holder.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
        
        proximityService = new Intent(getApplicationContext(), ProximityService.class);
        
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        boolean isGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(!isConnected()){
        	showMyMobileDataDialog();
        }
        if(!isGPS){
        	showMyDialog();
        	
        }
//        try {
//        	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,10 * 60 * 1000, 100, this);
//        	try {
//        		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,10 * 60 * 1000, 100, this);
//			} catch (Exception e) {
//				// TODO: handle exception
//			}
//		} catch (Exception e) {
//			// TODO: handle exception
//		} 
        startService();
    }

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		drawerLayout.closeDrawers();
		Toast.makeText(this, drawerListItems[arg2]+" Clicked!!!", Toast.LENGTH_SHORT).show();
		switch (arg2) {
		case 0:
			
			break;

		case 1:
			Intent showParkedCarIntent = new Intent(this, ShowParkedCar.class);
			startActivity(showParkedCarIntent);
			break;

		case 2:
			Intent settings = new Intent(this,SettingsActivity.class);
			startActivity(settings);
			break;
		
		case 3:
			Intent drivingLogsIntent = new Intent(this,DrivingLogsActivity.class);
			startActivity(drivingLogsIntent);
			break;
		
		default:
			break;
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		stopService();
		closeNotification();
	}
	
	private void closeNotification(){
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mNotificationManager.cancel(1234);
	}
	//Azhar code ends
}
