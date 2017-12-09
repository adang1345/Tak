import java.util.ArrayList;

public class AIPlayer extends Player {
	
	public AIPlayer(Stone.Color c, Board b) {
		super(c, b);
	}
	
	/** Return a list of moves that this player can do by placing a new stone. */
	private ArrayList<Move> getPossiblePlaceMoves() {
		ArrayList<Move> moves = new ArrayList<Move>();
		if (!isOut()) {
			for (Pair coords : board.emptyCells()) {
				String suffix = "(" + coords.row + "," + coords.col + ")";
				if (stones > 0) {
					moves.add(new Move("F" + suffix));
					moves.add(new Move("S" + suffix));
				}
				if (capstones > 0) {
					moves.add(new Move("C" + suffix));
				}
			}
		}
		return moves;
	}
	
	/** Return a list of moves that this player can do by moving a stack on the screen. */
	private ArrayList<Move> getPossibleMoveMoves() {
		return null;
	}
	
	/** Return all possible moves this player can make. */
	private ArrayList<Move> getPossibleMoves() {
		return null;
	}
	
	public int makeMove(StatusGUI status) {
		// TODO Auto-generated method stub
		return 0;
	}

}
