package com.watchdog.bayesian;

import java.util.List;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

public class Wifi {
	
	private static Wifi instance;
	private Context context;
	private WifiManager wifi;
	
	private Wifi(Context c) {
		wifi = (WifiManager) c.getSystemService(Context.WIFI_SERVICE);
		context = c;
	}
	
	public static Wifi getInstance(Context c) {
		if (instance == null) {
			instance = new Wifi(c);
		}
		return instance;
	}
	
	public void scan() {
		wifi.startScan();
	}
	
	public List<ScanResult> getRssiValues() {
		return wifi.getScanResults();
	}

}
