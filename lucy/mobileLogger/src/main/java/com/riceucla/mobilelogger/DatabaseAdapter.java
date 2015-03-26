package com.riceucla.mobilelogger;

import java.util.ArrayList;

import com.google.android.gms.maps.model.LatLng;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseAdapter 
{
	private static final double CONSECUTIVE_LATLNG_DISTANCE = 111111.0;
	private static final String BARS = " bars";
	private static SQLiteDatabase mydatabase;
	
	
	// returns the last noRows timestamps of table
	public static long[] fetchTimestampRecords(String table, long t1, long t2)
	{
		waitUntilAvailable();
		mydatabase = MainActivity.dbHelper.getWritableDatabase();
		Cursor mCursor = mydatabase.query(table, new String[] {"timestamp"}, 
				"timestamp BETWEEN "+t1+" AND "+t2, null, null, null, null);
		
		long[] records = new long[0];
		
		if (mCursor !=null) 
		{
			records = new long[mCursor.getCount()];
		    mCursor.moveToFirst();
		    
		    for (int i = 0; i<mCursor.getCount() ; i++) 
		    {            
		        records[i] = mCursor.getLong(mCursor.getColumnIndex("timestamp"));
		        mCursor.moveToNext();
		    }	
		    mCursor.close();
		}
		close();
		
		return records;
	}

	public static ArrayList<LatLng> fetchTrackPointRecords(long t1, long t2, boolean smoothened) 
	{
		waitUntilAvailable();
		mydatabase = MainActivity.dbHelper.getWritableDatabase();
		Cursor mCursor = mydatabase.query(DatabaseHelper.TABLE_LOC, new String[] {"longitude", "latitude"},
				"timestamp BETWEEN "+t1+" AND "+t2, null, null, null, null);
	    
		ArrayList<LatLng> records = new ArrayList<LatLng>();
		
		if (mCursor !=null) 
		{
			mCursor.moveToFirst();
			for (int i = 0; i < mCursor.getCount(); i++) 
			{            
				Double longitude =  mCursor.getDouble(mCursor.getColumnIndex("longitude"));
				Double latitude =  mCursor.getDouble(mCursor.getColumnIndex("latitude"));
				LatLng mLatLng = new LatLng(latitude, longitude);
				records.add(mLatLng);     
				mCursor.moveToNext();
			}
			mCursor.close();
		}
	    close();
	    
	    // ideally should use Kalman filter for smoothening, but for now, just apply a low-pass filter
	    if(smoothened)
	    {
	    	ArrayList<LatLng> filteredRecords = new ArrayList<LatLng>(records.size());
	    	
	    	for(int i=0; i<records.size(); i++)
	    	{
	    		if(i == 0) filteredRecords.add(records.get(i));
	    		else
	    		{
	    			double lat = (records.get(i-1).latitude + records.get(i).latitude)/2.0;
	    			double lng = (records.get(i-1).longitude + records.get(i).longitude)/2.0;
	    			filteredRecords.add(new LatLng(lat, lng));
	    		}
	    	}
	    	return filteredRecords;
	    }
	    
	    return records;

	}

	public static double[] fetchSpeedRecords(long t1, long t2)
	{
		long[] timestamps = fetchTimestampRecords(DatabaseHelper.TABLE_LOC, t1, t2);
		ArrayList<LatLng> locs = fetchTrackPointRecords(t1, t2, true);
		double[] speed = new double[locs.size()];
		
		for(int i=1; i<locs.size();i++)
		{
			double lat1 = locs.get(i-1).latitude;
			double lon1 = locs.get(i-1).longitude;
			double lat2 = locs.get(i).latitude;
			double lon2 = locs.get(i).longitude;
			double dlat = lat2 - lat1;
			double dlon = lon2 - lon1;
			
			speed[i] = Math.sqrt(Math.pow(dlat*CONSECUTIVE_LATLNG_DISTANCE, 2) 
					+ Math.pow(dlon*CONSECUTIVE_LATLNG_DISTANCE, 2))
					/(timestamps[i]-timestamps[i-1]+0.0);
		}
		return speed;
	}
	
	public static double[] fetchCellularSignalStrengthRecords(long t1, long t2) 
	{
		waitUntilAvailable();
		mydatabase = MainActivity.dbHelper.getWritableDatabase();
		Cursor mCursor = mydatabase.query(DatabaseHelper.TABLE_CELLULAR_CONNECTIONS, 
				new String[] { "signalStrength"}, "timestamp BETWEEN "+t1+" AND "+t2,
				null, null, null, null);
		
		/*Cursor mCursor = mydatabase.query(DatabaseHelper.TABLE_CELLULAR_CONNECTIONS, 
				new String[] { "signalStrength"}, "(signalStrength <> \'"+MainService.UNSUPPORTED+
				"\' AND "+"signalStrength <> \'"+MainService.UNSUPPORTED2+"\')", null, null, null, 
				DatabaseHelper.COLUMN_ID+" DESC", ""+noRows);*/
		
		double[] records = new double[0];
		
		if (mCursor !=null) 
		{
			records = new double[mCursor.getCount()];

			mCursor.moveToFirst();

			for (int i = 0; i<mCursor.getCount() ; i++) 
			{         
				Double mValue =  mCursor.getDouble(mCursor.getColumnIndex("signalStrength"));
				records[i] = mValue;     
				mCursor.moveToNext();
			}
			mCursor.close();
		}
		close();
	    
	    return records;
	}
	
	public static double[] fetchWiFiSignalStrengthRecords(long t1, long t2) 
	{
		waitUntilAvailable();
		mydatabase = MainActivity.dbHelper.getWritableDatabase();
		Cursor mCursor = mydatabase.query(DatabaseHelper.TABLE_WIFI, 
				new String[] { "available, quality"}, "timestamp BETWEEN "+t1+" AND "+t2, 
				null, null, null, null);
		
		double[] records = new double[0];
		
		if (mCursor !=null) 
		{
			records = new double[mCursor.getCount()];
			
			mCursor.moveToFirst();
	
		    for (int i = 0; i<mCursor.getCount() ; i++) 
		    {            
		    	double quality = mCursor.getDouble(mCursor.getColumnIndex("quality"));
		    	
		    	if(mCursor.getString(mCursor.getColumnIndex("available")).equals("yes") &&
		    			quality > -200.0	&&						// invalid values
		    			quality != -1.0	&&	quality < 256.0 )
		    		records[i] = quality;  
		    	else
		    		records[i] = MainService.NOT_AVAILABLE;
		        mCursor.moveToNext();
		    }
		    mCursor.close();
		}
	    close();
	    return records;
	}
	
	public static double[] fetchWiFiConnectionSpeedRecords(long t1, long t2) 
	{
		waitUntilAvailable();
		mydatabase = MainActivity.dbHelper.getWritableDatabase();
		Cursor mCursor = mydatabase.query(DatabaseHelper.TABLE_WIFI, 
				new String[] { "available, speed"}, "timestamp BETWEEN "+t1+" AND "+t2,
				null, null, null, null);
		
		double[] records = new double[0];
		
		if (mCursor !=null) 
		{
			records = new double[mCursor.getCount()];
			
			mCursor.moveToFirst();
	
		    for (int i = 0; i<mCursor.getCount() ; i++) 
		    {           
		    	if(mCursor.getString(mCursor.getColumnIndex("available")).equals("yes")  &&
		    	mCursor.getDouble(mCursor.getColumnIndex("speed")) != -1.0	)
		    		records[i] =  mCursor.getDouble(mCursor.getColumnIndex("speed"));
		    	else
		    		records[i] = MainService.NOT_AVAILABLE;
		    	
		    	mCursor.moveToNext();
		    }
		    mCursor.close();
		}
	    close();
	    return records;
	}	
	
	public static double[] fetchBatteryLevelRecords(long t1, long t2) 
	{
		waitUntilAvailable();
		mydatabase = MainActivity.dbHelper.getWritableDatabase();
		Cursor mCursor = mydatabase.query(DatabaseHelper.TABLE_DEVICE_STATUS, 
				new String[] { "batteryLevel"}, "timestamp BETWEEN "+t1+" AND "+t2,
				null, null, null, null);
		
		double[] records = new double[0];
		
		if (mCursor !=null) 
		{
			records = new double[mCursor.getCount()];

			mCursor.moveToFirst();

			for (int i = 0; i<mCursor.getCount() ; i++) 
			{            
				records[i] = mCursor.getDouble(mCursor.getColumnIndex("batteryLevel"));     
				mCursor.moveToNext();
			}
			mCursor.close();
		}
	    close();
	    
	    return records;
	}
	
	public static double[] fetchAvailableMemoryRecords(long t1, long t2) 
	{	
		waitUntilAvailable();
		mydatabase = MainActivity.dbHelper.getWritableDatabase();
		Cursor mCursor = mydatabase.query(DatabaseHelper.TABLE_DEVICE_STATUS, 
				new String[] { "availableMemory"}, "timestamp BETWEEN "+t1+" AND "+t2,
				null, null, null, null);
		
		double[] records = new double[0];
		
		if(mCursor != null)
		{
			records = new double[mCursor.getCount()];
			mCursor.moveToFirst();
	
			for (int i = 0; i<mCursor.getCount() ; i++) 
			{            
				records[i] = mCursor.getDouble(mCursor.getColumnIndex("availableMemory"));    
				mCursor.moveToNext();
			}
			mCursor.close();
		}
		close();
		return records;
	}
	
	public static double[] fetchTotalMemoryRecords(long t1, long t2) 
	{	
		waitUntilAvailable();
		mydatabase = MainActivity.dbHelper.getWritableDatabase();
		Cursor mCursor = mydatabase.query(DatabaseHelper.TABLE_DEVICE_STATUS, 
				new String[] { "totalMemory"}, "timestamp BETWEEN "+t1+" AND "+t2,
				null, null, null, null);
		
		double[] records = new double[0];
		
		if(mCursor != null)
		{
			records = new double[mCursor.getCount()];
			mCursor.moveToFirst();
	
			for (int i = 0; i<mCursor.getCount() ; i++) 
			{            
				records[i] = mCursor.getDouble(mCursor.getColumnIndex("totalMemory"));    
				mCursor.moveToNext();
			}
			mCursor.close();
		}
		close();
		return records;
	}
	
	public static boolean hasTotalMemoryRecords()
	{
		waitUntilAvailable();
		mydatabase = MainActivity.dbHelper.getWritableDatabase();
		Cursor mCursor = mydatabase.query(DatabaseHelper.TABLE_DEVICE_STATUS, 
				new String[] { "totalMemory"}, null, null, null, null, "1");
		
		if(mCursor != null)
		{
			mCursor.moveToLast();
			boolean has = mCursor.getInt(mCursor.getColumnIndex("totalMemory")) != MainService.NOT_AVAILABLE;
			
			close();
			
			return has;
		}
		else
		{
			close();
			return false;
		}
	}

	public static double[] fetchDataUploadRecords(long t1, long t2) 
	{
		waitUntilAvailable();
		mydatabase = MainActivity.dbHelper.getWritableDatabase();
		Cursor mCursor = mydatabase.query(DatabaseHelper.TABLE_CELLULAR_CONNECTIONS, 
				new String[] { "uploadSpeed"}, "timestamp BETWEEN "+t1+" AND "+t2, 
				null, null, null, null);

		double records[] = new double[0];

		if(mCursor != null)
		{
			records = new double[mCursor.getCount()];
			mCursor.moveToFirst();

			double offset = 0;
			for (int i = 0; i<mCursor.getCount() ; i++) 
			{            
				records[i] =  mCursor.getLong(mCursor.getColumnIndex("uploadSpeed")); 

				if(i == 0)
					offset = records[i];

				records[i] -= offset;
				records[i] /= (1024*1024);		// convert to MB
				
				mCursor.moveToNext();
			}
			mCursor.close();
		}
		close();
		return records;
	}
	
	public static double[] fetchDataDownloadRecords(long t1, long t2) 
	{
		waitUntilAvailable();
		mydatabase = MainActivity.dbHelper.getWritableDatabase();
		Cursor mCursor = mydatabase.query(DatabaseHelper.TABLE_CELLULAR_CONNECTIONS, 
				new String[] { "downloadSpeed"}, "timestamp BETWEEN "+t1+" AND "+t2, 
				null, null, null, null);

		double records[] = new double[0];

		if(mCursor != null)
		{
			records = new double[mCursor.getCount()];
			mCursor.moveToFirst();

			double offset = 0;
			for (int i = 0; i<mCursor.getCount() ; i++) 
			{            
				records[i] =  mCursor.getLong(mCursor.getColumnIndex("downloadSpeed")); 

				if(i == 0)
					offset = records[i];

				records[i] -= offset;
				records[i] /= (1024*1024);		// convert to MB
				
				mCursor.moveToNext();
			}
			mCursor.close();
		}
		close();
		return records;
	}

	public static double fetchScreenUnlockRecords(long t1, long t2) 
	{
		waitUntilAvailable();
		mydatabase = MainActivity.dbHelper.getWritableDatabase();
		Cursor mCursor = mydatabase.query(DatabaseHelper.TABLE_SCREEN_STATUS, 
				new String[] { "screenStatus"}, "timestamp BETWEEN "+t1+" AND "+t2, 
				null, null, null, null);

		double total = 0;
		int count = 0;
		
		if(mCursor != null)
		{
			total = mCursor.getCount();
			mCursor.moveToFirst();

			for (int i = 0; i<mCursor.getCount() ; i++) 
			{            
				if(mCursor.getString(mCursor.getColumnIndex("screenStatus")).equals("1")) count++;    		
				mCursor.moveToNext();
			}
			mCursor.close();
		}
	    close();
	    return (total != 0 ? (count+0.0)*100.0/total : MainService.NOT_AVAILABLE ); 
	    
	}

	public static String[] fetchNetworkTypeRecords(long t1, long t2) 
	{
		waitUntilAvailable();
		mydatabase = MainActivity.dbHelper.getWritableDatabase();
		Cursor mCursor = mydatabase.query(DatabaseHelper.TABLE_CELLULAR_CONNECTIONS, 
				new String[] { "networkType"}, "timestamp BETWEEN "+t1+" AND "+t2, 
				null, null, null, null);

		String records[] = new String[0];
		if(mCursor != null)
		{
			records = new String[mCursor.getCount()];
			mCursor.moveToFirst();

			for (int i = 0; i<mCursor.getCount() ; i++) 
			{            
				records[i] =  mCursor.getString(mCursor.getColumnIndex("networkType"));
				mCursor.moveToNext();
			}

			mCursor.close();
		}
		close();

		return records;
		
	}
	
	public static String[] fetchCellularBarsRecords(long t1, long t2) 
	{
		waitUntilAvailable();
		mydatabase = MainActivity.dbHelper.getWritableDatabase();
		
		Cursor mCursor = mydatabase.query(DatabaseHelper.TABLE_CELLULAR_CONNECTIONS, 
				new String[] { "bars"}, 
				"(timestamp BETWEEN "+t1+" AND "+t2+") AND bars <> "+MainService.NOT_AVAILABLE, 
				null, null, null, null);
		
		/*Cursor mCursor = mydatabase.query(DatabaseHelper.TABLE_CELLULAR_CONNECTIONS, 
				new String[] { "bars"}, "(bars <> \'"+MainService.UNSUPPORTED+
				"\' AND "+"bars <> \'"+MainService.UNSUPPORTED2+"\')", null, null, null,
				DatabaseHelper.COLUMN_ID+" DESC", ""+noRows);*/
		
		String[] records = new String[0];
		
		if(mCursor != null)
		{
			records = new String[mCursor.getCount()];
			mCursor.moveToFirst();

			for (int i = 0; i<mCursor.getCount() ; i++) 
			{            
				records[i] = mCursor.getString(mCursor.getColumnIndex("bars")) + BARS;
				mCursor.moveToNext();
			}
			mCursor.close();
		}
		close();
	    
	    return records;
		
	}

	public static String[] fetchWiFiSSIDRecords(long t1, long t2) 
	{
		waitUntilAvailable();
		mydatabase = MainActivity.dbHelper.getWritableDatabase();
		Cursor mCursor = mydatabase.query(DatabaseHelper.TABLE_WIFI, 
				new String[] { "ssid"}, "(timestamp BETWEEN "+t1+" AND "+t2+") AND available =\'yes\'" +
						"AND ssid <> ''", 
				null, null, null, null);

		String records[] = new String[0];

		if(mCursor != null)
		{
			records = new String[mCursor.getCount()];
			mCursor.moveToFirst();

			for (int i = 0; i<mCursor.getCount() ; i++) 
			{            
				records[i] =  mCursor.getString(mCursor.getColumnIndex("ssid"));
				mCursor.moveToNext();
			}

			mCursor.close();
		}
		close();

	    return records;

	}
	// Database import/export static functions


	// import database from SDCard
	/*	@SuppressWarnings("resource")
	public static boolean importDatabaseFromSD(String dirSDCardpath){
		try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();
            

            if (sd.canWrite()) {
                String  dstDBPath= "//data//" + MainActivity.myPackageName
                        + "//databases//" + DatabaseHelper.DATABASE_NAME;
                String srcDBPath  = dirSDCardpath + DatabaseHelper.DATABASE_NAME;
                File dstDB = new File(data, dstDBPath);
                File srcDB = new File(sd, srcDBPath);

                FileChannel src = new FileInputStream(srcDB).getChannel();
                FileChannel dst = new FileOutputStream(dstDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                return true;
                // Toast.makeText(getBaseContext(), backupDB.toString(), Toast.LENGTH_LONG).show();

            }
        } catch (Exception e) {

        	return false;
            // Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();

        }
		return true;
	}*/
	
	// export database to SDCard
/*	@SuppressWarnings("resource")
	public static boolean exportDatabaseToSD(String dirSDCardpath){
		try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String  currentDBPath= "//data//" + MainActivity.myPackageName
                        + "//databases//" + DatabaseHelper.DATABASE_NAME;
                String backupDBPath  = dirSDCardpath + DatabaseHelper.DATABASE_NAME;
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                return true;
                // Toast.makeText(getBaseContext(), backupDB.toString(), Toast.LENGTH_LONG).show();

            }
        } catch (Exception e) {

        	return false;
            // Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();

        }
		return true;
	}*/

	//  added these 2 methods for external SD card read/write errors checking, true = good
/*	public static boolean isExternalStoragePresent() {

        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            // Something else is wrong. It may be one of many other states, but
            // all we need
            // to know is we can neither read nor write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }
         if (!((mExternalStorageAvailable) && (mExternalStorageWriteable))) {
            Toast.makeText(this, "SD card not present", Toast.LENGTH_LONG)
                    .show();

        } 
        return (mExternalStorageAvailable) && (mExternalStorageWriteable);
    }

	public static String createDirIfNotExists(String path) { // syntax e.g. path = "/hssrp/data"

	    File folder = new File(Environment.getExternalStorageDirectory(), path);
	    if (!folder.exists()) {  // if directory does not exist, try to create it
	        if (!folder.mkdirs()) { // if it cannot create the directory, return null.
	            // Log.e("HSSRP:: ", "Problem creating Image folder");
	            return null;
	        }
	    }
	    //If directory exist or successfully created, then return it as a string value
	    String extStorageDirectory = folder.toString();
	    return extStorageDirectory;  // return the directory path string
	}*/

	public static void waitUntilAvailable()
	{
		try
		{
			MainActivity.dbHelper.semaphore.acquire();
		}
		catch(InterruptedException e)
		{
			e.printStackTrace();
			System.err.println("Interrupted exception!");
		}
	}

	public static void close()
	{
		if(mydatabase != null)
		{
			mydatabase.close();
			MainActivity.dbHelper.semaphore.release();
		}
	}
}
