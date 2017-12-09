/* A Player is a player of Tak */
public abstract class Player {
	public static final int NUM_STONES = 21; // 21
	public static final int NUM_CAPSTONES = 1; // 1

	protected int stones;  // # stones in inventory
	protected int capstones;  // # capstones in inventory
	protected Stone.Color color;  // color of player
	protected Board board;  // board that this player can access

	public int getStones() {
		return stones;
	}

	public int getCapstones() {
		return capstones;
	}

	public Stone.Color getColor() {
		return color;
	}

	/** Initialize a player whose stone color is c on board b. */
	protected Player(Stone.Color c, Board b) {
		stones = NUM_STONES;
		capstones = NUM_CAPSTONES;
		color = c;
		board = b;
	}

	/** Return true if this player owns a stack at (row,col), false otherwise. */
	public boolean ownsStack(int row, int col) {
		Stone top = board.topStone(row, col);
		return top != null && top.getColor() == color;
	}

	/** Return true if the (row,col) cell is empty or owned by this player. */
	private boolean canModify(int row, int col) {
		Stone top = board.topStone(row, col);
		return top == null || top.getColor() == color;
	}

	/** Modify board to make a move. Throws IllegalMove and keeps the state unmodified if the move is not legal. */
	protected void executeMove(Move m) {
		switch (m.getMoveType()) {
		case PLACE_FLAT_STONE:
			if (stones == 0) throw new Board.IllegalMove("no more regular stones");
			board.addStone(new Stone(color, Stone.Type.REGULAR, Stone.Status.FLAT), m.getRow(), m.getCol());
			stones--;
			break;
		case PLACE_STANDING_STONE:
			if (stones == 0) throw new Board.IllegalMove("no more regular stones");
			board.addStone(new Stone(color, Stone.Type.REGULAR, Stone.Status.STANDING), m.getRow(), m.getCol());
			stones--;
			break;
		case PLACE_CAPSTONE:
			if (capstones == 0) throw new Board.IllegalMove("no more capstones");
			board.addStone(new Stone(color, Stone.Type.CAPSTONE, Stone.Status.STANDING), m.getRow(), m.getCol());
			capstones--;
			break;
		case MOVE_STACK:
			if (!canModify(m.getRow(), m.getCol())) throw new Board.IllegalMove("you can't move the stack here");
			board.moveStack(m.getN(), m.getDirection(), m.getDrops(), m.getRow(), m.getCol());
		}
	}

	/** Return true if this player is out of pieces, false otherwise.  */
	public boolean isOut() {
		return stones == 0 && capstones == 0;
	}

	/** Return a clone of this player with board b. */
	public Player clone(Board b) {
		Class<? extends Player> subclass = this.getClass();
		Player newPlayer;
		try {
			newPlayer = subclass.getDeclaredConstructor(Stone.Color.class, Board.class).newInstance(color, b);
		} catch (Exception e) {
			throw new RuntimeException("can't initialize new player");
		}
		newPlayer.stones = stones;
		newPlayer.capstones = capstones;
		return newPlayer;
	}

	/** Choose a move and execute it if possible. If move executed successfully, return 0. If an error occurred, return
	 * an integer representing the error. */
	public abstract int makeMove(StatusGUI status);

}
