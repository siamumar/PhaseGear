package com.riceucla.mobilelogger;

import java.text.DecimalFormat;


public class CellularBarsFragment extends HistogramFragment
{
	private static final String AVERAGE = "Average: ";
	private static final String BARS = " bars";
	private DecimalFormat df;
	
	public CellularBarsFragment()
	{
		this.yLabel = "Fraction of time";
		this.xLabel = "Number of bars";
		this.yUnits = "%";
		this.dbTable = DatabaseHelper.TABLE_CELLULAR_CONNECTIONS;
		this.interval = MainService.cellularINTERVAL;
		this.layout = R.layout.cellular_bars_layout;
		this.layoutId = R.id.cellular_bars_layout;
		this.spinnerId = R.id.spinCBTimeScale;
		this.maxNoBars = Integer.MAX_VALUE;
		this.order = false;
		this.avgLabelId = R.id.txtCBAverage;
		
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
		
		df = new DecimalFormat("#.#");
	}
	
	// returns no invalid data. only entries of the form "x bars". might return 0-length array.
	@Override
	public String[] fetchRecords(long timeInterval) 
	{
		long now = System.currentTimeMillis();
		long before = now - timeInterval;
		
    	return DatabaseAdapter.fetchCellularBarsRecords(before, now);
	}
	
	@Override
	public void updateLabels(String[] data)
	{
		super.updateLabels(data);
		
		// write the average number of bars if there is data
		if(data.length > 0)
		{
			double[] fractions = histogram.getFractions();
			String[] labels = histogram.getLabels();

			double avg=0;
			for(int i=0; i<fractions.length; i++)
				avg += fractions[i]*(Double.parseDouble(labels[i].substring(0,1)))/100.0;
			
			tvAverage.setText(AVERAGE+ df.format(avg) +BARS);	
		}
		else
			tvAverage.setText(NO_DATA);
	}

}
