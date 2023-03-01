package priism_art.model.penetration;

import java.util.Collections;
import java.util.List;

import priism_art.utils.CSVUtils;

public class PenetrationClassifier {
	public static final String CONFIG = "C:\\Temp\\Priism\\PenetrationClassification.csv";

	List<PenetrationStop> stops;	
	public void loadConfig(String path) {
		stops = CSVUtils.readFromCSV(path, a -> 
					new PenetrationStop(a[0], Double.parseDouble(a[1])));
		Collections.sort(stops);
	}
	
	public String classify(double value) {
		for (PenetrationStop penetrationStop : stops) {
			if (penetrationStop.getValue() >= value) {
				return penetrationStop.getName();
			}
		}
		return null;
	}
	
	public static void main(String[] args) {
		PenetrationClassifier pc = new PenetrationClassifier();
		pc.loadConfig(CONFIG);
		System.out.println(pc);
		System.out.println(pc.classify(0.04));
		System.out.println(pc.classify(0.06));
		System.out.println(pc.classify(0.06));
		System.out.println(pc.classify(0.5));
		System.out.println(pc.classify(0.8));
		System.out.println(pc.classify(0.7));
		System.out.println(pc.classify(1));
	}
	
	
	
}
