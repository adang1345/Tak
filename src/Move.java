import java.util.Arrays;

/** An instance represents a move in Tak. A move is defined by a MoveType and its associated configuration.
 * PLACE_FLAT_STONE a,b represents placing a flat stone at (a,b)
 * PLACE_STANDING_STONE a,b represents placing a standing stone at (a,b)
 * PLACE_CAPSTONE a,b represents placing a capstone at (a,b) 
 * MOVE_STACK a,b,n,d,drops represents placing moving the top n stones from (a,b) in the direction d, dropping
 *            drops many stones along the way */
public class Move {
	/** The type of a move */
	public static enum MoveType {
		PLACE_FLAT_STONE, PLACE_STANDING_STONE, PLACE_CAPSTONE, MOVE_STACK
	}

	/** Exception to be raised when an unknown string command is made. */
	public static class IllegalCommand extends RuntimeException {
		public IllegalCommand(String message) {
			super(message);
		}
	}

	private MoveType moveType = MoveType.PLACE_FLAT_STONE;  // type of move
	private int row = -1;  // row of cell to modify
	private int col = -1;  // column of cell to modify
	private int n = -1;  // number of pieces to move off stack
	private Board.Direction direction = Board.Direction.NORTH;  // which direction to move pieces
	private int[] drops = new int[] {};  // drop pattern
	private String moveStr;  // original string used to generate this move

	public MoveType getMoveType() {
		return moveType;
	}

	public int getRow() {
		return row;
	}

	public int getCol() {
		return col;
	}

	public int getN() {
		return n;
	}

	public Board.Direction getDirection() {
		return direction;
	}

	public int[] getDrops() {
		return drops;
	}

	public String moveStr() {
		return moveStr;
	}

	/** Parse the string command s to construct a move. Raises IllegalCommand if s is not a legal command. Coordinates
	 * are represented as (row,column) and are 0-indexed in the string.
	 * 
	 * Placement of a flat stone at (a,b): "F(a,b)"
	 * Placement of a standing stone at (a,b): "S(a,b)"
	 * Placement of a capstone at (a,b): "C(a,b)"
	 * Move 1 stone from (a,b) in direction D (+-<>): "M(a,b)D"
	 * Move n stones from (a,b) in direction D with drop pattern p: "M(a,b)Dn[p]" */
	public Move(String s) {
		moveStr = s;
		if (s.length() < 6) throw new IllegalCommand(s);
		// first letter and coordinates
		if (s.charAt(0) == 'F') moveType = MoveType.PLACE_FLAT_STONE;
		else if (s.charAt(0) == 'S') moveType = MoveType.PLACE_STANDING_STONE;
		else if (s.charAt(0) == 'C') moveType = MoveType.PLACE_CAPSTONE;
		else if (s.charAt(0) == 'M') moveType = MoveType.MOVE_STACK;
		else throw new IllegalCommand(s);
		if (s.charAt(1) != '(' || s.charAt(3) != ',' || s.charAt(5) != ')' ||
				!Character.isDigit(s.charAt(2)) || !Character.isDigit(s.charAt(4))) {
			throw new IllegalCommand(s);
		}
		row = Character.getNumericValue(s.charAt(2));
		col = Character.getNumericValue(s.charAt(4));
		if (s.length() == 6 && s.charAt(0) != 'M') return;

		// direction
		if (s.length() < 7) throw new IllegalCommand(s);
		if (s.charAt(6) == '+') direction = Board.Direction.NORTH;
		else if (s.charAt(6) == '-') direction = Board.Direction.SOUTH;
		else if (s.charAt(6) == '<') direction = Board.Direction.WEST;
		else if (s.charAt(6) == '>') direction = Board.Direction.EAST;
		else throw new IllegalCommand(s);
		//System.out.println("direction set");
		if (s.length() == 7) {
			n = 1;
			drops = new int[] {1};
			return;
		}

		// drop configurations  "M(a,b)Dn[p]"
		if (s.length() < 11 || !Character.isDigit(s.charAt(7)) || s.charAt(8) != '[' || s.charAt(s.length()-1) != ']') {
			throw new IllegalCommand(s);
		}
		n = Character.getNumericValue(s.charAt(7));
		drops = new int[s.length() - 10];
		for (int i = 9; i < s.length()-1; i++) {
			if (!Character.isDigit(s.charAt(i))) throw new IllegalCommand(s);
			drops[i-9] = Character.getNumericValue(s.charAt(i));
		}
		if (Board.sum(drops) != n) throw new IllegalCommand(s);
	}

	/** Return a string representation of the drop pattern i. */
	private static String dropToString(int[] ds) {
		String s = "";
		for (int i : ds) s += i;
		return s;
	}

	/** Return a unique string representation of this move. For testing purposes. */
	public String toString() {
		String s = "";
		switch (moveType) {
		case PLACE_FLAT_STONE:
			s += "Place flat stone at ";
			break;
		case PLACE_STANDING_STONE:
			s += "Place standing stone at ";
			break;
		case PLACE_CAPSTONE:
			s += "Place capstone at ";
			break;
		case MOVE_STACK:
			s += "Move stack at ";
		}
		s += "(" + row + "," + col + ")";
		if (moveType == MoveType.MOVE_STACK) {
			s += ": " + n + " stones ";
			switch (direction) {
			case NORTH:
				s += "NORTH";
				break;
			case WEST:
				s += "WEST";
				break;
			case SOUTH:
				s += "SOUTH";
				break;
			case EAST:
				s += "EAST";
			}
			s += " with pattern " + dropToString(drops);
		}
		return s;
	}

	/** Return true if this and other represent the same move. */
	public boolean equals(Object other) {
		if (!(other instanceof Move)) return false;
		Move otherCast = (Move) other;
		return moveType == otherCast.moveType && row == otherCast.row && col == otherCast.col && n == otherCast.n &&
				direction == otherCast.direction && Arrays.equals(drops, otherCast.drops);
	}

	/** Return a hash that is calculated using the fields. */
	public int hashCode() {
		return moveType.hashCode() + (new Integer(row)).hashCode() + (new Integer(col)).hashCode() +
				(new Integer(n)).hashCode() + direction.hashCode() + Arrays.hashCode(drops);
	}

	public static void main(String[] args) {
		Move m = new Move("M(3,2)>5[5]");
		System.out.println(m);
	}

}
