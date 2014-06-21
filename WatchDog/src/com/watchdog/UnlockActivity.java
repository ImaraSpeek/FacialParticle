package com.watchdog;

import com.watchdog.R;
import com.watchdog.pubnub.PubNub;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class UnlockActivity extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_unlock);
		
		// Unlock the device, this should be replaced with authorization
		ApplicationState.getInstance(getApplicationContext()).setState(ApplicationState.LOCK_STATE_UNLOCKED);
		BluetoothAdapter a = BluetoothAdapter.getDefaultAdapter();
		PubNub.getInstance().sendMessage("UNLOCK@" + a.getAddress());
		Intent lockIntent = new Intent(UnlockActivity.this, LockedService.class);
		stopService(lockIntent);
		
		Button btnNo = (Button) findViewById(R.id.btnNot);
		btnNo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(UnlockActivity.this, StolenActivity.class);
				BluetoothAdapter a = BluetoothAdapter.getDefaultAdapter();
				i.putExtra("name", a.getName() + " (" + a.getAddress() + ")");
				startActivity(i);
			}
		});
		
	}

}
