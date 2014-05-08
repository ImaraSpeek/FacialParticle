package com.example.accelerationtestapp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements SensorEventListener {
	
	private SensorManager sensorManager;
	private Sensor accelerometer;
	private boolean measuring = false;
	private ArrayList<String[]> measurements = null;
	
	private TextView tenthSec;
	private TextView sec;
	private TextView tenSec;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		// Init accelerometer
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		
		// Create button click handler
		Button btnMain = (Button) findViewById(R.id.button);
		btnMain.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Button btn = (Button) v;
				if (btn.getText().equals("Start")) {
					startMeasurement();
					btn.setText("Stop");
				} else {
					stopMeasurement();
					btn.setText("Start");
				}
			}
		});
		
		tenthSec = (TextView) findViewById(R.id.textView2);
		sec = (TextView) findViewById(R.id.textView3);
		tenSec = (TextView) findViewById(R.id.textView4);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		sensorManager.unregisterListener(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER
				&& measuring) {
			String[] m = new String[8];
			m[0] = Long.toString(System.currentTimeMillis());
			m[1] = Float.toString(event.values[0]);
			m[2] = Float.toString(event.values[1]);
			m[3] = Float.toString(event.values[2]);
			
			// Filtered values
			int s = measurements.size();
			float a = 0.5f;
			if (s > 0) {
				m[4] = Float.toString(event.values[0] - (Float.parseFloat(measurements.get(s-1)[1])*(1-a) + event.values[0]*a));
				m[5] = Float.toString(event.values[1] - (Float.parseFloat(measurements.get(s-1)[2])*(1-a) + event.values[1]*a));
				m[6] = Float.toString(event.values[2] - (Float.parseFloat(measurements.get(s-1)[3])*(1-a) + event.values[2]*a));
				m[7] = Double.toString(Math.sqrt(Math.pow(Float.parseFloat(m[4]),2) + Math.pow(Float.parseFloat(m[5]),2) + Math.pow(Float.parseFloat(m[6]),2)));
			} else {
				m[4] = "0";
				m[5] = "0";
				m[6] = "0";
				m[7] = "0";
			}
			measurements.add(m);
			
			int i;
			double maxTenthSec = 0;
			double maxSec = 0;
			double maxTenSec = 0;
			for (i = s-1; i>=0; i--) {
				if (System.currentTimeMillis() - Long.parseLong(measurements.get(i)[0]) < 100) {
					maxTenthSec = Math.max(maxTenthSec, Double.parseDouble(measurements.get(i)[7]));
				}
				if (System.currentTimeMillis() - Long.parseLong(measurements.get(i)[0]) < 1000) {
					maxSec = Math.max(maxSec, Double.parseDouble(measurements.get(i)[7]));
				}
				if (System.currentTimeMillis() - Long.parseLong(measurements.get(i)[0]) < 5000) {
					maxTenSec = Math.max(maxTenSec, Double.parseDouble(measurements.get(i)[7]));
				}
				if (System.currentTimeMillis() - Long.parseLong(measurements.get(i)[0]) >= 5000) {
					break;
				}
			}
			
			tenthSec.setText("Largest acceleration in last 0.1 seconds: " + ((double)Math.round(maxTenthSec*100) / 100) + " m/s2");
			sec.setText("Largest acceleration in last 1 second: " + ((double)Math.round(maxSec*100) / 100) + " m/s2");
			tenSec.setText("Largest acceleration in last 5 seconds: " + ((double)Math.round(maxTenSec*100) / 100) + " m/s2");
			//Log.i("MOVE", Double.parseDouble(measurements.get(i)[7]) + " m/s2");
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) { }
	
	public void startMeasurement() {
		measuring = true;
		measurements = new ArrayList<String[]>();
	}
	
	public void stopMeasurement() {
		measuring = false;
		
		int fileIndex = 0;
		File root = Environment.getExternalStorageDirectory();
		File f = new File(root, "measurementAcc.csv");
		while (f.exists()) {
			f = new File(root, "measurementAcc" + fileIndex + ".csv");
			fileIndex++;
		}
		
		try {
			f.createNewFile();
			Log.i("TEST APP", "Writing measurement to " + f.getAbsolutePath());
			FileOutputStream w = new FileOutputStream(f);
			PrintWriter pw = new PrintWriter(w);
			for (int i=0; i<measurements.size(); i++) {
				String[] m = measurements.get(i);
				String txt = m[0] + "," + m[1] + "," + m[2] + "," + m[3];
				pw.println(txt);
			}
			pw.flush();
			pw.close();
			w.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		measurements = null;
	}
	
}
