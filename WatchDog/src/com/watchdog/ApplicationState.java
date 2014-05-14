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
	
}
