package nl.scholten.crypto.cryptobox.solver;

import java.util.HashSet;
import java.util.Set;

public class MatrixResult {
	public long tries;
	public long startTime;
	public long foundTime;	

	public int maxScore;
	protected Set<MatrixState> maxScorersSet;
	
	
	public MatrixResult() {
		tries = 0;
		startTime = System.currentTimeMillis();
		foundTime = -1;
		maxScore = -1;
		maxScorersSet = new HashSet<MatrixState>();
	}
}
