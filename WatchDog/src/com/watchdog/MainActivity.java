package com.watchdog;

import com.watchdog.ApplicationState.ConnectedDevice;
import com.watchdog.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	// Controls
	private Button btnLock;
	private TextView lblState;
	private Button btnLoc;
	private Button btnStopServices;
	private TextView lblDevices;
	private Button btnStartWatch;
	
	// Receivers
	BroadcastReceiver stateChangedReceiver;
	BroadcastReceiver lockedChangedReceiver;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		// Start Watch service
		Intent watchIntent = new Intent(MainActivity.this, WatchService.class);
		startService(watchIntent);
		
		
		btnLock = (Button) findViewById(R.id.btnLock);
		btnLock.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent lockIntent = new Intent(MainActivity.this, LockedService.class);
				startService(lockIntent);
			}
		});
		
		btnLoc = (Button) findViewById(R.id.btnLoc);
		btnLoc.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent locIntent = new Intent(MainActivity.this, LocalizationActivity.class);
				startActivity(locIntent);
			}
		});
		
		btnStopServices = (Button) findViewById(R.id.btnStopServices);
		btnStopServices.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent lockIntent = new Intent(MainActivity.this, LockedService.class);
				stopService(lockIntent);
				
				Intent watchIntent = new Intent(MainActivity.this, WatchService.class);
				stopService(watchIntent);
			}
		});
		
		btnStartWatch = (Button) findViewById(R.id.btnStartWatch);
		btnStartWatch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent watchIntent = new Intent(MainActivity.this, WatchService.class);
				startService(watchIntent);
			}
		});
		
		lblState = (TextView) findViewById(R.id.lblState);
		lblDevices = (TextView) findViewById(R.id.lblLockedDevices);
		
		stateChangedReceiver = new BroadcastReceiver() {
	        @Override
	        public void onReceive(Context context, Intent intent) {
	            updateState();
	        }
	    };
	    
	    lockedChangedReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				updateState();
			}
	    };
	}
	
	private void updateState() {
		String lbl = "Locked devices:\n";
		for (ConnectedDevice d : ApplicationState.getInstance(getApplicationContext()).getLockedDevices().values()) {
			lbl += d.name + " (" + (System.currentTimeMillis()-d.lastSeen) + " ms)\n";
		}
		lblDevices.setText(lbl);
		
		switch (ApplicationState.getInstance(getApplicationContext()).getState()) {
		case ApplicationState.LOCK_STATE_UNLOCKED:
			lblState.setText(getString(R.string.lbl_state_unlocked));
			break;
		case ApplicationState.LOCK_STATE_LOCKING:
			lblState.setText(getString(R.string.lbl_state_locking));
			break;
		case ApplicationState.LOCK_STATE_CALIBRATING:
			lblState.setText(getString(R.string.lbl_state_calibrating));
			break;
		case ApplicationState.LOCK_STATE_LOCKED:
			lblState.setText(getString(R.string.lbl_state_locked));
			break;

		default:
			break;
		}
	}
	
	@Override
	protected void onStart() {
	    super.onStart();
	    LocalBroadcastManager.getInstance(this).registerReceiver(stateChangedReceiver, new IntentFilter(ApplicationState.BROADCAST_LOCK_STATE_CHANGED));
	    LocalBroadcastManager.getInstance(this).registerReceiver(lockedChangedReceiver, new IntentFilter(ApplicationState.BROADCAST_LOCKED_DEVICES_CHANGED));
	    updateState();
	}

	@Override
	protected void onStop() {
	    LocalBroadcastManager.getInstance(this).unregisterReceiver(stateChangedReceiver);
	    LocalBroadcastManager.getInstance(this).unregisterReceiver(lockedChangedReceiver);
	    super.onStop();
	}

}
