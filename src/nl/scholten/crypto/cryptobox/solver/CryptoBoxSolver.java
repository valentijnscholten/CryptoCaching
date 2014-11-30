package nl.scholten.crypto.cryptobox.solver;

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
 * - use intern to filter duplicates. helps a bit serially, but no gain with FJ.
 * - maintain hashmap with scores, slows it down a lot.
 * - score by scan from start is very slow
 * - use indexof instead of countmatches speeds up 200%
 * - leave out keys with more than 2 equal oi adjacent save little <1% at 4 steps or 5 steps
 * - make sure all segments of adjacent row or segments of adjacent col are in ascending order as the order doesn't matter -> saves 40% at 4 steps.
 * 
 * 
 * 
 * Faster string search:
 * BoyerMooreHorspool 10x slower on single pattern match.
 * BNDM also 10x slower
 * shiftormismatch even slower and bad results.
 * plain java regex is 10 times slower!
 * 
 */import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.scholten.crypto.cryptobox.data.Crypto2;
import nl.scholten.crypto.cryptobox.data.CryptoBoxMatrix;

import org.apache.commons.lang3.StringUtils;


@SuppressWarnings("serial")
public class CryptoBoxSolver extends RecursiveTask<CryptoBoxSolver> {

	private AtomicLong triesGlobal = new AtomicLong();
	protected AtomicLong maxScoreGlobal = new AtomicLong();
	
	private static final int MAX_MAX_SCORERS = 10;

//	public final static boolean USE_FUZZY = true;
	public final static boolean USE_FUZZY = false;
	public final static boolean USE_FUZZY_RANDOM_SKIP = true;
	public final static boolean USE_FUZZY_RANDOM_KEY_ORDER = false;
	public final static int USE_FUZZY_RANDOM_SKIP_PERCENTAGE = 70; //at 70% I was still able to get a good enough solution for 1 & 2 
			
	public final static boolean COUNT_ATOMIC = true;
	public final static boolean GATHER_DUPLICATES = false;
	public final static boolean CACHE_SCORES = false;
	public final static boolean USE_INTERN = false;
	
	public final static int FJ_POOL_SIZE = Runtime.getRuntime().availableProcessors();
//	public final static int FJ_POOL_SIZE = Runtime.getRuntime().availableProcessors() / 4;
	
	/**
	 * How many parallel steps before going serial.
	 */
	public static int PARALLEL_STEPS = 1;

	private static final boolean DO_BEGIN_MATCH = true;
	private static final boolean DO_END_MATCH = true;
	private static final boolean DO_X_PAD_MATCH = true;

	private static final boolean FORCE_BEGIN_MATCH = false;
	private static final boolean FORCE_END_MATCH = false;
	private static final boolean FORCE_X_PAD_MATCH = false;

	private static final boolean USE_ARRAY_FOR_HITS = false;

	private static final boolean SCORE_BY_SCAN_FROM_START = false;
	// /TODO calculate these ? or make sure all words match these sizes by
	// splitting them up if needed?
	private static final int MIN_WORD_SIZE = 3;
	private static final int MAX_WORD_SIZE = 6;

	private static final boolean SCORE_BY_COUNT_MATCHES = true;
//	private static final boolean SCORE_BY_COUNT_MATCHES = false;
	private static final boolean REWARD_FIRST_OCCURRENCE_OF_WORD = false; // slows
																			// down
	private static final boolean SCORE_JAVA_REGEX = false;
//	private static final boolean SCORE_JAVA_REGEX = true;
	private static final boolean SCORE_EXPERIMENTAL = false;

	protected List<String> hits, beginnings, endings;
	protected Set<String> hitsSet;
	protected Set<String> words;
	protected List<List<OperationInstance>> headStarts;
	protected Pattern hitsPattern;
	protected int x_paddings;
	protected String x_padding;

	public final static List<OperationInstance> oisAll = new ArrayList<OperationInstance>();
	
	public List<OperationInstance> ois;
	public ArrayList<OperationInstance> opsLog;

	protected String data;
	protected char[] dataArray;

	private int stepsLeft;

	protected int size;
	private long tries;
	private long start;

	protected int maxScore;
	protected Set<CryptoBoxSolver> maxScorersSet;
	private int serialStepsLeft;
	private Double bruteTries;
	
	protected long found;

