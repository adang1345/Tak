import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.lang.ArrayIndexOutOfBoundsException;

/** A Board represents the state of a 5x5 Tak board */
public class Board implements Cloneable {

	public static final int SIZE = 5;  // size of the board
	public static final int CARRY_LIMIT = 5;  // carry limit for moving a stack

	public static enum Direction {
		NORTH, WEST, SOUTH, EAST
	}

	/** Exception to be raised when an illegal move is made */
	public static class IllegalMove extends RuntimeException {
		public IllegalMove(String message) {
			super(message);
		}
	}

	// cells of the board; (0,0) is bottom left
	private LinkedList<Stone>[][] cells;

	/** Initialize an empty board */
	public Board() {
		cells = new LinkedList[SIZE][SIZE];
		for (int i = 0; i < SIZE; i++) {
			for (int j = 0; j < SIZE; j++) {
				cells[i][j] = new LinkedList<Stone>();
			}
		}
	}

	/** Initialize a board with a deep copy of oldCells.
	 * Precondition: oldCells is not null and does not contain null in either of its two levels */
	private Board(LinkedList<Stone>[][] oldCells) {
		LinkedList<Stone>[][] newCells = new LinkedList[SIZE][SIZE];
		for (int i = 0; i < SIZE; i++) {
			for (int j = 0; j < SIZE; j++) {
				LinkedList<Stone> newStones = new LinkedList<Stone>();
				for (Stone oldStone : oldCells[i][j]) newStones.add(oldStone.clone());
				newCells[i][j] = newStones;
			}
		}
		cells = newCells;
	}

	/** Return the stones in the cell at (row, col). Throws IllegalMove if out of bounds. */
	public LinkedList<Stone> cellContents(int row, int col) {
		try {
			return cells[row][col];
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new IllegalMove("Out of bounds");
		}
	}

	/** Return true if (row, col) is within the bounds of this board, false otherwise. */
	private boolean isValidCell(int row, int col) {
		return row >= 0 && row < SIZE && col >= 0 && col < SIZE;
	}

	/** Return the top stone at (row, col), null if there are none.
	 * Precondition: (row, col) is a valid cell */
	public Stone topStone(int row, int col) {
		LinkedList<Stone> contents = cellContents(row, col);
		if (contents.isEmpty()) return null;
		else return contents.getFirst();
	}

	/** Return the top stone of the list of stones s, null if there is none. */
	private Stone topStone(LinkedList<Stone> s) {
		if (s.isEmpty()) return null;
		else return s.getFirst();
	}

	/** Return the top color of the stone at (row, col), null if there are none. */
	public Stone.Color topColor(int row, int col) {
		Stone s = topStone(row, col);
		if (s == null) return null;
		else return s.getColor();
	}

	/** Add a new stone to the board at (row, col). Raises IllegalMove if this move is impossible.
	 * Precondition: row and col are less than SIZE */
	public void addStone(Stone stone, int row, int col) {
		if (topStone(row, col) != null) {
			throw new IllegalMove("Cannot place new stone onto occupied cell (" + row + "," + col + ")");
		} else if (!isValidCell(row, col)) {
			throw new IllegalMove("Out of bounds");
		} else cellContents(row, col).addFirst(stone);
	}

	/** Return the sum of the elements of e. */	
	public static int sum(int[] e) {
		int acc = 0;
		for (int i : e) acc += i;
		return acc;
	}

