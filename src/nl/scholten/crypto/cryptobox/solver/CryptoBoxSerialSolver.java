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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nl.scholten.crypto.cryptobox.data.CryptoBoxResult;
import nl.scholten.crypto.cryptobox.data.MatrixState;
import nl.scholten.crypto.cryptobox.data.OperationInstance;
import nl.scholten.crypto.cryptobox.util.HeapPermute;

import org.apache.commons.lang3.Validate;

public class CryptoBoxSerialSolver extends CryptoBoxSolver {

	private static final List<OperationInstance> EMPTY_HEAD_START = new ArrayList<OperationInstance>();

	public CryptoBoxSerialSolver() {
		super();
	}

	public void preStart() {
		Validate.notNull(getScorer(), "scorer cannot be null");
		Validate.notNull(getStartMatrix(), "startMatrix cannot be null");
		Validate.isTrue(getStartMatrix().size > 0, "size must be above 0");
		Validate.isTrue(steps > 0, "steps must be above 0");
	}

	public CryptoBoxResult solve() {
		CryptoBoxResult winner = solveContinueFrom(new MatrixState(startMatrix, 4));
//		CryptoBoxResult winner = solveContinueFrom(new MatrixState(startMatrix, 12));
		logResult(winner);
		
//		try {System.in.read();} catch (IOException e1) {}

		while (steps < 1000000000) {
			//try all maxscorers with 4 next steps
			
			Set<List<OperationInstance>> permutations = new HashSet<List<OperationInstance>>();
			System.out.println("Generating permutations for " + winner.maxScorerStates.size() + " maxScorers.");
			for(MatrixState winState: winner.maxScorerStates) {
				List<OperationInstance> winStateOpsLog = winState.opsLog;

				//ignore last two of opslog ???
				if (winStateOpsLog.size() > 4) {
//					winStateOpsLog.remove(winStateOpsLog.size() - 1);
//					winStateOpsLog.remove(winStateOpsLog.size() - 1);
				}
				
				if (steps <= 10) {
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

			System.out.println("Calculating permutation winners " + permutations.size() + " permutations.");

			setHeadStarts(permutations);
			
//			CryptoBoxResult permuWinners = solveContinueFrom(new MatrixState(startMatrix, ((List<OperationInstance>)permutations.toArray()[0]).size()));
			CryptoBoxResult permuWinners = solveContinueFrom(new MatrixState(startMatrix, steps));			
			logResult(permuWinners);

			//convert permuwinner to new headstarts
			Set<List<OperationInstance>> permuWinnersOpsLogs = new HashSet<List<OperationInstance>>();

//			for (MatrixState permuWinner: permuWinners.maxScorerStates) {
//				permuWinnersOpsLogs.add(permuWinner.opsLog);
//			}

			for (String result: permuWinners.maxScorersUniqueResults.keySet()) {
				permuWinnersOpsLogs.add(permuWinners.maxScorersUniqueResults.get(result).opsLog);
			}

			
			System.out.println("Calculating next steps for steps=" + (steps + 4));
			setHeadStarts(permuWinnersOpsLogs);
			winner = solveContinueFrom(new MatrixState(startMatrix, steps + 4));
			logResult(winner);
		}
		
		//try all maxscorers with 4 next steps
		List<OperationInstance> winnerOpsLog = winner.maxScorerStates.toArray(new MatrixState[0])[0].opsLog;
		OperationInstance[] winnerOpsLogArray = winnerOpsLog.toArray(new OperationInstance[0]);
		
		//try all permutations as headstart to see best one
		Set<List<OperationInstance>> permutations = new HashSet<List<OperationInstance>>();
		HeapPermute.permute(winnerOpsLogArray, winnerOpsLogArray.length, permutations);
		
		setHeadStarts(permutations);
		
		CryptoBoxResult winner2 = solveContinueFrom(new MatrixState(startMatrix, steps));
		
		logResult(winner);
		logResult(winner2);
		
//		try {System.in.read();} catch (IOException e1) {}

		//try all maxscorers with 4 next steps
		for(MatrixState winState: winner.maxScorerStates) {
			List<OperationInstance> winStateOpsLog = winState.opsLog;
			
			setHeadStarts(Collections.singleton(winStateOpsLog));
			
			CryptoBoxResult nextWinner2 = solveContinueFrom(new MatrixState(startMatrix, steps + 4));
			
			logResult(nextWinner2);
		}
		
		return winner;
	}


	@Override
	public CryptoBoxResult solveContinueFrom(MatrixState state2) {
		System.out.println("Starting serially");
		this.steps = state2.stepsLeft;

		preStart();

		MatrixState state = new MatrixState(state2);
		//count existing opslog to make bruteTries correct.
		steps = state.opsLog.size() + steps;
		CryptoBoxResult winner = solveSeriallyInternal(state);

//		logResult(winner);
		return winner;
	}
	
	private CryptoBoxResult solveSeriallyInternal(MatrixState state) {
		Double bruteTries = Math.pow(oisCurrent.size(), steps);

		// no headstart -> use empty headstart
		if (headStarts == null || headStarts.isEmpty()) {
			return solvedSeriallyInternalHeadStart(new CryptoBoxResult(state.matrix.size, bruteTries.longValue()),
					state, EMPTY_HEAD_START);
		}

//		List<CryptoBoxResult> partialResults = new ArrayList<CryptoBoxResult>();
//		for (List<OperationInstance> headStart : headStarts) {
//			MatrixState state2 = new MatrixState(state);
//			partialResults.add(solvedSeriallyInternalHeadStart(
//					new CryptoBoxResult(state.matrix.size, bruteTries.longValue()), state2, headStart));
//		}
//
//		return CryptoBoxResult.joinResults(partialResults);
		
		CryptoBoxResult partialResult = new CryptoBoxResult(state.matrix.size, bruteTries.longValue());
		for (List<OperationInstance> headStart : headStarts) {
			MatrixState state2 = new MatrixState(state);
			solvedSeriallyInternalHeadStart(partialResult, state2, headStart);
		}

		return partialResult;
		
		
	}

	private CryptoBoxResult solvedSeriallyInternalHeadStart(
			CryptoBoxResult intermediateResult, MatrixState state,
			List<OperationInstance> headStart) {
		// apply headstart (can be empty)
		state.apply(headStart);

		// if no steps left, do scoring
		if (state.stepsLeft == 0) {
			doScoring(intermediateResult, state);

			logProgress(intermediateResult, state, false);

			return intermediateResult;
		}

		// else perform next step
		return doSolveSeriallyInternalNextStep(intermediateResult, state);
	}

	private CryptoBoxResult doSolveSeriallyInternalNextStep(
			CryptoBoxResult intermediateResult, MatrixState state) {

		if (state.stepsLeft == 0) {
			//headstart could have been full length, so in that case just calculate.
			return solvedSeriallyInternalHeadStart(intermediateResult, state, EMPTY_HEAD_START);
		}
		
		OperationInstance prevOIA = null;
		OperationInstance prevOIB = null;
		if (state.opsLog.size() > 0) {
			prevOIA = state.opsLog.get(state.opsLog.size() - 1);
		}
		if (state.opsLog.size() > 1) {
			prevOIB = state.opsLog.get(state.opsLog.size() - 2);
		}

		for (OperationInstance oi : oisCurrent) {
			// no more than 2 of the same oi adjacent
			if (prevOIA != prevOIB || (oi != prevOIA || oi != prevOIB)) {

				if ( // make sure all segments of the same type (row/col) are in
						// ascending order. Order doesn't matter, so only
						// calculate for those with ascending order)
				(prevOIA == null) // no previous OI, so always go.
						|| (oi.op.isRow != prevOIA.op.isRow) // row after col or
																// col after row
																// always go
						|| (oi.op.isRow == prevOIA.op.isRow && oi.index > prevOIA.index) // row
																							// after
																							// row
																							// should
																							// ascend,
																							// same
																							// for
																							// col
																							// after
																							// col.
																							// leaves
																							// only
																							// 43%
																							// of
																							// tries!
						|| (oi.op.isRow == prevOIA.op.isRow
								&& oi.index == prevOIA.index && (oi.op.isPositive == prevOIA.op.isPositive)) // CU_0
																												// CD_0
																												// not
																												// useful
																												// as
																												// they
																												// compensate
																												// eachother.
																												// leaves
																												// 39%
																												// of
																												// tries

				) {
					// the above are pure optimizations. All possible outcomes
					// are still calculated/scored.
					// the below are "fuzzy" optimizations. Skipping possible
					// valid opsLogs, so best solution might not be found, but
					// will give a good indication that might be enough to solve
					// it manually afterwards.
					if (!USE_FUZZY
							|| !USE_FUZZY_RANDOM_SKIP
							|| Math.random() * 100 > USE_FUZZY_RANDOM_SKIP_PERCENTAGE) {
						state.apply(oi);
						solvedSeriallyInternalHeadStart(intermediateResult,
								state, EMPTY_HEAD_START);
						state.unapply(oi);
					}
				}
			}
		}

		return intermediateResult;
	}

	private void doScoring(CryptoBoxResult partialResult, MatrixState state) {
		// if already cache, ignore
		int score = getScorer().score(state.matrix);

		// System.out.println("scored: " + state);

		processScore(partialResult, state, score);

	}

	private void processScore(CryptoBoxResult partialResult, MatrixState state,
			int score) {
		state.score = score;
		partialResult.merge(state);

	}

}
