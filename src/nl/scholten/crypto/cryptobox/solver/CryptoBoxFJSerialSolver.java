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
 */import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;

import nl.scholten.crypto.cryptobox.data.CryptoBoxResult;
import nl.scholten.crypto.cryptobox.data.MatrixState;
import nl.scholten.crypto.cryptobox.data.OperationInstance;
import nl.scholten.crypto.cryptobox.util.HeapPermute;

import org.apache.commons.lang3.Validate;

public class CryptoBoxFJSerialSolver extends CryptoBoxSolver {
	
	public final static int FJ_POOL_SIZE = Runtime.getRuntime().availableProcessors();
//	public final static int FJ_POOL_SIZE = Runtime.getRuntime().availableProcessors() / 4;
	
	/**
	 * How many parallel steps before going serial.
	 */
	public static int PARALLEL_STEPS = 1;

	public CryptoBoxFJSerialSolver() {
		super();
	}
	
	public void preStart() {
		Validate.notNull(getScorer(), "scorer cannot be null");
		Validate.notNull(getStartMatrix(), "startMatrix cannot be null");
		Validate.isTrue(getStartMatrix().size > 0, "size must be above 0");
		Validate.isTrue(steps > 0, "steps must be above 0");
		
	}
	
	public CryptoBoxResult solve() {
		System.out.println("Starting with fork join");

		preStart();

		ForkJoinPool mainPool = new ForkJoinPool(FJ_POOL_SIZE);
		
		CryptoBoxFJSerialSolverTask mainTask = new CryptoBoxFJSerialSolverTask(oisCurrent, steps, PARALLEL_STEPS, startMatrix, scorer, headStarts);
		CryptoBoxResult winner = mainPool.invoke(mainTask);

		logResult(winner);

		//expirement with permutations
		List<OperationInstance> winnerOpsLog = winner.maxScorersSet.toArray(new MatrixState[0])[0].opsLog;
		OperationInstance[] winnerOpsLogArray = winnerOpsLog.toArray(new OperationInstance[0]);
		
		//try all permutations as headstart to see best one
		Set<List<OperationInstance>> permutations = new HashSet<List<OperationInstance>>();
		HeapPermute.permute(winnerOpsLogArray, winnerOpsLogArray.length, permutations);
		
		CryptoBoxFJSerialSolverTask mainTask2 = new CryptoBoxFJSerialSolverTask(oisCurrent, steps, PARALLEL_STEPS, startMatrix, scorer, permutations);
		CryptoBoxResult winner2 = mainPool.invoke(mainTask);
		
		logResult(winner2);
		
		return winner;
	}

	@Override
	public CryptoBoxResult solveContinueFrom(MatrixState state) {
		throw new UnsupportedOperationException("not implemented yet");
	}

	
}
