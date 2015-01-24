package nl.scholten.crypto.cryptobox.solver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import nl.scholten.crypto.cryptobox.data.CryptoBoxMatrix;
import nl.scholten.crypto.cryptobox.data.CryptoBoxResult;
import nl.scholten.crypto.cryptobox.data.MatrixState;
import nl.scholten.crypto.cryptobox.data.OperationInstance;

import org.apache.commons.lang3.StringUtils;

public class CuckooSolver extends CryptoBoxSolver {

	public final static int MAX_RANDOM_WALK_ATTEMPTS = 1000;
	
	public final static int nrOfNests = 20;
	//fraction of nests to abandon
	public final static double p = 0.25;
	//fraction of parameters from end of opslog to change in randomwalk
//	public final static double R = 0.50;
	public final static double R = 0.10;
	
	public List<MatrixState> nests;
	public long maxGenerations;
	public final Random rand;
	
	public CuckooSolver() {
		rand = new Random();
	}
	
	@Override
	public CryptoBoxResult solve() {
		CryptoBoxResult result = new CryptoBoxResult(this.startMatrix.size, 1L);
		result.startTime = System.currentTimeMillis();

//		maxGenerations = 10;
//		maxGenerations = 5000;
//		maxGenerations = 50000;
		maxGenerations = 500000;
//		maxGenerations = 5000000;
		nests = createRandomNests(nrOfNests, steps, this.startMatrix);

		long globalMax = 0;
		for (int t = 0; t < maxGenerations; t++) {
//			System.out.println("generation " + t);
			//pick only from top 1-p fraction?
			int i = rand.nextInt(nrOfNests);
			MatrixState state = nests.get(i);
			
//			System.out.println("BEFORE: " + state.opsLog);
			MatrixState newState = performRandomWalkCheckConstraints(state, t, R);
//			System.out.println("AFTER: " + newState.opsLog);
			
		    int j = i;
		    while (j == i) {
		    	j = rand.nextInt(nrOfNests);
		    }  

			MatrixState targetState = nests.get(j);

//			if (newState.score > state.score && newState.score > targetState.score) {
			if (newState.score > targetState.score) {
				//new solution is better, so overwrite existing nest
//				System.out.println("improvement " + newState.score + " over " + targetState.score);
				targetState.copyFrom(newState);
			}
		      
			nests = abandonWorstSolutions(nests, nrOfNests, steps, t);
			
//			printScores(nests);
			MatrixState winner = nests.get(nests.size() - 1);
			if (winner.score > globalMax) {
				globalMax = winner.score;
				System.out.println("new winner: " + StringUtils.leftPad(String.valueOf(globalMax), 3)  + " iteration " + StringUtils.leftPad(String.valueOf(t), 10) + " " + winner.matrix.data);
			}
		}
		
		//sort and pick top nest as result.
		Collections.sort(nests);
		
		MatrixState winner = nests.get(nests.size() - 1);

		for (MatrixState nest: nests) {
			result.merge(nest);
		}
		
		result.foundTime = System.currentTimeMillis();
////		result.maxScore = winner.score;
//		result.maxScorerStates.add(winner);
//		result.maxScorersUniqueResults.put(winner.matrix.data, winner);
//		result.topScorersMap.put(new Integer(winner.score), Collections.singletonList(winner));
		

		logResult(result);
		printScores(nests);
		System.out.println(winner.matrix.data);
		return result;
	}
	
	private List<MatrixState> createRandomNests(int nrOfNests, long steps, CryptoBoxMatrix matrix) {
		List<MatrixState> nests = new ArrayList<MatrixState>(nrOfNests);
		for (int i = 0; i < nrOfNests; i++) {
			nests.add(createRandomNest(steps, matrix));
		}
//		System.out.println("Created nests: " + nests);
		return nests;
	}

	private MatrixState createRandomNest(long steps, CryptoBoxMatrix matrix) {
		MatrixState nest = new MatrixState(matrix, steps);
		List<OperationInstance> oisAll = getOisAll(matrix.size);
		List<OperationInstance> randomOpsLog;
		do {
			Collections.shuffle(oisAll);
			randomOpsLog = oisAll.subList(0, (int)steps);
//		} while (!true && !OpsLogHelper.isMeaningfull(randomOpsLog));
		} while (!OpsLogHelper.isMeaningfull(randomOpsLog));
		
		nest.apply(randomOpsLog);
		nest.score = scorer.score(nest.matrix);
		return nest;
	}
	
