import java.util.Comparator;
import java.util.List;


public class APDetails {
	public String bssid;
	public String ssid;
	public List<Measurement> measurements;
	public double mean;
	public double count;
	public double sum;
	public double deviation;
}

class APDetailsAverageLevelComparator implements Comparator<APDetails> {
    public int compare(APDetails ap1, APDetails ap2) {
        return (int) Math.signum(ap2.mean - ap1.mean);
    }
}
