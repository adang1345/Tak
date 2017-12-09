/* An instance represents a pair of integers */
public class Pair {
	public int row;
	public int col;

	public Pair(int row, int col) {
		this.row = row;
		this.col = col;
	}

	public boolean equals(Object other) {
		if (!(other instanceof Pair)) return false;
		Pair castOther = (Pair) other;
		return row == castOther.row && col == castOther.col;
	}

	public int hashCode() {
		return (new Integer(row * 10 + col)).hashCode();
	}

	public String toString() {
		return "(" + row + "," + col + ")";
	}

}
