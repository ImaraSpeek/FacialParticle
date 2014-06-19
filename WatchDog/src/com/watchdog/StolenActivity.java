package com.watchdog;

import com.watchdog.R;
import com.watchdog.pubnub.PubNub;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.widget.TextView;

public class StolenActivity extends Activity {
	
	// Controls
	private TextView lblStolen;
	private String deviceStolen;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle b = getIntent().getExtras();
		deviceStolen = b.getString("name");
		
		setContentView(R.layout.activity_stolen);
		
		lblStolen = (TextView) findViewById(R.id.lblStolen);
		lblStolen.setText("Device " + deviceStolen + " is being stolen!!");
		BluetoothAdapter a = BluetoothAdapter.getDefaultAdapter();
		PubNub.getInstance().sendMessage("STOLEN!@" + a.getAddress() + "@" + a.getName());
	}

}
