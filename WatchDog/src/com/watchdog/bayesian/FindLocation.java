package com.watchdog.bayesian;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.wifi.ScanResult;
import android.util.Log;

public class FindLocation {
	
	public static List<ScanResult> orderScanResults(List<ScanResult> results) {
		Collections.sort(results, new Comparator<ScanResult>() {
	        @Override
	        public int compare(ScanResult lhs, ScanResult rhs) {
	            return (lhs.level <rhs.level ? -1 : (lhs.level==rhs.level ? 0 : 1));
	        }
	    });
		return results;
	}
	
	public static List<String> getLocations(JSONArray data) throws JSONException {
		List<String> locs = new ArrayList<String>();
		for (int i = 0; i < data.length(); i++) {
			JSONObject o = data.getJSONObject(i);
			String bssid = o.getString("location");
			if (!locs.contains(bssid)) {
				locs.add(bssid);
			}
		}
		return locs;
	}
	
	public static double[] findLocation(List<ScanResult> scanResults, int noAPs, double[] posterior, JSONArray data, List<String> locations) throws JSONException {
		orderScanResults(scanResults);
		int max = noAPs;
		int it = 0;
		
		for (ScanResult r : scanResults) {
			if (it==max) break;
			it++;
			
			double sum = 0;
			for (int i=0; i<posterior.length; i++) {
				double mean = 0;
				double dev = 0;
				for (int j=0; j<data.length(); j++) {
					if (data.getJSONObject(j).getString("bssid").replace("\"", "").equals(r.BSSID.replace("\"", "")) && data.getJSONObject(j).getString("location").equals(locations.get(i))) {
						mean = data.getJSONObject(j).getDouble("mean");
						dev = data.getJSONObject(j).getDouble("deviation");
						if (dev == 0) {
							dev = 0.005;
						}
						break;
					}
				}
				
				posterior[i] = posterior[i] * normal(r.level, mean, dev);
				sum += posterior[i];
			}
			// Normalize
			for (int i=0; i<posterior.length; i++) {
				posterior[i] = posterior[i] / sum;
			}
			
		}
		return posterior;
	}
	
	public static double normal(double x, double mean, double dev) {
		return (1.0 / (dev * Math.sqrt(2.0*Math.PI))) * Math.exp((-Math.pow(x-mean,2))/(2*Math.pow(dev,2)));
	}

}
