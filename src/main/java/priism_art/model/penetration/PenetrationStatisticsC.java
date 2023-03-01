package priism_art.model.penetration;

import java.util.List;

import priism_art.model.Cell;
import priism_art.model.CellTypeInfo;

public class PenetrationStatisticsC extends PenetrationStatistics {

	public PenetrationStatisticsC(List<Cell> cells, List<CellTypeInfo> cellNames) {
		super(cells, cellNames);
	}

	@Override
	String getTargetCellName() {
		return "c";
	}

}