	/** Move the top n stones of the stack at (row, col) in the direction d, performing a single drop. Return the pair
	 * (row, col) of the cell on which the piece(s) is/were dropped. */
	private Pair moveStackOnce(int n, Direction d, int row, int col) {
		LinkedList<Stone> contents = cellContents(row, col);
		if (n < 1 || n > CARRY_LIMIT || n > contents.size()) {
			throw new IllegalMove("Invalid carry amount " + n);
		}
		LinkedList<Stone> nextContents;
		int nextRow = row;
		int nextCol = col;
		switch (d) {
		case NORTH:
			nextRow++;
			break;
		case WEST:
			nextCol--;;
			break;
		case SOUTH:
			nextRow--;
			break;
		case EAST:
			nextCol++;
		}
		try {
			nextContents = cellContents(nextRow, nextCol);
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new IllegalMove("Out of bounds");
		}
		Stone topStoneNext = topStone(nextContents);
		if (topStoneNext != null && topStoneNext.getType() == Stone.Type.CAPSTONE) {
			throw new IllegalMove("Can't capture capstone");
		} else if (topStoneNext != null && topStoneNext.getStatus() == Stone.Status.STANDING &&
				contents.get(n-1).getType() == Stone.Type.REGULAR) {
			throw new IllegalMove("Can't topple standing stone");
		}

		// move is valid at this point
		if (topStoneNext != null && topStoneNext.getStatus() == Stone.Status.STANDING) {
			topStoneNext.setStatus(Stone.Status.FLAT);
		}
		for (int i = n-1; i >= 0; i--) {
			Stone toRemove = contents.get(i);
			nextContents.addFirst(toRemove);
			contents.remove(i);
		}
		return new Pair(nextRow, nextCol);
	}

	/** Move the top n stones of the stack at (row, col) in the direction d, where drop specifies how many stones to
	 * drop at each step. Raises IllegalMove if this cannot be completed.
	 * Precondition: number of pieces here <= n <= CARRY_LIMIT and n > 0
	 *               The sum of the elements of drop is n. */
	public void moveStack(int n, Direction d, int[] drops, int row, int col) {
		if (sum(drops) != n) throw new IllegalMove("Carry amount " + n + " does not match drops");
		if (!isValidCell(row, col)) throw new IllegalMove("Out of bounds");
		if (topStone(row, col) == null) throw new IllegalMove("Can't move empty stack");
		if (cellContents(row, col).size() < n) throw new IllegalMove("Not enough stones");
		if (d == Direction.NORTH && row + drops.length >= SIZE ||
				d == Direction.WEST && col - drops.length < 0 ||
				d == Direction.SOUTH && row - drops.length < 0 ||
				d == Direction.EAST && col + drops.length >= SIZE) {
			throw new IllegalMove("Out of bounds");
		}
		int nextRow = row;
		int nextCol = col;
		int leftToDrop = n;
		for (int i = 0; i < drops.length; i++) {
			Pair nextRowCol = moveStackOnce(leftToDrop, d, nextRow, nextCol);
			nextRow = nextRowCol.row;
			nextCol = nextRowCol.col;
			leftToDrop -= drops[i];
		}

	}

	/** Return true if the stacks at (row1,col1) and (row2,col2) are next to each other and are part of the path for the
	 * player with color c. */
	private boolean hasLink(int row1, int col1, int row2, int col2, Stone.Color c) {
		int rowDiff = Math.abs(row1 - row2);
		int colDiff = Math.abs(col1 - col2);
		if (!(rowDiff == 1 && colDiff == 0 || rowDiff == 0 && colDiff == 1)) return false;
		Stone s1 = topStone(row1, col1);
		Stone s2 = topStone(row2, col2);
		if (s1 == null || s2 == null) return false;
		return c == s1.getColor() && c == s2.getColor() && s1.isPartOfPath() && s2.isPartOfPath();		
	}

	/** Return true if the player with color c owns the stack at (row,col) and this stack is part of the path. */
	public boolean ownsPath(Stone.Color c, int row, int col) {
		Stone s = topStone(row, col);
		return s != null && s.getColor() == c && s.isPartOfPath();
	}

	/** Return true if the player with color c owns that stack at (row,col). */
	public boolean ownsStack(Stone.Color c, int row, int col) {
		Stone s = topStone(row, col);
		return s != null && s.getColor() == c;
	}

	/** Return the number of stacks owned by a player with color c that are part of a path. */
	public int numOwnedPath(Stone.Color c) {
		int n = 0;
		for (int i = 0; i < SIZE; i++) {
			for (int j = 0; j < SIZE; j++) {
				if (ownsPath(c, i, j)) n++;
			}
		}
		return n;
	}

	/** Return the number of stacks owned by the player with color c. */
	public int numOwnedStacks(Stone.Color c) {
		int n = 0;
		for (int i = 0; i < SIZE; i++) {
			for (int j = 0; j < SIZE; j++) {
				if (ownsStack(c, i, j)) n++;
			}
		}
		return n;
	}

