package priism_art.utils;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.opencsv.CSVReader;
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

}
