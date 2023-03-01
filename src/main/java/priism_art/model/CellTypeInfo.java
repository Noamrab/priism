package priism_art.model;

import java.util.Arrays;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

public class CellTypeInfo {

	private String name;
	private String columnName;
	private boolean unique;
	private double threashold;
	private CellClass cellClass;
	

	private int index = -1;
	
	
	public CellTypeInfo(String columnName, boolean unique, double threashold, CellClass cellClass) {
		this.columnName = columnName;
		this.name = calcName(columnName);
		this.unique = unique;
		this.threashold = threashold;
		this.setCellClass(cellClass);
	}
	
	public CellTypeInfo(String columnName, boolean unique, double threashold, String cellClass) {
		this(columnName, unique, threashold, CellClass.fromString(cellClass));
	}
	
	private static String calcName(String columnName) {
		return StringUtils.substring(columnName, columnName.indexOf(":") + 1).toLowerCase().replace("mean", "").trim();
	}
	
	public static void main(String[] args) {
		var lst = Arrays.asList("other", "Nucleus: C mean","Nucleus: FOXP3 mean","Nucleus: HA mean","Cell: B220 mean","Cell: CD4 mean","Cell: CD8 mean","Cell: CD11C mean","Cell: F480 mean","Cell: LY6G mean","Cell: NCR1 mean");
		for (String string : lst) {
			System.out.println(calcName(string));
		}
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public boolean isUnique() {
		return unique;
	}

	public void setUnique(boolean unique) {
		this.unique = unique;
	}

	public double getThreashold() {
		return threashold;
	}

	public void setThreashold(double threashold) {
		this.threashold = threashold;
	}

	
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
	
	public boolean threasholdMet(String[] line) {
		return getThreashold() <= Double.parseDouble(line[getIndex()]);
	}

	@Override
	public String toString() {
		return "CellTypeInfo [name=" + name + ", columnName=" + columnName + ", unique=" + unique + ", threashold="
				+ threashold + ", goodCell=" + getCellClass() + ", index=" + index + "]";
	}

	

	@Override
	public int hashCode() {
		return Objects.hash(cellClass, columnName, index, threashold, unique);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CellTypeInfo other = (CellTypeInfo) obj;
		return cellClass == other.cellClass && Objects.equals(columnName, other.columnName) && index == other.index
				&& Double.doubleToLongBits(threashold) == Double.doubleToLongBits(other.threashold)
				&& unique == other.unique;
	}

	public CellClass getCellClass() {
		return cellClass;
	}

	public void setCellClass(CellClass cellClass) {
		this.cellClass = cellClass;
	}

	

	
}
