package nl.scholten.crypto.cryptobox;

/**
 * Performance notes:
 * - making startswith a hit helps, 10x performance/
 * - making startswith known hit mandatory helps most, doubling performance
 * - doing no score helps, doubling performance again.
 * - doing no apply does help, but not a lot (20%).
 * - fork join with SERIAL_STEPS == 3 == size-11 helps 400% 
 * - hype threading does help about 50%.
 * 
 * 
 * 
 * 
 * - all cu/cd exclusive -> order doesn't matter
 * [CU_9, CU_6, CU_5, CD_1], 
 * [CU_9, CU_6, CD_1, CU_5]
 * - also if first (or last?) operation is rl/rr but constant, and rest cu/cd exclusive:
 * 	[CU_8, CD_1, CU_1, RL_4], 
 *	[CU_8, CD_0, CU_0, RL_4], 
 * - also vice versa rl/rr exclusive, also with other parts constant
 * - rr compensate rl and cu compensate cl if index is equal and exclusive
 * - rr direct after rl same index does nothing
 * 
 * 
 * 973714 unique keys out of 40^4 = 2560000  = 0,38 = 38%, so over 60% should be dropped by optimize
 * 
 * step1: 
 * 
 * 
 */
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;

@SuppressWarnings("serial")
public abstract class Matrix extends RecursiveTask<Matrix> {

	private AtomicLong triesGlobal = new AtomicLong();
	private AtomicLong maxScoreGlobal = new AtomicLong();

	private final static boolean COUNT_ATOMIC = true;
	private final static boolean GATHER_DUPLICATES = true;
	
	/**
	 * How many paraellel steps before going serial.
	 */
	public static int PARALLEL_STEPS = 1;

	public static ForkJoinPool fjPool = new ForkJoinPool();

	private static enum OPERATION {
		COL_UP(true, false, "CU"), COL_DOWN(true, false, "CD"), ROW_LEFT(false, true, "RL"), ROW_RIGHT(false, true,
				"RR");

		private boolean isCol;
		private boolean isRow;
		private String value;

		private OPERATION(boolean isCol, boolean isRow, String value) {
			this.isCol = isCol;
			this.isRow = isRow;
			this.value = value;
		}

