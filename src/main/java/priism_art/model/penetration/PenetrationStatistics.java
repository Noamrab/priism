package priism_art.model.penetration;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import priism_art.model.Cell;
import priism_art.model.CellTypeInfo;

public class PenetrationStatistics {

	private Map<CellTypeInfo, Map<String, Integer>> statsMap;
	private int totalCells;
	private final CellTypeInfo targetCell;
	private final PenetrationClassifier pc;
	
	
	public PenetrationStatistics(List<Cell> cells, CellTypeInfo targetCell, PenetrationClassifier pc) {
		this.totalCells = cells.size();
		statsMap = new HashMap<>();
		this.targetCell = targetCell;
		this.pc = pc;
	}


	public void add(Cell cell, int minCountInDisc) {
		String classification = cell.getClassification(pc, getTargetCell(), minCountInDisc);
		synchronized (getStatsMap()) {
			Map<String, Integer> classToInt = getStatsMap().computeIfAbsent(cell.getCellType(), a -> new HashMap<>());
			classToInt.merge(classification, 1, Integer::sum);
		}
	}
	
	public List<String[]> toCSV() {
		List<String> classes = getClasses();
		List<String[]> ret = new LinkedList<>();
		for (Entry<CellTypeInfo, Map<String, Integer>> entry : getStatsMap().entrySet()) {
			String[] arr = new String[classes.size() + 2];
			ret.add(arr);
			arr[0] = entry.getKey().getName();
			int total = entry.getValue().values().stream().mapToInt(a -> a).sum();
			for (int i = 0; i < classes.size(); i++) {
				Integer classCount = entry.getValue().get(classes.get(i));
				classCount = classCount == null ? 0 : classCount;
			arr[i + 1] = Double.toString((double) classCount / total);
			}
			arr[classes.size() + 1] = Double.toString((double) total / totalCells);
		}
		ret.sort(Comparator.comparing(a -> a[0]));
		return ret;
	}

	private List<String> getClasses() {
		return getStatsMap().values().stream().map(a -> a.keySet()).flatMap(l -> l.stream()).distinct().sorted().collect(Collectors.toList());
	}
	
	public String[] getHeaders() {
		var lst = getClasses();
		lst.add(0, "Name");
		lst.add("% of total");
		return lst.toArray(new String[lst.size()]);
	}



	public CellTypeInfo getTargetCell() {
		return targetCell;
	}



	public Map<CellTypeInfo, Map<String, Integer>> getStatsMap() {
		return statsMap;
	}
	
	public String getTargetCellTypeName() {
		return targetCell.getName();
	}
	
	
}
