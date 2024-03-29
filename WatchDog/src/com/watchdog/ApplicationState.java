package com.watchdog;

import java.util.HashMap;
import java.util.Map;

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
	 * LOCKED DEVICES
	 */
	private Map<String, ConnectedDevice> lockedDevices = new HashMap<String, ConnectedDevice>();
	
	public static final String BROADCAST_LOCKED_DEVICES_CHANGED = "com.watchdog.broadcast.LOCKED_DEVICES_CHANGED";
	
	public void putLockedDevice(String mac, ConnectedDevice d) {
		lockedDevices.put(mac, d);
		Intent stateChangedIntent = new Intent(BROADCAST_LOCKED_DEVICES_CHANGED);
	    broadcaster.sendBroadcast(stateChangedIntent);
	}
	
	public void removeLockedDevice(String mac) {
		lockedDevices.remove(mac);
		Intent stateChangedIntent = new Intent(BROADCAST_LOCKED_DEVICES_CHANGED);
	    broadcaster.sendBroadcast(stateChangedIntent);
	}
	
	public boolean containsLockedDevice(String mac) {
		return lockedDevices.containsKey(mac);
	}
	
	public Map<String, ConnectedDevice> getLockedDevices() {
		return lockedDevices;
	}
	
	public String getLockedDeviceName(String mac) {
		return lockedDevices.get(mac).name;
	}
	
	public boolean getLockedDeviceStolen(String mac) {
		return lockedDevices.get(mac).maybeStolen || lockedDevices.get(mac).stolen;
	}
	
	public void setLockedDeviceMaybeStolen(String mac, boolean maybeStolen) {
		lockedDevices.get(mac).maybeStolen = maybeStolen;
	}
	
	public long getLockedDeviceLastSeen(String mac) {
		return lockedDevices.get(mac).lastSeen;
	}
	
	public void setLockedDeviceLastSeen(String mac, long time) {
		lockedDevices.get(mac).lastSeen = time;
		Intent stateChangedIntent = new Intent(BROADCAST_LOCKED_DEVICES_CHANGED);
	    broadcaster.sendBroadcast(stateChangedIntent);
	}
	
	public static class ConnectedDevice {
		public String name;
		public long lastSeen;
		public boolean maybeStolen = false;
		public boolean stolen = false;
		public ConnectedDevice(String name, long lastSeen) {
			this.name = name;
			this.lastSeen = lastSeen;
		}
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
