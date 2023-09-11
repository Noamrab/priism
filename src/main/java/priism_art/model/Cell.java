package priism_art.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import priism_art.model.penetration.PenetrationClassifier;

public class Cell {
	private double x;
	private double y;
	private CellTypeInfo cellType;
	private List<CellTypeInfo> cellTypeList;
	
	public double distance(Cell other) {
		return Math.sqrt(
				Math.pow(x - other.x, 2) + Math.pow(y - other.y, 2)
				);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(cellType, x, y);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Cell other = (Cell) obj;
		return Objects.equals(cellType, other.cellType)
				&& Double.doubleToLongBits(x) == Double.doubleToLongBits(other.x)
				&& Double.doubleToLongBits(y) == Double.doubleToLongBits(other.y);
	}
	
	

	

	@Override
	public String toString() {
		return "Cell [x=" + x + ", y=" + y + ", cellType=" + cellType + ", neighbourCount=" + neighbourCount + "]";
	}

	public Cell(double x, double y, List<CellTypeInfo> cellTypeList) {
		super();
		this.x = x;
		this.y = y;
		if (cellTypeList == null || cellTypeList.isEmpty()) {
			throw new RuntimeException("Cell types must be provided");
		}
		this.cellType = cellTypeList.get(0);
		this.cellTypeList = cellTypeList;
	}
	public double getX() {
		return x;
	}
	public void setX(double x) {
		this.x = x;
	}
	public double getY() {
		return y;
	}
	public void setY(double y) {
		this.y = y;
	}
	public CellTypeInfo getCellType() {
		return cellType;
	}
	public void setCellType(CellTypeInfo cellType) {
		this.cellType = cellType;
	}

	Map<CellTypeInfo, Integer> neighbourCount = new HashMap<>(); 
	@SuppressWarnings("null")
	public void addCellInRadious(Cell bCell) {
		getNeighbourCount().merge(bCell.getCellType(), 1, Integer::sum);
	}

	public Map<CellTypeInfo, Integer> getNeighbourCount() {
		return neighbourCount;
	}
	
	public int getNeighbourCount(CellTypeInfo key) {
		Integer val = getNeighbourCount().get(key);
		return val == null ? 0 : val;
	}
	
	public double getPenetrationByName(CellTypeInfo key) {
		int totalNeighbourCount = getTotalNeighbourCount();
		return totalNeighbourCount == 0 ? 0 : ((double)getNeighbourCount(key)) / totalNeighbourCount;
	}
	
	public int getOthersCount() {
		for (Entry<CellTypeInfo, Integer> entry : getNeighbourCount().entrySet()) {
			if (CellClass.OTHER.equals(entry.getKey().getCellClass())) {
				return entry.getValue();
			}
		}
		return 0;
	}
	
	public String getCellTypeName() {
		return cellTypeList.stream().map(CellTypeInfo::getName).collect(Collectors.joining("_"));
	}

	public int getTotalNeighbourCount() {
		return getNeighbourCount().values().stream().mapToInt(Integer::intValue).sum();
	}

	public String getClassification(PenetrationClassifier pc, CellTypeInfo cCellName, int minCellCountInDisc) {
		if (getTotalNeighbourCount() < minCellCountInDisc) {
			return "N/A";
		}
		return pc.classify(getPenetrationByName(cCellName));
	}
	
}
