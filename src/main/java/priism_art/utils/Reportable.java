package priism_art.utils;


public abstract class Reportable {

	public static final void updateStatus(String status) {
		System.out.println(status);
	}

	public static final void updateStatusFormat(String status, Object... args) {
		Reportable.updateStatus(String.format(status, args));
	}

	public static void updateStatusError(String status) {
		System.err.println(status);
	}
}
