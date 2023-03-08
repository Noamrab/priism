package priism_art;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;

import priism_art.model.Cell;
import priism_art.model.CellClass;
import priism_art.model.CellTypeInfo;
import priism_art.model.Grid;
import priism_art.model.penetration.PenetrationClassifier;
import priism_art.model.penetration.PenetrationStatistics;
import priism_art.model.penetration.PenetrationStatisticsC;
import priism_art.model.penetration.PenetrationStatisticsHA;
import priism_art.utils.MultiThreadedExecution;

public class Test {
private static final int PARTS = 3;
private static final int MIN_CELL_COUNT_IN_DISC = 200;
public static final int DISC_SIZE_PIXELS = 200;

//	private static final String FILE_NAME = "/Users/noamrabinovich/Downloads/Noam Collaboration/Cell Map.csv";
	private static final String FILE_NAME = "C:\\Temp\\Priism\\GM65-23-D3.csv";
	
	final int radius;
	List<Cell> goodCells;
	List<Cell> allCells;
	DataLoader dl;
	PenetrationClassifier pc;
	PenetrationStatistics psC;
	PenetrationStatistics psHA;

	private Grid grid;
	private String fileName;
	
	public Test(String fileName, int radius) {
		super();
		this.fileName = fileName;
		this.radius = radius;
	}

	public static void main(String[] args) throws FileNotFoundException, IOException, CsvException {
		perform(FILE_NAME, DISC_SIZE_PIXELS);
	}

	public static void perform(String fileName, int discSize) {
		Test t = new Test(fileName, discSize);
		t.init();
        
        t.calculate();
        t.write();
        System.out.println("-----");
	}
	



	private static final List<String> DETAILED_REPORT_HEADER = Arrays.asList("X", "Y", "Grid Position", "Cell Type", "Total in disc",  "Others", "C Classification", "HA Classification");
	private void write() {
		
		List<CellTypeInfo> cellNames = dl.configList;

		List<String> cellNamesStr = new LinkedList<>(); 
		cellNames.forEach(c -> {
			cellNamesStr.add(c.getName());
			cellNamesStr.add(c.getName() + "% w others");
		});  
		
		
		
		List<String[]> data = new LinkedList<>();
		for (Cell cell : goodCells) {
			var row = new String[cellNamesStr.size() + DETAILED_REPORT_HEADER.size()];
			data.add(row);
			int totalNeighbours = cell.getTotalNeighbourCount();
			row[0] = Double.toString(cell.getX());
			row[1] = Double.toString(cell.getY());
			row[2] = grid.getPartPositionStr(cell.getX(), cell.getY());
			row[3] = cell.getCellType().getName();
			row[4] = Integer.toString(totalNeighbours); 
			row[5] = Integer.toString(cell.getOthersCount());
			// We're doing classification twice - but it should be cheaper than keeping the results in a map
			String cClassification = cell.getClassification(pc, psC.getTargetCell(), MIN_CELL_COUNT_IN_DISC);
			String haClassification = cell.getClassification(pc, psHA.getTargetCell(), MIN_CELL_COUNT_IN_DISC);
			row[6] = cClassification;
			row[7] = haClassification;
			
			int i = 0;
			for (CellTypeInfo name : cellNames) {
				int value = cell.getNeighbourCount(name);
				row[DETAILED_REPORT_HEADER.size() + i] = Integer.toString(value);
				row[DETAILED_REPORT_HEADER.size() + i + 1] = Double.toString(cell.getPenetrationByName(name));
				i+= 2;
			}
		}
		cellNamesStr.addAll(0, DETAILED_REPORT_HEADER);
		String[] headerArray = new String[cellNamesStr.size()];
		writeDataLineByLine(fileName + "_output.csv", cellNamesStr.toArray(headerArray), data);

		writeDataLineByLine(fileName + "_c.csv", 	psC.getHeaders(), 	psC.toCSV());
		writeDataLineByLine(fileName + "_ha.csv", 	psHA.getHeaders(),	psHA.toCSV());
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
		
		var maxX = allCells.stream().mapToDouble(Cell::getX).max().getAsDouble();
		var maxY = allCells.stream().mapToDouble(Cell::getY).max().getAsDouble();
		grid = new Grid(maxX, maxY, PARTS);
		
		psC = new PenetrationStatisticsC(allCells, dl.configList);
		psHA = new PenetrationStatisticsHA(allCells, dl.configList);
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
			psC.add(gCell, pc, MIN_CELL_COUNT_IN_DISC);
			psHA.add(gCell, pc, MIN_CELL_COUNT_IN_DISC);
		}).start();
	}
	
	
}
	