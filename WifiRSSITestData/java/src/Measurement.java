
import java.util.Comparator;


public class Measurement {
	public long timestamp;
	public String ssid;
	public String bssid;
	public int level;
}

class MeasurementLevelComparator implements Comparator<Measurement> {
    public int compare(Measurement ap1, Measurement ap2) {
        return ap1.level - ap2.level;
    }
}