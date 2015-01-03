package nl.scholten.crypto.cryptobox.solver;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nl.scholten.crypto.cryptobox.data.CryptoBoxResult;
import nl.scholten.crypto.cryptobox.data.MatrixState;
import nl.scholten.crypto.cryptobox.data.OperationInstance;
import nl.scholten.crypto.cryptobox.util.Util;

import org.apache.commons.lang3.Validate;
import org.apache.commons.math3.util.CombinatoricsUtils;

public class CryptoBoxPermuSolver extends CryptoBoxSolver {

	protected int startPermu = 0, endPermu = 0;
	protected Set<List<OperationInstance>> permuSources;

	public CryptoBoxPermuSolver() {
		super();
	}

	public CryptoBoxPermuSolver(
			CryptoBoxSolver solver2) {
		super(solver2);
	}

	public CryptoBoxPermuSolver setPermuSources(Set<List<OperationInstance>> permuSources) {
		this.permuSources = permuSources;
		return this;
	}
	
	public void preStart() {
		Validate.notNull(getScorer(), "scorer cannot be null");
		Validate.notNull(getStartMatrix(), "startMatrix cannot be null");
		Validate.isTrue(getStartMatrix().size > 0, "size must be above 0");
		Validate.isTrue(steps >= 0, "steps must be non-negative");
		
	}

	public CryptoBoxResult solve() {
		CryptoBoxResult winner = solveContinueFrom();
		logResult(winner);
		return winner;		
	}

	public CryptoBoxResult solveInternal(long startPermu, long endPermu) {
		preStart();

		MatrixState state = new MatrixState(startMatrix, steps);
		//count existing opslog to make bruteTries correct.
		Double bruteTries = Math.pow(oisCurrent.size(), steps);
		
		CryptoBoxResult winner = doSolvePermus(new CryptoBoxResult(startMatrix.size, bruteTries.longValue()), state, permuSources, startPermu, endPermu);

		return winner;
	}
	
	private CryptoBoxResult doSolvePermus(
			CryptoBoxResult intermediateResult, MatrixState state, Set<List<OperationInstance>> permuSources, long startPermu, long endPermu) {
	
		//convert to array up front
		Set<OperationInstance[]> permuSourceArrays = new HashSet<OperationInstance[]>();
		for (List<OperationInstance> permuSource: permuSources) {
			permuSourceArrays.add(permuSource.toArray(new OperationInstance[0]));
		}
		
		for(long k = startPermu; k < endPermu; k++) {
			if (k % 10000000 == 0) System.out.println("k: " + k + "/" + endPermu + " " + (k*100)/endPermu + "%");
			for(OperationInstance[] permuSourceArray: permuSourceArrays) {
				OperationInstance[] perm = Util.permutation(k, permuSourceArray);
				
				if (OpsLogHelper.isMeaningfull(perm)) {
					state.apply(perm);
					doScoring(intermediateResult, state);
					logProgress(intermediateResult, state, false);
					state.unapply(perm);
				} else {
	//				System.out.println("not meaningfull: " + Arrays.asList(perm));
				}
			}
		}
		
		return intermediateResult;
	}
	
	
	private void doScoring(CryptoBoxResult partialResult, MatrixState state) {
		int score = getScorer().score(state.matrix);

		state.score = score;
		partialResult.merge(state);
	}

	@Override
	public CryptoBoxResult solveContinueFrom(MatrixState state) {
		throw new UnsupportedOperationException();
	}

	public CryptoBoxResult solveContinueFrom() {
		return solveInternal(0, CombinatoricsUtils.factorial((int)steps));
	}


	
}
