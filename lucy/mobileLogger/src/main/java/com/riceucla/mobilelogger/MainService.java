package com.riceucla.mobilelogger;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.Browser.BookmarkColumns;
import android.provider.CallLog;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

/*import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.NeighboringCellInfo;*/
// import android.util.Log;
// import android.widget.Toast;


@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class MainService extends Service
{	
	// ========== these are subject to change ===========
	public static int hour = 3600000;
	public static int min = 60000;
	public static int sec = 1000;
    //for testint purpose, use 10*sec as interval
	public static long screenINTERVAL = 2*min;
	public static long networkINTERVAL = 1*hour;
	public static long deviceINTERVAL = 1*hour;
	public static long cellularINTERVAL = 1*hour;
	public static long wifiINTERVAL = 1*hour;
	public static long appINTERVAL = 2*min;
	public static long locINTERVAL = 15*min;
	public static long webINTERVAL = 1*hour;
	public static long smsINTERVAL = 1*hour;
	public static long callINTERVAL = 1*hour;
    public static long stepsINTERVAL = 1*hour;
    public static long accelerometerINTERVAL = 30*min;
    //this is the interval in which we actively sample accelerometer data
    public static long accelerometerSampleINTERVAL = 30*sec;
    public static long accelerometerSampleFrequency = 1*sec;
	public static long INTERVAL = 12*sec;
    //changed for testing purpose
	public static long uploadINTERVAL = Config.UPLOAD_INTERVAL*sec;
	public static long uploadSUCCESS_INTERVAL = 2*hour;

	private long lastScreenCheck=System.currentTimeMillis()-24*hour;
	private long lastNetworkCheck=System.currentTimeMillis()-24*hour;
	private long lastDeviceCheck=System.currentTimeMillis()-24*hour;
	private long lastCellularCheck=System.currentTimeMillis()-24*hour;
	private long lastWifiCheck=System.currentTimeMillis()-24*hour;
	private long lastAppCheck=System.currentTimeMillis()-24*hour;
	private long lastLocCheck=System.currentTimeMillis()-24*hour;
	private long lastWebCheck=System.currentTimeMillis()-24*hour;
	private long lastSmsCheck=System.currentTimeMillis()-24*hour;
	private long lastCallCheck=System.currentTimeMillis()-24*hour;
    private long lastStepsCheck=System.currentTimeMillis()-24*hour;
    private long lastAccelerometerCheck=System.currentTimeMillis()-24*hour;
	private long lastUploadCheck = System.currentTimeMillis()-24*hour;
	
	// do not upload immediately, or the gui freezes when the app
	// is first started (in order to prevent concurrent access to the database)
	// tentatively wait 5 mins.
	public static long lastUploadSuccess = System.currentTimeMillis() - 5*min; //-24*hour;

	public static int memoryFactor = 2;	// measurements are considered current
										// if they are taken within the last
										// memoryFactor * [updating_interval_of_the_parameter]
										// seconds
	// ==================================================
	
	public static final int NOT_AVAILABLE = -1000;

	private Timer mTimer = new Timer();
	private ActivityManager mActivityManager;
	private SignalStrengthListener ssl;
	private TelephonyManager tManager;
	private SignalStrength signalStrength;	// cellular signal strength

    private static DatabaseHelper dbHelper;
	private static SQLiteDatabase mDatabase;

    //for accelometer and step counter
    private SensorManager mSensorManager;
    private Sensor stepsCounterSensor;
    private Sensor accelerometerSensor;
    private int totalSteps;
    private float lastAccelerationX;
    private float lastAccelerationY;
    private float lastAccelerationZ;
	
	public static String UUID="";

	public static SharedPreferences lastCheck;
	WakeLock wakeLock;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	static int i=0;
	@Override
	public void onCreate() 
	{
		mActivityManager = (ActivityManager) getApplication().getSystemService(Context.ACTIVITY_SERVICE);
		tManager = (TelephonyManager) getApplication().getSystemService(Context.TELEPHONY_SERVICE);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepsCounterSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        mSensorManager.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                totalSteps = (int)sensorEvent.values[0];
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        }, stepsCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);

        //use the linear accelerometer
        accelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mSensorManager.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                lastAccelerationX = sensorEvent.values[0];
                lastAccelerationY = sensorEvent.values[1];
                lastAccelerationZ = sensorEvent.values[2];
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        }, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);

        final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
        UUID=tm.getDeviceId();
        dbHelper = new DatabaseHelper(getApplicationContext(), UUID, getDate());
		ssl = new SignalStrengthListener();
		tManager.listen(ssl, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		
		lastCheck = getApplicationContext().getSharedPreferences("LastCheck", MODE_PRIVATE);
		lastScreenCheck=lastCheck.getLong("screen", System.currentTimeMillis()-24*hour);
		lastNetworkCheck=lastCheck.getLong("network", System.currentTimeMillis()-24*hour);
		lastDeviceCheck=lastCheck.getLong("device", System.currentTimeMillis()-24*hour);
		lastCellularCheck=lastCheck.getLong("cellular", System.currentTimeMillis()-24*hour);
		lastWifiCheck=lastCheck.getLong("wifi", System.currentTimeMillis()-24*hour);
		lastAppCheck=lastCheck.getLong("app", System.currentTimeMillis()-24*hour);
		lastLocCheck=lastCheck.getLong("loc", System.currentTimeMillis()-24*hour);
		lastWebCheck=lastCheck.getLong("web", System.currentTimeMillis()-24*hour);
		lastSmsCheck=lastCheck.getLong("sms", System.currentTimeMillis()-24*hour);
		lastCallCheck=lastCheck.getLong("call", System.currentTimeMillis()-24*hour);
        lastStepsCheck=lastCheck.getLong("steps", System.currentTimeMillis()-24*hour);
        lastAccelerometerCheck=lastCheck.getLong("accelerometer", System.currentTimeMillis()-24*hour);
		lastUploadCheck=lastCheck.getLong("upload", System.currentTimeMillis()-24*hour);
		
		// again, do not upload immediately
		lastUploadSuccess=lastCheck.getLong("uploadSuccess", System.currentTimeMillis()-5*min);
		// lastUploadSuccess=lastCheck.getLong("uploadSuccess", System.currentTimeMillis()-48*hour);
		
		/*lastScreenCheck=Math.min(lastScreenCheck, lastUploadSuccess);
		lastNetworkCheck=Math.min(lastAppCheck, lastUploadSuccess);
		lastDeviceCheck=Math.min(lastAppCheck, lastUploadSuccess);
		lastCellularCheck=Math.min(lastAppCheck, lastUploadSuccess);
		lastWifiCheck=Math.min(lastAppCheck, lastUploadSuccess);
		lastAppCheck=Math.min(lastAppCheck, lastUploadSuccess);
		lastLocCheck=Math.min(lastAppCheck, lastUploadSuccess);
		lastWebCheck=Math.min(lastAppCheck, lastUploadSuccess);
		lastSmsCheck=Math.min(lastAppCheck, lastUploadSuccess);
		lastCallCheck=Math.min(lastAppCheck, lastUploadSuccess);
		lastUploadCheck=Math.min(lastAppCheck, lastUploadSuccess);*/

/*		MyWakefulBroadcastReceiver myRssiChangeReceiver
		= new MyWakefulBroadcastReceiver();

			IntentFilter rssiFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
			this.registerReceiver(myRssiChangeReceiver, rssiFilter);

			WifiManager wifiMan=(WifiManager) getSystemService(Context.WIFI_SERVICE);
			wifiMan.startScan();*/
		
		//code to execute when the service is first created
		mTimer.scheduleAtFixedRate( new TimerTask() 
		{

			public void run() 
			{
				//System.out.println("=== Collect Logs ===");
				Editor editor = lastCheck.edit();
				if (System.currentTimeMillis()-lastSmsCheck>smsINTERVAL){
					getSMS();
					lastSmsCheck=System.currentTimeMillis();
					editor.putLong("sms", lastSmsCheck);
									}
				if (System.currentTimeMillis()-lastCallCheck>callINTERVAL){
					getCalls();
					lastCallCheck=System.currentTimeMillis();
					editor.putLong("call", lastCallCheck);
				}
				if (System.currentTimeMillis()-lastAppCheck>appINTERVAL){
					getApps();
					lastAppCheck=System.currentTimeMillis();
					editor.putLong("app", lastAppCheck);
				}
				if (System.currentTimeMillis()-lastLocCheck>locINTERVAL){
					try {
                        getLoc();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
					lastLocCheck=System.currentTimeMillis();
					editor.putLong("loc", lastLocCheck);
				}
				if (System.currentTimeMillis()-lastWebCheck>webINTERVAL){
					getWeb();
					lastWebCheck=System.currentTimeMillis();
					editor.putLong("web", lastWebCheck);
				}
				if (System.currentTimeMillis()-lastWifiCheck>wifiINTERVAL){
					getWifi();
					lastWifiCheck=System.currentTimeMillis();
					editor.putLong("wifi", lastWifiCheck);
				}
				if (System.currentTimeMillis()-lastCellularCheck>cellularINTERVAL){
					getCellular();
					lastCellularCheck=System.currentTimeMillis();
					editor.putLong("cellular", lastCellularCheck);
				}
				if (System.currentTimeMillis()-lastDeviceCheck>deviceINTERVAL){
					getDevice();
					lastDeviceCheck=System.currentTimeMillis();
					editor.putLong("device", lastDeviceCheck);
				}
				if (System.currentTimeMillis()-lastNetworkCheck>networkINTERVAL){
					getNetwork();
					lastNetworkCheck=System.currentTimeMillis();
					editor.putLong("network", lastNetworkCheck);
				}
				if (System.currentTimeMillis()-lastScreenCheck>screenINTERVAL){
					getScreen();
					lastScreenCheck=System.currentTimeMillis();
					editor.putLong("screen", lastScreenCheck);
				}
                if (System.currentTimeMillis()-lastStepsCheck>stepsINTERVAL){
                    getSteps();
                    lastStepsCheck=System.currentTimeMillis();
                    editor.putLong("steps", lastStepsCheck);
                }
				if (System.currentTimeMillis()-lastUploadCheck>uploadINTERVAL){
					upload();
					lastUploadCheck=System.currentTimeMillis();
					editor.putLong("upload", lastUploadCheck);
				}
				editor.commit();
				//System.out.println("=== Finished Collecting ===");
			}

		}, 0, INTERVAL);

        //create a second timer for task that needs higher sample frequency
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (System.currentTimeMillis()-lastAccelerometerCheck<accelerometerSampleINTERVAL){
                    getAcceleration();
                }
                if (System.currentTimeMillis() - lastAccelerometerCheck > accelerometerINTERVAL){
                    lastAccelerometerCheck=System.currentTimeMillis();
                    Editor editor = lastCheck.edit();
                    editor.putLong("accelerometer", lastAccelerometerCheck);
                    editor.commit();
                }
            }
        }, 0, accelerometerSampleFrequency);

	}
	public void upload()
	{
		if (System.currentTimeMillis()-lastUploadSuccess>uploadSUCCESS_INTERVAL)
		{
			waitUntilAvailable();
			mDatabase = dbHelper.getWritableDatabase();
			if (Uploader.upload(mDatabase, UUID))
			{
                cleanTables();
				close();
				lastUploadSuccess=System.currentTimeMillis();
				Editor editor = lastCheck.edit();
				editor.putLong("uploadSuccess", lastUploadSuccess);
				editor.commit();

			}
		}
	}

    /**
     * for testing purpose, add a force upload reference
     * NOTE: I changed the lastUploadSuccess and lastCheck variable to a static field
     */
    public static void forceUpload(){
        waitUntilAvailable();
        mDatabase = dbHelper.getWritableDatabase();
        if (Uploader.upload(mDatabase, UUID))
        {
            Log.v("mobilelogger", "forced upload");
            close();
            lastUploadSuccess=System.currentTimeMillis();
            Editor editor = lastCheck.edit();
            editor.putLong("uploadSuccess", lastUploadSuccess);
            editor.commit();

        }
    }
