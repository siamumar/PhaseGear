package com.riceucla.mobilelogger;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class TrafficFragment extends Fragment 
{

	//for case structure ref
	public static final String IMAGE_RESOURCE_ID = "iconResourceID";
	public static final String ITEM_NAME = "itemName";
	ImageView ivIcon;
	TextView tvItemName;

	private FragmentTabHost mTabHost;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) 
	{
		View view = inflater.inflate(R.layout.fragment_layout_traffic, container,
				false);
		mTabHost = (FragmentTabHost)view.findViewById(android.R.id.tabhost);
		mTabHost.setup(getActivity(), getChildFragmentManager(), R.id.realtabcontent_traffic);

		mTabHost.addTab(mTabHost.newTabSpec("traffic_data_download_fragment").setIndicator("Data Download"),
				TrafficDataDownloadFragment.class, null);
		mTabHost.addTab(mTabHost.newTabSpec("traffic_data_upload_fragment").setIndicator("Data Upload"),
				TrafficDataUploadFragment.class, null);

/*		// for horizontal scrollability
		TabWidget tw = (TabWidget) view.findViewById(android.R.id.tabs);
		LinearLayout ll = (LinearLayout) tw.getParent();
		HorizontalScrollView hs = new HorizontalScrollView(getActivity().getApplicationContext());
		hs.setLayoutParams(new FrameLayout.LayoutParams(
		    FrameLayout.LayoutParams.MATCH_PARENT,
		    FrameLayout.LayoutParams.WRAP_CONTENT));
		ll.addView(hs, 0);
		ll.removeView(tw);
		hs.addView(tw);
		hs.setHorizontalScrollBarEnabled(false);*/
		
		return view;
	}
}
