
public class TrainingData {
	
	public static final int noLocations = 2;
	
	public final DataPoint[] data = {
			/*
			 * Data for training point 0
			 */
			new DataPoint(0, "10:fe:ed:9f:58:a2", "TP-LINK_9F58A2", -91, 0.005), // dev=0
			new DataPoint(0, "00:0c:f6:4c:0a:f6", "Sitecom4C0AF6", -86, 0.005), // dev=0
			new DataPoint(0, "b8:62:1f:50:f9:46", "StLucia", -89.2308, 1.7867),
			new DataPoint(0, "00:25:9c:4d:02:50", "utrecht3515-2", -78.5789, 8.7325),
			new DataPoint(0, "ba:8d:12:62:61:46", "Online gast", -86.9667, 1.6501),
			new DataPoint(0, "72:18:8b:9c:32:dc", "Ziggo", -86.2963, 1.6828),
			new DataPoint(0, "fc:c8:97:80:68:c4", "H220N8068C4", -85.9091, 2.4271),
			new DataPoint(0, "00:1e:e5:7a:24:47", "De Jongh Draadloos", -87.7273, 0.90453),
			new DataPoint(0, "b8:8d:12:62:61:45", "Online", -85.9444, 1.5519),
			new DataPoint(0, "ea:40:f2:94:28:dc", "Ziggo", -81.122, 4.5836),
			new DataPoint(0, "9c:c7:a6:47:c8:12", "FRITZ!Box Fon WLAN 7360", -85.55, 1.7006),
			new DataPoint(0, "84:9c:a6:2a:c3:c0", "VGV75192AC3C0", -87, 0.005), // dev=0
			new DataPoint(0, "00:0c:f6:3b:9a:04", "Sitecom", -84.6667, 1.2309),
			new DataPoint(0, "00:25:86:c6:0d:04", "tom hart", -90.75, 1.4824),
			new DataPoint(0, "bc:05:43:f2:f7:0f", "FRITZ!Box Fon WLAN 7340", -88.0588, 1.5996),
			new DataPoint(0, "82:b6:86:85:61:a8", "HG655D-8561AB", -88.6667, 2.5364),
			new DataPoint(0, "64:d1:a3:20:4f:17", "HOOGHWINKELNEW_EXT", -81.0909, 1.7003),
			new DataPoint(0, "c6:25:06:5a:ed:46", "FRITZ!Box Guest Access", -84.7111, 2.4553),
			new DataPoint(0, "50:7e:5d:6a:79:78", "VGV75196A7979", -88.5556, 1.8782),
			new DataPoint(0, "90:72:40:14:54:58", "Wi-Fi-netwerk van R. Kaasenbrood", -62.6596, 6.5451),
			new DataPoint(0, "00:18:f6:ec:07:28", "CORRYHENK", -88.0789, 2.6952),
			new DataPoint(0, "c0:25:06:5a:ed:46", "FRITZ!Box Fon WLAN 7360", -84.1522, 2.431),
			new DataPoint(0, "7a:cb:a8:62:76:bc", "Mr-Brown", -86.9574, 2.904),
			new DataPoint(0, "fc:f5:28:bd:5a:b4", "Zy_private_CEXC", -88, 0),
			new DataPoint(0, "5c:96:9d:66:3e:31", "Mineel", -84.9722, 3.6213),
			new DataPoint(0, "fc:c8:97:8a:74:86", "Gijsbrecht16", -90, 0.005), // dev=0
			new DataPoint(0, "00:14:7f:72:a7:46", "SpeedTouch8EC007", -89, 0.005), // dev=0
			new DataPoint(0, "10:0d:7f:78:b5:fa", "VGV7519A0F78B_EXT", -89, 0.005), // dev=0
			new DataPoint(0, "4c:ac:0a:16:ea:58", "asmb", -87.75, 1.165),
			new DataPoint(0, "70:18:8b:9c:32:db", "Ziggo60CCB", -85.0303, 1.4467),
			new DataPoint(0, "90:72:40:14:54:59", "Wi-Fi-netwerk van R. Kaasenbrood", -79.5745, 7.1407),
			new DataPoint(0, "00:23:54:85:fd:ca", "Tele2modem-internet", -84.9565, 3.6329),
			new DataPoint(0, "fc:f5:28:1d:7a:2c", "LG-IK", -88, 2.0755),
			new DataPoint(0, "00:10:c6:39:97:13", "", -90.2105, 0.5353),
			new DataPoint(0, "84:9c:a6:63:e0:86", "utrecht3515", -73.4255, 2.5),
			new DataPoint(0, "e4:ce:8f:6c:f9:35", "Mineel", -81.4, 0.54772),
			new DataPoint(0, "c4:3d:c7:91:0d:6e", "ARJANHUIJZER-PC_Network-2.4G", -83.9787, 2.3819),
			new DataPoint(0, "e8:40:f2:94:28:db", "Duvel", -80.2979, 3.7003),
			new DataPoint(0, "00:0c:f6:a1:83:4c", "SitecomA1834C", -80.4286, 2.747),
			new DataPoint(0, "00:18:f6:67:52:c6", "SpeedTouch367917", -63.8936, 4.2027),
			new DataPoint(0, "08:96:d7:30:55:10", "Biomineral", -88.5714, 1.7415),
			new DataPoint(0, "e0:91:f5:57:be:9e", "starthere", -86.2683, 3.6402),
			new DataPoint(0, "3e:77:e6:32:e3:23", "Ziggo", -89, 0.005), // dev=0
			new DataPoint(0, "c4:3d:c7:91:0d:70", "ARJANHUIJZER-PC_Network", -96.9545, 1.362),
			new DataPoint(0, "bc:05:43:6f:ca:ae", "FRITZ!Box Fon WLAN 7340", -83.2222, 3.0785),
			new DataPoint(0, "fc:f5:28:92:d1:cc", "Zy_private_E4XHWM", -86.475, 2.5418),
			new DataPoint(0, "00:0c:f6:a0:b8:4c", "Westdijk Web", -85.9091, 1.0445),
			new DataPoint(0, "84:9c:a6:b4:da:52", "Vlinder", -81.9149, 3.3481),
			new DataPoint(0, "98:fc:11:6f:06:8a", "chmwidor", -87.5, 0.92582),
			new DataPoint(0, "00:11:f5:da:75:1b", "SpeedTouch0D6220", -90, 0.005), // dev=0
			
			/*
			 * Data for training point 1
			 */
			new DataPoint(1, "00:14:7f:72:a7:46", "SpeedTouch8EC007", -93, 0.0005), // dev=0
			new DataPoint(1, "00:0c:f6:4c:0a:f6", "Sitecom4C0AF6", -83.5385, 0.87706),
			new DataPoint(1, "ba:8d:12:62:61:46", "Online gast", -90.35, 1.4244),
			new DataPoint(1, "cc:b2:55:94:57:44", "Dagobert", -82.2857, 1.8898),
			new DataPoint(1, "72:2b:c1:cc:58:e8", "HG655D-CC58EB", -91, 1.0632),
			new DataPoint(1, "00:1d:68:73:d5:28", "SpeedTouch2E9098", -88.6538, 0.48516),
			new DataPoint(1, "72:18:8b:9c:32:dc", "Ziggo", -78.3269, 4.0231),
			new DataPoint(1, "70:18:8b:9c:32:db", "Ziggo60CCB", -78.8868, 4.3086),
			new DataPoint(1, "00:24:8c:2b:ac:cb", "Scheteris", -80.88, 5.461),
			new DataPoint(1, "90:72:40:14:54:59", "Wi-Fi-netwerk van R. Kaasenbrood", -93.65, 1.8612),
			new DataPoint(1, "00:23:54:85:fd:ca", "Tele2modem-internet", -87.4681, 2.6772),
			new DataPoint(1, "b8:8d:12:62:61:45", "Online", -87.6667, 2.9681),
			new DataPoint(1, "00:10:c6:39:97:13", "", -90.7742, 2.3052),
			new DataPoint(1, "ea:40:f2:94:28:dc", "Ziggo", -61.434, 5.3476),
			new DataPoint(1, "84:9c:a6:63:e0:86", "utrecht3515", -53.9245, 6.9444),
			new DataPoint(1, "c4:3d:c7:91:0d:6e", "ARJANHUIJZER-PC_Network-2.4G", -91, 0.0005), // dev=0
			new DataPoint(1, "e8:40:f2:94:28:db", "Duvel", -61.1321, 5.0841),
			new DataPoint(1, "00:0c:f6:a1:83:4c", "SitecomA1834C", -82.8, 1.5492),
			new DataPoint(1, "00:18:f6:67:52:c6", "SpeedTouch367917", -73.1321, 4.1419),
			new DataPoint(1, "64:d1:a3:20:4f:17", "HOOGHWINKELNEW_EXT", -75.0189, 3.2787),
			new DataPoint(1, "c6:25:06:5a:ed:46", "FRITZ!Box Guest Access", -81.1509, 4.5674),
			new DataPoint(1, "b4:82:fe:93:e5:24", "Thomson93E524", -92, 0.0005), // dev=0
			new DataPoint(1, "e0:91:f5:57:be:9e", "starthere", -86.8649, 3.6298),
			new DataPoint(1, "50:7e:5d:6a:79:78", "VGV75196A7979", -80.2264, 3.8762),
			new DataPoint(1, "90:72:40:14:54:58", "Wi-Fi-netwerk van R. Kaasenbrood", -72.8679, 4.1835),
			new DataPoint(1, "00:18:f6:ec:07:28", "CORRYHENK", -91.25, 0.75378),
			new DataPoint(1, "c0:25:06:5a:ed:46", "FRITZ!Box Fon WLAN 7360", -80.02, 3.8992),
			new DataPoint(1, "bc:05:43:6f:ca:ae", "FRITZ!Box Fon WLAN 7340", -87, 0.005), // dev=0
			new DataPoint(1, "fc:f5:28:92:d1:cc", "Zy_private_E4XHWM", -81.434, 3.0097),
			new DataPoint(1, "7a:cb:a8:62:76:bc", "Mr-Brown", -91, 0.005), // dev=0
			new DataPoint(1, "84:9c:a6:b4:da:52", "Vlinder", -82.75, 5.3463),
			new DataPoint(1, "98:fc:11:6f:06:8a", "chmwidor", -88.6, 2.0105),
			new DataPoint(1, "00:11:f5:da:75:1b", "SpeedTouch0D6220", -90, 0.005), // dev=0
			new DataPoint(1, "5c:96:9d:66:3e:31", "Mineel", -86.7727, 2.4286),
	};

	
	public class DataPoint {
		public int locid;
		public String bssid;
		public String ssid;
		public double mean;
		public double deviation;
		
		public DataPoint(int locid,String bssid, String ssid, double mean, double deviation) {
			this.locid = locid;
			this.bssid = bssid;
			this.ssid = ssid;
			this.mean = mean;
			this.deviation = deviation;
		}
	}
}

