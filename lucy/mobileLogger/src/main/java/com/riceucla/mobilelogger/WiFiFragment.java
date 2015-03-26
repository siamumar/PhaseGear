package com.riceucla.mobilelogger;

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

public class WiFiFragment extends Fragment {
	
	public static final String IMAGE_RESOURCE_ID = "iconResourceID";
	public static final String ITEM_NAME = "itemName";
	ImageView ivIcon;
	TextView tvItemName;	
	private FragmentTabHost mTabHost;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) 
	{
		View view = inflater.inflate(R.layout.fragment_layout_wifi, container,
				false);
		mTabHost = (FragmentTabHost)view.findViewById(android.R.id.tabhost);
		mTabHost.setup(getActivity(), getChildFragmentManager(), R.id.realtabcontent_wifi);

		mTabHost.addTab(mTabHost.newTabSpec("wifi_signal_strength_fragment").setIndicator("Signal Strength"),
				WiFiSignalStrengthFragment.class, null);
		mTabHost.addTab(mTabHost.newTabSpec("wifi_connection_speed_fragment").setIndicator("Connection Speed"),
				WiFiConnectionSpeedFragment.class, null);
		mTabHost.addTab(mTabHost.newTabSpec("wifi_ssid_fragment").setIndicator("SSID"),
				WiFiSSIDFragment.class, null);
		
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
