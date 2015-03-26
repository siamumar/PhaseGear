package com.riceucla.mobilelogger;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabWidget;
import android.widget.TextView;

import java.util.Calendar;

public class StatsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.stats_layout, container, false);
        ((TextView)view.findViewById(R.id.UUID)).setText(MainActivity.UUID);
        ((TextView)view.findViewById(R.id.LastSyncTime)).setText(MainActivity.lastSyncTime);

        return view;
    }
}