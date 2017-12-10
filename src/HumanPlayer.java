import java.util.TreeMap;

/* A Human Tak player. */
public class HumanPlayer extends Player {

	/** Create a human player.
	 * Precondition: strategy is Strategy.HUMAN */
	public HumanPlayer(Stone.Color c, State s, Strategy strategy, int depth) {
		super(c, s, strategy, depth);
		if (strategy != Strategy.HUMAN) throw new IllegalArgumentException("human strategy");
	}

	/** Choose a move and make it if possible. Return a single-key map describing what happened. If the move was
	 * successful, then the map contains the move used.
	 * 0->successful move: move was successful
	 * 1->null: command format was invalid
	 * 2->null: move was illegal */
	public ResultMove makeMove(StatusGUI status) {
		try {
			Move move = new Move(status.getText());
			executeMove(move);
			return new ResultMove(0, move);
		} catch (Move.IllegalCommand e) {
			return new ResultMove(1, null);
		} catch (Board.IllegalMove e) {
			return new ResultMove(2, null);
		}
	}

}
