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
	
	// Application state
	private ApplicationState appState;
	
	// LOG TAG
	private final String TAG = "LockedService";
	
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
	
	// Calibration variables
	private long timeLastMsrAboveCalibrationThreshold = -1;
	private double calibrationThreshold = 0.2; // Acceleration
	private long calibrationThresholdTime = 3000; // ms
	private long calibrationStart = -1;
	private long calibrationTime = 5000; // ms
	private double maxAccValue = -1;
	private int calibrationFactor = 4;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "onStartCommand");
		t = new Thread(new Runnable() {
			public void run() {
				startLock();
			}
		});
		t.start();
		
		return Service.START_STICKY;
	}
	
	public void startLock() {
		Log.i(TAG, "LockedService started");
		
		appState = ApplicationState.getInstance(getApplicationContext());
		
		// Initialize wakelock
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WatchDogWakeLock");
		wakeLock.acquire();
		
		// Initialize accelerometer
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		
		Log.i(TAG, "Going into LOCKING state");
		appState.setState(ApplicationState.LOCK_STATE_LOCKING);
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			double acc = -1;
			if (lastStored) {
				// Filter values
				float a = 0.5f;
				float x = event.values[0] - (lastX*(1-a) + event.values[0]*a);
				float y = event.values[1] - (lastY*(1-a) + event.values[1]*a);
				float z = event.values[2] - (lastZ*(1-a) + event.values[2]*a);
				
				acc = Math.sqrt(x*x + y*y + z*z);
				
				lastX = event.values[0];
				lastY = event.values[1];
				lastZ = event.values[2];
			} else {
				lastX = event.values[0];
				lastY = event.values[1];
				lastZ = event.values[2];
				lastStored = true;
			}
			
			if (appState.getState() == ApplicationState.LOCK_STATE_LOCKING) {
				if (acc > calibrationThreshold) {
					timeLastMsrAboveCalibrationThreshold = System.currentTimeMillis();
				}
				if (timeLastMsrAboveCalibrationThreshold > 0 
						&& (System.currentTimeMillis() - timeLastMsrAboveCalibrationThreshold) > calibrationThresholdTime) {
					// Acceleration has not been above "calibrtionThreshold" for "calibrationThresholdTime" ms, so go to calibration
					Log.i(TAG, "Going into CALIBRATING state");
					appState.setState(ApplicationState.LOCK_STATE_CALIBRATING);
				}
			}
			if (appState.getState() == ApplicationState.LOCK_STATE_CALIBRATING) {
				if (calibrationStart < 0) {
					calibrationStart = System.currentTimeMillis();
				}
				if ((System.currentTimeMillis() - calibrationStart) <= calibrationTime) {
					maxAccValue = Math.max(maxAccValue, acc);
				}
				else {
					// Calibration phase ended, set threshold
					threshold = maxAccValue*calibrationFactor;
					Log.i(TAG, "Going into LOCKED state with threshold " + threshold + " from maxAccValue " + maxAccValue);
					appState.setState(ApplicationState.LOCK_STATE_LOCKED);
				}
			}
			if (appState.getState() == ApplicationState.LOCK_STATE_LOCKED) {
				if (acc > threshold) {
					Log.i(TAG, "Threshold passed, strting unlock activity");
					// Threshold passed, start unlock activity
					Intent unlockIntent = new Intent(getBaseContext(), UnlockActivity.class);
					unlockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					getApplication().startActivity(unlockIntent);
					// Release listener, wakelock and stop service
					sensorManager.unregisterListener(this);
					wakeLock.release();
					Log.i(TAG, "Stopping LockedService");
					stopSelf();
				}
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
