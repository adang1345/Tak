import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.concurrent.TimeUnit;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;


/* An instance represents a graphical representation of the game status */
public class StatusGUI extends JPanel {

	public static final Color BACKGROUND_COLOR = new Color(0, 153, 51);
	public static final Font TURN_FONT = new Font("Tahoma", Font.BOLD, 25);
	public static final Font PLAYER_FONT = new Font("Arial", Font.BOLD, 20);
	public static final Dimension TEXTFIELD_DIM = new Dimension((int)Tak.STATUS_DIM.getWidth(), 30);
	public static final Border TEXTFIELD_BORDER = new LineBorder(Color.BLACK, 1);
	public static final int PADDING = 15;
	public static final Color PLIES_COLOR = new Color(0, 0, 102);
	public static final int AI_DELAY = 0;  // minimum number of milliseconds AI must take to move

	private Tak tak;
	private State state;
	private JLabel nextPlayer;
	private JLabel player1Info;
	private JLabel player2Info;
	private JLabel plies;
	private JTextField textField;
	private String statusMsg = "";
	private JLabel lastMove;
	private boolean gameOver;
	private AbstractAction action;

	/** Return the text that is currently in the text field. */
	public String getText() {
		return textField.getText();
	}

	public JTextField getTextField() {
		return textField;
	}

	public StatusGUI(Tak tak, State state) {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createCompoundBorder(new LineBorder(Tak.BORDER_COLOR, Tak.BORDER_THICKNESS),
				new EmptyBorder(PADDING, PADDING, PADDING, PADDING)));
		setPreferredSize(Tak.STATUS_DIM);
		setBackground(BACKGROUND_COLOR);

		this.tak = tak;
		this.state = state;
		gameOver = false;
		action = new EnterMoveListener();

		updateStatus();
	}

	/* Listener for when the user presses enter to input a move. */
	private class EnterMoveListener extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			State.GameStatus gs = state.makeMove(StatusGUI.this);
			switch (gs) {
			case PLAYER1_WIN:
				statusMsg = "Player 1 Wins";
				gameOver = true;
				break;
			case PLAYER2_WIN:
				statusMsg = "Player 2 Wins";
				gameOver = true;
				break;
			case DRAW:
				statusMsg = "Game is a Draw";
				gameOver = true;
				break;
			case ILLEGAL_MOVE:
				statusMsg = "Illegal Move";
				break;
			case INVALID_COMMAND:
				statusMsg = "Invalid Command";
				break;
			default:
				statusMsg = "";
			}
			tak.updateBoard();
			tak.updateStack();
			updateStatus();
		}
	}

	/** Return s with the characters '>' and '<' converted to escaped HTML. */
	private static String escapeHtml(String s) {
		return s.replace("<", "&lt;").replace(">", "&gt;");
	}
	
	/* Update status  */
	private void updateStatus() {
		removeAll();

		// initialize nextPlayer
		String nextPlayerText = "Player ";
		Color nextPlayerColor;
		if (state.getNextPlayer() == state.getPlayer1()) {
			nextPlayerText += "1";
			nextPlayerColor = Color.WHITE;
		} else {
			nextPlayerText += "2";
			nextPlayerColor = Color.BLACK;
		}
		nextPlayerText += "'s Turn (";
		if (state.getNextPlayer() instanceof HumanPlayer) nextPlayerText += "Human)";
		else nextPlayerText += "AI)";
		nextPlayer = new JLabel(nextPlayerText);
		nextPlayer.setFont(TURN_FONT);
		nextPlayer.setForeground(nextPlayerColor);
		add(nextPlayer);

		// add player1 info
		String player1Text = "<html><br /><u>Player 1</u><br />Stones: ";
		player1Text += state.getPlayer1().getStones() + "<br />Capstones: ";
		player1Text += state.getPlayer1().getCapstones() + "</html>";
		player1Info = new JLabel(player1Text);
		player1Info.setFont(PLAYER_FONT);
		player1Info.setForeground(Color.WHITE);
		add(player1Info);

		// add player2 info
		String player2Text = "<html><br /><u>Player 2</u><br />Stones: ";
		player2Text += state.getPlayer2().getStones() + "<br />Capstones: ";
		player2Text += state.getPlayer2().getCapstones() + "<br />&nbsp;</html>";
		player2Info = new JLabel(player2Text);
		player2Info.setFont(PLAYER_FONT);
		player2Info.setForeground(Color.BLACK);
		add(player2Info);

		// add plies
		plies = new JLabel("Plies Done: " + state.getPlies());
		plies.setFont(PLAYER_FONT);
		plies.setForeground(PLIES_COLOR);
		add(plies);

		// add last move made
		lastMove = new JLabel("<html>Last Move: " + escapeHtml(state.getLastMove()) + "<br />&nbsp;</html>");
		lastMove.setFont(PLAYER_FONT);
		lastMove.setForeground(PLIES_COLOR);
		add(lastMove);
		
		// Add text field. Keep previous one if it was an error. Make it uneditable if game is over or AI player's turn
		// is next.
		if (textField == null || statusMsg.equals("")) {
			textField = new JTextField();
			textField.setMaximumSize(TEXTFIELD_DIM);
			textField.setBorder(TEXTFIELD_BORDER);
			if (state.getNextPlayer() instanceof AIPlayer) {
				textField.setEditable(false);
			} else {
				textField.addActionListener(action);
			}
		}
		if (gameOver) {
			textField.setEditable(false);
			textField.removeActionListener(action);
		}
		add(textField);

		// add error message
		JLabel errorLabel = new JLabel("<html>&nbsp;<br />" + statusMsg + "</html>");
		errorLabel.setFont(PLAYER_FONT);
		errorLabel.setForeground(Color.RED);
		add(errorLabel);

		revalidate();
		repaint();

		/** Start new thread for AI player to do its thing if game is not already over. */
		if (state.getNextPlayer() instanceof AIPlayer &&
				(state.getStatus(state.getPrevPlayer()) == State.GameStatus.ONGOING)) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					autoMove();
				}
			});
		}
	}

	/** Trigger the next player to move automatically.
	 * Precondition: the next player is an AIPlayer and the game is not yet over */
	private void autoMove() {
		if (!(state.getNextPlayer() instanceof AIPlayer)) throw new RuntimeException("can't autostart human player");
		long startTime = System.currentTimeMillis();
		State.GameStatus gs = state.makeMove(this);
		long endTime = System.currentTimeMillis();
		try {
			TimeUnit.MILLISECONDS.sleep(AI_DELAY - startTime + endTime);
		} catch (InterruptedException e) {}
		switch (gs) {
		case PLAYER1_WIN:
			statusMsg = "Player 1 Wins";
			gameOver = true;
			break;
		case PLAYER2_WIN:
			statusMsg = "Player 2 Wins";
			gameOver = true;
			break;
		case DRAW:
			statusMsg = "Game is a Draw";
			gameOver = true;
			break;
		case ONGOING:
			break;
		default:
			statusMsg = "Internal Error for AI Move";
		}
		tak.updateBoard();
		tak.updateStack();
		updateStatus();
	}

}
