package com.watchdog;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.watchdog.bayesian.APDetails;
import com.watchdog.bayesian.FindLocation;
import com.watchdog.bayesian.Measurement;
import com.watchdog.bayesian.ReadMeasurements;
import com.watchdog.bayesian.Wifi;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LocalizationActivity extends Activity {
	
	private final Context context = this;
	
	// Views
	private Button btnCollectData;
	private Button btnAnalyzeData;
	private Button btnDetermineLoc;
	private TextView txtLocs;
	
	private Wifi wifi;
	private boolean measuring = false;
	private String measurements = "";
	private String measName = "";
	
	private Handler updateHandler = new Handler();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_localization);
		
		btnCollectData = (Button) findViewById(R.id.btnCollectData);
		btnCollectData.setOnClickListener(collectDataListener);
		
		btnAnalyzeData = (Button) findViewById(R.id.btnAnalyzeData);
		btnAnalyzeData.setOnClickListener(analyseDataListener);
		
		btnDetermineLoc = (Button) findViewById(R.id.btnDetermineLoc);
		btnDetermineLoc.setOnClickListener(determineLocListener);
		
		txtLocs = (TextView) findViewById(R.id.txtLocs);
		
		wifi = Wifi.getInstance(getApplicationContext());
		
	}
	
	OnClickListener collectDataListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (measuring) {
				btnCollectData.setText("Collect data");
				measuring = false;
				
				int fileIndex = 0;
				File root = Environment.getExternalStorageDirectory();
				Log.i("WatchDog", root.getAbsolutePath() + "/WatchDog/Measurements/" + measName + ".csv");
				File ms = new File(root, "WatchDog/Measurements/");
				ms.mkdir();
				File f = new File(ms, measName + ".csv");
				while (f.exists()) {
					f = new File(ms, measName + fileIndex + ".csv");
					fileIndex++;
				}
				
				try {
					f.createNewFile();
					Log.i("WatchDog", "Writing measurement to " + f.getAbsolutePath());
					FileOutputStream w = new FileOutputStream(f);
					PrintWriter pw = new PrintWriter(w);
					pw.print(measurements);
					pw.flush();
					pw.close();
					w.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				measurements = "";
			} else {
				// Starting measurement, first ask for name
				LayoutInflater li = LayoutInflater.from(context);
				View dialogView = li.inflate(R.layout.dialog_locname, null);
 
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setView(dialogView);
 
				final EditText locName = (EditText) dialogView.findViewById(R.id.txtLocName);
 
				builder
					.setCancelable(false)
					.setPositiveButton(android.R.string.ok,
					  new DialogInterface.OnClickListener() {
					    public void onClick(DialogInterface dialog,int id) {
							measName = locName.getText().toString();
							if (measName.length() == 0) {
								measName = "measurement";
							}
					    }
					  });
 
				AlertDialog alertDialog = builder.create();
 				alertDialog.show();
				
				
				btnCollectData.setText("Stop measuring");
				measuring = true;
			}
		}
	};
	
	OnClickListener analyseDataListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// Create the JSON arr
			JSONArray data = new JSONArray();
			
			File root = Environment.getExternalStorageDirectory();
			File ms = new File(root, "WatchDog/Measurements/");
			for (File f : ms.listFiles()) {
				Log.i("WatchDog", "Reading file " + f.getAbsolutePath());
				String locName = f.getName();
				List<Measurement> measurements = ReadMeasurements.read(f);
				Map<String, List<Measurement>> orderedMeasurements = ReadMeasurements.orderByBSSID(measurements);
				List<APDetails> apDetails = ReadMeasurements.getBSSIDProperties(orderedMeasurements);
				
				for (APDetails d : apDetails) {
					JSONObject obj = new JSONObject();
					try {
						obj.put("location", locName);
						obj.put("bssid", d.bssid);
						obj.put("ssid", d.ssid);
						obj.put("mean", d.mean);
						obj.put("deviation", d.deviation);
						data.put(obj);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
			
			File dir = new File(root, "WatchDog/");
			dir.mkdir();
			File f = new File(dir, "trainingdata.json");
						
			try {
				f.delete();
				f.createNewFile();
				Log.i("WatchDog", "Writing training data to " + f.getAbsolutePath());
				FileOutputStream w = new FileOutputStream(f);
				PrintWriter pw = new PrintWriter(w);
				pw.print(data.toString(4));
				pw.flush();
				pw.close();
				w.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	};
	
	OnClickListener determineLocListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			wifi.scan();
			
			// Get training data
			File root = Environment.getExternalStorageDirectory();
			File dir = new File(root, "WatchDog/");
			File f = new File(dir, "trainingdata.json");
			BufferedReader br = null;
			String file = "";
			String line = "";
			try {
				br = new BufferedReader(new FileReader(f));
				while ((line = br.readLine()) != null) {
					file += line;
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			JSONArray data = null;
			List<String> locations = null;
			try {
				data = new JSONArray(file);
				locations = FindLocation.getLocations(data);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			List<ScanResult> scanResults = wifi.getRssiValues();
			
			double[] prior = new double[locations.size()];
			for (int i=0; i<prior.length; i++) {
				prior[i] = 1.0/prior.length;
			}
			
			double[] posterior = null;
			try {
				posterior = FindLocation.findLocation(scanResults, 5, prior, data, locations);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			String loc = "";
			for (int i=0; i<posterior.length; i++) {
				loc += locations.get(i) + ": " + posterior[i] + "\n";
				Log.i("WatchDog", i+" "+posterior[i]);
			}
			txtLocs.setText(loc);
			
			
		}
	};
	
	Runnable measUpdater = new Runnable() {
	    @Override
	    public void run() {
	    	if (measuring) {
	    		List<ScanResult> res = wifi.getRssiValues();
	    		for (ScanResult r : res) {
	    			measurements += System.currentTimeMillis() + ",\"" + r.SSID + "\",\"" + r.BSSID + "\"," + r.level + "\n";
	    		}
	    	}
	    	Wifi.getInstance(getApplicationContext()).scan();
	    	
	        updateHandler.postDelayed(this, 1000);
	    }
	};
	
	@Override
	public void onStart() {
		super.onStart();
		
		updateHandler.post(measUpdater);
	}
	
	@Override
	public void onStop() {
		super.onStop();
		updateHandler.removeCallbacks(measUpdater);
	}

}
