package com.riceucla.mobilelogger;
 
public class WiFiConnectionSpeedFragment extends ChartFragment {
	
	public WiFiConnectionSpeedFragment()
	{
		this.yLabel = "Connection Speed";
		this.yUnits = "Mbps";
		this.dbTable = DatabaseHelper.TABLE_WIFI;
		this.interval = MainService.wifiINTERVAL;
		this.layout = R.layout.wifi_connection_speed_layout;
		this.layoutId = R.id.wifi_connection_speed_layout;
		this.spinnerId = R.id.spinWCSChartTimeScale;
		this.avgLabelId = R.id.txtWCSAverage;
		
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
		return DatabaseAdapter.fetchWiFiConnectionSpeedRecords(before, now);
	}

	@Override
	public void setLimitsIfNeeded() 
	{
		if(chart != null)
			chart.setAbsoluteLimits(0, Double.MAX_VALUE);
	}
	
 
}
