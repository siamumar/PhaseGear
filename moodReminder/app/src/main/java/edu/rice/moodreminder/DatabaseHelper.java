package edu.rice.moodreminder;

import android.content.Context; 
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.telephony.TelephonyManager;
import android.util.Log;

/*import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;*/
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Semaphore;

public class DatabaseHelper extends SQLiteOpenHelper {

	public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static String createTable;
	
	private static final String DATABASE_NAME = "LiveLab.db";
	private static final int DATABASE_VERSION = 1;

	public static HashMap<String, String[]> tables = new HashMap<String, String[]>();
	
	// used to restrict simultaneous access to the database from the service and the activity
	public final Semaphore semaphore = new Semaphore(1, true);

	public DatabaseHelper(Context context, String date) 
	{
        super(context, MainActivity.UUID + "_"+DATABASE_NAME, null, DATABASE_VERSION);

		// Populate SQL table columns
        String[] columns = new String[Config.parameters.length + 2];
        columns[0] = COLUMN_ID;
        columns[1] = COLUMN_TIMESTAMP;
        for (int i = 0; i < Config.parameters.length; i++)
            columns[i + 2] = Config.parameters[i];

        // Assemble SQL table creation command
        createTable = "create table if not exists " + Config.TABLE_NAME + "(" + COLUMN_ID + " integer primary key autoincrement, " + COLUMN_TIMESTAMP + " text not null, ";
        for (int i = 0; i < Config.parameters.length; i++)
            if (i != Config.parameters.length - 1)
                createTable += Config.parameters[i] + " text not null, ";
            else
                createTable += Config.parameters[i] + " text not null)";

        onUpgrade(this.getWritableDatabase(), 0, 1);
        create(this.getWritableDatabase());
        tables.put(Config.TABLE_NAME, columns);
	}

	@Override
	public void onCreate(SQLiteDatabase database) 
	{
		create(database);
	}
	
	public static void create(SQLiteDatabase database)
	{
		try {
            database.execSQL(createTable);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	{
		Log.w(DatabaseHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + Config.TABLE_NAME);
		onCreate(db);
	}

} 