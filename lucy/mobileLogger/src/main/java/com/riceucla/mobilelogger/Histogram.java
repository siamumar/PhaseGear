package com.riceucla.mobilelogger;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.support.v4.app.Fragment;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

public class Histogram
{	
	private static final String TOP = " (Top " ;
	private static final String SHOWN = " shown)";
	private GraphicalView view;
	private Fragment parent;
	private int containerId;
	
	private String[] labels;
	private double[] fractions;
	private String[] largestLabels;
	private double[] largest;
	private HashSet<String> distinct;
	private int maxNoBars;
	private boolean order;
	
	// labels
	private String chartTitle;
	private String xTitle;
	private String yTitle;
	private String seriesName;
	private String yUnits;
	
	// styling variables
	private int lineColor;
	private int lineWidth;
	private float labelsTextSize;
	private float axisTitleTextSize;
	private int backgroundColor;
	private double barSpacing;
	private float barWidth;
	private float labelsAngle;
	private float labelsPadding;
	private int[] margins = {60, 130, 100, 30};
	
	private DecimalFormat df;
	
	
	public Histogram(Fragment parent, int containerId, String[] data, int maxNoBars, boolean order,
			String xTitle, String yTitle, String yUnits)
	{
		this.parent = parent;
		this.containerId = containerId;
		this.maxNoBars = maxNoBars;
		this.xTitle = xTitle;
		this.yTitle = yTitle;
		this.yUnits = yUnits;
		this.order = order;
		
		this.df = new DecimalFormat("#.#");
		
		// make specific choices for now, can be made adjustable later
		this.lineColor = 0xFF40B4FF;
		this.lineWidth = 10;
		this.labelsTextSize= 30.0f;
		this.axisTitleTextSize= 40.0f;
		this.labelsAngle = -45.0f;
		this.labelsPadding = 70.0f;
		this.chartTitle = ""; 
		this.seriesName = "";
		this.barSpacing = 0.5;
		this.barWidth = 80.0f;
		this.backgroundColor = Color.BLACK;
	
		setLabelsAndFractions(data);
	}
	
	public void addChartToLayout()
	{
	     // Get chart container
	     LinearLayout layout = (LinearLayout) parent.getActivity().findViewById(containerId);

	     // Creating an intent to plot line chart using data set and multipleRenderer
         view=(GraphicalView)ChartFactory.getBarChartView(parent.getActivity().
        		 getBaseContext(), getDataset(), getRenderer(), BarChart.Type.DEFAULT);

         //  Adding click event to the Line Chart.
         view.setOnClickListener(new View.OnClickListener() 
         {
        	 @Override
        	 public void onClick(View arg0) 
        	 {
        		 SeriesSelection seriesSelection=view.getCurrentSeriesAndPoint();

        		 if(seriesSelection!=null)
        		 {
        			 double amount=(double)seriesSelection.getValue();
        			 
        			 //show message with coordinates if points clicked in toast
        			 Toast.makeText(parent.getActivity().getBaseContext(), 
        					largestLabels[(int)(seriesSelection.getXValue())] +" - "
        					+df.format(amount) + " " + yUnits, Toast.LENGTH_LONG).show();
        		 }
        	 }
         });

	     // Add the graphical view mChart object into the Linear layout .
	     layout.addView(view);
	     	     
	}
	
	public void update(String[] data)
	{
		setLabelsAndFractions(data);
		
		LinearLayout layout = (LinearLayout) parent.getActivity().findViewById(containerId);
		layout.removeView(view);
		
	    addChartToLayout();
	}
	
	public GraphicalView getView()
	{
		return view;
	}
	
