package com.watchdog;

import com.watchdog.pubnub.PubNub;
import com.watchdog.pubnub.PubNub.PubNubReceiver;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class WatchService extends Service implements PubNubReceiver {
	
	// LOG TAG
	private final String TAG = "LockedService";
	
	// Service thread
	private Thread t;
	// Wakelock
	private WakeLock wakeLock;
	// PubNub
	private PubNub pubnub;
	
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
		
		// Initialize PubNub
		pubnub = PubNub.getInstance();
		
		// Initialize wakelock
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WatchDogWakeLock");
		wakeLock.acquire();
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
        } catch (Throwable th) { }
		Log.i(TAG, "WatchService stopped");
		super.onDestroy();
	}

	@Override
	public void onReceiveMessage(String message) {
		
	}

}
