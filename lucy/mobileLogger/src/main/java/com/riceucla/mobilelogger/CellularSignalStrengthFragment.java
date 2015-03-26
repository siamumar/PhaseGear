package com.riceucla.mobilelogger;
 
public class CellularSignalStrengthFragment extends ChartFragment {
	
	public CellularSignalStrengthFragment()
	{
		this.yLabel = "Signal Strength";
		this.yUnits = "dBm";
		this.dbTable = DatabaseHelper.TABLE_CELLULAR_CONNECTIONS;
		this.interval = MainService.cellularINTERVAL;
		this.layout = R.layout.cellular_signal_strength_layout;
		this.layoutId = R.id.cellular_signal_strength_layout;
		this.spinnerId = R.id.spinSSChartTimeScale;
		this.avgLabelId = R.id.txtSSAverage;
		
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
		return DatabaseAdapter.fetchCellularSignalStrengthRecords(before, now);
	}

	@Override
	public void setLimitsIfNeeded() {
		
	}
	
 
}
