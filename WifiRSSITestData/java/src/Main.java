import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.math3.fitting.GaussianCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;



public class Main {

	public static void main(String[] args) {
		
		
		String[] files = {
				//"C:\\Users\\basva_000\\Documents\\GitHub\\IN4254\\WifiRSSITestData\\loc1.csv"
				//,
				"C:\\Users\\basva_000\\Documents\\GitHub\\IN4254\\WifiRSSITestData\\loc2.csv"
		};
		
		for (String f : files) {
			List<Measurement> measurements = ReadMeasurements.read(f);
			Map<String, List<Measurement>> orderedMeasurements = ReadMeasurements.orderByBSSID(measurements);
			List<APDetails> apDetails = ReadMeasurements.getBSSIDProperties(orderedMeasurements);
			// Sort by average level
			Collections.sort(apDetails, new APDetailsAverageLevelComparator());
			for (APDetails d : apDetails) {
				System.out.println(d.bssid + "(" + d.ssid + "): mean: " + d.mean + ", deviation: " + d.deviation);
			}
		}
		
		/*
		String[] files = {
				//"C:\\Users\\basva_000\\Documents\\GitHub\\IN4254\\WifiRSSITestData\\loc1.csv"
				//,
				"C:\\Users\\basva_000\\Documents\\GitHub\\IN4254\\WifiRSSITestData\\loc2.csv"
		};
		
		for (String f : files) {
			List<Measurement> measurements = ReadMeasurements.read(f);
			Map<String, List<Measurement>> orderedMeasurements = ReadMeasurements.orderByBSSID(measurements);
			
			for (Entry<String, List<Measurement>> e : orderedMeasurements.entrySet()) {
				//System.out.println(e.getKey() + "(" + e.getValue().get(0).ssid + ")");
				System.out.print("data = [");
				for (Measurement m : e.getValue()) {
					System.out.print(m.level + " ");
				}
				System.out.println("];");
				System.out.println("[mean, dev] = normfit(data);");
				System.out.println("disp(['new DataPoint(1, " + e.getValue().get(0).bssid + ", " + e.getValue().get(0).ssid + ", ' num2str(mean) ', ' num2str(dev) '),'])");
				System.out.println("");
			}
		}
			/*
			
			List<APDetails> apDetails = ReadMeasurements.getBSSIDProperties(orderedMeasurements, false);
			// Sort by average level
			Collections.sort(apDetails, new APDetailsAverageLevelComparator());
			
			APDetails ap = apDetails.get(0);
			
			GaussianCurveFitter fitter = GaussianCurveFitter.create();
			WeightedObservedPoints obs = new WeightedObservedPoints();
			
			
			System.out.println(ap.measurements.get(0).ssid);
			TreeMap<Integer, Double> pmf = ReadMeasurements.getPMF(apDetails.get(0));
			for (Entry<Integer, Double> e : pmf.entrySet()) {
				System.out.print(e.getKey() + "," + e.getValue() + " ");
				obs.add(e.getKey(), e.getValue());
			}
			System.out.println();
			
			for (int i = 0; i < ap.measurements.size(); i++) {
				System.out.print(ap.measurements.get(i).level + " ");
				obs.add(ap.measurements.get(i).level,1);
			}
			
			
			
			System.out.println("");
			
			System.out.print("Result: ");
			double[] fit = fitter.fit(obs.toList());
			for (int j = 0; j < fit.length; j++) {
				System.out.print(fit[j] + " ");
			}
			
			System.out.println("");
			
			//System.out.println(APDetails);
			/*int max = 5;
			for (int i = 0; i < max; i++) {
				System.out.println(apDetails.get(i).ssid + "," + apDetails.get(i).bssid + "," + apDetails.get(i).averageLevel);
				TreeMap<Integer, Double> pmf = ReadMeasurements.getPMF(apDetails.get(i));
				for (Entry<Integer, Double> e : pmf.entrySet()) {
					System.out.println(e.getKey() + "," + e.getValue());
				}
				System.out.println("");
			}
		}*/
		
		
		/*
		String f = "C:\\Users\\basva_000\\Documents\\GitHub\\IN4254\\WifiRSSITestData\\data2.csv";
		List<Measurement> measurements = ReadMeasurements.read(f);
		Map<String, List<Measurement>> orderedMeasurements = ReadMeasurements.orderByBSSID(measurements);
		List<APDetails> apDetails = ReadMeasurements.getBSSIDProperties(orderedMeasurements);
		// Sort by average level
		Collections.sort(apDetails, new APDetailsAverageLevelComparator());
		
		int max = 5;
		int it = 0;
		
		double[] posterior = new double[TrainingData.noLocations];
		for (int i=0; i<posterior.length; i++) {
			posterior[i] = 1.0/posterior.length;
		}
		
		TrainingData data = new TrainingData();
		
		for (APDetails d : apDetails) {
			if (it==max) break;
			it++;
			
			System.out.println(d.bssid + "(" + d.ssid + "): " + d.mean);
			
			double sum = 0;
			for (int i=0; i<posterior.length; i++) {
				double mean = 0;
				double dev = 0;
				for (int j=0; i<data.data.length; j++) {
					if (data.data[j].bssid.equals(d.bssid.replace("\"", "")) && data.data[j].locid == i) {
						mean = data.data[j].mean;
						dev = data.data[j].deviation;
						break;
					}
				}
				
				System.out.println(posterior[i] + " multiplied by " + normal(d.mean, mean, dev));
				posterior[i] = posterior[i] * normal(d.mean, mean, dev);
				sum += posterior[i];
			}
			// Normalize
			for (int i=0; i<posterior.length; i++) {
				posterior[i] = posterior[i] / sum;
			}
			
			for (int i=0; i<posterior.length; i++) {
				System.out.println("Loc " + i + ": " + posterior[i]);
			}
		}
		*/
		
		
		
	}
	
	public static double normal(double x, double mean, double dev) {
		return (1.0 / (dev * Math.sqrt(2.0*Math.PI))) * Math.exp((-Math.pow(x-mean,2))/(2*Math.pow(dev,2)));
	}

}