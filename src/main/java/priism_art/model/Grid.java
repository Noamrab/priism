package priism_art.model;

import org.apache.commons.lang3.tuple.Pair;

public class Grid {
	private double partSizeX; 
	private double partSizeY; 
	
	public Grid(double maxX, double maxY, int parts) {
		partSizeX = Math.ceil(maxX) / parts;
		partSizeY = Math.ceil(maxY) / parts;
	}
	
	public Pair<Integer, Integer> getPartPosition(double x, double y) {
		int xPos = (int) Math.floor(x / partSizeX);
		int yPos = (int) Math.floor(y / partSizeY);
		return Pair.of(xPos, yPos);
	}
	
	public String getPartPositionStr(double x, double y) {
		return getPartPosition(x, y).toString();
	}
	
}
