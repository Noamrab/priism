package priism_art;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import priism_art.model.penetration.AggregatedStatistics;
import priism_art.model.penetration.PenetrationStatistics;
import priism_art.utils.CSVUtils;


public class launcher {
	public static void main(String[] args) {
		String basePath = "C:\\Temp\\Priism\\new batch\\";
		var files = Arrays.asList("GM65-21-D1.csv","GM65-21-D2.csv","GM65-21-O1.csv","GM65-21-O2.csv","GM65-22-D1.csv","GM65-22-D2.csv","GM65-22-D3.csv","GM65-22-O1.csv");
		List<PenetrationStatistics> pStats = new LinkedList<>();
		for (String f : files) {
			Test t = new Test(basePath + f, Test.DISC_SIZE_PIXELS);
			t.perform();
			pStats.addAll(t.pStats);
		}
		System.out.println("Done with calculations for files. Starting with aggregated calculations");
		Map<String, List<PenetrationStatistics>> pMap = pStats.stream().collect(Collectors.groupingBy(p -> p.getTargetCellTypeName()));
		for (Entry<String, List<PenetrationStatistics>> entry : pMap.entrySet()) {
			AggregatedStatistics ag = new AggregatedStatistics(entry.getValue());
			ag.calculateStats();
			List<String[]> data = ag.toCSV();
			String[] headers = ag.getHeaders();
			CSVUtils.writeDataLineByLine(basePath + entry.getKey() + "_aggregated.csv", headers, data);
		}

	}

}
