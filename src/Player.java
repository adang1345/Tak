/* A Player is a player of Tak */
public abstract class Player {
	public static final int NUM_STONES = 21; // 21
	public static final int NUM_CAPSTONES = 1; // 1

	/** Supported playing strategies of a player */
	public static enum Strategy {
		HUMAN,
		RANDOM,

		// maximizes number of own controlled cells
		SELFISH,
		// minimizes opponent's controlled cells
		ATTACKER,
		// maximizes # own cells - # opponent's cells
		SELFISH_ATTACKER,
		// Maximize total number of my controlled pieces minus the total number of opponent's controlled pieces.
		GATHERER,
		// maximize the number of adjacent pairs of flat controlled cells (allowing double-counting)
		CLUSTERBUILDER,
		// combination of clusterbuilder and gatherer, weighing stacks of multiple own more
		CLUSTERBUILDER_GATHERER
	}

	protected int stones;  // # stones in inventory
	protected int capstones;  // # capstones in inventory
	protected Stone.Color color;  // color of player
	protected State state;  // board that this player can access
	protected Strategy strategy; // strategy this player uses
	protected int depth;

	public int getStones() {
		return stones;
	}

	public int getCapstones() {
		return capstones;
	}

	public Stone.Color getColor() {
		return color;
	}

	/** An instance represents an int (denoting the result of trying to do a move) and the move itself. */
	public static class ResultMove {
		public int result;
		public Move move;
		public ResultMove(int result, Move move) {
			this.result = result;
			this.move = move;
		}
	}

	/** Initialize a player whose stone color is c on board b. */
	protected Player(Stone.Color c, State s, Strategy strategy, int depth) {
		if (depth < 0) throw new IllegalArgumentException("invalid depth");
		stones = NUM_STONES;
		capstones = NUM_CAPSTONES;
		color = c;
		state = s;
		this.strategy = strategy;
		this.depth = depth;
	}

	/** Return true if this player owns a stack at (row,col), false otherwise. */
	public boolean ownsStack(int row, int col) {
		Stone top = state.getBoard().topStone(row, col);
		return top != null && top.getColor() == color;
	}

	/** Return true if the (row,col) cell is empty or owned by this player. */
	private boolean canModify(int row, int col) {
		Stone top = state.getBoard().topStone(row, col);
		return top == null || top.getColor() == color;
	}

	/** Modify state to make the move m. Throws IllegalMove and keeps the state unmodified if the move is not legal. */
	protected void executeMove(Move m) {
		switch (m.getMoveType()) {
		case PLACE_FLAT_STONE:
			if (stones == 0) throw new Board.IllegalMove("no more regular stones");
			state.getBoard().addStone(new Stone(color, Stone.Type.REGULAR, Stone.Status.FLAT), m.getRow(), m.getCol());
			stones--;
			break;
		case PLACE_STANDING_STONE:
			if (stones == 0) throw new Board.IllegalMove("no more regular stones");
			state.getBoard().addStone(new Stone(color, Stone.Type.REGULAR, Stone.Status.STANDING), m.getRow(), m.getCol());
			stones--;
			break;
		case PLACE_CAPSTONE:
			if (capstones == 0) throw new Board.IllegalMove("no more capstones");
			state.getBoard().addStone(new Stone(color, Stone.Type.CAPSTONE, Stone.Status.STANDING), m.getRow(), m.getCol());
			capstones--;
			break;
		case MOVE_STACK:
			if (!canModify(m.getRow(), m.getCol())) throw new Board.IllegalMove("you can't move the stack here");
			state.getBoard().moveStack(m.getN(), m.getDirection(), m.getDrops(), m.getRow(), m.getCol());
		}
	}

	/** Return true if this player is out of pieces, false otherwise.  */
	public boolean isOut() {
		return stones == 0 && capstones == 0;
	}

	/** Return a clone of this player, linking it to state s. */
	public Player clone(State s) {
		Class<? extends Player> subclass = this.getClass();
		Player newPlayer;
		try {
			newPlayer = subclass.getDeclaredConstructor(Stone.Color.class, State.class, Strategy.class, int.class)
					.newInstance(color, s, strategy, depth);
		} catch (Exception e) {
			throw new RuntimeException("can't initialize new player");
		}
		newPlayer.stones = stones;
		newPlayer.capstones = capstones;
		return newPlayer;
	}

	/** Choose a move and execute it if possible. If move executed successfully, return 0 and the move made. If an error
	 * occurred, return an integer representing the error and null. */
	public abstract ResultMove makeMove(StatusGUI status);

}
