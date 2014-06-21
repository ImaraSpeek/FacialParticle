

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;


public class ReadMeasurements {
	
	public static List<Measurement> read(String pathToCSV) {
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		
		List<Measurement> measurements = new ArrayList<Measurement>();
	 
		try {
	 
			br = new BufferedReader(new FileReader(pathToCSV));
			while ((line = br.readLine()) != null) {
	 
			        // use comma as separator
				String[] split = line.split(cvsSplitBy);
	 
				Measurement m = new Measurement();
				m.timestamp = Long.parseLong(split[0]);
				m.ssid = split[1];
				m.bssid = split[2];
				m.level = Integer.parseInt(split[3]);
				measurements.add(m);
	 
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
		return measurements;
	}
	
	public static Map<String, List<Measurement>> orderByBSSID(List<Measurement> measurements) {
		Map<String, List<Measurement>> ret = new HashMap<String, List<Measurement>>();
		for (Measurement m : measurements) {
			if (ret.containsKey(m.bssid)) {
				List<Measurement> ms = ret.get(m.bssid);
				ms.add(m);
				ret.put(m.bssid, ms);
			}
			else {
				List<Measurement> ms = new ArrayList<Measurement>();
				ms.add(m);
				ret.put(m.bssid, ms);
			}
		}
		return ret;
	}
	
	public static List<APDetails> getBSSIDProperties(Map<String, List<Measurement>> orderedMeasurements) {
		List<APDetails> detailList = new ArrayList<APDetails>();
		for (Entry<String, List<Measurement>> e : orderedMeasurements.entrySet()) {
			String bssid = e.getKey();
			List<Measurement> measurements = e.getValue();
			
			double sum = 0;
			double count = 0;
			for (Measurement m : measurements) {
				sum += m.level;
				count ++;
			}
			double avg = sum/count;
			
			double sumdiff = 0;
			for (Measurement m : measurements) {
				double diff = m.level - avg;
				sumdiff += Math.pow(diff, 2);
			}
			double variance = sumdiff / (count-1);
			
			if (measurements.get(0).ssid.equals("\"Vlinder\"")) {
				System.out.println(measurements.get(0).ssid + ":avg=" + avg + ",sumdiff=" + sumdiff + ",var="+variance);
			}
			
			APDetails details = new APDetails();
			details.measurements = measurements;
			details.ssid = measurements.get(0).ssid;
			details.bssid = bssid;
			details.mean = avg;
			details.count = count;
			details.sum = sum;
			details.deviation = Math.sqrt(variance);
			detailList.add(details);
		}
		
		
		return detailList;
	}

}
