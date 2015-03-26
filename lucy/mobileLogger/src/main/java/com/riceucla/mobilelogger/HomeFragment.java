package com.riceucla.mobilelogger;

import java.text.DecimalFormat;
import java.util.ArrayList;

import android.support.v4.app.ListFragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
 
public class HomeFragment extends ListFragment 
{
	public static final String LATITUDE = "Latitude";
	public static final String LONGITUDE = "Longitude";
	public static final int TEXT_COLOR = Color.WHITE;
	
	//for case structure ref
	public static final String IMAGE_RESOURCE_ID = "iconResourceID";
	public static final String ITEM_NAME = "itemName";
	private static final String WIFI_SIGNAL_STRENGTH = "WiFi Signal Strength";
	private static final String WIFI_SIGNAL_STRENGTH_UNITS = " dBm";
	private static final String WIFI_CONNECTION_SPEED = "WiFi Connection Speed";
	private static final String WIFI_CONNECTION_SPEED_UNITS = " Mbps";
	private static final String WIFI_SSID_UNITS = "";
	private static final String WIFI_SSID = "WiFi SSID";
	private static final String CELLULAR_SIGNAL_STRENGTH = "Cellular Signal Strength";
	private static final String CELLULAR_SIGNAL_STRENGTH_UNITS = " dBm";
	private static final String CELLULAR_NETWORK_TYPE = "Cellular Network Type";
	private static final String LOCATION_LATITUDE_UNITS = "";
	private static final String LOCATION_LONGITUDE = "Longitude";
	private static final String LOCATION_SPEED = "Speed";
	private static final String LOCATION_LATITUDE = "Latitude";
	private static final String CELLULAR_NETWORK_TYPE_UNITS = "";
	private static final String LOCATION_LONGITUDE_UNITS = "";
	private static final String LOCATION_SPEED_UNITS = " m/s";
	// private static final String DEVICE_MEMORY_USAGE = "Memory Usage";
	// private static final String DEVICE_MEMORY_USAGE_UNITS = " MB";
	private static final String DEVICE_BATTERY_LEVEL = "Battery Level";
	private static final String DEVICE_BATTERY_LEVEL_UNITS = "%";
	private static final String NO_DATA = "No Data";
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) 
    {
        View rootView = inflater.inflate(R.layout.home_layout, container, false);
        
        HomeItemsAdapter mAdapter = new HomeItemsAdapter(getActivity(), R.layout.list_item, getItems());
        setListAdapter(mAdapter);
        return rootView;
    }
    
    public void onResume()
    {
    	super.onResume();
    
        HomeItemsAdapter mAdapter = new HomeItemsAdapter(getActivity(), R.layout.list_item, getItems());
        setListAdapter(mAdapter);
    }
    
    
    public ArrayList<HomeItem> getItems()
    {
        ArrayList<HomeItem> items = new ArrayList<HomeItem>();
        
        DecimalFormat df1 = new DecimalFormat("#.#");
        DecimalFormat df4 = new DecimalFormat("#.0000");
        
        long now = System.currentTimeMillis();
        
        if(MainActivity.showWiFiSignalStrength)
        {
        	long before = now - MainService.memoryFactor*Math.max(MainService.wifiINTERVAL, MainService.INTERVAL);
        	
        	double[] strength = DatabaseAdapter.fetchWiFiSignalStrengthRecords(before, now);
        	int size = strength.length;
        	
        	if(size == 0 || strength[size-1] == (MainService.NOT_AVAILABLE +0.0) )
        		items.add(new HomeItem(WIFI_SIGNAL_STRENGTH, NO_DATA, ""));
        	else
        		items.add(new HomeItem(WIFI_SIGNAL_STRENGTH, strength[0]+"", WIFI_SIGNAL_STRENGTH_UNITS));
        }
        if(MainActivity.showWiFiConnectionSpeed)
        {
        	long before = now - MainService.memoryFactor*Math.max(MainService.wifiINTERVAL, MainService.INTERVAL);
        	
        	double[] speed = DatabaseAdapter.fetchWiFiConnectionSpeedRecords(before, now);
        	int size = speed.length;
        	
        	if(size == 0 || speed[size-1] == (MainService.NOT_AVAILABLE +0.0) )
        		items.add(new HomeItem(WIFI_CONNECTION_SPEED, NO_DATA, ""));
        	else
        		items.add(new HomeItem(WIFI_CONNECTION_SPEED, speed[0]+"", WIFI_CONNECTION_SPEED_UNITS));
        }
        if(MainActivity.showWiFiSSID)
        {
        	long before = now - MainService.memoryFactor*Math.max(MainService.wifiINTERVAL, MainService.INTERVAL);
        	
        	String[] type = DatabaseAdapter.fetchWiFiSSIDRecords(before, now);
        	if(type.length > 0)
        		items.add(new HomeItem(WIFI_SSID, type[type.length-1], WIFI_SSID_UNITS));
        	else
        		items.add(new HomeItem(WIFI_SSID, NO_DATA, ""));
        }
        if(MainActivity.showCellularSignalStrength)
        {
        	long before = now - MainService.memoryFactor*Math.max(MainService.cellularINTERVAL, MainService.INTERVAL);
        	double[] strength = DatabaseAdapter.fetchCellularSignalStrengthRecords(before, now);
        	
        	int size = strength.length;
        	
        	if(size == 0 || strength[size-1] == (double) MainService.NOT_AVAILABLE)
        		items.add(new HomeItem(CELLULAR_SIGNAL_STRENGTH, NO_DATA, ""));
        	else
        		items.add(new HomeItem(CELLULAR_SIGNAL_STRENGTH, ""+strength[size-1], 
        				CELLULAR_SIGNAL_STRENGTH_UNITS));
        	
        }
        if(MainActivity.showCellularNetworkType)
        {
        	long before = now - MainService.memoryFactor*Math.max(MainService.cellularINTERVAL, MainService.INTERVAL);
        	
        	String[] type = DatabaseAdapter.fetchNetworkTypeRecords(before, now);
        	if(type.length>0)
        		items.add(new HomeItem(CELLULAR_NETWORK_TYPE, type[type.length-1], CELLULAR_NETWORK_TYPE_UNITS));
        	else
        		items.add(new HomeItem(CELLULAR_NETWORK_TYPE, NO_DATA, ""));
        	
        }
        if(MainActivity.showLocationCoordinates)
        {
        	long before = now - MainService.memoryFactor*Math.max(MainService.locINTERVAL, MainService.INTERVAL);
        	
            ArrayList<LatLng> coordinates = DatabaseAdapter.fetchTrackPointRecords(before, now, false);
            int size = coordinates.size();
            
            if(size > 0)
            {
	            double latitude = coordinates.get(size-1).latitude;
	            double longitude = coordinates.get(size-1).longitude;
	        	items.add(new HomeItem(LOCATION_LATITUDE, df4.format(latitude), LOCATION_LATITUDE_UNITS));
	        	items.add(new HomeItem(LOCATION_LONGITUDE, df4.format(longitude), LOCATION_LONGITUDE_UNITS));
            }
        	else
        	{
        		items.add(new HomeItem(LOCATION_LATITUDE, NO_DATA, ""));
        		items.add(new HomeItem(LOCATION_LONGITUDE, NO_DATA, ""));
        	}
        }
        if(MainActivity.showLocationSpeed)
        {
        	long before = now - MainService.memoryFactor*Math.max(MainService.locINTERVAL, MainService.INTERVAL);
        	
        	double[] speed = DatabaseAdapter.fetchSpeedRecords(before, now);
        	int size = speed.length;
        	if(size>0)
        		items.add(new HomeItem(LOCATION_SPEED, ""+df1.format(speed[size-1]), LOCATION_SPEED_UNITS));
        	else
        		items.add(new HomeItem(LOCATION_SPEED, NO_DATA, ""));
        }
        if(MainActivity.showDeviceBatteryLevel)
        {
        	long before = now - MainService.memoryFactor*Math.max(MainService.deviceINTERVAL, MainService.INTERVAL);
        	
        	double[] battery = DatabaseAdapter.fetchBatteryLevelRecords(before, now);
        	int size = battery.length;
        	
        	if(size == 0 || battery[size-1] == (0.0+MainService.NOT_AVAILABLE) )
        		items.add(new HomeItem(DEVICE_BATTERY_LEVEL, NO_DATA, ""));
        	else
        		items.add(new HomeItem(DEVICE_BATTERY_LEVEL, df1.format(battery[size-1]), DEVICE_BATTERY_LEVEL_UNITS));
        }
        
        return items;
    }
    
    
    
    private class HomeItem
    {
    	String name;
    	String value;
    	String units;
    	public HomeItem(String name, String value, String units)
    	{
    		this.name = name;
    		this.value = value;
    		this.units = units;
    	}
    }
    
    private class HomeItemsAdapter extends ArrayAdapter<HomeItem> {

        private ArrayList<HomeItem> items;

        public HomeItemsAdapter(Context context, int textViewResourceId, ArrayList<HomeItem> items) {
        	super(context, textViewResourceId, items);
        	this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) 
        {
        	View v = convertView;
        	if (v == null) 
        	{
        		LayoutInflater vi = (LayoutInflater) getActivity()
        				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        		v = vi.inflate(R.layout.list_item, null);
        	}
        	HomeItem item = items.get(position);
        	if (item != null) 
        	{
        		TextView tt = (TextView) v.findViewById(R.id.toptext);
        		TextView bt = (TextView) v.findViewById(R.id.bottomtext);

        		tt.setTextColor(TEXT_COLOR);
        		bt.setTextColor(TEXT_COLOR);

        		if (tt != null) 
        			tt.setText(item.name);                            
        		if(bt != null)
        			bt.setText(item.value+item.units);
        	}
        	return v;
        }
    }
    
}
