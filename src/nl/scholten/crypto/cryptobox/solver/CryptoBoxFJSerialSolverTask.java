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

public class CryptoBoxFJSerialSolverTask extends RecursiveTask<CryptoBoxResult> {

	private static final long serialVersionUID = 1L;

	private List<OperationInstance> ois;
	private int paralellStepsLeft;
	private MatrixState state;
	private CryptoBoxScorer scorer;
	private Set<List<OperationInstance>> headStarts;

	public CryptoBoxFJSerialSolverTask(List<OperationInstance> ois,
			int stepsLeft, int paralellSteps, CryptoBoxMatrix matrix,
			CryptoBoxScorer scorer, Set<List<OperationInstance>> headStarts) {
		super();
		this.ois = ois;

		this.state = new MatrixState(matrix, stepsLeft);
		this.paralellStepsLeft = paralellSteps;

		this.scorer = scorer;
		this.headStarts = headStarts;
	}

	public CryptoBoxFJSerialSolverTask(CryptoBoxFJSerialSolverTask task2) {
		this.headStarts = new HashSet<List<OperationInstance>>();
		this.paralellStepsLeft = task2.paralellStepsLeft;
		this.scorer = task2.scorer;
		this.ois = task2.ois;
		this.state = new MatrixState(task2.state);
	}

	@Override
	protected CryptoBoxResult compute() {
		int l = ois.size();

		List<RecursiveTask<CryptoBoxResult>> tasks = new ArrayList<RecursiveTask<CryptoBoxResult>>();

		if (!headStarts.isEmpty()) {
			// if we have headstarts, first fork by headstarts
			for (List<OperationInstance> headStart : headStarts) {
				CryptoBoxFJSerialSolverTask task1 = new CryptoBoxFJSerialSolverTask(
						this);
				task1.state.apply(headStart);
				task1.state.opsLog = headStart;
				tasks.add(task1);
			}
			// tasks setup

		} else {
			// no head starts, so fork by OI

			if (paralellStepsLeft > 0) {
				for (OperationInstance oi : this.ois) {
					CryptoBoxFJSerialSolverTask task1 = new CryptoBoxFJSerialSolverTask(
							this);
					task1.state.apply(oi);
					task1.paralellStepsLeft--;
					tasks.add(task1);
				}

				// tasks setup
			} else {
				return new CryptoBoxSerialSolver().setScorer(scorer).setStartMatrix(state.matrix).solveContinueFrom(state);
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
