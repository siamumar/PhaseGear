package com.riceucla.mobilelogger;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class Uploader {

	public static String urlServer = Config.UPLOAD_BASE_URL;
    private String UUID = "";

	public static void setServer(String server) 
	{
		urlServer = server;
	}

	public static boolean upload(SQLiteDatabase database, String UUID)
	{
			try {
                //for testing purpose
                final String UPLOAD_BASE_URL = Config.UPLOAD_BASE_URL;

                // First determine which components the user wants to log, organized in a HashMap
                HashMap<String, Boolean> loggedComponents = new HashMap<String, Boolean>();
                loggedComponents.put(DatabaseHelper.TABLE_CALLS, Config.LOG_CALLS);
                loggedComponents.put(DatabaseHelper.TABLE_SMS, Config.LOG_SMS);
                loggedComponents.put(DatabaseHelper.TABLE_WEB, Config.LOG_WEB);
                loggedComponents.put(DatabaseHelper.TABLE_LOC, Config.LOG_LOCATION);
                loggedComponents.put(DatabaseHelper.TABLE_APP, Config.LOG_APP);
                loggedComponents.put(DatabaseHelper.TABLE_WIFI, Config.LOG_WIFI);
                loggedComponents.put(DatabaseHelper.TABLE_CELLULAR_CONNECTIONS, Config.LOG_CELLULAR);
                loggedComponents.put(DatabaseHelper.TABLE_DEVICE_STATUS, Config.LOG_DEVICE);
                loggedComponents.put(DatabaseHelper.TABLE_NETWORK, Config.LOG_NETWORK);
                loggedComponents.put(DatabaseHelper.TABLE_SCREEN_STATUS, Config.LOG_SCREEN_STATUS);
                loggedComponents.put(DatabaseHelper.TABLE_ACCELEROMETER, Config.LOG_ACCELEROMETER);
                loggedComponents.put(DatabaseHelper.TABLE_STEPS, Config.LOG_STEPS);

                for (String table : DatabaseHelper.tables.keySet()) {

                    if (!loggedComponents.get(table))
                        return true;

                    Cursor c = database.query(table, null, null, null, null, null, null);
                    JSONArray json = cur2Json(c);

                    // Create a new HttpClient and Post Header
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(UPLOAD_BASE_URL);

                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                    nameValuePairs.add(new BasicNameValuePair("uuid", UUID));
                    nameValuePairs.add(new BasicNameValuePair("data_type", table));
                    nameValuePairs.add(new BasicNameValuePair("json", json.toString()));
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    // Execute HTTP Post Request
                    HttpResponse response = httpclient.execute(httppost);
                    response.getEntity().writeTo(System.out);
                }

			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("mobilelogger upload exception : " + e.getMessage());
				return false;
			}
			return true;
	}

    /**
     * convert the given database cursor into a JSONArray object
     * @param cursor
     * @return
     */
    public static JSONArray cur2Json(Cursor cursor) {

        JSONArray resultSet = new JSONArray();
        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {
            JSONObject rowObject = new JSONObject();
            for (int i = 0; i < cursor.getColumnCount(); i++) {
                if (cursor.getColumnName(i) != null) {
                    try {
                        rowObject.put(cursor.getColumnName(i),
                                cursor.getString(i));
                    } catch (Exception e) {
                        Log.d("TEST", e.getMessage());
                    }
                }
            }
            resultSet.put(rowObject);
            cursor.moveToNext();
        }

        cursor.close();
        return resultSet;

    }

}
