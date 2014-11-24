package nl.scholten.crypto.cryptobox;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public abstract class Matrix {

	private static enum OPERATION {
		COL_UP, COL_DOWN, ROW_LEFT, ROW_RIGHT
	}

	protected String data;
	protected char[] dataArray;

	private int maxSteps;

	private int size;

	private long tries;

	private long start;

	private int maxScore;

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
		this.data = new String(input.toCharArray());
		//hacketiy hack to get access to the char[] as well.
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
	 * @param x
	 * @param y
	 * @return
	 */
	public void setXY(int x, int y, char c) {
		dataArray[(x * size) + y] = c;
	}
	
	/**
	 * x = row, y = col
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
		char temp = getXY(size -1, column);

		for (int row = size - 1; row > 0; row--) {
			setXY(row, column, getXY(row - 1, column));
		}

		setXY(0, column, temp);
	}

	public void shiftRowLeft(int row) {
		// TODO check out of bounds on row?

		char temp = getXY(row, 0);
		
		System.arraycopy(dataArray, (row * size) + 1, dataArray, (row * size),  (size -1));
		
		setXY(row, size - 1, temp);
		
	}

	public void shiftRowRight(int row) {
		// TODO check out of bounds on row?

		char temp = getXY(row, size - 1);
		
		System.arraycopy(dataArray, (row * size), dataArray, (row * size) + 1,  (size -1));
		
		setXY(row, 0, temp);

	}

	private void apply(OPERATION op, int index) {

		switch (op) {

		case COL_UP:
			shiftColumnUp(index);
			break;
		case COL_DOWN:
			shiftColumnDown(index);
			break;
		case ROW_LEFT:
			shiftRowLeft(index);
			break;
		case ROW_RIGHT:
			shiftRowRight(index);
			break;

		default:
			throw new IllegalStateException("Unknown operation " + op);
		}
	}

	// TODO deduplicate?
	private void unapply(OPERATION op, int index) {

		switch (op) {

		case COL_UP:
			shiftColumnDown(index);
			break;
		case COL_DOWN:
			shiftColumnUp(index);
			break;
		case ROW_LEFT:
			shiftRowRight(index);
			break;
		case ROW_RIGHT:
			shiftRowLeft(index);
			break;

		default:
			throw new IllegalStateException("Unknown operation " + op);
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

	abstract protected boolean isSolved();

	abstract protected int score();

	public static Matrix solve(Matrix org, Matrix m) {
		m.tries = 0;
		m.start = System.currentTimeMillis();
		return solveRecursive(org, m, m.maxSteps, new ArrayList<String>());
	}

	private static Matrix solveRecursive(Matrix org, Matrix m, int stepsLeft,
			List<String> opsLog) {
		if (stepsLeft == 0) {
			long now = System.currentTimeMillis();
			int score = m.score();
			boolean solved = false && m.isSolved();

			if (score >= m.maxScore || solved) {
				m.maxScore = score;
				System.out.println(opsLog);
				System.out.println(Matrix.toStringSideBySide(org, m));
				System.out.println("Score: " + m.score() + " isSolved: "
						+ solved);
				System.out.println("Plain: " + m.toString());
				System.out.println("Total tries: " + m.tries + " which is "
						+ (m.tries * 1000) / (Math.max(1, now - m.start))
						+ " tries/second. " + opsLog);
			} else {
				m.tries++;
				if (m.tries % 1000000 == 0) {
					System.out.println("Total tries: " + m.tries + " which is "
							+ (m.tries * 1000) / (Math.max(1, now - m.start))
							+ " tries/second. " + opsLog);
				}
			}
			return m;
		}

		for (int index = 0; index < m.size; index++) {
			// assume square matrix #rows=#cols
			for (OPERATION op : OPERATION.values()) {
				opsLog.add(op + "(" + index + ") ");
//				System.out.println(opsLog.toString());
				m.apply(op, index);
				// System.out.println(Matrix.toStringSideBySide(org, m));
				Matrix.solveRecursive(org, m, stepsLeft - 1, opsLog);
				m.unapply(op, index);
				opsLog.remove(opsLog.size() - 1);

			}
		}

		// System.gc();

		return m;
	}

}
