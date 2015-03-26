package com.riceucla.mobilelogger;
//imports
//import android.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabWidget;
import android.widget.TextView;


public class LocationFragment extends Fragment {

	public static final String IMAGE_RESOURCE_ID = "iconResourceID";
	public static final String ITEM_NAME = "itemName";
	ImageView ivIcon;
	TextView tvItemName;

	private FragmentTabHost mTabHost;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) 
	{
		View view = inflater.inflate(R.layout.fragment_layout_location, container,
				false);

		mTabHost = (FragmentTabHost)view.findViewById(android.R.id.tabhost);
		mTabHost.setup(getActivity(), getChildFragmentManager(), R.id.realtabcontent_location);

		mTabHost.addTab(mTabHost.newTabSpec("location_coordinates_fragment").setIndicator("Coordinates"),
				LocationCoordinatesFragment.class, null);
		mTabHost.addTab(mTabHost.newTabSpec("location_trajectory_fragment").setIndicator("Trajectory"),
				LocationTrajectoryFragment.class, null); 
		mTabHost.addTab(mTabHost.newTabSpec("location_speed_fragment").setIndicator("Speed"),
				LocationSpeedFragment.class, null);

		// for horizontal scrollability
		TabWidget tw = (TabWidget) view.findViewById(android.R.id.tabs);
		LinearLayout ll = (LinearLayout) tw.getParent();
		HorizontalScrollView hs = new HorizontalScrollView(getActivity().getApplicationContext());
		hs.setLayoutParams(new FrameLayout.LayoutParams(
		    FrameLayout.LayoutParams.MATCH_PARENT,
		    FrameLayout.LayoutParams.WRAP_CONTENT));
		ll.addView(hs, 0);
		ll.removeView(tw);
		hs.addView(tw);
		hs.setHorizontalScrollBarEnabled(false);
		
		return view;
	}


}