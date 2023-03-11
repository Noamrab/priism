package priism_art;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;

import priism_art.model.Cell;
import priism_art.model.CellClass;
import priism_art.model.CellTypeInfo;
import priism_art.model.Grid;
import priism_art.model.penetration.PenetrationClassifier;
import priism_art.model.penetration.PenetrationStatistics;
import priism_art.utils.MultiThreadedExecution;

public class Test {
private static final int PARTS = 3;
private static final int MIN_CELL_COUNT_IN_DISC = 200;
public static final int DISC_SIZE_PIXELS = 200;

//	private static final String FILE_NAME = "/Users/noamrabinovich/Downloads/Noam Collaboration/Cell Map.csv";
//	private static final String FILE_NAME = "C:\\Temp\\Priism\\GM65-23-D3.csv";
	
	final int radius;
	List<Cell> goodCells;
	List<Cell> allCells;
	DataLoader dl;
	PenetrationClassifier pc;
	List<PenetrationStatistics> pStats;

	private Grid grid;
	private String fileName;
	
	public Test(String fileName, int radius) {
		super();
		this.fileName = fileName;
		this.radius = radius;
	}

	public static void main(String[] args) throws FileNotFoundException, IOException, CsvException {
		//perform(FILE_NAME, DISC_SIZE_PIXELS);
	}

	public void perform() {
		init();
        
        calculate();
        write();
        System.out.println("-----");
	}
	



	private static final List<String> DETAILED_REPORT_HEADER = Arrays.asList("X", "Y", "Grid Position", "Cell Type", "Total in disc",  "Others");
	private void write() {
		
		List<CellTypeInfo> cellNames = dl.configList;
		List<String> reportHeader = new LinkedList<>(DETAILED_REPORT_HEADER);
		pStats.forEach(p -> {
//			"C Classification", "HA Classification"
			reportHeader.add(p.getTargetCellTypeName().toUpperCase() + " Classification"); 
		});

		List<String> cellNamesStr = new LinkedList<>(); 
		cellNames.forEach(c -> {
			cellNamesStr.add(c.getName());
			cellNamesStr.add(c.getName() + "% w others");
		});  
		
		
		
		List<String[]> data = new LinkedList<>();
		for (Cell cell : goodCells) {
			var row = new String[cellNamesStr.size() + reportHeader.size()];
			data.add(row);
			int totalNeighbours = cell.getTotalNeighbourCount();
			row[0] = Double.toString(cell.getX());
			row[1] = Double.toString(cell.getY());
			row[2] = grid.getPartPositionStr(cell.getX(), cell.getY());
			row[3] = cell.getCellType().getName();
			row[4] = Integer.toString(totalNeighbours); 
			row[5] = Integer.toString(cell.getOthersCount());
			// We're doing classification twice - but it should be cheaper than keeping the results in a map
			int i = 0;
			for (PenetrationStatistics pStat : pStats) {
				row[6 + i] = cell.getClassification(pc, pStat.getTargetCell(), MIN_CELL_COUNT_IN_DISC);
				i++;
			}
			
			i = 0;
			for (CellTypeInfo name : cellNames) {
				int value = cell.getNeighbourCount(name);
				row[reportHeader.size() + i] = Integer.toString(value);
				row[reportHeader.size() + i + 1] = Double.toString(cell.getPenetrationByName(name));
				i+= 2;
			}
		}
		cellNamesStr.addAll(0, reportHeader);
		String[] headerArray = new String[cellNamesStr.size()];
		writeDataLineByLine(fileName + "_output.csv", cellNamesStr.toArray(headerArray), data);
		
		for (PenetrationStatistics pStat : pStats) {
			writeDataLineByLine(fileName + "_" + pStat.getTargetCellTypeName() + ".csv", pStat.getHeaders(), pStat.toCSV());
		}
	}

	
	public static void writeDataLineByLine(String filePath, String[] header, List<String[]> data)
	{
	    // first create file object for file placed at location
	    // specified by filepath
	    File file = new File(filePath);
	    try {
	        // create FileWriter object with file as parameter
	        FileWriter outputfile = new FileWriter(file);
	  
	        // create CSVWriter object filewriter object as parameter
	        CSVWriter writer = new CSVWriter(outputfile);
	  
	        // adding header to csv
	        writer.writeNext(header);
	  
	        writer.writeAll(data);
	  
	        // closing writer connection
	        writer.close();
	    }
	    catch (IOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    }
	}
	
	
	
	private void init() {
		dl = new DataLoader();
		dl.loadConfig(DataLoader.CONFIG);
		pc = new PenetrationClassifier();
		pc.loadConfig(PenetrationClassifier.CONFIG);
		allCells = dl.readDataFile(fileName);
		Map<CellClass, List<Cell>> map = allCells.stream().collect(Collectors.groupingBy(c -> c.getCellType().getCellClass()));
		goodCells = map.get(CellClass.GOOD);
		Set<CellTypeInfo> badCellsTypes = map.get(CellClass.BAD).stream().map(Cell::getCellType).collect(Collectors.toSet());
		pStats = badCellsTypes.stream().map(c -> new PenetrationStatistics(allCells, c, pc)).collect(Collectors.toList());
		
		var maxX = allCells.stream().mapToDouble(Cell::getX).max().getAsDouble();
		var maxY = allCells.stream().mapToDouble(Cell::getY).max().getAsDouble();
		grid = new Grid(maxX, maxY, PARTS);
		
	}
	
	private void calculate() {
		MultiThreadedExecution.create(goodCells).setThreadCount(16).setProgressInterval(1250).setAction(gCell -> {
			for (Cell aCell : allCells) {
				if (aCell == gCell) {
					continue;
				}
				double dist = gCell.distance(aCell);
				if (dist <= radius) {
					gCell.addCellInRadious(aCell);
				}
			}
			pStats.forEach(ps -> ps.add(gCell, MIN_CELL_COUNT_IN_DISC));
		}).start();
	}
	
	
}
	