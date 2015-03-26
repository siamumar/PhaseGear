package com.riceucla.mobilelogger;

import java.text.DecimalFormat;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

public class Chart 
{	
	private final String NOW = "Now";
	private final String AGO = " ago";
	
	private GraphicalView view;
	private ChartFragment parent;
	private int containerId;
	
	// data variables
	private long[] xData;
	private double[] yData;
	private int noSeries;
	
	private DecimalFormat df;
	
	// labels
	private String chartTitle;
	private String xTitle;
	private String yTitle;
	private String seriesName;
	private String yUnits;
	private String timescale;
	private boolean noData;
	
	// styling variables
	private int lineColor;
	private int lineWidth;
	private float labelsTextSize;
	private float axisTitleTextSize;
	private int backgroundColor;
	private double yMinZoomLimit;
	private double yMaxZoomLimit;
	private double yInitialMin;
	private double yInitialMax;
	private double minAbsoluteLimit;
	private double maxAbsoluteLimit;
	private int colorAreaUnder;
	private int[] margins = {30, 130, 100, 30};
	
	
	public Chart(ChartFragment parent, int containerId, long[] xData, double[] yData,
			String xTitle, String yTitle, String yUnits, String timescale)
	{
		this.parent = parent;
		this.containerId = containerId;
		
		this.xData = xData;
		this.yData = yData;
		this.xTitle = xTitle;
		this.yTitle = yTitle;
		this.yUnits = yUnits;
		this.timescale = timescale;
		this.noSeries = 0;
		
		this.df = new DecimalFormat("#.##");
		
		// if there is no data, then show a straight line at 0
		if(this.xData.length==0 || this.yData.length==0)
		{
			this.xData = new long[2];
			this.yData = new double[2];
			this.yData[0] = 0;
			this.yData[1] = 0;
			this.xData[0] = this.parent.before;
			this.xData[1] = this.parent.now;
			noData = true;
			noSeries++;
		}
		else
			noData = false;
		
		// make specific choices for now, can be made adjustable later
		this.lineColor = 0xFF40B4FF;
		this.lineWidth = 10;
		this.labelsTextSize= 30.0f;
		this.axisTitleTextSize= 40.0f;
		this.chartTitle = ""; 
		this.seriesName = "";
		this.colorAreaUnder = 0x7040B4FF;
		this.backgroundColor = Color.BLACK;
	
		this.yMinZoomLimit = 1;	// if R = (max to min range of the dataset), the min. zoom limit is min-R*yMinZoomLimit
		this.yMaxZoomLimit = 1; // the rest is similar
		this.yInitialMin = 0.5;
		this.yInitialMax = 0.5;
		
		this.minAbsoluteLimit = -Double.MAX_VALUE;
		this.maxAbsoluteLimit = Double.MAX_VALUE;
	}
	
	public void addChartToLayout()
	{
	     // Get chart container
	     LinearLayout layout = (LinearLayout) parent.getActivity().findViewById(containerId);

	     XYMultipleSeriesDataset dataset = getDataset();	// this line must be called before the next one!
	     
	     // Creating an intent to plot line chart using data set and multipleRenderer
         view=(GraphicalView)ChartFactory.getLineChartView(parent.getActivity().
        		 getBaseContext(), dataset, getRenderer());

         //  Adding click event to the Line Chart.
         view.setOnClickListener(new View.OnClickListener() 
         {
        	 @Override
        	 public void onClick(View arg0) 
        	 {
        		 SeriesSelection series_selection=view.getCurrentSeriesAndPoint();

        		 if(series_selection!=null)
        		 {
        			 double amount=(double)series_selection.getValue();
        			 
        			 //show message with coordinates if points clicked in toast
        			 Toast.makeText(parent.getActivity().getBaseContext(), df.format(amount) + " " + yUnits, 
        					 Toast.LENGTH_LONG).show();
        		 }
        	 }
         });

	     // Add the graphical view mChart object into the Linear layout .
	     layout.addView(view);
	     	     
	}
	
	public void update(long[] xData, double[] yData, String timescale)
	{
		if(xData.length>0 && yData.length>0)
		{
			this.xData = xData;
			this.yData = yData;
			this.noData = false;
		}
		else
		{
			this.xData = new long[2];
			this.yData = new double[2];
			this.yData[0] = 0;
			this.yData[1] = 0;
			this.xData[0] = this.parent.before;
			this.xData[1] = this.parent.now;
			noData = true;
			noSeries++;
		}
		
		this.timescale = timescale;
		
		LinearLayout layout = (LinearLayout) parent.getActivity().findViewById(containerId);
		layout.removeView(view);
		
	    addChartToLayout();
	}
	
	public GraphicalView getView()
	{
		return view;
	}

