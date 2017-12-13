import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

/** Ask the user for preferences and start a Tak game. */
public class Start extends JFrame {

	public static final String[] STRATEGIES1 = new String[] {"-Select Player 1-", "Human", "Random", "Selfish",
			"Attacker", "SelfishAttacker", "Gatherer", "ClusterBuilder", "ClusterBuilderGatherer"};
	public static final String[] DEPTH1 = new String[] {"-Select Player 1 Depth-", "1", "2", "3", "4", "5"};
	public static final String[] STRATEGIES2 = new String[] {"-Select Player 2-", "Human", "Random", "Selfish",
			"Attacker", "SelfishAttacker", "Gatherer", "ClusterBuilder", "ClusterBuilderGatherer"};
	public static final String[] DEPTH2 = new String[] {"-Select Player 2 Depth-", "1", "2", "3", "4", "5"};

	JComboBox<String> player1Strategy;
	JComboBox<String> player1Depth;
	JComboBox<String> player2Strategy;
	JComboBox<String> player2Depth;
	JButton start;


	public Start() {
		setTitle("TakBot by Aohan Dang");
		setIconImage(new ImageIcon(getClass().getResource("/resources/Icon.png")).getImage());
		setLayout(new GridLayout(5,1));

		player1Strategy = new JComboBox<String>(STRATEGIES1);
		player1Strategy.setForeground(Color.BLACK);
		player1Strategy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				start.setEnabled(readyToPlay());
				String s = (String) player1Strategy.getSelectedItem();
				if (s.equals(STRATEGIES1[0]) || s.equals(STRATEGIES1[1]) || s.equals(STRATEGIES1[2])) {
					player1Depth.setEnabled(false);
				} else {
					player1Depth.setEnabled(true);
				}
			}
		});
		add(player1Strategy);

		player1Depth = new JComboBox<String>(DEPTH1);
		player1Depth.setForeground(Color.BLACK);
		player1Depth.setEnabled(false);
		player1Depth.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				start.setEnabled(readyToPlay());
			}
		});
		add(player1Depth);

		player2Strategy = new JComboBox<String>(STRATEGIES2);
		player2Strategy.setForeground(Color.BLACK);
		player2Strategy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				start.setEnabled(readyToPlay());
				String s = (String) player2Strategy.getSelectedItem();
				if (s.equals(STRATEGIES2[0]) || s.equals(STRATEGIES2[1]) || s.equals(STRATEGIES2[2])) {
					player2Depth.setEnabled(false);
				} else {
					player2Depth.setEnabled(true);
				}
			}
		});
		add(player2Strategy);

		player2Depth = new JComboBox<String>(DEPTH2);
		player2Depth.setForeground(Color.BLACK);
		player2Depth.setEnabled(false);
		player2Depth.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				start.setEnabled(readyToPlay());
			}
		});
		add(player2Depth);

		start = new JButton("Start Game");
		start.setForeground(Color.BLACK);
		start.setEnabled(false);
		start.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String p1type = (String) player1Strategy.getSelectedItem();
				int p1depth;
				try {
					p1depth = Integer.parseInt((String) player1Depth.getSelectedItem());
				} catch (NumberFormatException ex) {
					p1depth = 0;
				}
				String p2type = (String) player2Strategy.getSelectedItem();
				int p2depth;
				try {
					p2depth = Integer.parseInt((String) player2Depth.getSelectedItem());
				} catch (NumberFormatException ex) {
					p2depth = 0;
				}
				Tak.startGame(p1type, p1depth, p2type, p2depth);
				dispose();
			}
		});
		add(start);

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(300, 300);
		setResizable(false);

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((int) ((dim.getWidth() - getWidth()) / 2), (int) ((dim.getHeight() - getHeight()) / 2));
		setVisible(true);
	}

	/** Return true if the user has given all information needed to start a game, false otherwise. */
	public boolean readyToPlay() {
		int p1Selected = player1Strategy.getSelectedIndex();
		int p1depth = player1Depth.getSelectedIndex();
		if (p1Selected == 0) return false;
		if (p1Selected > 2 && p1depth == 0) return false;
		int p2Selected = player2Strategy.getSelectedIndex();
		int p2depth = player2Depth.getSelectedIndex();
		if (p2Selected == 0) return false;
		if (p2Selected > 2 && p2depth == 0) return false;
		return true;
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new Start();
			}
		});
	}

}
