package com.riceucla.mobilelogger;

public class DeviceMemoryConsumptionFragment extends ChartFragment 
{
	public DeviceMemoryConsumptionFragment()
	{
		this.yLabel = "Available Memory";
		this.yUnits = "MB";
		this.dbTable = DatabaseHelper.TABLE_DEVICE_STATUS;
		this.interval = MainService.deviceINTERVAL;
		this.layout = R.layout.device_memory_consumption_layout;
		this.layoutId = R.id.device_memory_consumption_layout;
		this.spinnerId = R.id.spinMCChartTimeScale;
		this.avgLabelId = R.id.txtMCAverage;

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
		//if(DatabaseAdapter.hasTotalMemoryRecords())
			//chart.setUpperBound(DatabaseAdapter.fetchTotalMemoryRecords(before, now));
		
		return DatabaseAdapter.fetchAvailableMemoryRecords(before, now);
	}

	@Override
	public void setLimitsIfNeeded() 
	{
		if(chart != null)
			chart.setAbsoluteLimits(0, Double.MAX_VALUE);
	}
}

