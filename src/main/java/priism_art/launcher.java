package priism_art;

import java.util.Arrays;

public class launcher {
	public static void main(String[] args) {
		String basePath = "C:\\Temp\\Priism\\new batch\\";
		var files = Arrays.asList("GM65-21-D1.csv","GM65-21-D2.csv","GM65-21-O1.csv","GM65-21-O2.csv","GM65-22-D1.csv","GM65-22-D2.csv","GM65-22-D3.csv","GM65-22-O1.csv");
		files.forEach(f -> {
			Test.perform(basePath + f, Test.DISC_SIZE_PIXELS);
		});
	}
}
