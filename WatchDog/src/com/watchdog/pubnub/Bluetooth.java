package com.watchdog.pubnub;

import java.util.ArrayList;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class Bluetooth {
	
	private static Bluetooth instance;
	private BluetoothAdapter adapter;
	private Context context;
	
	private ArrayList<BluetoothListener> listeners = new ArrayList<BluetoothListener>();
	
	private long startedDiscovery = 0;
	private final int discoveryTime = 3000;
	
	private Bluetooth(Context c) {
		adapter = BluetoothAdapter.getDefaultAdapter();
		context = c;
	}
	
	public static Bluetooth getInstance(Context c) {
		if (instance == null) {
			instance = new Bluetooth(c);
		}
		return instance;
	}
	
	public void enableBluetooth() {
		if (adapter.isEnabled()) {
			onBluetoothEnabled();
		} else {
			// Register receiver for bluetooth enabled broadcast
			BroadcastReceiver rec = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
					
					switch (state) {
						case (BluetoothAdapter.STATE_ON):
							onBluetoothEnabled();
					}
				}
				
			};
			context.registerReceiver(rec, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
			
			adapter.enable();
		}
	}
	
	public void discoverDevices() {
		if (!adapter.isDiscovering() || (System.currentTimeMillis() - startedDiscovery) > discoveryTime) {
			adapter.cancelDiscovery();
			Log.i("Bluetooth", "Starting discovery");
			startedDiscovery = System.currentTimeMillis();
			adapter.startDiscovery();
		}
	}
	
	public void registerBluetoothListener(BluetoothListener l) {
		listeners.add(l);
	}
	
	public void unregisterBluetoothListener(BluetoothListener l) {
		listeners.remove(l);
	}
	
	private void onBluetoothEnabled() {
		Log.i("Bluetooth", "Bluetooth enabled, registering discover receiver");
		// Register receiver for bluetooth device found broadcast
		BroadcastReceiver rec = new BroadcastReceiver() {
		    @Override
			public void onReceive(Context context, Intent intent) {
		        String action = intent.getAction();
		        
		        Log.i("BT", "onReceiver");

		        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
		            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
		            short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, (short) 0);
		            Log.i("Bluetooth", "FOUND: " + device.getName() + " " + device.getAddress() + ", rssi: " + rssi);
		            for (BluetoothListener l : listeners) {
		            	l.found(device, rssi);
		            }
		        }
		    }
		};
		context.registerReceiver(rec, new IntentFilter(BluetoothDevice.ACTION_FOUND));
	}
	
	public interface BluetoothListener {
		public void found(BluetoothDevice device, short RSSI);
	}

}
