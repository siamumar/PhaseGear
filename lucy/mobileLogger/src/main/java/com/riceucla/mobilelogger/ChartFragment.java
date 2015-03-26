package com.riceucla.mobilelogger;

import java.text.DecimalFormat;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
 
public abstract class ChartFragment extends Fragment {
	
	protected static final String AVERAGE = "Average";
	protected static final String CURRENT = "Current";
	protected static final String TIME_LABEL = "Time";
	protected static final String TIMESCALE = "Timescale: ";
	protected static final String NO_DATA = "No Data";
	
	protected View rootView;
	protected Chart chart;
	protected TextView tvAverage;
	protected Spinner spnChtTimeScale;
	protected DecimalFormat df;
	protected int timescaleIndex = 0;
	
	// to be set by the constructor of child classes
	protected String yLabel;
	protected String yUnits;
	protected String dbTable;
	protected long interval;
	protected int layout;
	protected int layoutId;
	protected int spinnerId;
	protected int avgLabelId;
	protected long[] timescaleOptionsValue;
	protected String[] timescaleOptionsName;
	
	protected long now;
	protected long before; 
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	
    	rootView = inflater.inflate(layout, container, false);  	
    	
    	df = new DecimalFormat("#.##");
    	generateChart();
    	
        return rootView;
    }
    
    @Override
	public void onResume() 
    {
        super.onResume();
   
        updateChart();
        
        if (chart.getView() == null) 
        {	
        	chart.addChartToLayout();  
        	chart.getView().bringToFront();
        } 
        else 
        {
        	chart.getView().repaint();
        }
    }
    
    public void generateChart()
    {
    	now = System.currentTimeMillis();
    	before = now - getTimeInterval();
        long timestamps[] = DatabaseAdapter.fetchTimestampRecords(dbTable, before, now);
        double measurements[] = fetchRecords();

        chart = new Chart(this, layoutId , timestamps, measurements, 
        		TIME_LABEL, yLabel, yUnits, timescaleOptionsName[timescaleIndex]);

        setLimitsIfNeeded();
        
        updateLabels(computeAverage(measurements), getCurrent(timestamps, measurements));
    }
    

	public long getTimeInterval()
    {
    	return timescaleOptionsValue[timescaleIndex];
    }
    
    
    public void updateChart()
    {
    	now = System.currentTimeMillis();
    	before = now - getTimeInterval();
        long timestamps[] = DatabaseAdapter.fetchTimestampRecords(dbTable, before, now);
        double measurements[] = fetchRecords();

    	chart.update(timestamps, measurements, timescaleOptionsName[timescaleIndex]); 
    	updateLabels(computeAverage(measurements), getCurrent(timestamps, measurements));

    }

	public void updateLabels(double avg, double cur)
    {
    	String avgString = (avg==(double)(MainService.NOT_AVAILABLE)) ? NO_DATA : (df.format(avg)+" "+yUnits );
    	String curString = (cur==(double)(MainService.NOT_AVAILABLE)) ? NO_DATA : (df.format(cur)+" "+yUnits );
    	
        tvAverage = (TextView) rootView.findViewById(avgLabelId);
        tvAverage.setText(AVERAGE+": " + avgString +"\n"+
        		CURRENT + " : "+ curString);
    }
    
    @Override
    public void onStart() {
    	super.onStart();
    	
		// Spinner of time scale selection
		spnChtTimeScale = (Spinner) getActivity().findViewById(spinnerId);
		
		String[] options = new String[timescaleOptionsName.length];
		for(int i=0; i<options.length; i++)
			options[i] = TIMESCALE + timescaleOptionsName[i];
		
		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>
			( getActivity().getApplicationContext(), R.layout.spinner_item, options); 
		
		spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_item);
		spnChtTimeScale.setAdapter(spinnerArrayAdapter);
		
		spnChtTimeScale.setOnItemSelectedListener(new Spinner.OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) 
			{
				timescaleIndex = position;
				updateChart();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) 
			{
			}

		});
    }

    // compute average ignoring the invalid data
    public static double computeAverage(double[] input)
    {
    	double sum = 0.0; 
    	int count = 0;
    	for (int i=0; i<input.length; i++) 
    	{  
    		if(input[i] != (double) MainService.NOT_AVAILABLE)
    		{
    			sum = sum + input[i];
    			count++;
    		}
    	}
    	if(count == 0) return (double)MainService.NOT_AVAILABLE;
    	else return sum / (0.0 + count);

    }

    public double getCurrent(long[] timestamps, double[] measurements) 
    {
    	long limit = now - MainService.memoryFactor*Math.max(MainService.INTERVAL, interval);
    	int i=timestamps.length-1;
    	
    	if(i>-1)
    	{
	    	// measurements considered up to date if taken after the limit
	    	while(timestamps[i] >= limit)
	    	{
	    		if(measurements[i] != MainService.NOT_AVAILABLE)
	    			return measurements[i];
	    		
	    		i--;
	    	}
	    	
	    	return MainService.NOT_AVAILABLE;
    	}
    	else
    		return MainService.NOT_AVAILABLE;
	}
    
	public abstract double[] fetchRecords();
	
	public abstract void setLimitsIfNeeded();
}
