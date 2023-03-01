package priism_art;

import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import priism_art.model.Cell;
import priism_art.model.CellTypeInfo;
import priism_art.utils.CSVUtils;

public class DataLoader {

	private static final String X_POS = "Centroid X px";
	private static final String Y_POS = "Centroid Y px";
	private static final String DB_FILE = "C:\\Temp\\Priism\\GM65-23-O1.csv";
	public static final String CONFIG = "C:\\Temp\\Priism\\Config.csv";
	
	private static final CellTypeInfo OTHER = new CellTypeInfo("other", false, 0, "other");

	// read configuration
	List<CellTypeInfo> configList;

	public void loadConfig(String path) {
		configList = CSVUtils.readFromCSV(path, a -> 
		new CellTypeInfo(a[0], 
				StringUtils.equalsIgnoreCase(a[1], "Y"), 
				Double.parseDouble(a[2]), 
				a[3]));
	}


	// get a map

	public List<Cell> readDataFile(String path) {
		try (CSVReader reader = new CSVReader(new FileReader(path))) {
			var header = reader.readNext();
			int xCol = -1;
			int yCol = -1; 
			Map<String, CellTypeInfo> cellTypeMap = configList.stream().collect(Collectors.toMap(a -> a.getColumnName().toLowerCase(), Function.identity()));
			for (int i = 0; i < header.length; i++) {
				CellTypeInfo cti = cellTypeMap.get(header[i].toLowerCase());
				if (cti != null) {
					cti.setIndex(i);
				} else if (StringUtils.equalsAnyIgnoreCase(X_POS, header[i])) {
					xCol = i;
				} else if (StringUtils.equalsAnyIgnoreCase(Y_POS, header[i])) {
					yCol = i;
				}
			}
			List<Cell> ret = new LinkedList<>();
			List<Cell> bad = new LinkedList<>();
			for (String[] line : reader) {
				
				List<CellTypeInfo> ctiList = getCellTypeInfo(line);
				
				if (ctiList.size() > 1 && ctiList.stream().anyMatch(CellTypeInfo::isUnique)) {
//					System.out.println("Skipping line. Multiple threasholds reached");
//					System.out.println(ctiList);
//					System.out.println(Arrays.toString(line));
					bad.add(new Cell(Double.parseDouble(line[xCol]), Double.parseDouble(line[yCol]), ctiList));
					continue;
				}
				ret.add(new Cell(Double.parseDouble(line[xCol]), Double.parseDouble(line[yCol]), ctiList));
				
			}
			System.out.printf("About %.2f%% of the lines are lost due to thresholds\n",((double)bad.size() / (ret.size() + bad.size())) * 100);
			return ret;
			
		} catch (CsvValidationException | IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to load data", e);
		}
	}


	private List<CellTypeInfo> getCellTypeInfo(String[] line) {
		List<CellTypeInfo> retList = configList.stream().filter(c -> c.threasholdMet(line)).collect(Collectors.toList());
		return retList.isEmpty() ? Arrays.asList(OTHER) : retList;
	}


	public static void main(String[] args) {
		DataLoader dl = new DataLoader();
		dl.loadConfig(CONFIG);
		List<Cell> ret = dl.readDataFile(DB_FILE);
		System.out.println(ret.size());
		Map<String, Long> val = ret.stream().collect(Collectors.groupingBy(c -> c.getCellType().toString(), Collectors.counting()));
		for (Entry<String, Long> cell : val.entrySet()) {
			System.out.println(cell);
		}
	}
}
