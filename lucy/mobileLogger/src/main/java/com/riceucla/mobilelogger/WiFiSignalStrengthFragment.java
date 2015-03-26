package com.riceucla.mobilelogger;
 
public class WiFiSignalStrengthFragment extends ChartFragment {
	
	public WiFiSignalStrengthFragment()
	{
		this.yLabel = "Signal Strength";
		this.yUnits = "dBm";
		this.dbTable = DatabaseHelper.TABLE_WIFI;
		this.interval = MainService.wifiINTERVAL;
		this.layout = R.layout.wifi_signal_strength_layout;
		this.layoutId = R.id.wifi_signal_strength_layout;
		this.spinnerId = R.id.spinWSSChartTimeScale;
		this.avgLabelId = R.id.txtWSSAverage;
		
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
		return DatabaseAdapter.fetchWiFiSignalStrengthRecords(before, now);
	}

	@Override
	public void setLimitsIfNeeded() {
		
	}
	
 
}
