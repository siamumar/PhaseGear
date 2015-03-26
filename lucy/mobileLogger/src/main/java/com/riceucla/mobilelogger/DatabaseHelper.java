package com.riceucla.mobilelogger;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/*import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;*/
import java.util.HashMap;
import java.util.concurrent.Semaphore;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String COLUMN_ID = "_id";

    private static final String DATABASE_NAME = "LiveLab.db";
    private static final int DATABASE_VERSION = 1;

    public static HashMap<String, String[]> tables = new HashMap<String, String[]>();

    // used to restrict simultaneous access to the database from the service and the activity
    public final Semaphore semaphore = new Semaphore(1, true);

    public static final String TABLE_CALLS = "calls";
    public static final String COLUMN_CALL_NUMBER = "number";
    public static final String COLUMN_CALL_TIMESTAMP = "timestamp";
    public static final String COLUMN_CALL_TYPE = "type";
    public static final String COLUMN_CALL_DURATION = "duration";
    private static final String CALL_CREATE = "create table if not exists "
            + TABLE_CALLS + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_CALL_NUMBER
            + " text not null, " + COLUMN_CALL_DURATION
            + " text not null, " + COLUMN_CALL_TYPE
            + " text not null, " + COLUMN_CALL_TIMESTAMP
            + " text not null)";
    String[] callsCols = {COLUMN_ID, COLUMN_CALL_NUMBER, COLUMN_CALL_DURATION, COLUMN_CALL_TYPE, COLUMN_CALL_TIMESTAMP};

    public static final String TABLE_SMS = "sms";
    public static final String COLUMN_SMS_NUMBER = "number";
    public static final String COLUMN_SMS_TIMESTAMP = "timestamp";
    public static final String COLUMN_SMS_LENGTH = "duration";
    public static final String COLUMN_SMS_TYPE = "type";
    public static final String SMS_CREATE = "create table if not exists "
            + TABLE_SMS + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_SMS_NUMBER
            + " text not null, " + COLUMN_SMS_LENGTH
            + " text not null, " + COLUMN_SMS_TYPE
            + " text not null, " + COLUMN_SMS_TIMESTAMP
            + " text not null)";
    String[] smsCols = {COLUMN_ID, COLUMN_SMS_NUMBER, COLUMN_SMS_LENGTH, COLUMN_SMS_TYPE, COLUMN_SMS_TIMESTAMP};


    public static final String TABLE_WEB = "web";
    public static final String COLUMN_WEB_DOMAIN = "domain";
    public static final String COLUMN_WEB_DATE = "timestamp";
    public static final String WEB_CREATE = "create table if not exists "
            + TABLE_WEB + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_WEB_DOMAIN
            + " text not null, " + COLUMN_WEB_DATE
            + " text not null)";
    String[] webCols = {COLUMN_ID, COLUMN_WEB_DOMAIN, COLUMN_WEB_DATE};


    public static final String TABLE_LOC = "loc";
    public static final String COLUMN_LOC_LONG = "longitude";
    public static final String COLUMN_LOC_LAT = "latitude";
    public static final String COLUMN_LOC_DATE = "timestamp";
    public static final String LOC_CREATE = "create table if not exists "
            + TABLE_LOC + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_LOC_LONG
            + " text not null, " + COLUMN_LOC_LAT
            + " text not null, " + COLUMN_LOC_DATE
            + " text not null)";
    String[] locCols = {COLUMN_ID, COLUMN_LOC_LONG, COLUMN_LOC_LAT, COLUMN_LOC_DATE};


    public static final String TABLE_APP = "app";
    public static final String COLUMN_APP_NAME = "name";
    public static final String COLUMN_APP_PID = "pid";
    public static final String COLUMN_APP_DATE = "timestamp";
    public static final String APP_CREATE = "create table if not exists "
            + TABLE_APP + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_APP_NAME
            + " text not null, " + COLUMN_APP_PID
            + " text not null, " + COLUMN_APP_DATE
            + " text not null)";
    String[] appCols = {COLUMN_ID, COLUMN_APP_NAME, COLUMN_APP_PID, COLUMN_APP_DATE};


    public static final String TABLE_WIFI = "wifi";
    public static final String COLUMN_WIFI_CONNECT_TIME = "timestamp";
    public static final String COLUMN_WIFI_SSID = "ssid";
    public static final String COLUMN_WIFI_CONNECTION_SPEED = "speed";
    public static final String COLUMN_WIFI_CONNECTION_QUALITY = "quality";
    public static final String COLUMN_WIFI_CONNECTION_AVAILABLE = "available";
    public static final String WIFI_CREATE = "create table if not exists "
            + TABLE_WIFI + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_WIFI_CONNECT_TIME
            + " text not null, " + COLUMN_WIFI_SSID
            + " text not null, " + COLUMN_WIFI_CONNECTION_SPEED
            + " text not null, " + COLUMN_WIFI_CONNECTION_QUALITY
            + " text not null, " + COLUMN_WIFI_CONNECTION_AVAILABLE
            + " text not null)";
    String[] wifiCols = {COLUMN_ID, COLUMN_WIFI_CONNECT_TIME, COLUMN_WIFI_SSID, COLUMN_WIFI_CONNECTION_SPEED,
            COLUMN_WIFI_CONNECTION_QUALITY, COLUMN_WIFI_CONNECTION_AVAILABLE};


    // === New tables ===

    public static final String TABLE_CELLULAR_CONNECTIONS = "cellular";
    public static final String COLUMN_CARRIER = "carrier";
    public static final String COLUMN_SIGNAL_STRENGTH = "signalStrength";
    public static final String COLUMN_BARS = "bars";
    public static final String COLUMN_DOWNLOAD_SPEED = "downloadSpeed";
    public static final String COLUMN_UPLOAD_SPEED = "uploadSpeed";
    public static final String COLUMN_NETWORK_TYPE = "networkType";
    public static final String COLUMN_INTERNET_CONNECTION = "internetConnection";
    public static final String COLUMN_INTERNET_TIMESTAMP = "timestamp";
    public static final String CELLULAR_CONNECTIONS_CREATE = "create table if not exists "
            + TABLE_CELLULAR_CONNECTIONS + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_CARRIER
            + " text not null, " + COLUMN_SIGNAL_STRENGTH
            + " text not null, " + COLUMN_BARS
            + " text not null, " + COLUMN_DOWNLOAD_SPEED
            + " text not null, " + COLUMN_UPLOAD_SPEED
            + " text not null, " + COLUMN_NETWORK_TYPE
            + " text not null, " + COLUMN_INTERNET_CONNECTION
            + " text not null, " + COLUMN_INTERNET_TIMESTAMP
            + " text not null)";
    String[] cellularCols = {COLUMN_ID, COLUMN_CARRIER, COLUMN_SIGNAL_STRENGTH, COLUMN_BARS, COLUMN_DOWNLOAD_SPEED,
            COLUMN_UPLOAD_SPEED, COLUMN_NETWORK_TYPE, COLUMN_INTERNET_CONNECTION, COLUMN_INTERNET_TIMESTAMP};


    public static final String TABLE_DEVICE_STATUS = "device";
    public static final String COLUMN_TIME_ZONE = "timeZone";
    public static final String COLUMN_BATTERY_STATUS = "batteryStatus";
    public static final String COLUMN_BATTERY_LEVEL = "batteryLevel";
    public static final String COLUMN_TOTAL_MEMORY = "totalMemory";
    public static final String COLUMN_AVAILABLE_MEMORY = "availableMemory";
    public static final String COLUMN_OS_NUMBER = "osNumber";
    public static final String COLUMN_DEVICE_TIMESTAMP = "timestamp";
    public static final String DEVICE_STATUS_CREATE = "create table if not exists "
            + TABLE_DEVICE_STATUS + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_TIME_ZONE
            + " text not null, " + COLUMN_BATTERY_STATUS
            + " text not null, " + COLUMN_BATTERY_LEVEL
            + " text not null, " + COLUMN_TOTAL_MEMORY
            + " text not null, " + COLUMN_AVAILABLE_MEMORY
            + " text not null, " + COLUMN_OS_NUMBER
            + " text not null, " + COLUMN_DEVICE_TIMESTAMP
            + " text not null)";
    String[] deviceCols = {COLUMN_ID, COLUMN_TIME_ZONE, COLUMN_BATTERY_STATUS, COLUMN_BATTERY_LEVEL, COLUMN_TOTAL_MEMORY,
            COLUMN_AVAILABLE_MEMORY, COLUMN_OS_NUMBER, COLUMN_DEVICE_TIMESTAMP};


    public static final String TABLE_NETWORK = "network";
    public static final String COLUMN_NET_TYPE = "networkType";
    public static final String COLUMN_PACKETS_RECEIVED = "packetsReceived";
    public static final String COLUMN_NETWORK_TIMESTAMP = "timestamp";
    public static final String NETWORK_CREATE = "create table if not exists "
            + TABLE_NETWORK + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_NET_TYPE
            + " text not null, " + COLUMN_PACKETS_RECEIVED
            + " text not null, " + COLUMN_NETWORK_TIMESTAMP
            + " text not null)";
    String[] networkCols = {COLUMN_ID, COLUMN_NET_TYPE, COLUMN_PACKETS_RECEIVED, COLUMN_NETWORK_TIMESTAMP};


    public static final String TABLE_SCREEN_STATUS = "screen";
    public static final String COLUMN_SCREEN_ON = "screenStatus";
    public static final String COLUMN_SCREEN_TIMESTAMP = "timestamp";
    public static final String SCREEN_CREATE = "create table if not exists "
            + TABLE_SCREEN_STATUS + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_SCREEN_ON
            + " text not null, " + COLUMN_SCREEN_TIMESTAMP
            + " text not null)";
    String[] screenCols = {COLUMN_ID, COLUMN_SCREEN_ON, COLUMN_SCREEN_TIMESTAMP};

    public static final String TABLE_STEPS = "steps";
    public static final String COLUMN_TOTAL_STEPS = "totalStep";
    public static final String COLUMN_STEPS_TIMESTEMP = "timestamp";
    public static final String STEP_CREATE = "create table if not exists "
            + TABLE_STEPS + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_TOTAL_STEPS
            + " text not null, " + COLUMN_STEPS_TIMESTEMP
            + " text not null)";
    String[] stepsCols = {COLUMN_ID, COLUMN_TOTAL_STEPS, COLUMN_STEPS_TIMESTEMP};

    public static final String TABLE_ACCELEROMETER = "accelerometer";
    public static final String COLUMN_ACCELERATION_X = "accelerationX";
    public static final String COLUMN_ACCELERATION_Y = "accelerationY";
    public static final String COLUMN_ACCELERATION_Z = "accelerationZ";
    public static final String COLUMN_ACCELEROMETER_TIMESTEMP = "timestamp";
    public static final String ACCELEROMETER_CREATE = "create table if not exists "
            + TABLE_ACCELEROMETER + "(" + COLUMN_ID
            + " integer primary key autoincrement, "
            + COLUMN_ACCELERATION_X + " text not null, "
            + COLUMN_ACCELERATION_Y + " text not null, "
            + COLUMN_ACCELERATION_Z + " text not null, "
            + COLUMN_ACCELEROMETER_TIMESTEMP + " text not null)";
    String[] accelerometerCols = {COLUMN_ID, COLUMN_ACCELERATION_X, COLUMN_ACCELERATION_Y, COLUMN_ACCELERATION_Z, COLUMN_ACCELEROMETER_TIMESTEMP};

    public DatabaseHelper(Context context, String uuid, String date)
    {
        super(context, uuid +"_"+DATABASE_NAME, null, DATABASE_VERSION);
        create(this.getWritableDatabase());

        tables.put(TABLE_CALLS, callsCols);
        tables.put(TABLE_SMS, smsCols);
        tables.put(TABLE_WEB, webCols);
        tables.put(TABLE_LOC, locCols);
        tables.put(TABLE_APP, appCols);
        tables.put(TABLE_WIFI, wifiCols);
        tables.put(TABLE_CELLULAR_CONNECTIONS, cellularCols);
        tables.put(TABLE_NETWORK, networkCols);
        tables.put(TABLE_DEVICE_STATUS, deviceCols);
        tables.put(TABLE_SCREEN_STATUS, screenCols);
        tables.put(TABLE_STEPS, stepsCols);
        tables.put(TABLE_ACCELEROMETER, accelerometerCols);
    }
	
	
