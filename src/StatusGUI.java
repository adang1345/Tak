import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
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

	private Tak tak;
	private State state;
	private JLabel nextPlayer;
	private JLabel player1Info;
	private JLabel player2Info;
	private JLabel plies;
	private JTextField textField;
	private String statusMsg = "";
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
		
		do updateStatus();
		while (state.getNextPlayer() instanceof AIPlayer);

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
		plies = new JLabel("<html>Plies Done: " + state.getPlies() + "<br />&nbsp;</html>");
		plies.setFont(PLAYER_FONT);
		plies.setForeground(new Color(0, 0, 102));
		add(plies);

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

		// make move automatically if AI player
		if (state.getNextPlayer() instanceof AIPlayer) {
			state.makeMove(this);
			tak.updateBoard();
		}

		revalidate();
		repaint();
	}

	/* If current player is AI player, trigger it to make a move and repeat. Otherwise, do nothing. */
	private void startAutoMoves() {

	}

}
