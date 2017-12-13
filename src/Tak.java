import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.*;


/** GUI for the Tak game. */
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
	private StatusGUI statusGUI;
	private State state;
	private String logPath; // path of log file to write

	public State getstate() {
		return state;
	}

	/** Start a new Tak game from state s. */
	private Tak(State s) {
		// basic window properties
		super("TakBot by Aohan Dang");
		setIconImage(new ImageIcon(getClass().getResource("/resources/Icon.png")).getImage());
		//setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(WINDOW_DIM);
		setResizable(false);
		getContentPane().setBackground(BACKGROUND_COLOR);
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		// write to log file on close
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				createLogFile();
			}
		});

		state = s;
		logPath = null;

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
		statusGUI.getTextField().requestFocusInWindow();
	}

	private Tak(State s, String logPath) {
		this(s);
		this.logPath = logPath;
	}

	/** Write information to log file if specified log path is not null, then quit. */
	private void createLogFile() {
		if (logPath != null) {
			try {
				PrintWriter pw = new PrintWriter(new FileWriter(logPath));
				pw.print(state.getAllMoves());
				pw.println("Result\t" + state.getStatus(state.getPrevPlayer()));
				pw.close();
			} catch (IOException e) {
				System.out.println("Error creating log file");
			}
		}
		dispose();
		System.exit(0);
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

	/** Start a game. */
	public static void startGame(String p1type, int p1depth, String p2type, int p2depth) {
		State s = new State();
		Player p1, p2;
		switch (p1type) {
		case "Human":
			p1 = new HumanPlayer(Stone.Color.WHITE, s, Player.Strategy.HUMAN, 0);
			break;
		case "Random":
			p1 = new AIPlayer(Stone.Color.WHITE, s, Player.Strategy.RANDOM, 0);
			break;
		case "Selfish":
			p1 = new AIPlayer(Stone.Color.WHITE, s, Player.Strategy.SELFISH, p1depth);
			break;
		case "SelfishAttacker":
			p1 = new AIPlayer(Stone.Color.WHITE, s, Player.Strategy.SELFISH_ATTACKER, p1depth);
			break;
		case "Attacker":
			p1 = new AIPlayer(Stone.Color.WHITE, s, Player.Strategy.ATTACKER, p1depth);
			break;
		case "Gatherer":
			p1 = new AIPlayer(Stone.Color.WHITE, s, Player.Strategy.GATHERER, p1depth);
			break;
		case "ClusterBuilder":
			p1 = new AIPlayer(Stone.Color.WHITE, s, Player.Strategy.CLUSTERBUILDER, p1depth);
			break;
		case "ClusterBuilderGatherer":
			p1 = new AIPlayer(Stone.Color.WHITE, s, Player.Strategy.CLUSTERBUILDER_GATHERER, p1depth);
			break;
		default:
			throw new IllegalArgumentException("invalid player1 type");
		}
		switch (p2type) {
		case "Human":
			p2 = new HumanPlayer(Stone.Color.BLACK, s, Player.Strategy.HUMAN, 0);
			break;
		case "Random":
			p2 = new AIPlayer(Stone.Color.BLACK, s, Player.Strategy.RANDOM, 0);
			break;
		case "Selfish":
			p2 = new AIPlayer(Stone.Color.BLACK, s, Player.Strategy.SELFISH, p2depth);
			break;
		case "SelfishAttacker":
			p2 = new AIPlayer(Stone.Color.BLACK, s, Player.Strategy.SELFISH_ATTACKER, p2depth);
			break;
		case "Attacker":
			p2 = new AIPlayer(Stone.Color.BLACK, s, Player.Strategy.ATTACKER, p2depth);
			break;
		case "Gatherer":
			p2 = new AIPlayer(Stone.Color.BLACK, s, Player.Strategy.GATHERER, p2depth);
			break;
		case "ClusterBuilder":
			p2 = new AIPlayer(Stone.Color.BLACK, s, Player.Strategy.CLUSTERBUILDER, p2depth);
			break;
		case "ClusterBuilderGatherer":
			p2 = new AIPlayer(Stone.Color.BLACK, s, Player.Strategy.CLUSTERBUILDER_GATHERER, p2depth);
			break;
		default:
			throw new IllegalArgumentException("invalid player2 type");
		}
		s.addPlayers(p1, p2);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new Tak(s);
			}
		});
	}

}