	public void setLabelsAndFractions(String[] data)
	{
		this.distinct = new HashSet<String>(Arrays.asList(data));
		
		this.labels = new String[distinct.size()];
		
		// compute the fractions and labels
		this.fractions = new double[distinct.size()];
		
		Iterator<String> it = distinct.iterator();
		
		int index = 0;
		while(it.hasNext())
		{
			int count = 0;
			String item = it.next();

			for(int i=0; i<data.length; i++)
				if(item.equals(data[i])) count++;

			labels[index] = item;
			fractions[index] = 100*(0.0+count)/(0.0+data.length);

			index++;
		}

		findLargest();
	}
	
	
	// if there are too many entries, take the largest maxNoBars of them.
	// can be optimized, but really not worth it
	public void findLargest()
	{
		// put values in decreasing order
		if(order)
		{
			double max = 0;
			int maxIndex = 0;
			int n = Math.min(maxNoBars, fractions.length);

			largest = new double[n];
			largestLabels = new String[n];		

			double[] copyFractions = fractions;

			for(int i=0; i<n; i++)
			{
				max=0;
				for(int j=0;j<copyFractions.length;j++)
				{
					if(copyFractions[j]>max && (i==0 || copyFractions[j]<=largest[i-1]) )
					{
						maxIndex=j;
						max=copyFractions[j];
					}
				}
				largest[i]=copyFractions[maxIndex];
				largestLabels[i] = labels[maxIndex];
				copyFractions[maxIndex] = -1;		// needed for the algorithm to work
			}
		}
		else
		{
			// put labels in alphabetical order using insertion sort
			for(int i=1; i<labels.length; i++)
			{
				int j = i;
				while(j>0 && labels[j-1].compareTo(labels[j])>0 )
				{
					String sTemp = labels[j-1];
					labels[j-1] = labels[j];
					labels[j] = sTemp;
					
					double temp = fractions[j-1];
					fractions[j-1] = fractions[j];
					fractions[j] = temp;
					
					j--;
				}
			}
			
			largest = fractions;
			largestLabels = labels;
		}
	}


	public XYMultipleSeriesRenderer getRenderer()
	{
		XYSeriesRenderer Xrenderer=new XYSeriesRenderer();
		XYMultipleSeriesRenderer mRenderer=new XYMultipleSeriesRenderer();

		Xrenderer.setColor(lineColor);
		Xrenderer.setDisplayChartValues(false);
		Xrenderer.setLineWidth(lineWidth);
		Xrenderer.setFillPoints(true);	

		mRenderer.setChartTitle(chartTitle);
		
		if(fractions != null && maxNoBars < fractions.length)
			mRenderer.setXTitle(xTitle + TOP + maxNoBars + SHOWN);
		else
			mRenderer.setXTitle(xTitle);
		
		mRenderer.setYTitle(yTitle + " ("+yUnits+")");
		mRenderer.setApplyBackgroundColor(true);
		mRenderer.setBackgroundColor(backgroundColor);
		mRenderer.setLabelsTextSize(labelsTextSize);
		mRenderer.setAxisTitleTextSize(axisTitleTextSize);
		mRenderer.setMargins(margins);
		mRenderer.setYLabelsAlign(Paint.Align.RIGHT);
		mRenderer.setZoomButtonsVisible(false);
		mRenderer.setXLabels(0);
		mRenderer.setShowGrid(true);
		mRenderer.setClickEnabled(true);
		mRenderer.setShowLegend(false);
		mRenderer.setBarSpacing(barSpacing);
		mRenderer.setBarWidth(barWidth);
		mRenderer.setZoomEnabled(false);
		mRenderer.setPanEnabled(false);
		mRenderer.setXLabelsAngle(labelsAngle);
		mRenderer.setXLabelsPadding(labelsPadding);

		double[] initial = {-1, largest.length, 0, 100};
		mRenderer.setRange(initial);
		
		for(int i=0; i<largestLabels.length; i++)
			mRenderer.addXTextLabel(i, largestLabels[i]);
		
		
		mRenderer.addSeriesRenderer(Xrenderer);
		
		return mRenderer;
	}

	public XYMultipleSeriesDataset getDataset() 
	{
		// add the fraction data to the dataset
		XYSeries xSeries=new XYSeries(seriesName);

		for(int i=0; i<largest.length; i++)
			xSeries.add(i, largest[i]);

		XYMultipleSeriesDataset dataset=new XYMultipleSeriesDataset(); 
		dataset.addSeries(xSeries);

		return dataset;
	}
	
	public double[] getFractions()
	{
		return fractions;
	}
	
	public String[] getLabels()
	{
		return labels;
	}
}
