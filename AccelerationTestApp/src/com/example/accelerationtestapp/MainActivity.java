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

public class MainActivity extends Activity implements SensorEventListener {
	
	private SensorManager sensorManager;
	private Sensor accelerometer;
	private boolean measuring = false;
	private ArrayList<String[]> measurements = null;
	
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
			String[] m = new String[4];
			m[0] = Long.toString(System.currentTimeMillis());
			m[1] = Float.toString(event.values[0]);
			m[2] = Float.toString(event.values[1]);
			m[3] = Float.toString(event.values[2]);
			measurements.add(m);
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
