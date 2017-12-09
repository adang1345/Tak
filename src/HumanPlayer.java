/* A Human Tak player. */
public class HumanPlayer extends Player {

	public HumanPlayer(Stone.Color c, Board b) {
		super(c, b);
	}

	/** Choose a move and make it if possible. Return values mean the following.
	 * 0: move was successful
	 * 1: command format was invalid
	 * 2: move was illegal */
	public int makeMove(StatusGUI status) {
		try {
			executeMove(new Move(status.getText()));
			return 0;
		} catch (Move.IllegalCommand e) {
			return 1;
		} catch (Board.IllegalMove e) {
			return 2;
		}
	}

}
