package com.watchdog;

import com.watchdog.R;

import android.app.Activity;
import android.os.Bundle;

public class UnlockActivity extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_unlock);
		
		// Unlock the device, this should be replaced with authorization
		ApplicationState.getInstance(getApplicationContext()).setState(ApplicationState.LOCK_STATE_UNLOCKED);
		
	}

}