	public CryptoBoxSolver(String input, int size, int stepsLeft, List<String> hits, List<String> beginnings, List<String> endings, 
			List<List<OperationInstance>> headStarts) {
		this.init(input, size, stepsLeft, hits, beginnings, endings, headStarts);
	}
	
	protected void init(String input, int size, int stepsLeft, List<String> hits, List<String> beginnings, List<String> endings, 
			List<List<OperationInstance>> headStarts) {
		this.headStarts = headStarts;
		
		if (headStarts == null || headStarts.isEmpty()) {
			this.headStarts = new ArrayList<List<OperationInstance>>();
			this.headStarts.add(new ArrayList<CryptoBoxSolver.OperationInstance>());
		}
		this.size = size;
		this.opsLog = new ArrayList<CryptoBoxSolver.OperationInstance>();
		this.ois = getOisAll();
		this.stepsLeft = stepsLeft;
		this.bruteTries = Math.pow(40d, stepsLeft);

		this.maxScore = 0;
		this.maxScorersSet = Collections.synchronizedSet(new HashSet<CryptoBoxSolver>());
		this.maxScorersSet.add(this);
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
		
		this.hits = hits;
		this.hitsSet = new HashSet<String>(hits);
		this.beginnings = beginnings;

		this.x_paddings = StringUtils.countMatches(input, "X");
		this.x_padding = StringUtils.rightPad("", x_paddings, "X");

		this.endings = new ArrayList<String>();
		for (String ending: endings) {
			String newEnding = ending;
			// remove xpads
			while (newEnding.endsWith("X")) {
				newEnding = StringUtils.removeEnd(newEnding, "X");
			}
			// add fixed xpads
			newEnding += x_padding;
			this.endings.add(ending);
		}

		String hitsPatternString = "(";
		for (String hit : hits) {
			if (hitsPatternString.length() > 1)
				hitsPatternString += "|";
			hitsPatternString += hit;
		}
		hitsPatternString += ")";
		this.hitsPattern = Pattern.compile(hitsPatternString);

		this.maxScore = this.score();
		this.maxScorersSet.add(this);
		this.maxScoreGlobal.set(this.maxScore);		
		
	}

	
	public CryptoBoxSolver(CryptoBoxSolver m) {
		this.init(m);
	}

	protected void init(CryptoBoxSolver m) {
		this.data = new String(m.data.toCharArray());
		
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
		
		this.size = m.size;
		this.stepsLeft = m.stepsLeft;
		this.opsLog = m.opsLog;
		this.headStarts = m.headStarts;
		
		this.ois = m.ois;
		
		this.bruteTries = m.bruteTries;

		this.maxScore = 0;
		this.maxScorersSet = Collections.synchronizedSet(new HashSet<CryptoBoxSolver>());
		this.maxScorersSet.add(this);
		this.serialStepsLeft = 3;
		
		this.hits = m.hits;
		this.hitsSet = m.hitsSet;
		this.beginnings = m.beginnings;

		this.x_paddings = m.x_paddings;
		this.x_padding = m.x_padding;

		this.endings = m.endings;
		this.hitsPattern = m.hitsPattern;
		
		this.maxScore = m.maxScore;
		this.start = m.start;
		this.tries = m.tries;
		this.serialStepsLeft = m.serialStepsLeft;
		this.triesGlobal = m.triesGlobal;
		this.maxScoreGlobal = m.maxScoreGlobal;
		this.found = m.found;
		
		this.maxScorersSet = new HashSet<CryptoBoxSolver>(m.maxScorersSet);
	}


	public static enum OPERATION {
		CU(false, true, "CU"), CD(false, false, "CD"), RL(true, false, "RL"), RR(true,
				true, "RR");

		public boolean isRow;
		public boolean isPositive;
		public String value;