	private MatrixState performRandomWalkCheckConstraints(MatrixState state, int generation, double fraction) {
		OperationInstance[] newOpsLog = state.opsLog.toArray(new OperationInstance[0]);
		int tries = 0;
		do {
			if (tries > MAX_RANDOM_WALK_ATTEMPTS) {
	            System.out.printf("Could not generate new random solution! Perhaps you should widen your constraints.");
	            //TODO make sure newState == null is handled correctly?
	            return null;
			}

			
//			OperationInstance[] newOpsLog = performRandomWalkFractionEnd(opsLog, state.matrix.size, generation, fraction);
			newOpsLog = performRandomWalkFractionAll(newOpsLog, state.matrix.size, generation, fraction);

			/* If the random walk resulted in a solution that is not within constraints,
		     * then try another random walk from the original solution. */
			
		    tries++;
		} while(!checkConstraint(newOpsLog));
		if (tries > 100) System.out.println("Needed " + tries + " to perform walk");
		
		MatrixState newState = new MatrixState(this.startMatrix, steps);
		newState.apply(newOpsLog);
		newState.score = scorer.score(newState.matrix);
		
		return newState;
	}
	
	private OperationInstance[] performRandomWalkFractionAll(OperationInstance[] opsLog, int size, int generation, double fraction) {

		//for fraction R of opsLog items, shift item in the opslog by a random index
		//total number of ops == 4 * matrix.size
		for (int i = 0; i < opsLog.length; i++) {
			if (rand.nextDouble() < fraction) {
				int shift = rand.nextInt(4 * size);
//				System.out.println("shifting " + shift);
				OperationInstance newOI = OpsLogHelper.shift(opsLog[i], shift, size);
				opsLog[i] = newOI;
			}
		}

		return opsLog;
    }	

	private OperationInstance[] performRandomWalkFractionEnd(OperationInstance[] opsLog, int size, int generation, double fraction) {
		//for fraction R of opsLog items, shift item in the opslog by a random index
		//total number of ops == 4 * matrix.size
		Double nrOfOIsToShift = fraction * opsLog.length;
		
		for (int i = 0; i < nrOfOIsToShift.intValue(); i++) {
			int shift = rand.nextInt(4 * size);
//			System.out.println("shifting " + shift);
			OperationInstance newOI = OpsLogHelper.shift(opsLog[opsLog.length - i - 1], shift, size);
//			opsLog[i] = newOI;
			opsLog[opsLog.length - i - 1] = newOI;
		}
		return opsLog;
    }	
	
	private List<MatrixState> abandonWorstSolutions(List<MatrixState> nests,
			int nrOfNests, long steps, int generation) {
		
		List<MatrixState> sortedNests = new ArrayList<MatrixState>(nests);
		Collections.sort(sortedNests);
//		printScores(nests);

		//TODO any rounding needed for nrOfNestsToAbanadon?
		Double nrOfNestsToAbanadon = p * nrOfNests;
		
//		System.out.println("dropping " + nrOfNestsToAbanadon.intValue() + " nests.");
		//leave out with first nrOfNestsToAbandon
//		sortedNests = sortedNests.subList(nrOfNestsToAbanadon.intValue(), sortedNests.size());
		
		//TODO use random walk from abandoned solution instead of random solution?
//		sortedNests.addAll(createRandomNests(nrOfNestsToAbanadon.intValue(), steps, this.startMatrix));
		
		for (int i = 0; i < nrOfNestsToAbanadon; i++) {
			MatrixState abandon = sortedNests.get(i);
//			System.out.println("ABANDONED: " + abandon);
			MatrixState replacer = performRandomWalkCheckConstraints(abandon, generation, 1 - R);
			sortedNests.set(i, replacer);
//			System.out.println("GENERATED: " + sortedNests.get(i));
		}
		
		Collections.sort(sortedNests);
		
		return sortedNests;
	}

	private void printScores(List<MatrixState> nests) {
		for (MatrixState nest: nests) {
			System.out.print(nest.score + ", ");
		}		
		System.out.println();
	}

	private boolean checkConstraint(OperationInstance[] opsLog) {
		// TODO Remove constraint check?
//		boolean isMeaningfull = OpsLogHelper.isMeaningfull(opsLog);
		boolean isMeaningfull = true;
		if (!isMeaningfull) {
			System.out.println("Not meaningfull: " + Arrays.toString(opsLog));
		}
		return isMeaningfull;
	}

	
	@Override
	public CryptoBoxResult solveContinueFrom(MatrixState state) {
		// TODO Auto-generated method stub
		return null;
	}

}
