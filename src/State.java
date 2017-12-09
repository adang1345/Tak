import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

/* A State represents the game state, which includes the inventory of each player along with the board state. */
public class State implements Cloneable {

	/** The status of a game */
	public static enum GameStatus {
		ONGOING, DRAW, PLAYER1_WIN, PLAYER2_WIN, ILLEGAL_MOVE, INVALID_COMMAND
	}

	private Board board;       // the game board
	private int plies;         // number of plies made
	private Player player1;    // player that goes first, is white
	private Player player2;    // player that goes second, is black
	private Player nextPlayer; // player that goes next

	public Board getBoard() {
		return board;
	}

	public int getPlies() {
		return plies;
	}

	public Player getPlayer1() {
		return player1;
	}

	public Player getPlayer2() {
		return player2;
	}

	public Player getNextPlayer() {
		return nextPlayer;
	}

	/** Initialize the beginning of the game.
	 * Precondition: player1 must be white and player2 must be black. */
	public State(Board b, Player player1, Player player2) {
		if (player1.getColor() != Stone.Color.WHITE || player2.getColor() != Stone.Color.BLACK) {
			throw new RuntimeException("player1 must be white and player2 must be black");
		}
		board = b;
		plies = 0;
		this.player1 = player1;
		this.player2 = player2;
		nextPlayer = player1;
	}

	public String toString() {
		String s = "" + plies + " moves made\n" +
				"\nPlayer1 (White)\n" + player1.getStones() + " stones, " + player1.getCapstones() + " capstones\n" +
				"\nPlayer2 (Black)\n" + player2.getStones() + " stones, " + player2.getCapstones() + " capstones\n\n" +
				board.toString();
		return s;
	}

	/** Have the next player make a move and return the game status. If a move cannot be made, leave the state unchanged
	 * and return the status describing the problem. */
	public GameStatus makeMove(StatusGUI status) {
		GameStatus gs;
		int s = nextPlayer.makeMove(status);
		if (s != 0) {
			if (s == 1) gs = GameStatus.INVALID_COMMAND;
			else gs = GameStatus.ILLEGAL_MOVE;
			return gs;
		}
		gs = getStatus(nextPlayer);
		nextPlayer = nextPlayer == player1 ? player2 : player1;
		plies += 1;
		// System.out.println(Board.toGraphString(board.toGraph(nextPlayer.getColor())));
		return gs;
	}


	/** Return true if there is a path from start to anything in ends in graph, false otherwise. This function uses
	 * depth-first search.
	 * Precondition: graph has a copy of start and end. */
	private static boolean hasPath(HashMap<Pair,HashSet<Pair>> graph, Pair start, HashSet<Pair> ends) {
		HashSet<Pair> explored = new HashSet<Pair>();
		LinkedList<Pair> frontier = new LinkedList<Pair>();
		frontier.add(start);
		while (!frontier.isEmpty()) {
			Pair node = frontier.pop();
			explored.add(node);
			for (Pair child : graph.get(node)) {
				if (!explored.contains(child) && !frontier.contains(child)) {
					if (ends.contains(child)) return true;
					frontier.addFirst(child);
				}
			}
		}
		return false;
	}

	/** Return true if player p has a bridge on the board, false otherwise. */
	private boolean hasBridge(Player p) {
		// get graph representation of cells owned by next player
		HashMap<Pair,HashSet<Pair>> graph = board.toGraph(p.getColor());

		// search for horizontal bridge
		HashSet<Pair> startLeft = new HashSet<Pair>();
		HashSet<Pair> endRight = new HashSet<Pair>();
		for (int i = 0; i < Board.SIZE; i++) {
			startLeft.add(new Pair(i, 0));
			endRight.add(new Pair(i, Board.SIZE-1));
		}
		for (Pair start : startLeft) {
			if (hasPath(graph, start, endRight)) return true;
		}

		// search for vertical bridge
		HashSet<Pair> startBottom = new HashSet<Pair>();
		HashSet<Pair> endTop = new HashSet<Pair>();
		for (int i = 0; i < Board.SIZE; i++) {
			startBottom.add(new Pair(0, i));
			endTop.add(new Pair(Board.SIZE-1, i));
		}
		for (Pair start : startBottom) {
			if (hasPath(graph, start, endTop)) return true;
		}

		return false;
	}

	/** Return the status of this game, where p is the last player that played. */
	public GameStatus getStatus(Player p) {
		// win condition for having a bridge complete
		if (hasBridge(p)) {
			if (p == player1) return GameStatus.PLAYER1_WIN;
			else return GameStatus.PLAYER2_WIN;
		}
		Player otherPlayer = p == player1 ? player2 : player1;
		if (hasBridge(otherPlayer)) {
			if (otherPlayer == player1) return GameStatus.PLAYER1_WIN;
			else return GameStatus.PLAYER2_WIN;
		}

		// win condition for having the most flat owned stacks
		if (board.isFull() || player1.isOut() || player2.isOut()) {
			int player1Owned = board.numOwnedPath(player1.getColor());
			int player2Owned = board.numOwnedPath(player2.getColor());
			if (player1Owned > player2Owned) return GameStatus.PLAYER1_WIN;
			else if (player1Owned == player2Owned) return GameStatus.DRAW;
			else return GameStatus.PLAYER2_WIN;
		}
		return GameStatus.ONGOING;
	}

	/** Return the possible legal moves for the current player.
	 * Precondition: the game is not yet over. */
	public Move[] getPossibleMoves() {
		ArrayList<Move> moves = new ArrayList<Move>();
		return null;
	}

	/** Create a clone of this State */
	public State clone() {
		// clone the board first
		Board newBoard = board.clone();
		// clone the players with the new board
		Player newPlayer1 = player1.clone(newBoard);
		Player newPlayer2 = player2.clone(newBoard);
		// create and tweak the fields of a new state
		State newState = new State(newBoard, newPlayer1, newPlayer2);
		newState.plies = plies;
		newState.nextPlayer = nextPlayer == player1 ? newPlayer1 : newPlayer2;
		return newState;
	}
	
	//	public static void main(String[] args) {
	//		Board b = new Board();
	//		State s = new State(b, new HumanPlayer(Stone.Color.WHITE, b), new HumanPlayer(Stone.Color.BLACK, b));
	//		System.out.println(s);
	//		while (true) {
	//			System.out.println("Player1's Turn\n--------------");
	//			s.player1.makeMove();
	//			s.moves += 1;
	//			System.out.println(s);
	//			System.out.println("Player2's Turn\n--------------");
	//			s.player2.makeMove();
	//			s.moves += 1;
	//			System.out.println(s);
	//		}
	//	}
}