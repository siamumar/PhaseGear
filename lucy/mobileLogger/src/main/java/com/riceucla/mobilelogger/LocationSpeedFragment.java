package com.riceucla.mobilelogger;
 
public class LocationSpeedFragment extends ChartFragment {
	
	public LocationSpeedFragment()
	{
		this.yLabel = "Speed";
		this.yUnits = "m/s";
		this.dbTable = DatabaseHelper.TABLE_LOC;
		this.interval = MainService.locINTERVAL;
		this.layout = R.layout.location_speed_layout;
		this.layoutId = R.id.location_speed_layout;
		this.spinnerId = R.id.spinLSChartTimeScale;
		this.avgLabelId = R.id.txtLSAverage;
		
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
		return DatabaseAdapter.fetchSpeedRecords(before, now);
	}

	@Override
	public void setLimitsIfNeeded() 
	{
		if(chart != null)
			chart.setAbsoluteLimits(0, Double.MAX_VALUE);
	}
	
 
}