	/** Return true if this board is full, false otherwise. */
	public boolean isFull() {
		for (int i = 0; i < SIZE; i++) {
			for (int j = 0; j < SIZE; j++) {
				if (topStone(i, j) == null) return false;
			}
		}
		return true;
	}

	/** Return an array of coordinates of all empty cells on this board. */
	public ArrayList<Pair> emptyCells() {
		ArrayList<Pair> cells = new ArrayList<Pair>();
		for (int i = 0; i < SIZE; i++) {
			for (int j = 0; j < SIZE; j++) {
				if (topStone(i, j) == null) cells.add(new Pair(i, j));
			}
		}
		return cells;
	}

	/** Return a graph representation of all the paths in this board for the player with color c. This graph is
	 * represented as a HashMap from a Pair to a set of Pairs that the first pair is next to. */
	public HashMap<Pair,HashSet<Pair>> toGraph(Stone.Color c) {
		HashMap<Pair,HashSet<Pair>> g = new HashMap<Pair,HashSet<Pair>>();
		for (int row = 0; row < SIZE; row++) {
			for (int col = 0; col < SIZE; col++) {
				HashSet<Pair> neighbors = new HashSet<Pair>();
				g.put(new Pair(row, col), neighbors);
				// check north
				if (row < SIZE-1 && hasLink(row, col, row+1, col, c)) {
					neighbors.add(new Pair(row+1, col));
					// System.out.println("added neighbor to north");
				}
				// check west
				if (col > 0 && hasLink(row, col, row, col-1, c)) {
					neighbors.add(new Pair(row, col-1));
					// System.out.println("added neighbor to west");
				}
				// check south
				if (row > 0 && hasLink(row, col, row-1, col, c)) {
					neighbors.add(new Pair(row-1, col));
					// System.out.println("added neighbor to south");
				}
				// check east
				if (col < SIZE-1 && hasLink(row, col, row, col+1, c)) {
					neighbors.add(new Pair(row, col+1));
					// System.out.println("added neighbor to east");
				}
			}
		}
		return g;
	}

	/** Return a string representation of a graph */
	public static String toGraphString(HashMap<Pair,HashSet<Pair>> g) {
		String s = "";
		for (Entry<Pair, HashSet<Pair>> entry : g.entrySet()) {
			s += entry.getKey().toString() + ": ";
			for (Pair neighbor : entry.getValue()) {
				s += neighbor.toString() + " ";
			}
			s += "\n";
		}
		return s;
	}

	public String toString() {
		String s = "";
		for (int i = 0; i < SIZE; i++) {
			for (int j = 0; j < SIZE; j++) {
				s += "Cell (" + i + ", " + j + ")\n";
				for (Stone e : cellContents(i, j)) s += e.toString();
				s += "\n";
			}
		}
		return s;
	}

	/** Return a clone of this board. */
	public Board clone() {
		return new Board(cells);
	}

