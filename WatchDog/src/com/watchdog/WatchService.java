package com.watchdog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
	private final Handler handler = new Handler();
	private Runnable watch;
	
	// List of unlocked devices
	private Map<String, Long> unlockedDevices = new HashMap<String, Long>();
	
	// BT Scan interval
	private int scanInterval = 5000; // ms
	// Watch variables
	int latencyTime = 10000; // ms
	int stolenTime = 20000; // ms
	
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
		pubnub.addReceiver(this);
		
		// Initialize BT
		bt = Bluetooth.getInstance(getApplicationContext());
		bt.enableBluetooth();
		bt.registerBluetoothListener(this);
		
		// Initialize wakelock
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WatchDogWakeLock");
		wakeLock.acquire();
		
		watch = new Runnable() {
			@Override
			public void run() {
				bt.discoverDevices();
				
				for (Entry<String, ConnectedDevice> e : appState.getLockedDevices().entrySet()) {
					ConnectedDevice d = e.getValue();
					if ((System.currentTimeMillis() - d.lastSeen) > latencyTime && !d.maybeStolen) {
						// Hasn't been seen for a while
						pubnub.sendMessage("STOLEN?@" + e.getKey());
						d.maybeStolen = true;
					}
					else if ((System.currentTimeMillis() - d.lastSeen) > stolenTime && d.maybeStolen && !d.stolen) {
						pubnub.sendMessage("STOLEN!@" + e.getKey() + "@" + e.getValue().name);
						d.stolen = true;
						Log.i(TAG, "STOLEN " + e.getKey() + " (" + e.getValue().name + ")");
						// TODO: STOLEN
					}
				}
				
				handler.postDelayed(this, scanInterval);
			}
		};
		handler.postDelayed(watch, scanInterval);
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
            handler.removeCallbacks(watch);
        } catch (Throwable th) { }
		Log.i(TAG, "WatchService stopped");
		super.onDestroy();
	}

	@Override
	public void onReceiveMessage(String message) {
		Log.i(TAG, "onReceive");
		String firstPart = message.split("@")[0];
		if (firstPart.equals("LOCKED!")) {
			Log.i(TAG, "LOCKED!");
			String mac = message.split("@")[1];
			if (unlockedDevices.containsKey(mac)) {
				String name = message.split("@")[2];
				long time = unlockedDevices.remove(mac);
				Log.i(TAG, "Start monitoring locked device " + mac + "(" + name + ")");
				appState.putLockedDevice(mac, new ConnectedDevice(name, time));
			}
		}
		else if (firstPart.equals("LOCKED?")) {
			String mac = message.split("@")[1];
			if (appState.containsLockedDevice(mac)) {
				String name = appState.getLockedDeviceName(mac);
				pubnub.sendMessage("LOCKED!@" + mac + "@" + name);
			}
		}
		else if (firstPart.equals("STOLEN?")) {
			String mac = message.split("@")[1];
			if (!appState.getLockedDeviceStolen(mac)) {
				pubnub.sendMessage("NOTSTOLEN@" + mac);
			}
		}
		else if (firstPart.equals("STOLEN!")) {
			// TODO: STOLEN!!
			String mac = message.split("@")[1];
			String name = message.split("@")[2];
			Log.i(TAG, "STOLEN " + mac + " (" + name + ")");
		}
		else if (firstPart.equals("NO!")) {
			String mac = message.split("@")[1];
			appState.setLockedDeviceMaybeStolen(mac, false);
			unlockedDevices.put(mac, appState.getLockedDeviceLastSeen(mac));
			appState.removeLockedDevice(mac);
		}
	}

	@Override
	public void found(BluetoothDevice device, short RSSI) {
		Log.i(TAG, "Found " + device.getName() + "(" + device.getAddress() + ")");
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
