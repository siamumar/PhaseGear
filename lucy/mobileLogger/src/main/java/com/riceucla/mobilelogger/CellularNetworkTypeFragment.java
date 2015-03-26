package com.riceucla.mobilelogger;

public class CellularNetworkTypeFragment extends HistogramFragment
{
	public CellularNetworkTypeFragment()
	{
		this.yLabel = "Fraction of time";
		this.xLabel = "Network Type";
		this.yUnits = "%";
		this.dbTable = DatabaseHelper.TABLE_NETWORK;
		this.interval = MainService.networkINTERVAL;
		this.layout = R.layout.cellular_network_type_layout;
		this.layoutId = R.id.cellular_network_type_layout;
		this.spinnerId = R.id.spinCNTTimeScale;
		this.maxNoBars = Integer.MAX_VALUE;
		this.order = true;
		this.avgLabelId = R.id.txtCNTAverage;
		
		this.timescaleOptionsValue = new long[4];
		timescaleOptionsValue[0] = 10*MainService.min;
		timescaleOptionsValue[1] = 30*MainService.min;
		timescaleOptionsValue[2] = 1*MainService.hour;
		timescaleOptionsValue[3] = 6*MainService.hour;
		
		this.timescaleOptionsName = new String[4];
		timescaleOptionsName[0] = "10 minutes";
		timescaleOptionsName[1] = "30 minutes";
		timescaleOptionsName[2] = "1 hour";
		timescaleOptionsName[3] = "6 horus";
	}
	
	@Override
	public String[] fetchRecords(long timeInterval) 
	{
		long now = System.currentTimeMillis();
		long before = now - timeInterval;
    	String[] records = DatabaseAdapter.fetchNetworkTypeRecords(before, now);
    	
    	return records;
	}

}
