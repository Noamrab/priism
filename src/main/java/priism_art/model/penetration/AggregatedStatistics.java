package priism_art.model.penetration;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import priism_art.model.CellTypeInfo;

public class AggregatedStatistics {

	private static final int CELL_NUM = 4;
	List<PenetrationStatistics> stats;
	Map<CellTypeInfo, Map<String, DescriptiveStatistics[]>> retMap;
	
	
	public AggregatedStatistics(List<PenetrationStatistics> stats) {
		this.stats = stats;
	}

	public String[] getHeaders() {
		List<String> lst = new LinkedList<>();
		for (String string : PenetrationStatistics.getClasses(retMap)) {
			lst.add(string);
			// Create spacing
			for (int i = 0; i < CELL_NUM - 1; i++) {
				lst.add(null);
			}
		}
		lst.add(0, "Name");
		return lst.toArray(new String[lst.size()]);
	}
	
	public List<String[]> toCSV() {
		List<String[]> lst = PenetrationStatistics.toCSV(retMap, (classes, entry) -> {
			int arrSize = classes.size() * CELL_NUM;
			String[] arr = new String[arrSize  + 1];
			for (int i = 0; i < arrSize; i+=CELL_NUM) {
				DescriptiveStatistics[] dArr = entry.getValue().get(classes.get(i / CELL_NUM));
				DescriptiveStatistics stats = dArr[0];
				arr[i + 1] = Double.toString(stats.getMean());
				arr[i + 2] = Double.toString(stats.getStandardDeviation());
				arr[i + 3] = Double.toString(dArr[1].getMean());
				arr[i + 4] = Double.toString(dArr[1].getStandardDeviation());
			}
			return arr;
		});
		if (!lst.isEmpty()) {
			String[] header = new String[lst.get(0).length];
			for (int i = 1; i < header.length; i+=CELL_NUM) {
				header[i] = "Pen Mean";
				header[i + 1] = "Pen Std Dev";
				header[i + 2] = "Density Mean";
				header[i + 3] = "Density Std Dev";
			}
			lst.add(0,header);
		}
		return lst;

	}
	
	public Map<CellTypeInfo, Map<String, DescriptiveStatistics[]>> calculateStats() {

		retMap = new HashMap<>();

		for (PenetrationStatistics penetrationStatistics : stats) {
			Map<CellTypeInfo, Map<String, Integer>> sMap = penetrationStatistics.getStatsMap();
			for (Entry<CellTypeInfo, Map<String, Integer>> entry : sMap.entrySet()) {
				CellTypeInfo key = entry.getKey();
				Map<String, Integer> value = entry.getValue();
				Map<String, DescriptiveStatistics[]> innerResMap = retMap.computeIfAbsent(key, k -> new HashMap<String, DescriptiveStatistics[]>());
				int total = entry.getValue().values().stream().mapToInt(a -> a).sum();
				for (Entry<String, Integer> innerEntry : value.entrySet()) {
					String innerKey = innerEntry.getKey();
					DescriptiveStatistics[] dArr = innerResMap.computeIfAbsent(innerKey, k -> new DescriptiveStatistics[] {new DescriptiveStatistics(), new DescriptiveStatistics()});
					DescriptiveStatistics values = dArr[0];
					double innerValue = (double)innerEntry.getValue();
					values.addValue(innerValue / total);
					
					DescriptiveStatistics density = dArr[1];
					density.addValue(innerValue / penetrationStatistics.getCellCount());
				}
				DescriptiveStatistics[] desArr = innerResMap.computeIfAbsent(PenetrationStatistics.OF_TOTAL, k -> new DescriptiveStatistics[] {new DescriptiveStatistics(), new DescriptiveStatistics()});
				desArr[0].addValue((double) total / penetrationStatistics.getTotalCells());
			}
		}

		// Padd with zeros if values are missing to have the DescriptiveStatistics correctly
		retMap.forEach((typeInfo, innerMap) -> innerMap.forEach((key, dStat) -> {
			for (DescriptiveStatistics descriptiveStatistics : dStat) {
				int currentSize = (int) descriptiveStatistics.getN();
				if (currentSize > stats.size()) {
					throw new RuntimeException("You have more values than expcted");
				}
				for (int i = currentSize; i < stats.size(); i++) {
					descriptiveStatistics.addValue(0.0);
				}
			}
		}));

		return retMap;

	}
}