/*	public int onStartCommand(Intent intent, int flags, int startId) 
	{
		Log.w("Starting LiveLab","Starting LiveLab Service");
		Context context = getApplicationContext();
		CharSequence text = "LiveLab Started";
		int duration = 50000000;

		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
		return Service.START_NOT_STICKY;
	}*/



	public void getCalls()
	{
		//System.out.println("getCalls");
		try{
			Uri uri = CallLog.Calls.CONTENT_URI;
			Cursor c = getContentResolver().query(uri, null, null, null, null);
			if (c.moveToFirst()) 
			{
				for (int i=0; i<c.getCount(); i++) 
				{
					String number=c.getString(c.getColumnIndex(CallLog.Calls.NUMBER));

					// number=number.substring(number.length()-10, number.length());

					String date=""+c.getLong(c.getColumnIndex(CallLog.Calls.DATE));
					String duration=""+c.getLong(c.getColumnIndex(CallLog.Calls.DURATION));

					String type = c.getString(c.getColumnIndex(CallLog.Calls.TYPE));

					if (Long.parseLong(date) > lastCallCheck)
					{
						////System.out.println(" + " + hash(number)+":"+date+","+duration);
						ContentValues values = new ContentValues();
						values.put(DatabaseHelper.COLUMN_CALL_NUMBER, hash(number));
						values.put(DatabaseHelper.COLUMN_CALL_TYPE, type);
						values.put(DatabaseHelper.COLUMN_CALL_TIMESTAMP, date);
						values.put(DatabaseHelper.COLUMN_CALL_DURATION, duration);

						waitUntilAvailable();
						mDatabase = dbHelper.getWritableDatabase();
						mDatabase.insert(DatabaseHelper.TABLE_CALLS, null, values);
						close();
					}

					c.moveToNext();
				}
			}

			c.close();
		}
		catch(Exception e)
		{
			System.out.println("Calls Failed");
			e.printStackTrace();
		}
	}

    public void getSteps()
    {

        Log.v("STEP data", "" + totalSteps);
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_TOTAL_STEPS, totalSteps);
        values.put(DatabaseHelper.COLUMN_STEPS_TIMESTEMP, System.currentTimeMillis());

        waitUntilAvailable();
        mDatabase = dbHelper.getWritableDatabase();
        mDatabase.insert(DatabaseHelper.TABLE_STEPS, null, values);
        close();

    }

    public void getAcceleration()
    {

        ContentValues values = new ContentValues();

        Log.v("ACCELERATION data","x:" + lastAccelerationX + "y:" + lastAccelerationY + "z:" + lastAccelerationZ);

        values.put(DatabaseHelper.COLUMN_ACCELERATION_X, lastAccelerationX);
        values.put(DatabaseHelper.COLUMN_ACCELERATION_Y, lastAccelerationY);
        values.put(DatabaseHelper.COLUMN_ACCELERATION_Z, lastAccelerationZ);
        values.put(DatabaseHelper.COLUMN_ACCELEROMETER_TIMESTEMP, System.currentTimeMillis());

        waitUntilAvailable();
        mDatabase = dbHelper.getWritableDatabase();
        mDatabase.insert(DatabaseHelper.TABLE_ACCELEROMETER, null, values);
        close();

    }

    public void getSMS()
	{
		try
		{
			//System.out.println("getSMS");
			//Uri Calls = Uri.parse("content://call_log/calls");
			Cursor c = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
			if (c.moveToFirst()) {
				for (int i=0; i<c.getCount(); i++) {
					// Do stuff with calls here! We can do stuff with the following!
					String number=c.getString(c.getColumnIndex("address"));
					/*if (number.length()>=10){
						number=number.substring(number.length()-10,number.length());
					}*/
					String date=""+c.getLong(c.getColumnIndex("date"));
					String length=""+c.getString(c.getColumnIndex("body")).length();

					if (Long.parseLong(date) > lastSmsCheck){
						//System.out.println(" + " + hash(number)+":"+date+","+length);
						ContentValues values = new ContentValues();
						values.put(DatabaseHelper.COLUMN_SMS_NUMBER, hash(number));
						values.put(DatabaseHelper.COLUMN_SMS_TIMESTAMP, date);
						values.put(DatabaseHelper.COLUMN_SMS_TYPE, "incoming");
						values.put(DatabaseHelper.COLUMN_SMS_LENGTH, length);

						waitUntilAvailable();
						mDatabase = dbHelper.getWritableDatabase();
						mDatabase.insert(DatabaseHelper.TABLE_SMS, null, values);
						close();
					}


					c.moveToNext();
				}
			}
			c.close();
		}
		catch(Exception e)
		{
			System.err.println(e);
			e.printStackTrace();
		}
		
		try
		{
			//System.out.println("getSMS");
			//Uri Calls = Uri.parse("content://call_log/calls");
			Cursor c = getContentResolver().query(Uri.parse("content://sms/sent"), null, null, null, null);
			if (c.moveToFirst()) {
				for (int i=0; i<c.getCount(); i++) {
					// Do stuff with calls here! We can do stuff with the following!
					String number=c.getString(c.getColumnIndex("address"));
					if (number.length()>=10){
						number=number.substring(number.length()-10,number.length());
					}
					String date=""+c.getLong(c.getColumnIndex("date"));
					String length=""+c.getString(c.getColumnIndex("body")).length();

					try{
						if (Long.parseLong(date) > lastSmsCheck){
							//System.out.println(" + " + hash(number)+":"+date+","+length);
							ContentValues values = new ContentValues();
							values.put(DatabaseHelper.COLUMN_SMS_NUMBER, hash(number));
							values.put(DatabaseHelper.COLUMN_SMS_TIMESTAMP, date);
							values.put(DatabaseHelper.COLUMN_SMS_TYPE, "outgoing");
							values.put(DatabaseHelper.COLUMN_SMS_LENGTH, length);
							
							waitUntilAvailable();
							mDatabase = dbHelper.getWritableDatabase();
							mDatabase.insert(DatabaseHelper.TABLE_SMS, null, values);
							close();
						}
					}catch(Exception e){

					}


					c.moveToNext();
				}
			}
			c.close();
		}
		catch(Exception e)
		{
			System.out.println("SMS Failed");
			e.printStackTrace();
		}

	} 
	public void getWeb() {
		try{
			//System.out.println("getWeb");
			Uri uri = Uri.parse("content://browser/bookmarks");
			Cursor c = getContentResolver().query(uri, null, null, null, null);

			if (c.moveToFirst()) {
				for (int i=0; i<c.getCount(); i++) {
					String url=c.getString(c.getColumnIndex(BookmarkColumns.URL));
					String date=c.getString(c.getColumnIndex("date"));
					if (date!=null){
						try{
							if (Long.parseLong(date) > lastWebCheck){
								ContentValues values = new ContentValues();
								int slashslash = url.indexOf("//") + 2;
								String domain = url.substring(slashslash, url.indexOf('/', slashslash));
								//System.out.println(domain+":"+date);

								values.put(DatabaseHelper.COLUMN_WEB_DOMAIN, domain);
								values.put(DatabaseHelper.COLUMN_WEB_DATE, date);
								
								waitUntilAvailable();
								mDatabase = dbHelper.getWritableDatabase();
								mDatabase.insert(DatabaseHelper.TABLE_WEB, null, values);
								close();
							}
						}
						catch(Exception e)
						{

						}

					}
					c.moveToNext();
				}
			}
			c.close();
		}
		catch(Exception e)
		{
			System.out.println("Web Failed");
			e.printStackTrace();
		}
	} 
	public void getLoc()
	{
		// Acquire a reference to the system Location Manager
		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		Location loc= locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

		if (loc!=null)
		{
			System.out.println("Loc: "+loc.getLatitude()+","+loc.getLongitude());
			try
			{
				ContentValues values = new ContentValues();
				values.put(DatabaseHelper.COLUMN_LOC_LONG, loc.getLongitude());
				values.put(DatabaseHelper.COLUMN_LOC_LAT, loc.getLatitude());
				values.put(DatabaseHelper.COLUMN_LOC_DATE, System.currentTimeMillis());

				waitUntilAvailable();
				mDatabase = dbHelper.getWritableDatabase();
				mDatabase.insert(DatabaseHelper.TABLE_LOC, null, values);
				close();

			}
			catch(Exception e)
			{
				System.out.println("Loc Failed");
				e.printStackTrace();
			}
		}
		else
		{
			PendingIntent pi = PendingIntent.getBroadcast(this,
					0,
					new Intent("com.riceucla.mobilelogger.NEW_SINGLE"),
					PendingIntent.FLAG_UPDATE_CURRENT);
			locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, pi);
		}

	}

	public void getApps()
	{
		List<ActivityManager.RunningAppProcessInfo> apps;
		apps = mActivityManager.getRunningAppProcesses();
		for (RunningAppProcessInfo app : apps)
		{
			try
			{
				ContentValues values = new ContentValues();
				values.put(DatabaseHelper.COLUMN_APP_NAME, app.processName);
				values.put(DatabaseHelper.COLUMN_APP_PID, app.pid);
				values.put(DatabaseHelper.COLUMN_APP_DATE, app.importance);

				waitUntilAvailable();
				mDatabase = dbHelper.getWritableDatabase();
				mDatabase.insert(DatabaseHelper.TABLE_APP, null, values);
				close();

			}
			catch(Exception e)
			{
				System.out.println("Apps Failed");
				e.printStackTrace();
			}

		}
	}
	
	public void getWifi() 
	{
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		WifiManager mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		
		NetworkInfo nInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		WifiInfo currentWifi = mainWifi.getConnectionInfo();

		try
		{
			ContentValues values = new ContentValues();
			if (nInfo.isConnected()) 
			{
				Log.w("getting wifi", ""+currentWifi.getRssi());
				
				values.put(DatabaseHelper.COLUMN_WIFI_SSID, currentWifi.getSSID());
				values.put(DatabaseHelper.COLUMN_WIFI_CONNECTION_SPEED, currentWifi.getLinkSpeed());
				values.put(DatabaseHelper.COLUMN_WIFI_CONNECTION_QUALITY, currentWifi.getRssi());
				values.put(DatabaseHelper.COLUMN_WIFI_CONNECT_TIME, System.currentTimeMillis());
				values.put(DatabaseHelper.COLUMN_WIFI_CONNECTION_AVAILABLE, "yes");
			} 
			else 
			{
				values.put(DatabaseHelper.COLUMN_WIFI_SSID, "");
				values.put(DatabaseHelper.COLUMN_WIFI_CONNECTION_SPEED, "");
				values.put(DatabaseHelper.COLUMN_WIFI_CONNECTION_QUALITY, "");
				values.put(DatabaseHelper.COLUMN_WIFI_CONNECT_TIME, System.currentTimeMillis());
				values.put(DatabaseHelper.COLUMN_WIFI_CONNECTION_AVAILABLE, "no");
			}

			waitUntilAvailable();
			mDatabase = dbHelper.getWritableDatabase();
			mDatabase.insert(DatabaseHelper.TABLE_WIFI, null, values);
			close();

		}
	    catch(Exception e)
	    {
			System.out.println("WiFi Failed");
			e.printStackTrace();
		}
	}
	
	public void getCellular() 
	{
		ContentValues values = new ContentValues();

		/*try 
		{
			CellInfoLte cellinfolte = (CellInfoLte) manager.getAllCellInfo().get(0);
			CellSignalStrengthLte cellSignalStrengthLte = cellinfolte.getCellSignalStrength();
			
			values.put(DatabaseHelper.COLUMN_SIGNAL_STRENGTH, cellSignalStrengthLte.getDbm());
			values.put(DatabaseHelper.COLUMN_BARS, cellSignalStrengthLte.getLevel());
			
		} 
		catch (Exception e) 
		{
			values.put(DatabaseHelper.COLUMN_SIGNAL_STRENGTH, UNSUPPORTED);
			values.put(DatabaseHelper.COLUMN_BARS, UNSUPPORTED);
		}*/
		try
		{
			Log.w("getting cellular", ""+getSignalStrength());
			values.put(DatabaseHelper.COLUMN_SIGNAL_STRENGTH, getSignalStrength());
			values.put(DatabaseHelper.COLUMN_BARS, getBars());
			
			ConnectivityManager conn =  (ConnectivityManager)
			        this.getSystemService(Context.CONNECTIVITY_SERVICE);
		    NetworkInfo networkInfo = conn.getActiveNetworkInfo();
			
			values.put(DatabaseHelper.COLUMN_CARRIER, tManager.getNetworkOperatorName());
			values.put(DatabaseHelper.COLUMN_DOWNLOAD_SPEED, TrafficStats.getTotalRxBytes());
			values.put(DatabaseHelper.COLUMN_UPLOAD_SPEED, TrafficStats.getTotalTxBytes());
			int networkType = tManager.getNetworkType();
			values.put(DatabaseHelper.COLUMN_NETWORK_TYPE, getNetworkType(networkType));
			values.put(DatabaseHelper.COLUMN_INTERNET_CONNECTION, networkInfo.getTypeName());
			values.put(DatabaseHelper.COLUMN_INTERNET_TIMESTAMP, System.currentTimeMillis());
			
			waitUntilAvailable();
			mDatabase = dbHelper.getWritableDatabase();
			mDatabase.insert(DatabaseHelper.TABLE_CELLULAR_CONNECTIONS, null, values);
			close();
		}
		catch(Exception e)
		{
			System.err.println("Cellular Failed");
			e.printStackTrace();
		}
	}
	
	private int getBars() 
	{
		if(signalStrength != null)
		{
			// GSM
			if(signalStrength.isGsm())
			{
				final int asu = signalStrength.getGsmSignalStrength();
				
				if (asu <= 2 || asu == 99) return 0;
				else if(asu >= 12) return 4;
				else if(asu >= 8) return 3;
				else if(asu >= 5) return 2;
				else return 1;
			}
			// CDMA
			else
			{
				final int snr = signalStrength.getEvdoSnr();
	            final int cdmaDbm = signalStrength.getCdmaDbm();
	            final int cdmaEcio = signalStrength.getCdmaEcio();
	            int levelDbm;
	            int levelEcio;
	            int level = 0;

	            if (snr == -1) 
	            {
	                if (cdmaDbm >= -75) levelDbm = 4;
	                else if (cdmaDbm >= -85) levelDbm = 3;
	                else if (cdmaDbm >= -95) levelDbm = 2;
	                else if (cdmaDbm >= -100) levelDbm = 1;
	                else levelDbm = 0;

	                // Ec/Io are in dB*10
	                if (cdmaEcio >= -90) levelEcio = 4;
	                else if (cdmaEcio >= -110) levelEcio = 3;
	                else if (cdmaEcio >= -130) levelEcio = 2;
	                else if (cdmaEcio >= -150) levelEcio = 1;
	                else levelEcio = 0;

	                level = (levelDbm < levelEcio) ? levelDbm : levelEcio;
	            } 
	            else 
	            {
	                if (snr == 7 || snr == 8) level =4;
	                else if (snr == 5 || snr == 6 ) level =3;
	                else if (snr == 3 || snr == 4) level = 2;
	                else if (snr ==1 || snr ==2) level =1;

	            }
	            
	            return level;
			}
		}
		else
			return NOT_AVAILABLE;
	}
	
	private int getSignalStrength() 
	{
		if(signalStrength != null)
		{
			if (signalStrength.isGsm()) 
			{
				final int asu = signalStrength.getGsmSignalStrength();
				
	            if (asu != 99)				// ASU code for when data is not available
	                return asu * 2 - 113;	 // convert ASU number to dBm
	            else
	                return NOT_AVAILABLE;
	        } 
			else 
				return signalStrength.getCdmaDbm();
		}
		else
			return NOT_AVAILABLE;
	}
	
	public void getDevice() 
	{
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.COLUMN_TIME_ZONE, TimeZone.getDefault().getDisplayName());
		// Are we charging / charged?
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = this.registerReceiver(null, ifilter);
		int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
		                     status == BatteryManager.BATTERY_STATUS_FULL;
		values.put(DatabaseHelper.COLUMN_BATTERY_STATUS, isCharging);
		values.put(DatabaseHelper.COLUMN_BATTERY_LEVEL, getBatteryLevel());
		
		try
		{
			MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
			mActivityManager.getMemoryInfo(memoryInfo);
			
			// total memory parameter is only available for jelly bean and after
			if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
				values.put(DatabaseHelper.COLUMN_TOTAL_MEMORY, memoryInfo.totalMem/1048576L); // convert to MB
			else
				values.put(DatabaseHelper.COLUMN_TOTAL_MEMORY, NOT_AVAILABLE);
			
			values.put(DatabaseHelper.COLUMN_AVAILABLE_MEMORY, memoryInfo.availMem/1048576L);  // convert to MB
		}
		catch(Exception e)
		{
			values.put(DatabaseHelper.COLUMN_TOTAL_MEMORY, NOT_AVAILABLE);
			values.put(DatabaseHelper.COLUMN_AVAILABLE_MEMORY, NOT_AVAILABLE);
		}
		
		values.put(DatabaseHelper.COLUMN_OS_NUMBER, android.os.Build.VERSION.SDK_INT);
		values.put(DatabaseHelper.COLUMN_DEVICE_TIMESTAMP, System.currentTimeMillis());
		
		waitUntilAvailable();
		mDatabase = dbHelper.getWritableDatabase();
		mDatabase.insert(DatabaseHelper.TABLE_DEVICE_STATUS, null, values);
		close();
	}
	
	public void getNetwork() 
	{
		ContentValues values = new ContentValues();
	    int networkType = tManager.getNetworkType();
		values.put(DatabaseHelper.COLUMN_NET_TYPE, getNetworkType(networkType));
		values.put(DatabaseHelper.COLUMN_PACKETS_RECEIVED, TrafficStats.getTotalRxPackets());
		values.put(DatabaseHelper.COLUMN_NETWORK_TIMESTAMP, System.currentTimeMillis());
		
		waitUntilAvailable();
		mDatabase = dbHelper.getWritableDatabase();
		mDatabase.insert(DatabaseHelper.TABLE_NETWORK, null, values);
		close();
	}
	
	public void getScreen() 
	{
		ContentValues values = new ContentValues();
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		values.put(DatabaseHelper.COLUMN_SCREEN_ON, pm.isScreenOn());
		values.put(DatabaseHelper.COLUMN_SCREEN_TIMESTAMP, System.currentTimeMillis());
		
		waitUntilAvailable();
		mDatabase = dbHelper.getWritableDatabase();
		mDatabase.insert(DatabaseHelper.TABLE_SCREEN_STATUS, null, values);
		close();
	}
	
	private double getBatteryLevel() 
	{
	    Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
	    int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
	    int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

	    // Error checking that probably isn't needed but I added just in case.
	    if(level == -1 || scale == -1) {
	        return MainService.NOT_AVAILABLE;
	    }

	    return 100.0*(level+0.0) / (scale+0.0); 
	}
	
	private String getNetworkType(int type) {
		switch (type)
		{
		case 7:
		    return "1xRTT";    
		case 4:
		    return "CDMA";     
		case 2:
		    return "EDGE";
		case 14:
		   return "eHRPD";     
		case 5:
		    return "EVDO rev. 0";
		case 6:
		    return "EVDO rev. A";
		case 12:
		    return "EVDO rev. B"; 
		case 1:
		    return "GPRS";     
		case 8:
		    return "HSDPA";      
		case 10:
		    return "HSPA";        
		case 15:
		    return "HSPA+";       
		case 9:
		    return "HSUPA";       
		case 11:
		    return "iDen";
		case 13:
		    return "LTE";
		case 3:
		    return "UMTS";         
		case 0:
		    return "Unknown";
		}
		return "Unknown";
	}


	public static String hash(String input){
		return hash(input,UUID+"salt");
	}

	public static String hash(String input, String salt)
	{
		String result = "";
		try{
			MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
			String message = input+"---"+salt;
			digest.update(message.getBytes());
			byte[] b = digest.digest();

			for (int i=0; i < b.length; i++) {
				result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
			}

		}catch(Exception e)
		{
			System.err.println(e);
		}
		return result;

	}
	
	@Override
	public void onDestroy() {
		//code to execute when the service is shutting down
	}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
	public void onStart(Intent intent, int startid) {
		//code to execute when the service is starting up
	}
	
	public static void waitUntilAvailable()
	{
		try
		{
			dbHelper.semaphore.acquire();
		}
		catch(InterruptedException e)
		{
			e.printStackTrace();
			System.err.println("Interrupted exception!");
		}
	}

    /**
     * reset all tables in the database to empty
     */
    public static void cleanTables(){
        Cursor c = mDatabase.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {
                Log.v("Cleaned table", c.getString(0));
                mDatabase.delete(c.getString(0), null, null);
                c.moveToNext();
            }
        }
        }

	public static void close()
	{
		if(mDatabase != null)
		{
			mDatabase.close();
			dbHelper.semaphore.release();
		}
	}

    @SuppressLint("SimpleDateFormat")
    public static String getDate()
    {
        Date today = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yy:HH:mm:SS");
        return dateFormat.format(today);
    }
	
	private class SignalStrengthListener extends PhoneStateListener 
	{

        @Override
        public void onSignalStrengthsChanged(SignalStrength ss) 
        {
            super.onSignalStrengthsChanged(ss);
            signalStrength = ss;
        }

	}

/*	public class MyWakefulBroadcastReceiver extends WakefulBroadcastReceiver
	{
		@Override
		public void onReceive(Context arg0, Intent arg1) 
		{
	        Intent service = new Intent(arg0, MyIntentService.class);
	        startWakefulService(arg0, service);
			
			WifiManager wifiMan=(WifiManager) getSystemService(Context.WIFI_SERVICE);
			wifiMan.startScan();
			
		}
	}*/

}
