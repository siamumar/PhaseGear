package com.riceucla.mobilelogger;

public class WiFiSSIDFragment extends HistogramFragment
{
	public WiFiSSIDFragment()
	{
		this.yLabel = "Fraction of time";
		this.xLabel = "SSID";
		this.yUnits = "%";
		this.dbTable = DatabaseHelper.TABLE_WIFI;
		this.interval = MainService.wifiINTERVAL;
		this.layout = R.layout.wifi_ssid_layout;
		this.layoutId = R.id.wifi_ssid_layout;
		this.spinnerId = R.id.spinWSSIDTimeScale;
		this.maxNoBars = 12;
		this.order = true;
		this.avgLabelId = R.id.txtWSSIDAverage;
		
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
	public String[] fetchRecords(long timeInterval) 
	{
		long now = System.currentTimeMillis();
		long before = now - timeInterval;
    	String[] records = DatabaseAdapter.fetchWiFiSSIDRecords(before, now);
    	
    	return records;
	}

}