	public XYMultipleSeriesRenderer getRenderer()
	{
		XYMultipleSeriesRenderer mRenderer=new XYMultipleSeriesRenderer();

		for(int i=0; i<noSeries; i++)
		{
			XYSeriesRenderer Xrenderer=new XYSeriesRenderer();
			
			Xrenderer.setColor(lineColor);
			Xrenderer.setDisplayChartValues(false);
			Xrenderer.setLineWidth(lineWidth);
			Xrenderer.setFillPoints(true);	
	
			XYSeriesRenderer.FillOutsideLine fill = 
					new XYSeriesRenderer.FillOutsideLine(XYSeriesRenderer.FillOutsideLine.Type.BELOW);
			fill.setColor(colorAreaUnder);
			Xrenderer.addFillOutsideLine(fill);
			
			mRenderer.addSeriesRenderer(Xrenderer);
		}

		mRenderer.setChartTitle(chartTitle);
		mRenderer.setXTitle(xTitle);
		mRenderer.setYTitle(yTitle + " ("+yUnits+")");
		mRenderer.setApplyBackgroundColor(true);
		mRenderer.setBackgroundColor(backgroundColor);
		mRenderer.setLabelsTextSize(labelsTextSize);
		mRenderer.setAxisTitleTextSize(axisTitleTextSize);
		mRenderer.setMargins(margins);
		mRenderer.addXTextLabel(this.parent.before, timescale+AGO);
		mRenderer.addXTextLabel(this.parent.now, NOW);
		mRenderer.setYLabelsAlign(Paint.Align.RIGHT);
		mRenderer.setXLabels(0);
		mRenderer.setShowGrid(true);
		mRenderer.setClickEnabled(true);
		mRenderer.setShowLegend(false);

		mRenderer.setZoomEnabled(!noData);
		mRenderer.setPanEnabled(!noData);
		mRenderer.setZoomButtonsVisible(!noData);
		
		if(!noData)
		{
			// set range and zoom limits
			double max = -Double.MAX_VALUE;
			double min = Double.MAX_VALUE;
			
			for(double d: yData)
			{
				if(d != (double)MainService.NOT_AVAILABLE)
				{
					if(d < min) min = d;
					if(d > max) max = d;
				}
			}
			
			if(minAbsoluteLimit != -Double.MAX_VALUE)
				min = minAbsoluteLimit;
			
			if(maxAbsoluteLimit != Double.MAX_VALUE)
				max = maxAbsoluteLimit;
			
			double range;
			
			if(max == -Double.MAX_VALUE && min == Double.MAX_VALUE)	// all data was invalid
				range = 0;
			else
				range = max - min;

			double[] limits = {this.parent.before, 
					this.parent.now,    
					(minAbsoluteLimit == -Double.MAX_VALUE ? min-range*yMinZoomLimit : minAbsoluteLimit ), 
					(maxAbsoluteLimit == Double.MAX_VALUE ? max+range*yMaxZoomLimit : maxAbsoluteLimit )
					};
			double[] initial = {this.parent.before, 
					this.parent.now, 
					(minAbsoluteLimit == -Double.MAX_VALUE ? min-range*yInitialMin : minAbsoluteLimit ), 
					(maxAbsoluteLimit == Double.MAX_VALUE ? max+range*yInitialMax : maxAbsoluteLimit )
					};
			mRenderer.setZoomLimits(limits);
			mRenderer.setPanLimits(limits);
			mRenderer.setRange(initial);
		}
		
		return mRenderer;
	}


	public XYMultipleSeriesDataset getDataset() 
	{
		XYMultipleSeriesDataset dataset=new XYMultipleSeriesDataset(); 
		XYSeries xSeries=new XYSeries(seriesName);
		
		boolean newSeries = true;
		noSeries = 0;
		
		for(int i=0; i<xData.length; i++)	
		{
			if(newSeries)
			{
				xSeries=new XYSeries(seriesName);
				newSeries = false;
			}
			
			if( yData[i] != (double)MainService.NOT_AVAILABLE )
			{
				xSeries.add(xData[i], yData[i]);
				
				if(i == xData.length-1)
				{
					dataset.addSeries(xSeries);
					noSeries++;
				}
			}
			else
			{	
				if(xSeries.getItemCount() > 0 || 
						(dataset.getSeriesCount()==0 && i == xData.length-1))	// no valid data at all
				{	
					dataset.addSeries(xSeries);
					noSeries++;
				}
				
				newSeries = true;
			}
		}
		return dataset;
	}

	public void setAbsoluteLimits(double min, double max)
	{
		minAbsoluteLimit = min;
		maxAbsoluteLimit = max;
	}
	
	public void setUpperBound(double[] upperBound) 
	{
		// TODO Auto-generated method stub
		
	}
	
	
	
}
