package com.example.scalemed.bluetoothDevice;

/**
 * Created by 21zhou on 10/1/14.
 */
public interface IBluetoothDevice {

    static final int DISCONNECTED = -1;
    static final int CONNECTED = 1;

    /**
     * turn on the device
     */
    public void turnOn();

    /**
     * turn off the device
     */
    public void turnOff();

    /**
     *
     * @return the connected Status of the device, -1 for disconnected, 1 for connected
     */
    public int getConnectionStatus();
}
