package com.watchdog;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

public class ApplicationState {

	private Context context;
	private static ApplicationState instance;
	
	private LocalBroadcastManager broadcaster;
	
	private ApplicationState(Context c) {
		context = c;
		broadcaster = LocalBroadcastManager.getInstance(c);
	}
	
	public static ApplicationState getInstance(Context c) {
		if (instance == null) {
			instance = new ApplicationState(c);
		}
		return instance;
	}
	
	
	/**
	 * LOCK STATE
	 */
	// Unlocked state, default
	public static final int LOCK_STATE_UNLOCKED = 0;
	// Locking state, started when user clicks 'lock' and is in the process of placing the phone on a surface
	public static final int LOCK_STATE_LOCKING = 1;
	// Calibrating state, started when phone is placed on surface and the accelerometer is being calibrated
	public static final int LOCK_STATE_CALIBRATING = 2;
	// Locked mode, started when the phone is calibrated and is in lock mode
	public static final int LOCK_STATE_LOCKED = 3;
	
	public static final String BROADCAST_LOCK_STATE_CHANGED = "com.watchdog.broadcast.LOCK_STATE_CHANGED";
	
	private int state = LOCK_STATE_UNLOCKED;
	
	public int getState() {
		return state;
	}
	
	public void setState(int s) {
		state = s;
		Intent stateChangedIntent = new Intent(BROADCAST_LOCK_STATE_CHANGED);
	    broadcaster.sendBroadcast(stateChangedIntent);
	}
	
}
