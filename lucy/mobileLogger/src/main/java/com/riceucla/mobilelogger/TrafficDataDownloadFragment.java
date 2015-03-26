package com.riceucla.mobilelogger;

import android.widget.TextView;
 
public class TrafficDataDownloadFragment extends ChartFragment {
	
	public TrafficDataDownloadFragment()
	{
		this.yLabel = "Data Download";
		this.yUnits = "MB";
		this.dbTable = DatabaseHelper.TABLE_CELLULAR_CONNECTIONS;
		this.interval = MainService.cellularINTERVAL;
		this.layout = R.layout.traffic_data_download_layout;
		this.layoutId = R.id.traffic_data_download_layout;
		this.spinnerId = R.id.spinDLChartTimeScale;
		this.avgLabelId = R.id.txtDLAverage;
		
		this.timescaleOptionsValue = new long[4];
		timescaleOptionsValue[0] = 10*MainService.min;
		timescaleOptionsValue[1] = 30*MainService.min;
		timescaleOptionsValue[2] = 1*MainService.hour;
		timescaleOptionsValue[3] = 6*MainService.hour;
		
		this.timescaleOptionsName = new String[4];
		timescaleOptionsName[0] = "10 minutes";
		timescaleOptionsName[1] = "30 minutes";
		timescaleOptionsName[2] = "1 hour";
		timescaleOptionsName[3] = "6 hours";
	}
	
	@Override
	public double[] fetchRecords() 
	{
		return DatabaseAdapter.fetchDataDownloadRecords(before, now);
	}

	@Override
	public void setLimitsIfNeeded() 
	{
		if(chart != null)
			chart.setAbsoluteLimits(0, Double.MAX_VALUE);
		
	}
	
	@Override
	public void updateLabels(double avg, double cur)
    {
    	String curString = (cur==(double)(MainService.NOT_AVAILABLE)) ? NO_DATA : (df.format(cur)+" "+yUnits );
    	
        tvAverage = (TextView) rootView.findViewById(avgLabelId);
        tvAverage.setText(CURRENT + " : "+ curString);
    }
	
 
}
