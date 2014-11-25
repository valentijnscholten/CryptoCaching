package nl.scholten.crypto.cryptobox;

/**
 * Performance notes:
 * - making startswith a hit helps, 10x performance/
 * - making startswith known hit mandatory helps most, doubling performance
 * - doing no score helps, doubling performance again.
 * - doing no apply does help, but not a lot (20%).
 */
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public abstract class Matrix {

	private static enum OPERATION {
		COL_UP(0, "CU"), COL_DOWN(1, "CD"), ROW_LEFT(2, "RL"), ROW_RIGHT(3, "RR");
		
		private int number;
		private String value;

		private OPERATION(int number, String value) {
			this.number = number;
			this.value = value;
		}
		
		public static OPERATION parseValue(String value) {
			for (OPERATION op: OPERATION.values()) {
				if (op.getValue().equals(value)) return op;
			}
			return null;
		}

		public String getValue() {
			return this.value;
		}

		public String toString() {
			return getValue();
		}

		
	}

	public class OperationInstance {
		public OPERATION op;
		public int index;

		public OperationInstance(OPERATION op, int index) {
			this.op = op;
			this.index = index;
		}
		
		public String toString() {
			return op.toString() + "_" + index;
		}
	}
	
	protected String data;
	protected char[] dataArray;

	private int maxSteps;

	private int size;

	private long tries;

	private long start;

	private int maxScore;
	private List<List<OperationInstance>> maxScorers;
	private List<String> maxScorersDetails;

	public Matrix(String input, int size, int maxSteps) {
		String cleaned = StringUtils.deleteWhitespace(input);
		// String[] rows = cleaned.split("(?<=\\G.{" + size + "})");
		// char[][] matrix = new char[size][size];
		// int i = 0;
		// for (String row: rows) {
		// matrix[i] = row.toCharArray();
		// i++;
		// }
		init(cleaned, size, maxSteps);
	}

	// public Matrix(char[][] input, int size, int maxSteps) {
	// init(clone2DArray(input), size, maxSteps);
	// }

	public Matrix(Matrix m1) {
		init(m1.data, m1.size, m1.maxSteps);
	}

	protected void init(String input, int size, int maxSteps) {
		this.size = size;
		this.maxSteps = maxSteps;
		this.maxScore = 0;
		this.maxScorers = new ArrayList<List<OperationInstance>>();
		this.maxScorersDetails = new ArrayList<String>();
		this.data = new String(input.toCharArray());
		// hacketiy hack to get access to the char[] as well.
		Field field;
		try {
			field = data.getClass().getDeclaredField("value");
			field.setAccessible(true);
			this.dataArray = ((char[]) field.get(data));
		} catch (NoSuchFieldException | SecurityException
				| IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException();
		}
	}

	public String toString() {
		return data;
	}

	public String toStringPretty() {
		StringBuilder result = new StringBuilder();
		String[] rows = data.split("(?<=\\G.{" + size + "})");

		for (String row : rows) {
			if (result.length() > 0)
				result.append("\n");
			result.append(row);
		}
		return result.toString();
	}

	// TODO deduplicate shifts
	/**
	 * x = row, y = col
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public void setXY(int x, int y, char c) {
		dataArray[(x * size) + y] = c;
	}

	/**
	 * x = row, y = col
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public char getXY(int x, int y) {
		return dataArray[(x * size) + y];
	}

	public void shiftColumnUp(int column) {
		// TODO check out of bounds on column?
		char temp = getXY(0, column);

		for (int row = 0; row < size - 1; row++) {
			setXY(row, column, getXY(row + 1, column));
		}

		setXY(size - 1, column, temp);
	}

	public void shiftColumnDown(int column) {
		// TODO check out of bounds on column?
		char temp = getXY(size - 1, column);

		for (int row = size - 1; row > 0; row--) {
			setXY(row, column, getXY(row - 1, column));
		}

		setXY(0, column, temp);
	}

	public void shiftRowLeft(int row) {
		// TODO check out of bounds on row?

		char temp = getXY(row, 0);

		System.arraycopy(dataArray, (row * size) + 1, dataArray, (row * size),
				(size - 1));

		setXY(row, size - 1, temp);

	}

	public void shiftRowRight(int row) {
		// TODO check out of bounds on row?

		char temp = getXY(row, size - 1);

		System.arraycopy(dataArray, (row * size), dataArray, (row * size) + 1,
				(size - 1));

		setXY(row, 0, temp);

	}

	private void apply(OperationInstance oi) {

		switch (oi.op) {

		case COL_UP:
			shiftColumnUp(oi.index);
			break;
		case COL_DOWN:
			shiftColumnDown(oi.index);
			break;
		case ROW_LEFT:
			shiftRowLeft(oi.index);
			break;
		case ROW_RIGHT:
			shiftRowRight(oi.index);
			break;

		default:
			throw new IllegalStateException("Unknown operation " + oi.op);
		}
	}

	// TODO deduplicate?
	private void unapply(OperationInstance oi) {

		switch (oi.op) {

		case COL_UP:
			shiftColumnDown(oi.index);
			break;
		case COL_DOWN:
			shiftColumnUp(oi.index);
			break;
		case ROW_LEFT:
			shiftRowRight(oi.index);
			break;
		case ROW_RIGHT:
			shiftRowLeft(oi.index);
			break;

		default:
			throw new IllegalStateException("Unknown operation " + oi.op);
		}
	}

	public String getRow(int i) {
		return data.substring(i * size, (i * size) + size);
	}

	public static String toStringSideBySide(Matrix... matrices) {

		if (matrices.length == 0)
			return "";
		StringBuilder result = new StringBuilder();

		for (int row = 0; row < matrices[0].size; row++) {
			for (Matrix m : matrices) {
				if (result.length() > 0
						&& result.charAt(result.length() - 1) != '\n')
					result.append("\t");
				result.append(m.getRow(row));
			}
			result.append("\n");
		}
		return result.toString();
	}

	/**
	 * Creates an independent copy(clone) of the char array.
	 * 
	 * @param array
	 *            The array to be cloned.
	 * @return An independent 'deep' structure clone of the array.
	 */
	public static char[][] clone2DArray(char[][] array) {
		int rows = array.length;
		// int rowIs=array[0].length ;

		// clone the 'shallow' structure of array
		char[][] newArray = (char[][]) array.clone();
		// clone the 'deep' structure of array
		for (int row = 0; row < rows; row++) {
			newArray[row] = (char[]) array[row].clone();
		}

		return newArray;
	}

	/**
	 * parses state string, sets internal field maxscore, maxscorers, maxscorersdetails. Return external opsLog.
	 * @param state
	 * @return
	 */
	public List<OperationInstance> parseState(String state) {
		String[] parts = state.split("][[");
		//parts[0] == last opslog
		return parseOpsLog(parts[0]);
		//parts[1] == maxScorers opslog
//		maxScorers = parseMaxScorers(parts[1]);
		//TODO replay to get maxscore
		//parts[2] == maxScorersDetails
//		maxScorersDetails = parseMaxScorersDetails(parts[2]);
	}
	
	private List<OperationInstance> parseOpsLog(String stateString) {
		//[CD_0, CU_8, RL_5, CU_2]
		String clean = stateString;
		clean = StringUtils.remove(stateString, ']');
		clean = StringUtils.remove(clean, '[');
		clean = StringUtils.deleteWhitespace(clean);
		
//		String[] parts = 
		return null;
	}

	abstract protected boolean isSolved();

	abstract protected int score();

	// doesn't really return the best matrix, prints results only
	public static void solve(Matrix org, Matrix m) {
		m.tries = 0;
		m.start = System.currentTimeMillis();
		solveRecursive(org, m, m.maxSteps, new ArrayList<OperationInstance>());
		long now = System.currentTimeMillis();
		System.out.println("Total tries: " + m.tries + " maxScore: "
				+ m.maxScore + " total time " + ((now - m.start) / 1000)
				+ "s. which is " + (m.tries / (now - m.start))
				+ " tries per second. maxScorers: " + m.maxScorers);
		System.out.println(m.maxScorersDetails);
	}

	private static void solveRecursive(Matrix org, Matrix m, int stepsLeft,
			ArrayList<OperationInstance> opsLog) {

		if (stepsLeft == 0) {
			long now = System.currentTimeMillis();
			int score = m.score();
			// int score = 0;
			boolean solved = false && m.isSolved();

			if (score > 10 && score >= m.maxScore || solved) {
				if (score > m.maxScore) {
					m.maxScorers.clear();
					m.maxScorersDetails.clear();
				}
				m.maxScore = score;
				m.maxScorers.add((ArrayList<OperationInstance>) opsLog.clone());
				m.maxScorersDetails.add(new String(m.data.toCharArray()));
				System.out.println(opsLog);
				System.out.println(Matrix.toStringSideBySide(org, m));
				System.out.println("Score: " + m.score() + " isSolved: "
						+ solved);
				System.out.println("Plain: " + m.toString());
				System.out.println("Current tries: " + m.tries + " which is "
						+ (m.tries * 1000) / (Math.max(1, now - m.start))
						+ " tries/second. " + opsLog);
			} else {
				m.tries++;
//				if (m.tries % 10000000000l == 0) {
				if (m.tries % 1l == 0) {
					System.out.println("Current tries: " + m.tries + " which is "
							+ (m.tries * 1000) / (Math.max(1, now - m.start))
							+ " tries/second. maxScore: "
							+ m.maxScore + " state: " + opsLog + m.maxScorers + m.maxScorersDetails);
				}
			}
			// if (score > 0) System.out.println(opsLog + " score: " + score);
			return;
		}

		for (int index = 0; index < m.size; index++) {
			// assume square matrix #rows=#cols
			for (OPERATION op : OPERATION.values()) {
				OperationInstance oi = m.new OperationInstance(op, index);
				opsLog.add(oi);
				// System.out.println(opsLog.toString());
				m.apply(oi);
				// System.out.println(Matrix.toStringSideBySide(org, m));
				Matrix.solveRecursive(org, m, stepsLeft - 1, opsLog);
				m.unapply(oi);
				opsLog.remove(opsLog.size() - 1);

			}
		}

		// System.gc();
	}

}