	public static void main(String[] args) {
		// tests for two pieces
		Board b1 = new Board();
		b1.addStone(new Stone(Stone.Color.WHITE, Stone.Type.REGULAR, Stone.Status.FLAT), 0, 0);
		b1.addStone(new Stone(Stone.Color.BLACK, Stone.Type.REGULAR, Stone.Status.FLAT), 0, 1);
		int[] arr1 = new int[] {1};
		b1.moveStack(1, Direction.EAST, arr1, 0, 0);
		// b1.moveStack(0, Direction.SOUTH, new int[] {0}, 0, 1);  exception raised upon moving empty stack
		// b1.moveStack(n, d, drops, row, col);
		b1.moveStack(1, Direction.EAST, arr1, 0, 1);
		// b1.moveStack(2, Direction.WEST, new int[] {1,1}, 0, 2); exception raised upon moving more stones than exist
		b1.moveStack(1, Direction.NORTH, arr1, 0, 2);
		b1.moveStack(1, Direction.WEST, arr1, 1, 2);
		b1.moveStack(1, Direction.SOUTH, arr1, 1, 1);
		// b1.moveStack(2, Direction.SOUTH, new int[] {2}, 0, 1); out of bounds on south
		// b1.moveStack(2, Direction.SOUTH, new int[] {1,1}, 0, 1); out of bounds on south
		// b1.moveStack(2, Direction.WEST, new int[] {1,1}, 0, 1); out of bounds on west
		b1.moveStack(2, Direction.WEST, new int[] {2}, 0, 1);
		b1.addStone(new Stone(Stone.Color.WHITE, Stone.Type.REGULAR, Stone.Status.FLAT), 4, 4);
		// b1.moveStack(1, Direction.EAST, arr1, 4, 4); out of bounds on east
		// b1.moveStack(1, Direction.NORTH, arr1, 4, 4); out of bounds on north
		b1.moveStack(1, Direction.SOUTH, arr1, 4, 4);
		// System.out.println(b1.toString());

		// tests for large stacks, standing stones
		Board b2 = new Board();
		b2.addStone(new Stone(Stone.Color.BLACK, Stone.Type.REGULAR, Stone.Status.FLAT), 0, 0);
		for (int i = 0; i < 5; i++) {
			Stone.Color c = i % 2 == 0 ? Stone.Color.BLACK : Stone.Color.WHITE;
			b2.addStone(new Stone(c, Stone.Type.REGULAR, Stone.Status.FLAT), 0, 1);
			b2.moveStack(1, Direction.WEST, arr1, 0, 1);
		}
		b2.addStone(new Stone(Stone.Color.WHITE, Stone.Type.REGULAR, Stone.Status.STANDING), 0, 3);
		// b2.moveStack(6, Direction.EAST, new int[] {6}, 0, 0); exceed carry amount
		b2.moveStack(5, Direction.EAST, new int[] {5}, 0, 0);
		b2.moveStack(5, Direction.WEST, new int[] {5}, 0, 1);
		b2.moveStack(5, Direction.EAST, new int[] {3,2}, 0, 0);  // drop to 2 spots
		b2.moveStack(2, Direction.WEST, new int[] {2}, 0, 2);
		b2.moveStack(3, Direction.WEST, new int[] {3}, 0, 1);
		// b2.moveStack(4, Direction.WEST, new int[] {2,2}, 0, 0); out of bounds for drop multiple
		// b2.moveStack(4, Direction.EAST, new int[] {1,1,2}, 0, 0);  topple standing stone
		b2.moveStack(4, Direction.EAST, new int[] {1,3}, 0, 0);
		b2.moveStack(1, Direction.WEST, arr1, 0, 3); // place standing stone on top of stack
		// b2.moveStack(1, Direction.EAST, new int[] {1}, 0, 1); topple standing stone on top of others
		b2.moveStack(4, Direction.NORTH, new int[] {1,1,1,1}, 0, 2);

		// tests for capstone
		b2.addStone(new Stone(Stone.Color.BLACK, Stone.Type.CAPSTONE, Stone.Status.STANDING), 0, 2);
		// b2.moveStack(1, Direction.SOUTH, arr1, 1, 2); can't capture capstone
		// b2.moveStack(2, Direction.EAST, new int[] {1,1}, 0, 1); can't capture capstone
		b2.moveStack(1, Direction.WEST, arr1, 0, 2);
		b2.moveStack(4, Direction.NORTH, new int[] {1,2,1}, 0, 1);
		b2.moveStack(1, Direction.EAST, arr1, 3, 1);
		// b2.moveStack(2, Direction.NORTH, new int[] {2}, 3, 2); can't topple standing stone with more than capstone
		b2.moveStack(1, Direction.NORTH, arr1, 3, 2);
		b2.addStone(new Stone(Stone.Color.WHITE, Stone.Type.CAPSTONE, Stone.Status.STANDING), 4, 4);
		// b2.moveStack(2, Direction.EAST, new int[] {1,1}, 4, 2); can't topple capstone with capstone
		b2.moveStack(1, Direction.SOUTH, arr1, 4, 4);
		b2.addStone(new Stone(Stone.Color.WHITE, Stone.Type.REGULAR, Stone.Status.STANDING), 4, 4);
		b2.moveStack(2, Direction.EAST, new int[] {1,1}, 4, 2);
		System.out.println(b2);

	}

}
