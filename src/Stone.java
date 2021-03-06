/** A Stone represents a Tak piece */
public class Stone implements Cloneable {

	/** Exception to be raised when an illegal stone is made or set. An illegal stone is defined as a capstone that is
	 * flat. */
	public static class IllegalStone extends RuntimeException {
		public IllegalStone(String message) {
			super(message);
		}
	}

	/** Color of a piece */
	public static enum Color {
		WHITE, BLACK;
		public Color other() {
			if (this == WHITE) return BLACK;
			else return WHITE;
		}
	}

	/** type of a piece */
	public static enum Type {
		REGULAR, CAPSTONE
	}

	/** Status of a piece. A capstone cannot be flat. */
	public static enum Status {
		FLAT, STANDING
	}

	private Color color;  // color of the piece
	private Type type;  // type of the piece
	private Status status; // status of the piece, non-flat if it is a capstone

	/** Initialize a regular stone. */
	public Stone(Color color, Type type, Status status) {
		if (type == Type.CAPSTONE && status == Status.FLAT) throw new IllegalStone("Can't construct stone");
		this.color = color;
		this.type = type;
		this.status = status;
	}


	/** Return the color of this stone. */
	public Color getColor() {
		return color;
	}

	/** Return the type of this stone. */
	public Type getType() {
		return type;
	}

	/** Return the status of this stone. */
	public Status getStatus() {
		return status;
	}

	/** Set the status of this stone. */
	public void setStatus(Status status) {
		if (type == Type.CAPSTONE && status == Status.FLAT) throw new IllegalStone("Can't set stone");
		this.status = status;
	}

	/** Return true if this stone counts as part of a path, false otherwise. */
	public boolean isPartOfPath() {
		return type == Type.CAPSTONE || status == Status.FLAT;
	}

	/** A string representation of this stone. */
	public String toString() {
		String let = color == Color.WHITE ? "W" : "B";
		if (type == Type.CAPSTONE) return let + let + let + "\n" + let + let + let + "\n" + let + let + let + "\n";
		else if (status == Status.STANDING) return " " + let + " \n" + " " + let + " \n" + " " + let + " \n";
		else return "   \n   \n" + let + let + let + "\n";
	}

	/** Return a clone of this object. */
	public Stone clone() {
		return new Stone(color, type, status);
	}

}
