package com.riceucla.mobilelogger;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
 
public abstract class HistogramFragment extends Fragment {
	
	protected static final String TIMESCALE = "Timescale: ";
	protected static final String NO_DATA = "No Data";
	
	protected View rootView;
	protected Histogram histogram;
	protected TextView tvAverage;
	protected static int timescaleIndex = 0;
	
	// to be set by the constructor of child classes
	protected String yLabel;
	protected String xLabel;
	protected String yUnits;
	protected String dbTable;
	protected long interval;
	protected int layout;
	protected int layoutId;
	protected int spinnerId;
	protected int maxNoBars;
	protected boolean order;
	protected int avgLabelId;
	protected long[] timescaleOptionsValue;
	protected String[] timescaleOptionsName;
	
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	
    	rootView = inflater.inflate(layout, container, false);  	
    	
    	String[] data = fetchRecords(getTimeInterval());
    	
        histogram = new Histogram(this, layoutId, data, maxNoBars, order, xLabel, yLabel, yUnits);
    	
        updateLabels(data);
        
        return rootView;
    }
    
    @Override
	public void onResume() 
    {
        super.onResume();
        
		String[] data = fetchRecords(getTimeInterval());
		histogram.update(data);
		updateLabels(data);
        
        if (histogram.getView() == null) 
        {	
        	histogram.addChartToLayout();  
        	histogram.getView().bringToFront();
        } 
        else 
        {
        	histogram.getView().repaint();
        }
    }
    
    @Override
    public void onStart() 
    {
    	super.onStart();
    	
		// Spinner of time scale selection
		Spinner spnChtTimeScale = (Spinner) getActivity().findViewById(spinnerId);
		
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
				String[] data = fetchRecords(getTimeInterval());
				histogram.update(data);
				updateLabels(data);
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
    
    // to be overriden in child classes whenever we need to display average
    public void updateLabels(String[] data)
    {
    	tvAverage = (TextView) rootView.findViewById(avgLabelId);
    	if(data.length == 0)
            tvAverage.setText(NO_DATA);
    	else
    		tvAverage.setText("");
    }
    
	public abstract String[] fetchRecords(long timeInterval);
	
}
