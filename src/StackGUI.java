import java.awt.Color;
import java.awt.GridLayout;
import java.util.LinkedList;

import javax.swing.JPanel;
import javax.swing.border.LineBorder;

/* An instance is a graphical representation of a stack of pieces on a cell. */
public class StackGUI extends JPanel {
	
	public static final Color BACKGROUND_COLOR = Tak.BACKGROUND_COLOR;
	
	private static class StackGUICell extends JPanel {
		public static final LineBorder BORDER = new LineBorder(Color.BLACK);
		public static final Color BACKGROUND_COLOR = new Color(232, 122, 44);
		
		private JPanel[] grid;  // grid of 9 JPanels to denote color
		
		/* Initialize a cell in the Stack component of the GUI with stone stone. If stone is null, have an empty
		 * cell. */
		public StackGUICell(Stone stone) {
			// general button settings
			setBackground(BACKGROUND_COLOR);
			setLayout(new GridLayout(3,3));
			setBorder(BORDER);
			
			// set grid colors
			grid = new JPanel[9];
			for (int i = 0; i < 9; i++) {
				JPanel jp = new JPanel();
				jp.setBackground(BACKGROUND_COLOR);
				grid[i] = jp;
				add(jp);
			}
			if (stone == null) return;
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
	
	private Board board;
	private StackGUICell[] stack;
	
	public StackGUI(Board b) {
		super(new GridLayout(Tak.STACK_SIZE, 1));
		setBorder(new LineBorder(Tak.BORDER_COLOR));
		setPreferredSize(Tak.STACK_DIM);
		setBackground(BACKGROUND_COLOR);
		
		board = b;
		stack = new StackGUICell[Tak.STACK_SIZE];
		for (int i = 0; i < Tak.STACK_SIZE; i++) {
			StackGUICell newCell = new StackGUICell(null);
			stack[i] = newCell;
			add(newCell);
		}
	}
	
	/* Update this stack to include up to Tak.STACK_SIZE stones at the top at (row,col). */
	public void updateStack(int row, int col) {
		removeAll();
		LinkedList<Stone> ss = board.cellContents(row, col);
		int i = Math.max(0, stack.length - ss.size());
		for (int j = 0; j < i; j++) stack[j] = new StackGUICell(null);
		for (Stone s: ss) {
			stack[i++] = new StackGUICell(s);
			if (i == stack.length) break;
		}
		for (i = 0; i < stack.length; i++) add(stack[i]);
		revalidate();
		repaint();
	}
	
}
