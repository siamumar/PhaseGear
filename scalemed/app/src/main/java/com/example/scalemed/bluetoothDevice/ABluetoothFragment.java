package com.example.scalemed.bluetoothDevice;


import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.widget.Toast;

import android.os.Handler;

/**
 * Created by 21zhou on 10/1/14.
 */
abstract class ABluetoothFragment extends Fragment {

    private BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 2;

    // time in millis before each reconnection attempt
    private static final long TIME_RECONNECTION_ATTEMPT = 4000;
    private static final int MAX_RECONNECTION_ATTEMPTS = 3;

    private int numConnectionAttempts = 0;

    private Handler mHandler;

    /**
     * provided runnable tack to reconnect the device
     */
    private Runnable mReconnectTask = new Runnable() {
        public void run() {
            if (getDevice().getConnectionStatus() == IBluetoothDevice.DISCONNECTED) {
                getDevice().turnOn();

                displayConnectionFailed();
            }
            mHandler.postDelayed(this, TIME_RECONNECTION_ATTEMPT);
        }
    };


    public ABluetoothFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //get the default bluetooth adapter on the creation of this fragment
        //if there is no adapter, show error
        if (!isBluetoothAvailable()){
            Toast.makeText(getActivity(), "Bluetooth is not available.", Toast.LENGTH_LONG).show();
        }

        mHandler = new Handler();
    }

    @Override
    public void onStart() {
        super.onStart();

        //if the bluetooth device is not enabled, request the user to enable it
        if (!mBluetoothAdapter.isEnabled()) {
            requestEnableBluetooth();
        }
        // Otherwise, setup the data service
        else {
            initService();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mHandler.postDelayed(mReconnectTask, TIME_RECONNECTION_ATTEMPT);
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mReconnectTask);
    }

    /**
     * The default behavior of onStop call would not turn off the device
     */
    @Override
    public void onStop() {
        super.onStop();
        mHandler.removeCallbacks(mReconnectTask);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mReconnectTask);
    }

    public void requestEnableBluetooth(){
        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
    }

    public boolean isBluetoothAvailable(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        return mBluetoothAdapter != null;
    }

    /**
     * update the reconnection attempts, if it is larger than the maximum reconnection attempts limit,
     * warn the user
     */
    private void displayConnectionFailed() {
        Toast.makeText(getActivity(), "Failed to connect. Reconnecting...", Toast.LENGTH_SHORT).show();

        numConnectionAttempts += 1;
        if (numConnectionAttempts >= MAX_RECONNECTION_ATTEMPTS) {
            Toast.makeText(getActivity(), "Please replace the batteries or check that you're using the correct sensor.", Toast.LENGTH_SHORT).show();
        }
    }

    public void initService(){
        getDevice().turnOn();
        mHandler.removeCallbacks(mReconnectTask);
        mHandler.postDelayed(mReconnectTask, TIME_RECONNECTION_ATTEMPT);
    }

    /**
     * abstract method to get the IBluetoothDevice related to this fragment
     * @return the IBluetoothDevice
     */
    public abstract IBluetoothDevice getDevice();




}
