package priism_art.model;

public enum CellClass {

	GOOD("Good"),
	BAD("Bad"),
	OTHER("Other");
	
	private final String name;

	public String getName() {
		return name;
	}

	private CellClass(String name) {
		this.name = name;
	}
	
	public static CellClass fromString(String cellClassStr) {
		switch (cellClassStr.toLowerCase()) {
		case "good":
			return GOOD;
		case "bad":
			return BAD;
		case "other":
			return OTHER;
		default:
			throw new RuntimeException("Unrecognized class " + cellClassStr);
		}
	}
	
}
