package com.riceucla.mobilelogger;

public class DeviceEnergyConsumptionFragment extends ChartFragment 
{
	public DeviceEnergyConsumptionFragment()
	{
		this.yLabel = "Battery Level";
		this.yUnits = "%";
		this.dbTable = DatabaseHelper.TABLE_DEVICE_STATUS;
		this.interval = MainService.deviceINTERVAL;
		this.layout = R.layout.device_energy_consumption_layout;
		this.layoutId = R.id.device_energy_consumption_layout;
		this.spinnerId = R.id.spinECChartTimeScale;
		this.avgLabelId = R.id.txtECAverage;

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
		return DatabaseAdapter.fetchBatteryLevelRecords(before, now);
	}

	@Override
	public void setLimitsIfNeeded() 
	{
		if(chart != null)
			chart.setAbsoluteLimits(0, 100);
	}
}

