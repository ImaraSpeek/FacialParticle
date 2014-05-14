package com.watchdog;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class LockedService extends Service implements SensorEventListener {
	
	// Service thread
	private Thread t;
	// Wakelock
	private WakeLock wakeLock;
	
	// Sensor variables
	private SensorManager sensorManager;
	private Sensor accelerometer;
	
	// Measurements variables
	private float lastX;
	private float lastY;
	private float lastZ;
	private boolean lastStored = false;
	private double threshold = 1;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		t = new Thread(new Runnable() {
			public void run() {
				startLock();
			}
		});
		t.start();
		
		return Service.START_STICKY;
	}
	
	public void startLock() {
		Log.i("WATCHDOG", "Locked service started");
		
		// Initialize wakelock
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WatchDogWakeLock");
		wakeLock.acquire();
		
		// Initialize accelerometer
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			if (lastStored) {
				// Filter values
				float a = 0.5f;
				float x = event.values[0] - (lastX*(1-a) + event.values[0]*a);
				float y = event.values[1] - (lastY*(1-a) + event.values[1]*a);
				float z = event.values[2] - (lastZ*(1-a) + event.values[2]*a);
				
				double acc = Math.sqrt(x*x + y*y + z*z);
				
				if (acc > threshold) {
					// Threshold passed, start unlock activity
					Intent unlockIntent = new Intent(getBaseContext(), UnlockActivity.class);
					unlockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					getApplication().startActivity(unlockIntent);
					// Release listener, wakelock and stop service
					sensorManager.unregisterListener(this);
					wakeLock.release();
					stopSelf();
				}
				
				lastX = event.values[0];
				lastY = event.values[1];
				lastZ = event.values[2];
			} else {
				lastX = event.values[0];
				lastY = event.values[1];
				lastZ = event.values[2];
				lastStored = true;
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) { }
	
	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
