import java.awt.*;

import javax.swing.*;


/* GUI for the Tak game. */
public class Tak extends JFrame {
	public static final Dimension SCREEN_DIM = Toolkit.getDefaultToolkit().getScreenSize();
	public static final Dimension WINDOW_DIM = new Dimension((int)(SCREEN_DIM.getWidth()*0.8),
			(int)(SCREEN_DIM.getHeight()*0.8));
	public static final Dimension BOARD_DIM = new Dimension((int)(WINDOW_DIM.getHeight()*0.95),
			(int)(WINDOW_DIM.getHeight() * 0.95));
	public static final Dimension STACK_DIM = new Dimension((int)(BOARD_DIM.getWidth()/10), (int)BOARD_DIM.getHeight());
	public static final Dimension STATUS_DIM =
			new Dimension((int)(0.97*(WINDOW_DIM.getWidth()-BOARD_DIM.getWidth()-STACK_DIM.getWidth())),
					(int)BOARD_DIM.getHeight());
	public static final Color BORDER_COLOR = Color.BLACK;
	public static final int BORDER_THICKNESS = 2;
	public static final Color BACKGROUND_COLOR = Color.BLACK;
	public static final int STACK_SIZE = 10;

	private BoardGUI boardGUI;
	private StackGUI stackGUI;
	private JPanel statusGUI;
	private State state;

	private Tak(State s) {
		// basic window properties
		super("Tak");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(WINDOW_DIM);
		setResizable(false);
		getContentPane().setBackground(BACKGROUND_COLOR);
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		state = s;

		// stack information box
		stackGUI = new StackGUI(state.getBoard());
		c.gridx = 0;
		add(stackGUI, c);

		// board grid
		boardGUI = new BoardGUI(this, state.getBoard());
		c.gridx = 1;
		add(boardGUI, c);

		// status box
		statusGUI = new StatusGUI(this, state);
		c.gridx = 2;
		add(statusGUI, c);

		setVisible(true);
	}

	/* Update the stack by having it display the stack at (row,col). */
	public void updateStack(int row, int col) {
		stackGUI.updateStack(row, col);
	}

	/* Reset the stack display to be empty. */
	public void updateStack() {
		getContentPane().removeAll();

		GridBagConstraints c = new GridBagConstraints();
		// stack information box
		stackGUI = new StackGUI(state.getBoard());
		c.gridx = 0;
		add(stackGUI, c);

		// board grid
		c.gridx = 1;
		add(boardGUI, c);

		// status box
		c.gridx = 2;
		add(statusGUI, c);

		revalidate();
		repaint();
	}

	/* Update the board component. */
	public void updateBoard() {
		getContentPane().removeAll();

		GridBagConstraints c = new GridBagConstraints();
		// stack information box
		c.gridx = 0;
		add(stackGUI, c);

		// board grid
		boardGUI = new BoardGUI(this, state.getBoard());
		c.gridx = 1;
		add(boardGUI, c);

		// status box
		c.gridx = 2;
		add(statusGUI, c);

		revalidate();
		repaint();
	}

	public static void main(String[] args) {
		// 2 human players
		State s = new State();
		Player p1human = new HumanPlayer(Stone.Color.WHITE, s, Player.Strategy.HUMAN);
		Player p1ai = new AIPlayer(Stone.Color.WHITE, s, Player.Strategy.RANDOM);
		Player p2human = new HumanPlayer(Stone.Color.BLACK, s, Player.Strategy.HUMAN);
		Player p2ai = new AIPlayer(Stone.Color.BLACK, s, Player.Strategy.RANDOM);
		s.addPlayers(p1ai, p2ai);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new Tak(s);
			}
		});
	}
}
