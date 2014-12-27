package nl.scholten.crypto.cryptobox.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;

public class CryptoBoxResult {
	public long tries;
	public long startTime;
	public long foundTime;	

	public int maxScore;
	public Set<MatrixState> maxScorerStates;
	public Map<String, MatrixState> maxScorersUniqueResults;

	public static volatile AtomicLong triesGlobal = new AtomicLong();
	public static volatile AtomicLong maxScoreGlobal = new AtomicLong();
	
	public static Long bruteTries;
	
	public CryptoBoxResult(int size, Long bruteTries) {
		tries = 0;
		startTime = System.currentTimeMillis();
		foundTime = -1;
		maxScore = -1;
		maxScorerStates = new HashSet<MatrixState>();
		maxScorersUniqueResults = new HashMap<String, MatrixState>();
		triesGlobal = new AtomicLong();
		maxScoreGlobal = new AtomicLong();
		this.bruteTries = bruteTries;

	}
	
	public CryptoBoxResult merge(CryptoBoxResult result2) {
		if (result2.maxScore > 0 && result2.maxScore >= this.maxScore) {

			if (result2.maxScore > this.maxScore) {
				this.maxScorerStates.clear();
				this.maxScorersUniqueResults.clear();
				this.foundTime = result2.foundTime;
			} else {
//				this.foundTime = Math.min(a, b)
			}

			this.maxScore = result2.maxScore;
			this.maxScorerStates.addAll(result2.maxScorerStates);
			//possibly overwrites existing results, but we don't care we need only one oplog per unique result
			this.maxScorersUniqueResults.putAll(result2.maxScorersUniqueResults);

			System.out.println("join: new max score " + StringUtils.leftPad(String.valueOf(result2.maxScore), 3)
					+ " for: " + result2.maxScorerStates);
		}
		this.tries += result2.tries;
		this.startTime = Math.min(this.startTime, result2.startTime);

		return this;
	}
	
	public CryptoBoxResult merge(MatrixState state) {
		if (state.score > 0 && state.score >= this.maxScore) {

			if (state.score > this.maxScore) {
				this.maxScorerStates.clear();
				this.maxScorersUniqueResults.clear();
			}

			this.maxScore = state.score;
			MatrixState newState = new MatrixState(state);
			this.maxScorerStates.add(newState);
			//possibly overwrites existing unique result, we don't care only need one
			this.maxScorersUniqueResults.put(newState.matrix.data, newState);

			this.foundTime = System.currentTimeMillis();

//			System.out.println("join: new (local) max score " + this.maxScore + " for: " + state);
			
			long maxGlobal = maxScoreGlobal.get();
			if (state.score > maxGlobal) {
				// we have to make sure another thread hasn't found a different high
				// score which might get overwritten by us
				if (maxScoreGlobal.compareAndSet(maxGlobal, state.score)) {
					long now = System.currentTimeMillis();
					System.out.println("serial: new max score " + state.toString());
//					System.out.println("serial: new max score " + state.toStringPretty());
				}
			}
			
		} else {
////			System.out.println("join: not (local) max score " + StringUtils.leftPad(String.valueOf(this.maxScore), 3)
//					+ " for: " + state);
			
		}
		this.tries += 1;
		triesGlobal.getAndIncrement();

		return this;
	}
	
	public static CryptoBoxResult joinResults(List<CryptoBoxResult> partialResults) {
		CryptoBoxResult winner = null;
		for(CryptoBoxResult partialResult: partialResults){
			if (winner == null) {
				winner = partialResult;
			} else {
				winner.merge(partialResult);
			}
		}

		return winner;
	}
	
	
	public MatrixState getWinner() {
		if (maxScorerStates.isEmpty()) return null;
		
		return (MatrixState)maxScorerStates.toArray()[0];
	}
	
	@Override
	public String toString() {
		long now = System.currentTimeMillis();
		long delta = Math.max(1, now - this.startTime);
		long foundDelta = Math.max(1, now - this.foundTime);
		long triesPerSecond = ((this.tries * 1000) / delta);
		
		StringBuilder result = new StringBuilder();
		result.append("Total tries: " + this.tries);
		result.append(" maxScore: " + this.maxScore);
		result.append(" total time " + ((delta < 60000)?delta + "ms.":((delta / 1000) + "s.")));
		result.append(" i.e. " + triesPerSecond + " tries per second");
		result.append(" solution found after: " + ((foundDelta < 60000)?foundDelta + "ms.":(foundDelta / 1000) + "s."));
		result.append(" maxScorers: " + this.maxScorerStates.size() + "(" + this.maxScorersUniqueResults.size() + " unique) ");			
		if (this.maxScorerStates.size() <= 100) {
			result.append(" data: " + this.maxScorerStates);
		}
		
		return result.toString();
				
	}
	
}


