package com.watchdog;

import android.content.Context;

public class ApplicationState {

	private Context context;
	private static ApplicationState instance;
	
	private ApplicationState(Context c) {
		context = c;
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
	public final int LOCK_STATE_UNLOCKED = 0;
	// Locking state, started when user clicks 'lock' and is in the process of placing the phone on a surface
	public final int LOCK_STATE_LOCKING = 1;
	// Calibrating state, started when phone is placed on surface and the accelerometer is being calibrated
	public final int LOCK_STATE_CALIBRATING = 2;
	// Locked mode, started when the phone is calibrated and is in lock mode
	public final int LOCK_STATE_LOCKED = 3;
	
	private int state = LOCK_STATE_UNLOCKED;
	
	public int getState() {
		return state;
	}
	
	public void setState(int s) {
		state = s;
	}
	
}
