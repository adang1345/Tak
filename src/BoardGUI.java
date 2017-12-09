import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.LineBorder;

/* The GUI for the board */
public class BoardGUI extends JPanel {
	
	private static class BoardGUICell extends JButton {
		public static final Color BACKGROUND_COLOR = new Color(0, 204, 255);
		public static final LineBorder BORDER = new LineBorder(Color.BLACK);
		
		private Tak tak;
		private JPanel[] grid;  // grid of 9 JPanels to denote color
		private int row;
		private int col;
		
		public BoardGUICell(Tak tak, Board b, int row, int col) {
			// general button settings
			setBackground(BACKGROUND_COLOR);
			setRolloverEnabled(false);
			setFocusPainted(false);
			setLayout(new GridLayout(3,3));
			setBorder(BORDER);
			
			this.tak = tak;
			this.row = row;
			this.col = col;
			
			// add listener for click
			addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					tak.updateStack(row, col);
				}
			});
			
			// set grid colors based on what top stone is
			grid = new JPanel[9];
			for (int i = 0; i < 9; i++) {
				JPanel jp = new JPanel();
				jp.setBackground(BACKGROUND_COLOR);
				grid[i] = jp;
				add(jp);
			}
			Stone stone = b.topStone(row, col);
			if (stone == null) {
				return;
			}
			Color stoneColor = stone.getColor() == Stone.Color.WHITE ? Color.WHITE : Color.BLACK;
			int[] toColor;
			if (stone.getType() == Stone.Type.CAPSTONE) {
				toColor = new int[] {1,3,4,5,6,7,8};
			} else if (stone.getStatus() == Stone.Status.FLAT) {
				toColor = new int[] {6,7,8};
			} else {
				toColor = new int[] {1,4,7};
			}
			for (int i : toColor) grid[i].setBackground(stoneColor);
		}
	}
	
	private Tak tak;  // parent Tak window
	private Board board; // the underlying board
	private BoardGUICell[][] cells;  // the GUI cells
	
	public BoardGUI(Tak tak, Board b) {
		// general panel settings
		super(new GridLayout(Board.SIZE, Board.SIZE));
		setBorder(new LineBorder(Tak.BORDER_COLOR, Tak.BORDER_THICKNESS));
		setPreferredSize(Tak.BOARD_DIM);
		setBackground(Tak.BACKGROUND_COLOR);
		
		this.tak = tak;
		board = b;
		
		// cells
		cells = new BoardGUICell[Board.SIZE][Board.SIZE];
		for (int i = 0; i < Board.SIZE; i++) {
			for (int j = 0; j < Board.SIZE; j++) {
				BoardGUICell newCell = new BoardGUICell(tak, board, i, j);
				cells[i][j] = newCell;
			}
		}
		for (int i = Board.SIZE-1; i >= 0; i--) {
			for (int j = 0; j < Board.SIZE; j++) {
				add(cells[i][j]);
			}
		}
	}
	
	/* Update this board's display */
	/*public void updateBoard() {
		removeAll();
		
		cells = new BoardGUICell[Board.SIZE][Board.SIZE];
		for (int i = 0; i < Board.SIZE; i++) {
			for (int j = 0; j < Board.SIZE; j++) {
				BoardGUICell newCell = new BoardGUICell(tak, board, i, j);
				cells[i][j] = newCell;
			}
		}
		for (int i = Board.SIZE-1; i >= 0; i--) {
			for (int j = 0; j < Board.SIZE; j++) {
				add(cells[i][j]);
			}
		}
		
		revalidate();
		repaint();
	}*/
	
	
}
