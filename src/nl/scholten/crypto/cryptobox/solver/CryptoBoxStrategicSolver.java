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
 */
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nl.scholten.crypto.cryptobox.data.CounterSingletons;
import nl.scholten.crypto.cryptobox.data.CryptoBoxResult;
import nl.scholten.crypto.cryptobox.data.MatrixState;
import nl.scholten.crypto.cryptobox.data.OperationInstance;
import nl.scholten.crypto.cryptobox.util.HeapPermute;

import org.apache.commons.lang3.Validate;

public class CryptoBoxStrategicSolver extends CryptoBoxSolver {

	private static final List<OperationInstance> EMPTY_HEAD_START = new ArrayList<OperationInstance>();

	public CryptoBoxStrategicSolver() {
		super();
	}

	public void preStart() {
		Validate.notNull(getScorer(), "scorer cannot be null");
		Validate.notNull(getStartMatrix(), "startMatrix cannot be null");
		Validate.isTrue(getStartMatrix().size > 0, "size must be above 0");
		Validate.isTrue(steps > 0, "steps must be above 0");
	}

	public CryptoBoxResult solve() {
		preStart();

		CounterSingletons.reset();
		long stratSteps = Math.min(steps, 4);
		long deltaSteps = 4;
//		long extraIterations = 2 * (steps / deltaSteps);
		long extraIterations = Math.max(0, 2 * ((steps / deltaSteps) - 1));
		

		CounterSingletons.reset();
		CryptoBoxSolver solverFirst = new CryptoBoxFJSerialSolver(this);
		CryptoBoxResult winnerFirst = solverFirst.setSteps(stratSteps).solve();
		
		Set<List<OperationInstance>> permuWinnersOpsLogs = winnerFirst.getMaxScorerOpsLogs();

		stratSteps += deltaSteps;
		
		while (stratSteps <= steps + (extraIterations * deltaSteps)) { 
			long previousWinnerLength = 0;
			if (permuWinnersOpsLogs.size() > 0) previousWinnerLength = ((List<OperationInstance>)permuWinnersOpsLogs.toArray()[0]).size();
			
			//first try next 4 steps
			System.out.println("Calculating next steps for steps=" + stratSteps + " with " + permuWinnersOpsLogs.size() + " previous winners of length " + previousWinnerLength);

			System.out.println("prefixes:");
			CounterSingletons.reset();
			CryptoBoxSolver solverPrefix = new CryptoBoxFJSerialSolver(this);
			solverPrefix.setPrefixes(permuWinnersOpsLogs);
			CryptoBoxResult winnerPrefix = solverPrefix.setSteps(stratSteps).solve();

//			if (stratSteps > 4) System.exit(0);

			System.out.println("postfixes:");
			CounterSingletons.reset();
			CryptoBoxSolver solverPostfix = new CryptoBoxFJSerialSolver(this);
//			CryptoBoxSolver solverPostfix = new CryptoBoxSerialSolver(this);
			solverPostfix.setPostfixes(permuWinnersOpsLogs);
			CryptoBoxResult winnerPostfix = solverPostfix.setSteps(stratSteps).solve();

			if (winnerPrefix.maxScore > winnerPostfix.maxScore) System.out.println("fix winner: prefix"); else
			if (winnerPrefix.maxScore == winnerPostfix.maxScore) System.out.println("fix winner: prefix-postfix"); else
			System.out.println("fix winner: postfix");

			//should we keep both winners?
			CryptoBoxResult winner = CryptoBoxResult.joinResults(new CryptoBoxResult[]{winnerPrefix, winnerPostfix});

			
			//for all maxscorers, get their permutations and find the best one(s)
			Set<List<OperationInstance>> permutations = new HashSet<List<OperationInstance>>();
			System.out.println("Generating permutations for " + winner.maxScorerStates.size() + " maxScorers.");
			for(MatrixState winState: winner.maxScorerStates) {
				List<OperationInstance> winStateOpsLog = winState.opsLog;

				if (stratSteps <= 10) {
					if (permutations.contains(winStateOpsLog)) {
						System.out.println("Skipping permutations of " + winStateOpsLog);
					}
					
					//try all permutations of winner to find best ones
				
					OperationInstance[] winnerOpsLogArray = winStateOpsLog.toArray(new OperationInstance[0]);
					
					//gather all permutations for all maxscorers (set, so unique)
					HeapPermute.permute(winnerOpsLogArray, winnerOpsLogArray.length, permutations);
										
				} else {
					//stick to winner
					permutations.add(winStateOpsLog);
				}
			}

			System.out.println("Calculating permutation winners for " + permutations.size() + " permutations.");

			CounterSingletons.reset();
			CryptoBoxSolver permuSolver = new CryptoBoxFJSerialSolver(this);
			permuSolver.setSteps(stratSteps);
			permuSolver.setPrefixes(permutations);
			
			CryptoBoxResult permuWinners = permuSolver.solve();			

			//convert permuwinner to new headstarts
//			for (MatrixState permuWinner: permuWinners.maxScorerStates) {
//				permuWinnersOpsLogs.add(permuWinner.opsLog);
//			}
			permuWinnersOpsLogs = permuWinners.getMaxScorerUniqueResultOpsLogs();
			
			stratSteps += deltaSteps;
//			if (stratSteps > steps) stratSteps = steps;

		}
		System.out.println("Finished");
		
		return null;
	}

	@Override
	public CryptoBoxResult solveContinueFrom(MatrixState state) {
		throw new UnsupportedOperationException();
	}

}
