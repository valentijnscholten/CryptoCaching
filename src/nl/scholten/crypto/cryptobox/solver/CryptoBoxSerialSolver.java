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
 */import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

import nl.scholten.crypto.cryptobox.data.CryptoBoxMatrix;
import nl.scholten.crypto.cryptobox.data.OPERATION;
import nl.scholten.crypto.cryptobox.data.OperationInstance;

import org.apache.commons.lang3.StringUtils;


@SuppressWarnings("serial")
public class CryptoBoxSerialSolver {

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
	private static final List<OperationInstance> EMPTY_HEAD_START = new ArrayList<OperationInstance>();
	
	public List<OperationInstance> ois;
	public ArrayList<OperationInstance> opsLog;

	protected String data;
	protected char[] dataArray;

	private int stepsLeft;

	protected int size;
	private long tries;
	private long startTime;

	protected int maxScore;
	protected Set<CryptoBoxSerialSolver> maxScorersSet;
	private int serialStepsLeft;
	private Double bruteTries;
	
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

	public void preStart() {
		duplicates = new HashMap<String, List<List<OperationInstance>>>();
		scoreCache = new HashMap<String, Integer>();
		triesGlobal = new AtomicLong();
		maxScoreGlobal = new AtomicLong();
		CryptoBoxSerialSolver m = this;
		m.tries = 0;
		m.startTime = System.currentTimeMillis();
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
	
	private void logResult(MatrixResult result) {
		long now = System.currentTimeMillis();
		
		System.out.println("Total tries serial: " + winner.tries + " maxScore: "
				+ winner.maxScore + " total time " + (((now-winner.start)<60000)?(now-winner.start) + "ms.":((now - winner.start) / 1000) + "s.")
				+ " which is " + ((winner.tries * 1000) / (Math.max(1,now - winner.start)))
				+ " tries per second. solution found after: " + (((winner.found-winner.start)<60000)?(winner.found-winner.start) + "ms.":((winner.found - winner.start) / 1000) + "s.")
				+ "maxScorers: "
				+ ((CryptoBoxSerialSolver)winner.maxScorersSet.toArray()[0]).opsLog);
		
		System.out.println("maxScorers: " + winner.maxScorersSet.size() + ": "+ winner.maxScorersSet);
		
		for (CryptoBoxSerialSolver maxScorer: winner.maxScorersSet){
			System.out.println(maxScorer.opsLog);
		}
		
		System.out.println(triesGlobal.get() + " / " + bruteTries.longValue() + " = " + (triesGlobal.get() * 100 / bruteTries.longValue()) + "%");
		
		if (GATHER_DUPLICATES) {
			printDuplicates();
		}
	}
	
	public MatrixResult solve(CryptoBoxMatrix m, int steps) {
		System.out.println("Starting serially");
		preStart();
		MatrixResult result = solveSeriallyInternal(m, steps);

		logResult(result);
		return result;
	}

	private MatrixResult solveSeriallyInternal(CryptoBoxMatrix m, int steps) {
		//no headstart -> use empty headstart
		if (headStarts == null || headStarts.isEmpty()) {
			return solvedSeriallyInternalHeadStart(new MatrixResult(), new MatrixState(m, steps), EMPTY_HEAD_START);
		}
		
		List<MatrixResult> partialResults = new ArrayList<MatrixResult>();
		for(List<OperationInstance> headStart: headStarts) {
			
			MatrixState state = new MatrixState(m, steps);
			
			partialResults.add(solvedSeriallyInternalHeadStart(new MatrixResult(), state, headStart));
		}
		
		return joinResults(partialResults);
	}
	
	private MatrixResult solvedSeriallyInternalHeadStart(MatrixResult intermediateResult, MatrixState state, List<OperationInstance> headStart) {
		//apply headstart (can be empty)
		state.matrix.apply(headStart);
		
		//if no steps left, do scoring
		if (stepsLeft == 0) {
			if (!CACHE_SCORES || !scoreCache.containsKey(data)) {
				doScoring();
			}
					
			logProgress(this, false);

			CryptoBoxSerialSolver winner = ((CryptoBoxSerialSolver)maxScorersSet.toArray()[0]);
			return winner;
		}
		
		//else perform next step
		return doSolveSeriallyInternalNextStep(intermediateResult, state);
	}
	
	private MatrixResult joinResults(List<MatrixResult> partialResults) {
		int joinedMaxScore = -1;
		long sumTries = 0;
		Set<MatrixState> joinedMaxScorers = new HashSet<MatrixState>();
		MatrixResult winner = new MatrixResult();
		for(MatrixResult partialResult: partialResults){
			
			sumTries += partialResult.tries;
			
			if (partialResult.maxScore >= joinedMaxScore) {
				
				if (partialResult.maxScore > joinedMaxScore) {
					joinedMaxScorers.clear();
				}

				joinedMaxScorers.addAll(partialResult.maxScorersSet);

				winner.foundTime = partialResult.foundTime;
				
				System.out.println("join: new max score " + partialResult.maxScore + " for: " +
						partialResult.maxScorersSet);
			}
		}
		winner.tries = sumTries;
		winner.maxScorersSet = new HashSet<MatrixState>(joinedMaxScorers);
		winner.startTime = winner.startTime;

		return winner;
		
	}

	private MatrixResult doSolveSeriallyInternalNextStep(MatrixResult intermediateResult, MatrixState state) {
		
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
							state.matrix.apply(oi);
							solvedSeriallyInternalHeadStart(intermediateResult, state, EMPTY_HEAD_START);
							state.matrix.unapply(oi);
						}
				}
			 }
		}

		return intermediateResult;
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

	private void logProgress(CryptoBoxSerialSolver m, boolean force) {
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
					/ (Math.max(1, now - m.startTime))
					+ " tries/second. maxScore: " + m.maxScore + " state: "

					+ m.opsLog + ((CryptoBoxSerialSolver)m.maxScorersSet.toArray()[0]).opsLog + m.maxScorersSet);
			System.out.println("current data: " + ((CryptoBoxSerialSolver)m.maxScorersSet.toArray()[0]).data + " " + m.toString());
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
				CryptoBoxSerialSolver m2 = new CryptoBoxSerialSolver(this);
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
				final CryptoBoxSerialSolver winner;
				if (score > maxScore) {
					maxScorersSet.clear();

					maxScore = score;
					found = System.currentTimeMillis();
					winner = new CryptoBoxSerialSolver(this);
					
					// store winner for future reference.
					if (maxScorersSet.size() < MAX_MAX_SCORERS) {
						// don't add to much scorers otherwise out of memory. if
						// there are more than 10, probably the right one isn't in
						// there.
						maxScorersSet.add(winner);
					}

				} else {
					//equal score, so do detailedScoring to determine winner
					maxScorersSet.add(new CryptoBoxSerialSolver(this));
					//filter set
					
					Set<CryptoBoxSerialSolver> newMaxScorers = new HashSet<CryptoBoxSerialSolver>();
					int newMaxScore = -1;
					for (CryptoBoxSerialSolver m: maxScorersSet) {
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
							+ "(" + (now - startTime) + "ms.)"
							+ " for: " + opsLog + " " + data + "\n"
							+ toStringPretty());
					logProgress(this, true);
				}
			}
		}
	}
	

	
}