		private OPERATION(boolean isRow, boolean isPositive, String value) {
			this.isRow = isRow;
			this.isPositive = isPositive;
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

	public static class OperationInstance {
		public OPERATION op;
		public int index;

		public OperationInstance(OPERATION op, int index) {
			this.op = op;
			this.index = index;
		}

		public String toString() {
			return op.toString() + "_" + index;
		}
		
		//TODO not used?
		public boolean equals(OperationInstance oi) {
			return this.op == oi.op && this.index == oi.index;
		}
	}

	
	//key = m.data
	Map<String, List<List<OperationInstance>>> duplicates = new HashMap<String, List<List<OperationInstance>>>();
	Map<String, Integer> scoreCache = new HashMap<String, Integer>();


	private synchronized List<OperationInstance> getOisAll() {
		//make sure we only have one OI of each possibility.
		if (oisAll.isEmpty()) {
			for (OPERATION op : OPERATION.values()) {
				
				for (int j = 0; j < size; j++) {
	//			for (int j = size - 1; j >=0; j--) {			
					oisAll.add(new OperationInstance(op, j));
				}
			}
	
			//TODO select random order to have different initial results in different runs?
			if (USE_FUZZY && USE_FUZZY_RANDOM_KEY_ORDER) {
				long seed = System.nanoTime();
				Collections.shuffle(oisAll, new Random(seed));
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

		case CU:
			shiftColumnUp(oi.index);
			break;
		case CD:
			shiftColumnDown(oi.index);
			break;
		case RL:
			shiftRowLeft(oi.index);
			break;
		case RR:
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

		case CU:
			shiftColumnDown(oi.index);
			break;
		case CD:
			shiftColumnUp(oi.index);
			break;
		case RL:
			shiftRowRight(oi.index);
			break;
		case RR:
			shiftRowLeft(oi.index);
			break;

		default:
			throw new IllegalStateException("Unknown operation " + oi.op);
		}
	}

	public String getRow(int i) {
		return data.substring(i * size, (i * size) + size);
	}

	public static String toStringSideBySide(CryptoBoxSolver... matrices) {

		if (matrices.length == 0)
			return "";
		StringBuilder result = new StringBuilder();

		for (int row = 0; row < matrices[0].size; row++) {
			for (CryptoBoxSolver m : matrices) {
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
	
	public void preStart() {
		duplicates = new HashMap<String, List<List<OperationInstance>>>();
		scoreCache = new HashMap<String, Integer>();
		triesGlobal = new AtomicLong();
		maxScoreGlobal = new AtomicLong();
		CryptoBoxSolver m = this;
		m.tries = 0;
		m.start = System.currentTimeMillis();
		m.serialStepsLeft = stepsLeft - PARALLEL_STEPS;
	}
	
	
	@SuppressWarnings("unchecked")
	private void printDuplicates() {
		int i = 0;
		for (Map.Entry<String, List<List<OperationInstance>>> entry: duplicates.entrySet()) {
			if (entry.getValue().size() > 1) {
				System.out.println(entry.getValue());
				i++;
			}
		}
		System.out.println("Duplicates: " + i);
		
	}
	
	private void logResult(CryptoBoxSolver winner) {
		long now = System.currentTimeMillis();
		
		System.out.println("Total tries serial: " + winner.tries + " maxScore: "
				+ winner.maxScore + " total time " + (((now-winner.start)<60000)?(now-winner.start) + "ms.":((now - winner.start) / 1000) + "s.")
				+ " which is " + ((winner.tries * 1000) / (Math.max(1,now - winner.start)))
				+ " tries per second. solution found after: " + (((winner.found-winner.start)<60000)?(winner.found-winner.start) + "ms.":((winner.found - winner.start) / 1000) + "s.")
				+ "maxScorers: "
				+ ((CryptoBoxSolver)winner.maxScorersSet.toArray()[0]).opsLog);
		
		System.out.println("maxScorers: " + winner.maxScorersSet.size() + ": "+ winner.maxScorersSet);
		
		for (CryptoBoxSolver maxScorer: winner.maxScorersSet){
			System.out.println(maxScorer.opsLog);
		}
		
		System.out.println(triesGlobal.get() + " / " + bruteTries.longValue() + " = " + (triesGlobal.get() * 100 / bruteTries.longValue()) + "%");
		
		if (GATHER_DUPLICATES) {
			printDuplicates();
		}
	}
	
	public void solveSerially() {
		System.out.println("Starting serially");
		preStart();
		CryptoBoxSolver winner = solveSeriallyInternal();

//		Matrix winner = maxScorers.get(0);
		
		logResult(winner);
	}

	public void solveFJ() {
		System.out.println("Starting with fork join");

		preStart();

		ForkJoinPool mainPool = new ForkJoinPool(FJ_POOL_SIZE);
		CryptoBoxSolver m = this;

		CryptoBoxSolver winner = mainPool.invoke(m);

		logResult(winner);
	}

	private void logProgress(CryptoBoxSolver m, boolean force) {
		long now = System.currentTimeMillis();
		long effectiveTries = m.tries;
		if (!force) {
			if (COUNT_ATOMIC) {
				effectiveTries = triesGlobal.get();
			}
		}
		if (force || effectiveTries % 10000000l == 0) {
			System.out.println("Current tries: " + effectiveTries + " ("
					+ (effectiveTries * 100 / bruteTries.longValue())
					+ "%) which is " + (effectiveTries * 1000)
					/ (Math.max(1, now - m.start))
					+ " tries/second. maxScore: " + m.maxScore + " state: "

					+ m.opsLog + ((CryptoBoxSolver)m.maxScorersSet.toArray()[0]).opsLog + m.maxScorersSet);
			System.out.println("current data: " + ((CryptoBoxSolver)m.maxScorersSet.toArray()[0]).data + " " + m.toString());
		}
	}

	private void doScoring() {
		// if already cache, ignore
		int score = score();
		if (CACHE_SCORES)
			scoreCache.put(new String(data.toCharArray()), score);

		tries++;
		if (COUNT_ATOMIC)
			triesGlobal.getAndIncrement();

		processScore(score);
		
		if (GATHER_DUPLICATES) {
			if (score > 0) {
				CryptoBoxSolver m2 = new CryptoBoxSolver(this);
				putOrCreate(duplicates, m2.data, m2.opsLog);
			}
		}
	}

	private void processScore(int score) {
		long maxglobal = maxScoreGlobal.get();
		if (score >= maxglobal) {
			// we have to make sure another thread hasn't found a different high
			// score which might get overwritten by us
			if (maxScoreGlobal.compareAndSet(maxglobal, score)) {
				final CryptoBoxSolver winner;
				if (score > maxScore) {
					maxScorersSet.clear();

					maxScore = score;
					found = System.currentTimeMillis();
					winner = new CryptoBoxSolver(this);
					
					// store winner for future reference.
					if (maxScorersSet.size() < MAX_MAX_SCORERS) {
						// don't add to much scorers otherwise out of memory. if
						// there are more than 10, probably the right one isn't in
						// there.
						maxScorersSet.add(winner);
					}

				} else {
					//equal score, so do detailedScoring to determine winner
					maxScorersSet.add(new CryptoBoxSolver(this));
					//filter set
					
					Set<CryptoBoxSolver> newMaxScorers = new HashSet<CryptoBoxSolver>();
					int newMaxScore = -1;
					for (CryptoBoxSolver m: maxScorersSet) {
						int scoreDetailed = m.scoreDetailed();
						if (scoreDetailed > newMaxScore) {
							newMaxScore = scoreDetailed; 
							newMaxScorers.clear();
							newMaxScorers.add(m);
						} else if (scoreDetailed == newMaxScore) {
							newMaxScorers.add(m);
						} else {
							//lower score, so ignore.
						}
					}
					maxScorersSet = newMaxScorers;
					maxScore = newMaxScore;
				}


				if (score >= maxglobal) {
					long now = System.currentTimeMillis();
					System.out.println("serial: new max score "
							+ StringUtils.leftPad(String.valueOf(maxScore), 4)
							+ "(" + (now - start) + "ms.)"
							+ " for: " + opsLog + " " + data + "\n"
							+ toStringPretty());
					logProgress(this, true);
				}
			}
		}
	}
	
	private CryptoBoxSolver solveSeriallyInternal() {
		//no headstart -> use empty headstart
		if (headStarts == null || headStarts.isEmpty()) return solvedSeriallyInternalHeadStart(new ArrayList<CryptoBoxSolver.OperationInstance>());
		
		List<CryptoBoxSolver> partialResults = new ArrayList<CryptoBoxSolver>();
		for(List<OperationInstance> headStart: headStarts) {
			
			CryptoBoxSolver m = new CryptoBoxSolver(this);
			m.headStarts = null;
			m.start = start;
			
			partialResults.add(m.solvedSeriallyInternalHeadStart(headStart));
		}
		
		return joinResults(partialResults);
	}
	
	private CryptoBoxSolver joinResults(List<CryptoBoxSolver> partialResults) {
		int joinedMaxScore = -1;
		long sumTries = 0;
		CryptoBoxSolver winner = null;
		Set<CryptoBoxSolver> joinedMaxScorers = new HashSet<CryptoBoxSolver>();

		for(CryptoBoxSolver m: partialResults){
			int score = m.score();
			sumTries += m.tries;
			if (score >= joinedMaxScore) {
				if (score > joinedMaxScore) {
					joinedMaxScorers.clear();
				}
				winner = m;
				joinedMaxScorers.addAll(winner.maxScorersSet);
				joinedMaxScore = score;
	
				System.out.println("join: new max score for: " +
				winner.data + " opsLog " + winner.opsLog + " score: "
				+ score);
			}
		}
		winner.tries = sumTries;
		winner.maxScorersSet = new HashSet<CryptoBoxSolver>(joinedMaxScorers);
		winner.start = start;

		return winner;
		
	}
	
	private void apply(List<OperationInstance> headStart) {
		for (OperationInstance oi: headStart) {
			apply(oi);
		}
	}

	private CryptoBoxSolver solvedSeriallyInternalHeadStart(List<OperationInstance> headStart) {
		apply(headStart);
		if (stepsLeft == 0) {
			if (!CACHE_SCORES || !scoreCache.containsKey(data)) {
				doScoring();
			}
					
			logProgress(this, false);

			CryptoBoxSolver winner = ((CryptoBoxSolver)maxScorersSet.toArray()[0]);
			return winner;
		}
		return doSolveSeriallyInternalNextStep();
	}
	
	private CryptoBoxSolver doSolveSeriallyInternalNextStep() {
		
		OperationInstance prevOIA = null;
		OperationInstance prevOIB = null;
		List<OperationInstance> opsLog2 = opsLog;
		if (opsLog.size() > 0) {
			prevOIA = opsLog.get(opsLog.size() - 1);
		} 
		if (opsLog.size() > 1) {
			prevOIB = opsLog.get(opsLog.size() - 2);
		}

		for (OperationInstance oi : ois) {
			//no more than 2 of the same oi adjacent
			if (prevOIA != prevOIB || (oi != prevOIA || oi != prevOIB)) {
				
				if (	//make sure all segments of the same type (row/col) are in ascending order. Order doesn't matter, so only calculate for those with ascending order)
						(prevOIA == null) //no previous OI, so always go.
						||
						(oi.op.isRow != prevOIA.op.isRow) // row after col or col after row always go
						||
						(oi.op.isRow == prevOIA.op.isRow && oi.index > prevOIA.index) // row after row should ascend, same for col after col. leaves only 43% of tries!
						||
						(oi.op.isRow == prevOIA.op.isRow && oi.index == prevOIA.index && (oi.op.isPositive == prevOIA.op.isPositive)) //CU_0 CD_0 not useful as they compensate eachother. leaves 39% of tries
						
						) {
							//the above are pure optimizations. All possible outcomes are still calculated/scored.
							//the below are "fuzzy" optimizations. Skipping possible valid opsLogs, so best solution might not be found, but will give a good indication that might be enough to solve it manually afterwards.
						if (!USE_FUZZY || !USE_FUZZY_RANDOM_SKIP || Math.random() * 100 > USE_FUZZY_RANDOM_SKIP_PERCENTAGE) {
							apply(oi);
							CryptoBoxSolver result = solveSeriallyInternal();
							unapply(oi);
						}
				}
			 }
		}

		CryptoBoxSolver winner = ((CryptoBoxSolver)maxScorersSet.toArray()[0]);
		winner.tries = tries;
		winner.maxScorersSet = maxScorersSet;
		return winner;
	}

	public CryptoBoxSolver compute() {
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
				this.found = System.currentTimeMillis();

				return this;
			}
			CryptoBoxSolver m1 = new CryptoBoxSolver(this);
			// by default ois is now all ois
			// apply ois before computing
			m1.apply(ois.get(0));
			return m1.compute();
		} else {
			if (stepsLeft > serialStepsLeft) {
				// multiple ops, so fork
				List<RecursiveTask<CryptoBoxSolver>> tasks = new ArrayList<RecursiveTask<CryptoBoxSolver>>();

				for (List<OperationInstance> headStart: headStarts) {
					for (OperationInstance oi : ois) {
						CryptoBoxSolver m1 = new CryptoBoxSolver(this);
						// only one ois
						m1.ois = Collections.singletonList(oi);
						m1.apply(headStart);
						m1.headStarts = new ArrayList<List<OperationInstance>>();
						tasks.add(m1);
					}
				}

				// will return when all are done.
				List<RecursiveTask<CryptoBoxSolver>> results = (List<RecursiveTask<CryptoBoxSolver>>) invokeAll(tasks);

				List<CryptoBoxSolver> partialResults = new ArrayList<CryptoBoxSolver>();
				for (RecursiveTask<CryptoBoxSolver> task : results) {
					CryptoBoxSolver m = task.getRawResult();
					partialResults.add(m);
				}

				return joinResults(partialResults);
			} else {
				// too few stepsLeft, so compute serially.
				CryptoBoxSolver partWinner = solveSeriallyInternal();
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

	/**
	 * Based on opsLog we can immediately see some solutions that are duplicates
	 * of others. 1: if all operations are row operations on mutual exclusive
	 * columns, the order doesn't matter. So only use the one where indexes are
	 * ascending.
	 * 
	 * @return
	 */
	public boolean isDuplicate() {
		if (!CACHE_SCORES && !USE_INTERN)
			return false;

		if (CACHE_SCORES && duplicates.containsKey(data))
			return true;

		if (USE_INTERN) {
			String newData = new String(data.toCharArray());
			// if this data value was interned before, it was scored before so
			// duplicate
			if (newData.intern() != newData)
				return true;
		}
		return false;

	}

	public boolean isHit(String s) {
		if (USE_ARRAY_FOR_HITS) {
			for (String hit : hits) {
				if (hit.equals(s)) {
					return true;
				}
			}
			return false;
		} else {
			return hitsSet.contains(s);
		}
	}

	protected int scoreByScanFromStart(int result) {
		int prev_b = 0;
		int a = 0;
		int wordsTotalSize = 0;

		while (a < (size * size) - MIN_WORD_SIZE) {

			// TODO ignore last XXXXs if there's a match
			for (int b = Math.min(a + MAX_WORD_SIZE, (size * size)); b >= a
					+ MIN_WORD_SIZE; b--) {
				String s = data.substring(a, b);

				if (isHit(s)) {
					// the substring is a word, hooray!
					if (a == prev_b) {
						// word is adjecent to previous word, hallelujah
						result += 100;
					}
					// another contains should be fast as hashcode is cache for
					// s.
					if (REWARD_FIRST_OCCURRENCE_OF_WORD && !words.contains(s)) {
						// first time word is found
						result += 10;
						words.add(s);
					}

					// add point for each hit.
					result += 1;
					wordsTotalSize += s.length();
					// System.out.println("found: " + s + " wordTotalSize " +
					// wordsTotalSize);

					a = b - 1;// pre-compensate for a++ in outer loop.
					prev_b = b;
					break;
				}
			}
			a++;
		}
		// if
		// (data.equals("NOORDVIJFEENGRADENVIERNULPUNTZESACHTNULMINUTENOOSTVIERGRADENTWEEVIERPUNTZEVENDRIENEGENMINUTENXXXXXXX"))
		// {
		// System.out.println("VALENTIJN");
		// }
		if (wordsTotalSize == ((size * size) - x_paddings)) {
			// all words adjacent, means it is solved (should be).
			System.out.println("Solved: " + data);
			result += 10000;
		}
		return result;
	}

	protected int score() {
		return scoreInteral(false);
	}

	protected int scoreInteral(boolean detailed) {
//		 System.out.println("scoring: " + opsLog + " data: " + data);
		// TODO more subtle optimization needed. This drops to many options, but
		// since the key is random, chances are it's ok.
		// TODO idea: make sure all segments of row ops and segments of col ops
		// are in ascending order of index, because within a segment, the order
		// doesn't matter.
		// if (isAllRow() || isAllCol()) return 0;
		if (isDuplicate())
			return 0;
		int result = 0;

		if (DO_BEGIN_MATCH) {
			boolean beginMatch = false;
			for (String beginning : beginnings) {
				if (data.startsWith(beginning)) {
					result += 10*beginning.length();
					beginMatch = true;
					break;
				}
			}

			if (FORCE_BEGIN_MATCH && !beginMatch)
				return 0;
		}

		if (DO_END_MATCH) {
			boolean endMatch = false;
			for (String ending : endings) {
				if (data.endsWith(ending)) {
					result += 10 * ending.length();
					endMatch = true;
					break;
				}BeginningScorer.java
			}

			if (FORCE_END_MATCH && !endMatch)
				return 0;
		}

		if (DO_X_PAD_MATCH) {
			boolean padMatch = false;

			String end = data.substring(data.length() - x_paddings,
					data.length());
			int count = StringUtils.countMatches(end, "X");

			// System.out.println(count + " x_pads for: " + data);
			result += count;
			padMatch = count == x_paddings;

			if (padMatch)
				result += 10;

			if (FORCE_X_PAD_MATCH && !padMatch)
				return 0;
		}

		if (SCORE_BY_COUNT_MATCHES)
			return scoreByCountMatches(result, detailed);
		if (SCORE_BY_SCAN_FROM_START)
			return scoreByScanFromStart(result);
		if (SCORE_JAVA_REGEX)
			return scoreJavaRegex(result);

		if (SCORE_EXPERIMENTAL)
			return scoreExpiremental(result);

		throw new IllegalStateException("Select a score method");
	}

	private boolean isAllCol() {
		for (OperationInstance oi : opsLog) {
			if (oi.op.isRow)
				return false;
		}
		return true;
	}

	private boolean isAllRow() {
		for (OperationInstance oi : opsLog) {
			if (!oi.op.isRow)
				return false;
		}
		return true;
	}

	protected int scoreByCountMatches(int result, boolean detailed) {

		for (String hit : hits) {
			if (REWARD_FIRST_OCCURRENCE_OF_WORD) {
				// extra score for first occurrence to stimulate more different
				// words
				int count = StringUtils.countMatches(data, hit);
				if (count > 0)
					result += 10;
				result += (count * 10); // 10 pts per word
			} else if (detailed) {
				//detailed scoring counts *all* matches of each hit
				int count = StringUtils.countMatches(data, hit);
				if (count > 0)
					result += 10;
				result += count; // 1 pts per word
			} else {
				// using only index doesn't help much (10%) ->> ehhh later it
				// looks like 200% gain when going parallel.
				if (data.indexOf(hit) > -1) {
					result += 10;// 10 pts per word
				}
			}
		}
		
//		System.out.println("scored: " + opsLog + " score: " + StringUtils.leftPad(String.valueOf(result), 4) + " data: " + data);

		return result;
	}

	protected int scoreJavaRegex(int result) {
		Matcher matcher = this.hitsPattern.matcher(data);

		// do first match to check if it is at index 0;
		if (matcher.find()) {
			if (matcher.start() == 0) {
				// found a match at the start, which means bonus.
				result += 25;
//				System.out.println("index: " + matcher.start() + " matched: " + matcher.group() + " START_BONUS");
			} else {
				result += 10;
//				System.out.println("index: " + matcher.start() + " matched: " + matcher.group());
			}

			while (matcher.find()) {
				// skip last match as it has different logging
				int a = matcher.start();
				int b = data.length();
				int c = matcher.group().length();
				if (matcher.start() == (data.length() - matcher.group()
								.length())) {
					// found a match at the start, which means bonus.
					result += 25;
//					System.out.println("index: " + matcher.start() + " matched: " + matcher.group() + " END_BONUS");
				} else {
					result += 10;
//					System.out.println("index: " + matcher.start() + " matched: " + matcher.group());
				}
			}

		} else {
			// no matches at all
		}
		return result;
		
	}
	
	protected int scoreExpiremental(int result) {


		return result;
	}

	protected int scoreDetailed() {
		int result =  scoreInteral(true);
//		System.out.println("Scored detailed for " + data + " score: " + StringUtils.leftPad(String.valueOf(result), 4));
		return result;
	}
	
	public static void main(String[] args) throws IOException {
		CryptoBoxMatrix m = new Crypto2(
				StringUtils
						.rightPad(
								// "TWEEACDEENFKDRIEZEVENAT",
								"GOEDGEDAANGANAARVIJFEENGRADENVIERNULPUNTVIERDRIEVIJENNLIEGRUFADERDRIENLPUNTNEGENNEGENVIJFVALERNIETIN",
//								"NOORDVIJFEENGRADENVIERNULPUNTZESACHTNULMINUTENOOSTVIERGRADENTWEEVIERPUNTZEVENDRIENEGENMINUTENXXXXXXX",
								100, 'X'));

		System.out.println(m.score());
	}

	
}
