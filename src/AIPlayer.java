import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Random;

/** An instance represents an AI that can play Tak. */
public class AIPlayer extends Player {

	/** An instance represents a state, the move required to get to this state, and the heuristic evaluation of this
	 * state. */
	protected static class MoveStateEval {
		public Move move;
		public State state;
		public int eval;
		public MoveStateEval(Move move, State state, int eval) {
			this.move = move;
			this.state = state;
			this.eval = eval;
		}
	}
	
	/* Hard-coded permutations of partitions of 1 through 5 with maximal length 4. The 0th element is an empty
	 * placeholder. */
	public static final String[][] PARTITIONS = new String[][] {
		null,
		new String[] {"1"},
		new String[] {"2", "11"},
		new String[] {"3", "21", "12", "111"},
		new String[] {"4", "31", "13", "22", "211", "121", "112", "1111"},
		new String[] {"5", "41", "14", "32", "23", "311", "131", "113", "221", "212", "122", "2111", "1211", "1121", "1112"}
	};
	public static final char[] DIRECTIONS = new char[] {'+', '-', '<', '>'};
	public static final Random RANDOM = new Random();
	
	public AIPlayer(Stone.Color c, State s, Strategy strategy) {
		super(c, s, strategy);
	}

	/** Return true if the (row,col) cell is empty or owned by the next player. */
	private static boolean canModify(State state, int row, int col) {
		Stone top = state.getBoard().topStone(row, col);
		return top == null || top.getColor() == state.getNextPlayer().color;
	}

	/** Return a copy of the state that results from having the next player making the move m. We update everything
	 * about the new state when the new move is made. The original state is unmodified. Throws IllegalMove if the move
	 * is not legal. */
	private static State childState(State state, Move m) {
		state = state.clone();
		switch (m.getMoveType()) {
		case PLACE_FLAT_STONE:
			if (state.getNextPlayer().stones == 0) throw new Board.IllegalMove("no more regular stones");
			state.getBoard().addStone(new Stone(state.getNextPlayer().color, Stone.Type.REGULAR, Stone.Status.FLAT),
					m.getRow(), m.getCol());
			state.getNextPlayer().stones--;
			break;
		case PLACE_STANDING_STONE:
			if (state.getNextPlayer().stones == 0) throw new Board.IllegalMove("no more regular stones");
			state.getBoard().addStone(new Stone(state.getNextPlayer().color, Stone.Type.REGULAR, Stone.Status.STANDING),
					m.getRow(), m.getCol());
			state.getNextPlayer().stones--;
			break;
		case PLACE_CAPSTONE:
			if (state.getNextPlayer().capstones == 0) throw new Board.IllegalMove("no more capstones");
			state.getBoard().addStone(new Stone(state.getNextPlayer().color, Stone.Type.CAPSTONE, Stone.Status.STANDING),
					m.getRow(), m.getCol());
			state.getNextPlayer().capstones--;
			break;
		case MOVE_STACK:
			if (!canModify(state, m.getRow(), m.getCol())) throw new Board.IllegalMove("you can't move the stack here");
			state.getBoard().moveStack(m.getN(), m.getDirection(), m.getDrops(), m.getRow(), m.getCol());
		}
		state.swapNextPlayer();
		state.incPlies();
		return state;
	}

	/** Evaluate state s in the perspective of the player with color c using strategy strategy. */
	public static int evaluate(State s, Stone.Color c, Strategy strategy) {
		switch (strategy) {
		case RANDOM:
			return RANDOM.nextInt();
		default:
			throw new UnsupportedOperationException("evaluate strategy not supported");
		}
	}

	/** Return a priority queue of all (nextState,moveToGetHere,nextStateEval) that the next player in state s can do.
	 * The queue is ordered using the evaluation function in the perspective of the player with color c using strategy
	 * strategy. If maxMin is true, it is a max-queue. If maxMin is false, then it's a min-queue. */
	private static PriorityQueue<MoveStateEval> getPossibleMoves(State s, boolean maxMin, Stone.Color c, Player.Strategy strategy) {
		// Create a priority queue to optimize order in which nodes will be expanded by minimax
		PriorityQueue<MoveStateEval> queue;
		if (maxMin) {
			queue = new PriorityQueue<MoveStateEval>(new Comparator<MoveStateEval>() {
				public int compare(MoveStateEval s1, MoveStateEval s2) {
					return s2.eval - s1.eval;
				}
			});
		} else {
			queue = new PriorityQueue<MoveStateEval>(new Comparator<MoveStateEval>() {
				public int compare(MoveStateEval s1, MoveStateEval s2) {
					return s1.eval - s2.eval;
				}
			});
		}

		// if game is already over, there are no more moves
		if (s.getPlies() != 0 && s.getStatus(s.getPrevPlayer()) != State.GameStatus.ONGOING) return queue;

		// moves for placing new stone
		if (!s.getNextPlayer().isOut()) {
			for (Pair coords : s.getBoard().emptyCells()) {
				String suffix = "(" + coords.row + "," + coords.col + ")";
				if (s.getNextPlayer().stones > 0) {
					Move flatMove = new Move("F" + suffix);
					State flatState = childState(s, flatMove);
					int flatEval = evaluate(flatState, c, strategy);
					queue.offer(new MoveStateEval(flatMove, flatState, flatEval));
					Move standMove = new Move("S" + suffix);
					State standState = childState(s, standMove);
					int standEval = evaluate(standState, c, strategy);
					queue.offer(new MoveStateEval(standMove, standState, standEval));
				}
				if (s.getNextPlayer().capstones > 0) {
					Move capMove = new Move("C" + suffix);
					State capState = childState(s, capMove);
					int capEval = evaluate(capState, c, strategy);
					queue.offer(new MoveStateEval(capMove, capState, capEval));
				}
			}
		}
		
		// moves for moving a stack
		for (int i = 0; i < Board.SIZE; i++) {
			for (int j = 0; j < Board.SIZE; j++) {
				if (s.getBoard().topColor(i, j) != c) continue;  // skip cells we can't move
				for (char d : DIRECTIONS) {
					for (int n = 1; n <= Math.min(Board.CARRY_LIMIT, s.getBoard().cellContents(i, j).size()); n++) {
						for (String dropPattern : PARTITIONS[n]) {
							String stackMoveString = "M(" + i + "," + j + ")" + d + n + "[" + dropPattern + "]";
							Move stackMove = new Move(stackMoveString);
							State stackState;
							try {  // skip illegal moves
								stackState = childState(s, stackMove);
							} catch (Board.IllegalMove e) {
								continue;
							}
							int stackEval = evaluate(stackState, c, strategy);
							queue.offer(new MoveStateEval(stackMove, stackState, stackEval));
						}
					}
				}
			}
		}
		//for (MoveStateEval entry : queue) System.out.println(entry.move);
		return queue;
	}

	public ResultMove makeMove(StatusGUI status) {
		PriorityQueue<MoveStateEval> branches = getPossibleMoves(state, true, color, strategy);
		Move chosenMove = branches.peek().move;
		executeMove(chosenMove);
		return new ResultMove(0, chosenMove);
	}

}
