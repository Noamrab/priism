package priism_art.utils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;

public class CSVUtils {
	public static <T> List<T> readFromCSV(String path, Function<String[], T> mapper) {
		try (CSVReader reader = new CSVReader(new FileReader(path))) {
			return reader.readAll().stream()
					.skip(1)
					.map(mapper).collect(Collectors.toList());
		} catch (IOException | CsvException e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to load config", e);
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

}
