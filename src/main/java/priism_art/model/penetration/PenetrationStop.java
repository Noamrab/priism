package priism_art.model.penetration;

import java.util.Objects;

public class PenetrationStop implements Comparable<PenetrationStop> {
	private String name;
	private double value;
	
	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public PenetrationStop(String name, double value) {
		this.name = name;
		this.value = value;
	}

	@Override
	public int compareTo(PenetrationStop o) {
		return Double.compare(value, o.getValue());
	}

	@Override
	public String toString() {
		return "PenetrationStop [name=" + name + ", value=" + value + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PenetrationStop other = (PenetrationStop) obj;
		return Objects.equals(name, other.name)
				&& Double.doubleToLongBits(value) == Double.doubleToLongBits(other.value);
	}
	
	
	
}
