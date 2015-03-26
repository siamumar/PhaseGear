package com.riceucla.mobilelogger;

import java.util.ArrayList;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import android.support.v4.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Html;
// import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.support.v4.app.Fragment;
 
public class CellularConnectivityMapFragment extends Fragment
{
	private static final int ZERO_BAR_COLOR = Color.RED;
	private static final int ONE_BAR_COLOR = 0xffdd6100;  // orange
	private static final int TWO_BAR_COLOR = Color.YELLOW;
	private static final int THREE_BAR_COLOR = 0xff80ff00; // light green
	private static final int FOUR_BAR_COLOR = 0xff008000; // dark green
	private static final int NO_DATA_COLOR = Color.BLACK;
	
	private static final String TIMESCALE = "Timescale: ";
	
	private static int lineWidth = 5;
	private static float zoom = 12.0f;
	private static int locationSamplesPerSegment = 40;  // change depending on sampling intervals
														// setting too low results in performance issues
	
	String htmlLegend="<font color=\"red\">Red: </font>0 bars<br>"+
					  "<font color=\"#dd6100\">Orange: </font>1 bar<br>"+
					  "<font color=\"yellow\">Yellow: </font>2 bars<br>"+
					  "<font color=\"#80ff00\">L.green: </font>3 bars<br>"+
					  "<font color=\"#008000\">D.green: </font>4 bars<br>"+
					  "<font color=\"black\">Black: </font>No data<br>";
	
	private static View view;
	private static GoogleMap mMap;
	private static Polyline trackLine = null;
	private static Double currentLatitude;
	private static Double currentLongitude;
	private static ArrayList<LatLng> trackPointsList;
	
	private static long[] timescaleOptionsValue = {10*MainService.min, 30*MainService.min,
											1*MainService.hour, 6*MainService.hour};
	private static String[] timescaleOptionsName = {"10 minutes", "30 minutes",
											"1 hour", "6 hours"};
	
	private static int timescaleIndex = 0;
	
