package nl.scholten.crypto.cryptobox.solver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.RecursiveTask;

import nl.scholten.crypto.cryptobox.data.CryptoBoxMatrix;
import nl.scholten.crypto.cryptobox.data.CryptoBoxResult;
import nl.scholten.crypto.cryptobox.data.MatrixState;
import nl.scholten.crypto.cryptobox.data.OperationInstance;
import nl.scholten.crypto.cryptobox.scorer.CryptoBoxScorer;
import nl.scholten.crypto.cryptobox.util.Util;

public class CryptoBoxFJSerialSolverTask extends RecursiveTask<CryptoBoxResult> {

	private static final long serialVersionUID = 1L;

	private static final int MAX_FORKS = 8;

	private List<OperationInstance> ois;
	private long paralellStepsLeft;
	private MatrixState state;
	private CryptoBoxScorer scorer;
	private Set<List<OperationInstance>> prefixes;
	private Set<List<OperationInstance>> postfixes;
	private Set<List<OperationInstance>> permuSources;

	public CryptoBoxFJSerialSolverTask(List<OperationInstance> ois,
			long stepsLeft, long paralellSteps, CryptoBoxMatrix matrix,
			CryptoBoxScorer scorer, Set<List<OperationInstance>> prefixes, Set<List<OperationInstance>> postfixes, Set<List<OperationInstance>> permuSources) {
		super();
		this.ois = ois;

		this.state = new MatrixState(matrix, stepsLeft);
		this.paralellStepsLeft = paralellSteps;

		this.scorer = scorer;
		this.prefixes = prefixes;
		this.postfixes = postfixes;
		this.permuSources = permuSources;
	}

	public CryptoBoxFJSerialSolverTask(CryptoBoxFJSerialSolverTask task2) {
		this.prefixes = new HashSet<List<OperationInstance>>();
		this.paralellStepsLeft = task2.paralellStepsLeft;
		this.scorer = task2.scorer;
		this.ois = task2.ois;
		this.state = new MatrixState(task2.state);
		this.permuSources = new HashSet<List<OperationInstance>>();
	}

	@Override
	protected CryptoBoxResult compute() {
		List<RecursiveTask<CryptoBoxResult>> tasks = new ArrayList<RecursiveTask<CryptoBoxResult>>();

		if (paralellStepsLeft > 0) {

			if (!permuSources.isEmpty()) {
				//fork by permuSources
				Set<Set<List<OperationInstance>>> permuSourcesSubsets = Util.subSet(permuSources, MAX_FORKS);
				for (Set<List<OperationInstance>> subset: permuSourcesSubsets) {
					CryptoBoxFJSerialSolverTask task1 = new CryptoBoxFJSerialSolverTask(
							this);
					task1.permuSources = subset;
					task1.paralellStepsLeft--;
					tasks.add(task1);
				}
			} else if (prefixes.size() < MAX_FORKS) {
				//fork by prefix, but obey MAX_FORKS
				//fork by prefixes as usual
				for(List<OperationInstance> prefix: prefixes) {
					//always at least one, the empty opslog
					if (prefix.size() > 0) {
						//fork by prefix, never by postfix. postfix will ripple through to serial
						CryptoBoxFJSerialSolverTask task1 = new CryptoBoxFJSerialSolverTask(
								this);
						task1.state.apply(prefix);
						//pre/post fixes are not copied, so set postfixes
						task1.postfixes = this.postfixes;
						task1.state.opsLog = prefix;
						task1.paralellStepsLeft--;
						tasks.add(task1);
					} else {
						int test = 0;
						for (OperationInstance oi : this.ois) {
							CryptoBoxFJSerialSolverTask task1 = new CryptoBoxFJSerialSolverTask(
									this);
							//pre/post fixes are not copied, so set postfixes
							task1.postfixes = this.postfixes;
							task1.state.apply(oi);
							task1.paralellStepsLeft--;
							tasks.add(task1);
						}
					}
				}
			} else {
				
				Set<Set<List<OperationInstance>>> subsets = Util.subSet(prefixes, MAX_FORKS);
				for (Set<List<OperationInstance>> subset: subsets) {
					CryptoBoxFJSerialSolverTask task1 = new CryptoBoxFJSerialSolverTask(
							this);
					//pre/post fixes are not copied, so set them
					task1.prefixes = subset;
					task1.postfixes = this.postfixes;
					task1.paralellStepsLeft--;
					tasks.add(task1);
				}
			}
			
		} else {
			//go serial
			if (!permuSources.isEmpty()) {
				CryptoBoxPermuSolver permuSolver = new CryptoBoxPermuSolver();
				permuSolver.setPermuSources(permuSources).setSteps(state.stepsLeft).setScorer(scorer).setStartMatrix(state.matrix).setPrefixes(prefixes).setPostfixes(postfixes);
				return permuSolver.solveContinueFrom();
			} else {
				return new CryptoBoxSerialSolver().setScorer(scorer).setStartMatrix(state.matrix).setPrefixes(prefixes).setPostfixes(postfixes).solveContinueFrom(state);
			}
		} 
		
		// will return when tasks all are done.
		List<RecursiveTask<CryptoBoxResult>> results = (List<RecursiveTask<CryptoBoxResult>>) invokeAll(tasks);
		List<CryptoBoxResult> partialResults = new ArrayList<CryptoBoxResult>();
		for (RecursiveTask<CryptoBoxResult> task : results) {
			CryptoBoxResult m = task.getRawResult();
			partialResults.add(m);
		}
		
		return CryptoBoxResult.joinResults(partialResults);
	}


}
