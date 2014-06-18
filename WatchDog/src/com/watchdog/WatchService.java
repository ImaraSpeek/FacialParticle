package com.watchdog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.watchdog.ApplicationState.ConnectedDevice;
import com.watchdog.pubnub.Bluetooth;
import com.watchdog.pubnub.Bluetooth.BluetoothListener;
import com.watchdog.pubnub.PubNub;
import com.watchdog.pubnub.PubNub.PubNubReceiver;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class WatchService extends Service implements PubNubReceiver, BluetoothListener {
	
	// LOG TAG
	private final String TAG = "LockedService";
	
	// Application state
	private ApplicationState appState;
	
	// Service thread
	private Thread t;
	// Wakelock
	private WakeLock wakeLock;
	// PubNub
	private PubNub pubnub;
	// Bluetooth
	private Bluetooth bt;
	// Handler
	final Handler handler = new Handler();
	
	// List of unlocked devices
	private Map<String, Long> unlockedDevices = new HashMap<String, Long>();
	
	// BT Scan interval
	private int scanInterval = 5000; // ms
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "onStartCommand");
		t = new Thread(new Runnable() {
			public void run() {
				startWatch();
			}
		});
		t.start();
		
		return Service.START_STICKY;
	}
	
	public void startWatch() {
		Log.i(TAG, "WatchService started");
		
		appState = ApplicationState.getInstance(getApplicationContext());
		
		// Initialize PubNub
		pubnub = PubNub.getInstance();
		
		// Initialize BT
		bt = Bluetooth.getInstance(getApplicationContext());
		bt.enableBluetooth();
		bt.registerBluetoothListener(this);
		
		// Initialize wakelock
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WatchDogWakeLock");
		wakeLock.acquire();
		
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				bt.discoverDevices();
				handler.postDelayed(this, scanInterval);
			}
		}, scanInterval);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onDestroy() {
		try {
            wakeLock.release();
            pubnub.unsubscribe();
            bt.unregisterBluetoothListener(this);
        } catch (Throwable th) { }
		Log.i(TAG, "WatchService stopped");
		super.onDestroy();
	}

	@Override
	public void onReceiveMessage(String message) {
		String firstPart = message.split("@")[0];
		if (firstPart.equals("LOCKED!")) {
			String mac = message.split("@")[1];
			if (unlockedDevices.containsKey(mac)) {
				String name = message.split("@")[2];
				unlockedDevices.remove(mac);
				appState.putLockedDevice(mac, new ConnectedDevice(name, System.currentTimeMillis()));
			}
		}
		else if (firstPart.equals("LOCKED?")) {
			String mac = message.split("@")[1];
			if (appState.containsLockedDevice(mac)) {
				String name = appState.getLockedDeviceName(mac);
				pubnub.sendMessage("LOCKED!@" + mac + "@" + name);
			}
		}
	}

	@Override
	public void found(BluetoothDevice device, short RSSI) {
		String mac = device.getAddress();
		if (appState.containsLockedDevice(mac)) {
			appState.setLockedDeviceLastSeen(mac, System.currentTimeMillis());
		}
		else if (unlockedDevices.containsKey(mac)) {
			unlockedDevices.put(mac, System.currentTimeMillis());
		}
		else {
			// Fist time device found, ask if locked
			pubnub.sendMessage("LOCKED?@" + mac);
			unlockedDevices.put(mac, System.currentTimeMillis());
		}
	}


}