	private long now;
	private long before;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.cellular_connectivity_map_layout, container, false);
        
        // read location data from database and assign to a track points list array for display
        now = System.currentTimeMillis();
        before = now - getTimeInterval();
        
		trackPointsList = DatabaseAdapter.fetchTrackPointRecords(before, now, true);

        if (trackPointsList.size() > 0) 
        {  // take the last location as your current location
        	currentLatitude = trackPointsList.get(trackPointsList.size()-1).latitude;
        	currentLongitude =  trackPointsList.get(trackPointsList.size()-1).longitude;
        }
            
		setUpMap();

        return view;
    }
    
    public long getTimeInterval()
    {
    	return timescaleOptionsValue[timescaleIndex];
    }
    
    @Override
    public void onStart() 
    {
    	super.onStart();
		
		// Spinner of time scale selection
		Spinner spnTimeScale = (Spinner) getActivity().findViewById(R.id.spinCCMTimeScale);
		
		String[] options = new String[timescaleOptionsName.length];
		for(int i=0; i<options.length; i++)
			options[i] = TIMESCALE + timescaleOptionsName[i];
		
		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>
			( getActivity().getApplicationContext(), R.layout.spinner_item, options); 
		
		spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_item);
		spnTimeScale.setAdapter(spinnerArrayAdapter);
		
		spnTimeScale.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) 
			{
				timescaleIndex = position;
				
		        now = System.currentTimeMillis();
		        before = now - getTimeInterval();
		        
				trackPointsList = DatabaseAdapter.fetchTrackPointRecords(before, now, true);
				
				displayTrack();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) 
			{
				displayTrack();
			}
			
		});
		
		TextView legend = (TextView) getActivity().findViewById(R.id.txtCCMLegend);
		legend.setText(Html.fromHtml(htmlLegend));
    }
    
    
	/**** The mapfragment's id must be removed from the FragmentManager
	 **** or else if the same it is passed on the next time then 
	 **** app will crash ****/
    @Override
	public void onPause() 
	{
		super.onPause();
		if (mMap != null) 
		{
			FragmentTransaction ft = MainActivity.fragmentManager.beginTransaction();
			ft.remove(MainActivity.fragmentManager.findFragmentById(R.id.connectivity_map));
			ft.commit();
			mMap = null;
		}
	}

    @Override
    public void onResume() 
	{
		super.onResume();
		
        // read location data from database and assign to a track points list array for display
        now = System.currentTimeMillis();
        before = now - getTimeInterval();
        
		trackPointsList = DatabaseAdapter.fetchTrackPointRecords(before, now, true);
		
		if (mMap != null)
			displayTrack();
	}
	

	public void setUpMap() 
	{
		// Do a null check to confirm that we have not already instantiated the map.
		if (mMap == null) 
		{
			// Try to obtain the map from the SupportMapFragment.
			mMap = ((SupportMapFragment) MainActivity.fragmentManager
					.findFragmentById(R.id.connectivity_map)).getMap();
			
			mMap.setOnMapClickListener( new OnMapClickListener() 
			{
				@Override
				public void onMapClick(LatLng arg0) 
				{
					if (mMap != null)
						displayTrack();
				}
			});

			mMap.setOnMapLongClickListener(new OnMapLongClickListener()
			{
				@Override
				public void onMapLongClick(LatLng mylatLng) 
				{
					// For dropping a marker at a point on the Map
					mMap.addMarker(new MarkerOptions().position(mylatLng).
							title("Your Marker").snippet("You marked this."));
				}

			});

			mMap.setOnMyLocationChangeListener( new OnMyLocationChangeListener() 
			{
				@Override
				public void onMyLocationChange(Location arg0) 
				{
					if (mMap != null)
						displayTrack();
				}
			}); 

			mMap.setMyLocationEnabled(true);
			
			if (trackPointsList.size() > 0) 
				mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLatitude,
					currentLongitude), zoom));
			else
			{
				LocationManager locationManager = (LocationManager) getActivity().
						getSystemService(Context.LOCATION_SERVICE);
				Location loc= locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(loc.getLatitude(),
						loc.getLongitude()), zoom));
			}
			
			if (mMap != null)
				displayTrack();
		}
	}

	public void displayTrack()
	{
		if ((trackPointsList.size() > 1) && (mMap != null)) 
		{		
			//erase the old track line first
			if (trackLine != null) trackLine.remove();
			
			// each segment has a single color. compute the number of segments.
			int noSegments = trackPointsList.size() / locationSamplesPerSegment;
			
			if(trackPointsList.size() % locationSamplesPerSegment != 0)
				noSegments++;
			
			for(int i=0; i<noSegments; i++)
			{
				PolylineOptions segment = new PolylineOptions().width(lineWidth).color(getColor(i));
				for(int j=0; j<=locationSamplesPerSegment; j++)
				{
					int index = Math.min(i*locationSamplesPerSegment+j, trackPointsList.size()-1);
					segment.add(trackPointsList.get(index));
				}
				trackLine = mMap.addPolyline(segment);  // draw the track
			}

		}
	}
	
	// return a color based on the signal strength at the location given by the index
	public int getColor(int index)
	{
		// get the timestamps for location data

		long[] timestamps = DatabaseAdapter.fetchTimestampRecords(DatabaseHelper.TABLE_LOC, before, now);
		
		// get the signal strength records between the interval corresponding to (index-1) and index
		int first = index*locationSamplesPerSegment;
		int last = Math.min((index+1)*locationSamplesPerSegment-1, timestamps.length-1);
		
		String[] bars = DatabaseAdapter.fetchCellularBarsRecords(timestamps[first], timestamps[last]);
		
		// take the average if there are multiple sample points in the interval
		int avgBars=0;
		if(bars.length>0)
		{
			for(String s: bars)
				avgBars += Integer.parseInt(s.substring(0,1));
			
			avgBars = (int) Math.round( (avgBars+0.0)/bars.length );
		}
		else		
			avgBars = MainService.NOT_AVAILABLE;		// no data! (sample signal strength more often to avoid this)
		
		
		return mapBarsToColor(avgBars);
	}

	public static int mapBarsToColor(int bars)
	{
		switch(bars)
		{
		case 0: return ZERO_BAR_COLOR;
		case 1: return ONE_BAR_COLOR;
		case 2: return TWO_BAR_COLOR;
		case 3: return THREE_BAR_COLOR;
		case 4: return FOUR_BAR_COLOR;
		case MainService.NOT_AVAILABLE: return NO_DATA_COLOR;
		default: return NO_DATA_COLOR;
		}
	}
	
}