		public static OPERATION parseValue(String value) {
			for (OPERATION op : OPERATION.values()) {
				if (op.getValue().equals(value))
					return op;
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

	public List<OperationInstance> ois;
	public ArrayList<OperationInstance> opsLog;

	protected String data;
	protected char[] dataArray;

	private int stepsLeft;

	private int size;
	private long tries;
	private long start;

	private int maxScore;
	private List<Matrix> maxScorers;
	private int serialStepsLeft;
	private Double bruteTries;
	
	Map<String, List<List<OperationInstance>>> duplicates = new HashMap<String, List<List<OperationInstance>>>();

	public Matrix(String input, int size, int maxSteps) {
		String cleaned = StringUtils.deleteWhitespace(input);
		// String[] rows = cleaned.split("(?<=\\G.{" + size + "})");
		// char[][] matrix = new char[size][size];
		// int i = 0;
		// for (String row: rows) {
		// matrix[i] = row.toCharArray();
		// i++;
		// }
		init(cleaned, size, maxSteps, new ArrayList<OperationInstance>());
	}

	protected void init(Matrix m) {
		this.init(m.data, m.size, m.stepsLeft, m.opsLog);
		this.maxScore = m.maxScore;
		this.maxScorers = new ArrayList<Matrix>(m.maxScorers);
		this.start = m.start;
		this.tries = m.tries;
		this.serialStepsLeft = m.serialStepsLeft;
		this.triesGlobal = m.triesGlobal;
		this.maxScoreGlobal = m.maxScoreGlobal;
	}

	protected void init(String input, int size, int stepsLeft,
			List<OperationInstance> opsLog) {
		this.size = size;
		this.opsLog = new ArrayList<Matrix.OperationInstance>(opsLog);
		this.ois = getOisAll();
		this.stepsLeft = stepsLeft;
		this.bruteTries = Math.pow(40d, stepsLeft);

		this.maxScore = 0;
		this.maxScorers = new ArrayList<Matrix>();
		this.maxScorers.add(this);
		this.serialStepsLeft = 3;

		// deep copy
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

	private List<OperationInstance> getOisAll() {
		ArrayList<OperationInstance> oisAll = new ArrayList<OperationInstance>();
		for (OPERATION op : OPERATION.values()) {
//			for (int j = 0; j < size; j++) {
			for (int j = size - 1; j >=0; j--) {			
				oisAll.add(new OperationInstance(op, j));
			}
		}
		return oisAll;
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
		opsLog.add(oi);
		stepsLeft--;
		// System.out.println("applying " + oi + " opsLog is now: " + opsLog);

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
		// System.out.println("unapplying " + oi);
		// TODO check value?
		opsLog.remove(opsLog.size() - 1);
		stepsLeft++;

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
	 * parses state string, sets internal field maxscore, maxscorers,
	 * maxscorersdetails. Return external opsLog.
	 * 
	 * @param state
	 * @return
	 */
	public List<OperationInstance> parseState(String state) {
		String[] parts = state.split("][[");
		// parts[0] == last opslog
		return parseOpsLog(parts[0]);
		// parts[1] == maxScorers opslog
		// maxScorers = parseMaxScorers(parts[1]);
		// TODO replay to get maxscore
		// parts[2] == maxScorersDetails
		// maxScorersDetails = parseMaxScorersDetails(parts[2]);
	}

	private List<OperationInstance> parseOpsLog(String stateString) {
		// [CD_0, CU_8, RL_5, CU_2]
		String clean = stateString;
		clean = StringUtils.remove(stateString, ']');
		clean = StringUtils.remove(clean, '[');
		clean = StringUtils.deleteWhitespace(clean);

		// String[] parts =
		return null;
	}

	abstract protected boolean isSolved();

	abstract protected int score();

	abstract protected Matrix copy();

	public void preStart() {
		duplicates = new HashMap<String, List<List<OperationInstance>>>();
		triesGlobal = new AtomicLong();
		maxScoreGlobal = new AtomicLong();
		Matrix m = this;
		m.tries = 0;
		m.start = System.currentTimeMillis();
		m.serialStepsLeft = stepsLeft - PARALLEL_STEPS;
	}
	
	public void solveSerially() {
		System.out.println("Starting serially");
		preStart();
		Matrix m = this;
		Matrix winner = m.solveSeriallyInternal();
		long now = System.currentTimeMillis();

		System.out.println("Total tries serial: " + m.tries + " maxScore: "
				+ m.maxScore + " total time " + ((now - m.start) / 1000)
				+ "s. which is " + ((m.tries * 1000) / (now - m.start))
				+ " tries per second. maxScorers: "
				+ m.maxScorers.get(0).opsLog);
		System.out.println(m.maxScorers);
		System.out.println(winner.data + " score: " + winner.score());
		System.out.println(triesGlobal.get());
		
		if (GATHER_DUPLICATES) {
			printDuplicates();
		}

	}

	
	@SuppressWarnings("unchecked")
	private void printDuplicates() {
		for (Map.Entry<String, List<List<OperationInstance>>> entry: duplicates.entrySet()) {
			if (entry.getValue().size() > 1) {
				System.out.println(entry.getValue());
			}
		}
		System.out.println(duplicates.size());

	}

	public void solveFJ() {
		System.out.println("Starting with fork join");

		preStart();

		ForkJoinPool mainPool = new ForkJoinPool();
		Matrix m = this;

		Matrix winner = mainPool.invoke(m);

		long now = System.currentTimeMillis();
		System.out.println("Total tries fj: " + winner.tries + " maxScore: "
				+ winner.maxScore + " total time "
				+ ((now - winner.start) / 1000) + "s. which is "
				+ ((winner.tries * 1000) / (now - winner.start))
				+ " tries per second. maxScorers: "
				+ winner.maxScorers.get(0).opsLog);
		System.out.println(winner.maxScorers);
		System.out.println(winner.data + " score: " + winner.score());
		System.out.println(triesGlobal.get());
	}

	private void logProgress(Matrix m, boolean force) {
		long now = System.currentTimeMillis();
		long effectiveTries = m.tries;
		if (!force) {
			if (COUNT_ATOMIC) {
				effectiveTries = triesGlobal.get();
			}
		}
		if (force || effectiveTries % 10000000l == 0) {
//			 if (m.tries % 10l == 0) {
			System.out.println("Current tries: " + effectiveTries + " ("
					+ (effectiveTries * 100 / bruteTries.longValue())
					+ "%) which is " + (effectiveTries * 1000)
					/ (Math.max(1, now - m.start))
					+ " tries/second. maxScore: " + m.maxScore + " state: "
					+ m.opsLog + m.maxScorers.get(0).opsLog + m.maxScorers);
		}
	}

	private Matrix solveSeriallyInternal() {
		Matrix m = this;
		int s = m.stepsLeft;
		if (m.stepsLeft == 0) {
			long now = System.currentTimeMillis();
			int score = m.score();
			// int score = 0;
			boolean solved = false && m.isSolved();

			m.tries++;
			if (COUNT_ATOMIC)
				triesGlobal.getAndIncrement();

			
			if (score > 10 && score >= m.maxScore || solved) {
				if (score > m.maxScore) {
					m.maxScorers.clear();
				}
				m.maxScore = score;

				// store winner for future reference.
				Matrix winner = m.copy();
				m.maxScorers.add(winner);


				if (score >= maxScoreGlobal.get()) {
					maxScoreGlobal.getAndSet(score);
					System.out.println("serial: new max score " + StringUtils.leftPad(String.valueOf(score), 4)  + " for: "
							+ m.opsLog + " " + m.data + "\n" + toStringPretty());
					logProgress(m, true);
				}
				// return winner;
			}
			if (GATHER_DUPLICATES) {
				Matrix m2 = m.copy();
				putOrCreate(duplicates, m2.data, m2.opsLog);
			}
			// else {
			logProgress(m, false);
			// }
			// TODO return proper matrix
			Matrix winner = m.maxScorers.get(0);
			return winner;
		}

		// for (int index = 0; index < m.size; index++) {
		// assume square matrix #rows=#cols
		// for (OPERATION op : OPERATION.values()) {
		// OperationInstance oi = m.new OperationInstance(op, index);
		for (OperationInstance oi : m.ois) {
			m.apply(oi);
			Matrix result = solveSeriallyInternal();
			m.unapply(oi);
			// }
		}
		// TODO return proper matrix
		Matrix winner = m.maxScorers.get(0);
		winner.tries = m.tries;
		winner.maxScorers = m.maxScorers;
		return winner;

		// System.gc();
	}

	public Matrix compute() {
		int l = ois.size();
		int s = stepsLeft;

		if (ois.size() == 0) {
			throw new IllegalStateException("no op");
		} else if (ois.size() == 1) {
			if (stepsLeft == 0) {
				System.out.println("returning score " + this.score()
						+ " opsLog: " + opsLog + " data: " + this.data);
				this.tries++;
				if (COUNT_ATOMIC)
					triesGlobal.getAndIncrement();
				return this;
			}
			Matrix m1 = (Matrix) this.copy();
			// deafult all ois
			// apply ois before computing
			m1.apply(ois.get(0));
			return m1.compute();
		} else {
			if (stepsLeft > serialStepsLeft) {
				// multiple ops, so fork
				List<RecursiveTask<Matrix>> tasks = new ArrayList<RecursiveTask<Matrix>>();

				int numOfCores = Runtime.getRuntime().availableProcessors();
				int segmentSize = (ois.size() / numOfCores)
						+ ((ois.size() % numOfCores) > 0 ? 1 : 0);

				// for(int i = 0; i < numOfCores; i++) {
				// Matrix m1 = this.duplicate();
				// //only one ois
				// m1.ois = ois.subList(i * segmentSize, Math.min(ois.size(),
				// (i+1) * segmentSize));
				// System.out.println("ois=" + m1.ois);
				// tasks.add(m1);
				// }

				for (OperationInstance oi : ois) {
					Matrix m1 = this.copy();
					// only one ois
					m1.ois = Collections.singletonList(oi);
					System.out.println("ois=" + m1.ois);
					tasks.add(m1);
				}

				// will return when all are done.
				List<RecursiveTask<Matrix>> results = (List<RecursiveTask<Matrix>>) invokeAll(tasks);

				int maxScore = -1;
				long sumTries = 0;
				Matrix winner = null;
				List<Matrix> maxScorers = new ArrayList<Matrix>();
				for (RecursiveTask<Matrix> task : results) {
					Matrix m = task.getRawResult();
					int score = m.score();
					sumTries += m.tries;
					if (score >= maxScore) {
						if (score > maxScore) {
							maxScorers.clear();
						}
						maxScore = score;
						maxScorers.add(m);
						winner = m;
						winner.maxScore = score;
						winner.maxScorers = maxScorers;
						// System.out.println("fj: new max score for: " +
						// winner.data + " opsLog " + winner.opsLog + " score: "
						// + score);
					}
					// System.out.println("fj: LOWER score for: " + winner.data
					// + " opsLog " + winner.opsLog + " score: " + score);
				}
				winner.tries = sumTries;
				winner.start = start;
				return winner;
			} else {
				// too few stepsLeft, so compute serially.
				Matrix partWinner = solveSeriallyInternal();
				return partWinner;
			}
		}
	}

	public static <X, Y> List<Y> putOrCreate(Map<X, List<Y>> map, X key, Y value) {
		List<Y> result = map.get(key);
		if (result == null) {
			result = new ArrayList<Y>();
			map.put(key, result);
		}
		result.add(value);
		return result;
	}
	
}