/*	
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		
		tables.put(TABLE_CALLS, callsCols);
		tables.put(TABLE_SMS, smsCols);
		tables.put(TABLE_WEB, webCols);
		tables.put(TABLE_LOC, locCols);
		tables.put(TABLE_APP, appCols);
		tables.put(TABLE_WIFI, wifiCols);
		tables.put(TABLE_CELLULAR_CONNECTIONS, cellularCols);
		tables.put(TABLE_NETWORK, networkCols);
		tables.put(TABLE_DEVICE_STATUS, deviceCols);
		tables.put(TABLE_SCREEN_STATUS, screenCols);
		
		// uncomment the following in the first run ONLY IF you are uploading a readily available database
		// to a device
		try
		{
	    	//Open your local db as the input stream
	    	InputStream myInput = context.getAssets().open(DATABASE_NAME);
	 
	    	// Path to the just created empty db
	    	String outFileName = "/data/data/edu.ucla.hssrp/databases/" + DATABASE_NAME;
	 
	    	//Open the empty db as the output stream
	    	OutputStream myOutput = new FileOutputStream(outFileName);
	 
	    	//transfer bytes from the inputfile to the outputfile
	    	byte[] buffer = new byte[1024];
	    	int length;
	    	while ((length = myInput.read(buffer))>0){
	    		myOutput.write(buffer, 0, length);
	    	}
	 
	    	//Close the streams
	    	myOutput.flush();
	    	myOutput.close();
	    	myInput.close();
		}
		catch(IOException o)
		{
			throw new Error("Error copying database");
		}
	}*/

    @Override
    public void onCreate(SQLiteDatabase database)
    {
        create(database);
    }

    public static void create(SQLiteDatabase database)
    {
        System.out.println("SQL CREATE");
        try {
            database.execSQL(CALL_CREATE);
        } catch (Exception e) {
        }
        try {
            database.execSQL(SMS_CREATE);
        } catch (Exception e) {
        }
        try {
            database.execSQL(WEB_CREATE);
        } catch (Exception e) {
        }
        try {
            database.execSQL(LOC_CREATE);
        } catch (Exception e) {
        }
        try {
            database.execSQL(APP_CREATE);
        } catch (Exception e) {
        }
        try {
            database.execSQL(WIFI_CREATE);
        } catch (Exception e) {
        }
        try {
            database.execSQL(CELLULAR_CONNECTIONS_CREATE);
        } catch (Exception e) {
        }
        try {
            database.execSQL(DEVICE_STATUS_CREATE);
        } catch (Exception e) {
        }
        try {
            database.execSQL(NETWORK_CREATE);
        } catch (Exception e) {
        }
        try {
            database.execSQL(SCREEN_CREATE);
        } catch (Exception e) {
        }
        try {
            database.execSQL(STEP_CREATE);
        } catch (Exception e) {
        }
        try {
            database.execSQL(ACCELEROMETER_CREATE);
        } catch (Exception e) {
        }
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        Log.w(DatabaseHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CALLS);
        onCreate(db);
    }

} 