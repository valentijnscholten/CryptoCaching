package nl.scholten.crypto.cryptobox.solver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import nl.scholten.crypto.cryptobox.data.CounterSingletons;
import nl.scholten.crypto.cryptobox.data.CryptoBoxMatrix;
import nl.scholten.crypto.cryptobox.data.CryptoBoxResult;
import nl.scholten.crypto.cryptobox.data.MatrixState;
import nl.scholten.crypto.cryptobox.data.OPERATION;
import nl.scholten.crypto.cryptobox.data.OperationInstance;
import nl.scholten.crypto.cryptobox.scorer.CryptoBoxScorer;

import org.apache.commons.lang3.StringUtils;

public abstract class CryptoBoxSolver {
	
	//don't skip duplicates
//	protected final static boolean DO_ALL_OPS_LOGS = true;

//	protected final static boolean USE_FUZZY = false;
	protected final static boolean USE_FUZZY = true;
	protected final static boolean USE_FUZZY_RANDOM_KEY_ORDER = false;
//	protected final static boolean USE_FUZZY_RANDOM_KEY_ORDER = true;
	protected final static boolean USE_FUZZY_RANDOM_SKIP = false;
//	protected final static boolean USE_FUZZY_RANDOM_SKIP = true;
	protected final static int USE_FUZZY_RANDOM_SKIP_PERCENTAGE = 90; //at 70% I was still able to get a good enough solution for 1 & 2 
			
	protected final static boolean COUNT_ATOMIC = true;
	
	protected final static boolean DO_BEGIN_MATCH = true;
	protected final static boolean DO_END_MATCH = true;
	protected final static boolean DO_X_PAD_MATCH = true;

	protected final static boolean FORCE_BEGIN_MATCH = false;
	protected final static boolean FORCE_END_MATCH = false;
	protected final static boolean FORCE_X_PAD_MATCH = false;

	protected final static boolean USE_ARRAY_FOR_HITS = false;


	protected final static boolean SCORE_BY_COUNT_MATCHES = true;
//	protected final static boolean SCORE_BY_COUNT_MATCHES = false;
	protected final static boolean REWARD_FIRST_OCCURRENCE_OF_WORD = false; // slows down
	protected final static boolean SCORE_JAVA_REGEX = false;
//	protected final static boolean SCORE_JAVA_REGEX = true;

	
	protected final static int MAX_MAX_SCORERS = 10;
	
	public static final List<OperationInstance> oisAll = new ArrayList<OperationInstance>();
	public static final Set<List<OperationInstance>> DEFAULT_PREFIXES;
	static {
		Set<List<OperationInstance>> temp = new HashSet<List<OperationInstance>>();
		temp.add(new LinkedList<OperationInstance>());
		DEFAULT_PREFIXES = Collections.unmodifiableSet(temp);
	}
	public static final Set<List<OperationInstance>> DEFAULT_POSTFIXES = DEFAULT_PREFIXES;
	
	protected Set<List<OperationInstance>> prefixes;
	protected Set<List<OperationInstance>> postfixes;
	
	protected CryptoBoxScorer scorer;
	protected CryptoBoxMatrix startMatrix;
	protected long steps = -1;
	protected List<OperationInstance> oisCurrent = new ArrayList<OperationInstance>();

	public CryptoBoxSolver() {
		this.prefixes = DEFAULT_PREFIXES;
		this.postfixes = DEFAULT_POSTFIXES;
	}
	
	public CryptoBoxSolver(CryptoBoxSolver solver2) {
		this.setStartMatrix(solver2.startMatrix);
		this.setScorer(solver2.scorer);
		this.setSteps(solver2.steps);
		this.setPrefixes(solver2.prefixes);
		this.setPostfixes(solver2.postfixes);
	}

	public CryptoBoxSolver setPrefixes(Set<List<OperationInstance>> prefixes) {
		if (prefixes != null && !prefixes.isEmpty()) this.prefixes = prefixes;
		return this;
	}

	public CryptoBoxSolver setPostfixes(Set<List<OperationInstance>> postfixes) {
		if (postfixes != null && !postfixes.isEmpty()) this.postfixes = postfixes;
		return this;
	}

	
	public CryptoBoxSolver setSteps(long steps) {
		this.steps = steps;
		return this;
	}

	public CryptoBoxScorer getScorer() {
		return scorer;
	}

	public CryptoBoxSolver setScorer(CryptoBoxScorer scorer) {
		this.scorer = scorer;
		return this;
	}

	public CryptoBoxMatrix getStartMatrix() {
		return startMatrix;
	}

	public CryptoBoxSolver setStartMatrix(CryptoBoxMatrix startMatrix) {
		this.startMatrix = startMatrix;
		if (this.oisCurrent.isEmpty()) this.oisCurrent = getOisAll(startMatrix.size);
		return this;
	}

	public CryptoBoxSolver setOisCurrent(List<OperationInstance> oisCurrent) {
		this.oisCurrent = oisCurrent;
		if (this.oisCurrent.isEmpty()) this.oisCurrent = getOisAll(startMatrix.size);

		return this;
	}
	
	//TODO support mixed sizes?
	protected synchronized List<OperationInstance> getOisAll(int size) {
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

	protected void logResult(CryptoBoxResult result) {
		long now = System.currentTimeMillis();
		
		StringBuilder message = new StringBuilder();
		message.append("Total tries: " + result.tries + " maxScore: "
				+ result.maxScore + " total time " + (((now-result.startTime)<60000)?(now-result.startTime) + "ms.":((now - result.startTime) / 1000) + "s.")
				+ " which is " + ((result.tries * 1000) / (Math.max(1,now - result.startTime)))
				+ " tries per second. solution found after: " + (((result.foundTime-result.startTime)<60000)?(result.foundTime-result.startTime) + "ms.":((result.foundTime - result.startTime) / 1000) + "s."));
	
		System.out.println(message);
		
		
		int i = 0;
		for (MatrixState maxScorer: result.maxScorerStates){
			System.out.println(maxScorer);
			if (i >= 100) {
				System.out.println("...stopped after printing 100 maxscorers (" + result.maxScorerStates.size() + " total).");
				break;
			}
			i++;
		}
		System.out.println("maxScorers: " + result.maxScorerStates.size());
		System.out.println("maxScorersUniqueResults: " + result.maxScorersUniqueResults.size());
		
		System.out.println(CounterSingletons.TRIES.counter.get() + " / " + result.bruteTries + " = " + (CounterSingletons.TRIES.counter.get() * 100 / result.bruteTries.longValue()) + "%");
	}

	protected void logProgress(CryptoBoxResult intermediateResult, MatrixState state, boolean force) {
		long now = System.currentTimeMillis();
		long effectiveTries = intermediateResult.tries;
		if (!force) {
			if (COUNT_ATOMIC) {
				effectiveTries = CounterSingletons.TRIES.counter.get();
			}
		}
		if (force || effectiveTries % 10000000l == 0) {
			
//			System.out.println(state +""+  intermediateResult);
			System.out.println(StringUtils.leftPad(String.valueOf(CounterSingletons.TRIES.counter.get()), 10)  + " "  + intermediateResult);
		}
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
	
	public abstract CryptoBoxResult solve();

	public abstract CryptoBoxResult solveContinueFrom(MatrixState state);
	
}
