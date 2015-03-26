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
 
public class LocationCoordinatesFragment extends ListFragment 
{
	public static final String LATITUDE = "Latitude";
	public static final String LONGITUDE = "Longitude";
	public static final String NO_DATA = "No Data";
	public static final int TEXT_COLOR = Color.WHITE;
	
	private DecimalFormat df = new DecimalFormat("#.0000");
	
	private long now;
	private long before;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) 
    {
        View rootView = inflater.inflate(R.layout.location_coordinates_layout, container, false);
        
        now = System.currentTimeMillis();
        before = now - MainService.memoryFactor*Math.max(MainService.locINTERVAL, MainService.INTERVAL);
        
 		ArrayList<LatLng> coordinates = DatabaseAdapter.fetchTrackPointRecords(before, now, false);
 		
 		double latitude, longitude;
 		
 		int size = coordinates.size();
 		
 		if(size > 0)
 		{
	        latitude = coordinates.get(size-1).latitude;
	        longitude = coordinates.get(size-1).longitude;
 		}
 		else
 		{
 			latitude = MainService.NOT_AVAILABLE;
 			longitude = MainService.NOT_AVAILABLE;
 		}
 		
        ArrayList<Coordinate> cdts = new ArrayList<Coordinate>();
        cdts.add(new Coordinate(LATITUDE, latitude));
        cdts.add(new Coordinate(LONGITUDE, longitude));
        
        CoordinatesAdapter mAdapter = new CoordinatesAdapter(getActivity(), R.layout.list_item, cdts);
        setListAdapter(mAdapter);
        
        return rootView;
    }
   
    private class Coordinate
    {
    	String name;
    	Double value;
    	
    	public Coordinate(String name, Double value)
    	{
    		this.name = name;
    		this.value = value;
    	}
    }
    
    private class CoordinatesAdapter extends ArrayAdapter<Coordinate> {

        private ArrayList<Coordinate> coordinates;

        public CoordinatesAdapter(Context context, int textViewResourceId, ArrayList<Coordinate> coordinates) {
        	super(context, textViewResourceId, coordinates);
        	this.coordinates = coordinates;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) 
        {
        	View v = convertView;
        	if (v == null) {
        		LayoutInflater vi = (LayoutInflater) getActivity()
        				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        		v = vi.inflate(R.layout.list_item, null);
        	}
        	Coordinate coordinate = coordinates.get(position);
        	if (coordinate != null) {
        		TextView tt = (TextView) v.findViewById(R.id.toptext);
        		TextView bt = (TextView) v.findViewById(R.id.bottomtext);

        		tt.setTextColor(TEXT_COLOR);
        		bt.setTextColor(TEXT_COLOR);

        		if (tt != null) 
        			tt.setText(coordinate.name);                            
        		if(bt != null)
        		{
        			if(coordinate.value == MainService.NOT_AVAILABLE)
        				bt.setText(NO_DATA);
        			else
        				bt.setText(df.format(coordinate.value));
        			
        		}
        	}
        	return v;
        }
    }

}
