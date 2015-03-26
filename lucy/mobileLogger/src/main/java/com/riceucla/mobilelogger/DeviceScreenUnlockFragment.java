package com.riceucla.mobilelogger;

import java.text.DecimalFormat;
import java.util.ArrayList;

import android.support.v4.app.ListFragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
 
public class DeviceScreenUnlockFragment extends ListFragment 
{
	public static final String SCREEN_UNLOCKED = "Screen unlocked:";
	public static final String PCT_OF_TIME = "% of the time";
	public static final int TEXT_COLOR = Color.WHITE;
	private static final String TIMESCALE = "Timescale: ";
	public static final String NO_DATA = "No Data";

	private long[] timescaleOptionsValue = {10*MainService.min, 30*MainService.min,
			1*MainService.hour, 6*MainService.hour};
	private String[] timescaleOptionsName = {"10 minutes", "30 minutes",
			"1 hour", "6 hours"};
	
	private int timescaleIndex = 0;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) 
    {
        View rootView = inflater.inflate(R.layout.device_screen_unlock_layout, container, false);
        update();

        return rootView;
    }

	public void onResume()
	{
    	super.onResume();
    	update();
    }
    
    @Override
    public void onStart() {
    	super.onStart();
    	
		// Spinner of time scale selection
		Spinner spinner = (Spinner) getActivity().findViewById(R.id.spinDSUTimeScale);
		
		String[] options = new String[timescaleOptionsName.length];
		for(int i=0; i<options.length; i++)
			options[i] = TIMESCALE + timescaleOptionsName[i];
		
		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>
			( getActivity().getApplicationContext(), R.layout.spinner_item_white, options); 
		
		spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_item_dropdown_white);
		spinner.setAdapter(spinnerArrayAdapter);
		
		spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) 
			{
				timescaleIndex = position;
				update();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) 
			{
			}
			
		});
    }
    
    public long getTimeInterval()
    {
    	return timescaleOptionsValue[timescaleIndex];
    }
    
    public void update()
    {
    	long now = System.currentTimeMillis();
    	long before = now - getTimeInterval();
    	
    	ArrayList<Double> unlockedArray = new ArrayList<Double>();
    	unlockedArray.add(DatabaseAdapter.fetchScreenUnlockRecords(before, now));

    	setListAdapter(new UnlockAdapter(getActivity(), R.layout.list_item, unlockedArray));
    }

    private class UnlockAdapter extends ArrayAdapter<Double> 
    {

        private ArrayList<Double> unlockedArray;
        private DecimalFormat df;

        public UnlockAdapter(Context context, int textViewResourceId, ArrayList<Double> unlockedArray) 
        {
        	super(context, textViewResourceId, unlockedArray);
        	this.unlockedArray = unlockedArray;
        	this.df = new DecimalFormat("#.#");
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
        	Double unlockData = unlockedArray.get(position);
        	if (unlockData != null) 
        	{
        		TextView tt = (TextView) v.findViewById(R.id.toptext);
        		TextView bt = (TextView) v.findViewById(R.id.bottomtext);

        		tt.setTextColor(TEXT_COLOR);
        		bt.setTextColor(TEXT_COLOR);

        		if (tt != null) 
        			tt.setText(SCREEN_UNLOCKED);        			
        		if(bt != null)
        			bt.setText( unlockData == MainService.NOT_AVAILABLE ?
        					NO_DATA : df.format(unlockData)+PCT_OF_TIME);
        		
        	}
        	return v;
        }
    }

}
