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

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
// import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.support.v4.app.Fragment;
 
public class LocationTrajectoryFragment extends Fragment 
{
	private static final String TIMESCALE = "Timescale: ";
	private static int lineColor = Color.BLUE;
	private static int lineWidth = 5;
	private static float zoom = 12.0f;
	
	private static View view;
	private static GoogleMap mMap;
	private static Polyline trackLine = null;
	private static Double currentLatitude;
	private static Double currentLongitude;
	private static ArrayList<LatLng> trackPointsList;
	DatabaseAdapter databaseAdapter;
	
	private long now;
	private long before;
	
	private long[] timescaleOptionsValue = {10*MainService.min, 30*MainService.min,
											1*MainService.hour, 6*MainService.hour};
	private String[] timescaleOptionsName = {"10 minutes", "30 minutes",
											"1 hour", "6 hours"};
	
	private int timescaleIndex = 0;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.location_trajectory_layout, container, false);
        
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
    public void onStart() {
    	super.onStart();
		
		// Spinner of time scale selection
		Spinner spnTimeScale = (Spinner) getActivity().findViewById(R.id.spinLTTimeScale);
		
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
    }
    
    public int getNumberOfSamples(int timescaleIndex)
    {
    	return (int)Math.ceil((timescaleOptionsValue[timescaleIndex]+0.0)/MainService.locINTERVAL);
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
			MainActivity.fragmentManager.beginTransaction()
			.remove(MainActivity.fragmentManager.findFragmentById(R.id.trajectory_map)).commit();

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
					.findFragmentById(R.id.trajectory_map)).getMap();
			
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

	public static void displayTrack()
	{
		if ((trackPointsList.size() > 1) && (mMap != null)) 
		{
			// set the PolyLine the defined color and width
			PolylineOptions rectLine = new PolylineOptions().width(lineWidth).color(
					lineColor);
			
			//erase the old track line first
			if (trackLine != null) trackLine.remove();
			
			// read the points collected in the trackPointsList into the PolyLine
			for (int i=0; i<trackPointsList.size(); i++) {
				rectLine.add(trackPointsList.get(i));
			}
			trackLine = mMap.addPolyline(rectLine);  // draw the track
		}
	}
	
}
