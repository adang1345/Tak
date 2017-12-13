import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

/** A State represents the game state, which includes the inventory of each player along with the board state. */
public class State implements Cloneable {

	/** The status of a game */
	public static enum GameStatus {
		ONGOING, DRAW, PLAYER1_WIN, PLAYER2_WIN, ILLEGAL_MOVE, INVALID_COMMAND
	}

	private Board board;                  // the game board
	private int plies;                    // number of plies made
	private Player player1;               // player that goes first, is white
	private Player player2;               // player that goes second, is black
	private Player nextPlayer;            // player that goes next
	private ArrayList<String> allMoves;   // array of all moves made so far, represented as strings

	public Board getBoard() {
		return board;
	}

	public int getPlies() {
		return plies;
	}

	/** Increment the number of plies. */
	public void incPlies() {
		plies++;
	}

	/** Return the last move done in string form. If no moves have been made yet, return "None". */
	public String getLastMove() {
		if (allMoves.size() == 0) return "None";
		return allMoves.get(allMoves.size() - 1);
	}

	/** Return the list of all moves as a string, where moves are separated by a new line. */
	public String getAllMoves() {
		String acc = "";
		for (int i = 0; i < allMoves.size(); i++) acc += "Player " + (i % 2 + 1) + "\t" + allMoves.get(i) + "\n";
		return acc;
	}

	public Player getPlayer1() {
		return player1;
	}

	public Player getPlayer2() {
		return player2;
	}

	public Player getPlayer(Stone.Color c) {
		switch (c) {
		case WHITE:
			return player1;
		default:
			return player2;
		}
	}

	public Player getNextPlayer() {
		return nextPlayer;
	}

	public Player getPrevPlayer() {
		return nextPlayer == player1 ? player2 : player1;
	}

	/** Initialize the beginning of the game. Initially, player1, player2, and nextPlayer are null. They must be
	 * added when the players are initialized. */
	public State() {
		board = new Board();
		plies = 0;
		player1 = null;
		player2 = null;
		nextPlayer = null;
		allMoves = new ArrayList<String>();
	}

	/** Add player1 and player2 as players to this state. This should be called just after this state is initialized.
	 * Precondition: player1 and player2 have not yet been linked to this state.
	 *               player1 is white, and player2 is black. */
	public void addPlayers(Player player1, Player player2) {
		if (this.player1 != null || this.player2 != null || nextPlayer != null) {
			throw new IllegalArgumentException("can't do addPlayers when players already initialized");
		}
		if (player1.getColor() != Stone.Color.WHITE || player2.getColor() != Stone.Color.BLACK) {
			throw new IllegalArgumentException("wrong player colors");
		}
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
		Player.ResultMove rm = nextPlayer.makeMove(status);
		if (rm.result != 0) {
			if (rm.result == 1) gs = GameStatus.INVALID_COMMAND;
			else gs = GameStatus.ILLEGAL_MOVE;
			return gs;
		}
		gs = getStatus(nextPlayer);
		//System.out.println(gs);
		nextPlayer = nextPlayer == player1 ? player2 : player1;
		plies += 1;
		allMoves.add(rm.move.moveStr());
		return gs;
	}

	/** Swap the next player. */
	public void swapNextPlayer() {
		nextPlayer = nextPlayer == player1 ? player2 : player1;
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

	/** Create a clone of this State */
	public State clone() {
		State newState = new State();
		newState.board = board.clone();
		newState.plies = plies;
		Player newPlayer1 = player1.clone(newState);
		Player newPlayer2 = player2.clone(newState);
		newState.addPlayers(newPlayer1, newPlayer2);
		newState.nextPlayer = nextPlayer == player1 ? newPlayer1 : newPlayer2;
		newState.allMoves = new ArrayList<String>(allMoves);
		return newState;
	}

}
