package nl.scholten.crypto.cryptobox.data;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;

public class CryptoBoxResult {
	public long tries;
	public long startTime;
	public long foundTime;	

	public int maxScore;
	public Set<MatrixState> maxScorersSet;

	public static volatile AtomicLong triesGlobal = new AtomicLong();
	public static volatile AtomicLong maxScoreGlobal = new AtomicLong();
	
	public static Long bruteTries;
	
	public CryptoBoxResult(int size, Long bruteTries) {
		tries = 0;
		startTime = System.currentTimeMillis();
		foundTime = -1;
		maxScore = -1;
		maxScorersSet = new HashSet<MatrixState>();
		triesGlobal = new AtomicLong();
		maxScoreGlobal = new AtomicLong();
		this.bruteTries = bruteTries;

	}
	
	public CryptoBoxResult merge(CryptoBoxResult result2) {
		if (result2.maxScore > 0 && result2.maxScore >= this.maxScore) {

			if (result2.maxScore > this.maxScore) {
				this.maxScorersSet.clear();
				this.foundTime = result2.foundTime;
			} else {
//				this.foundTime = Math.min(a, b)
			}

			this.maxScore = result2.maxScore;
			this.maxScorersSet.addAll(result2.maxScorersSet);


			System.out.println("join: new max score " + StringUtils.leftPad(String.valueOf(result2.maxScore), 3)
					+ " for: " + result2.maxScorersSet);
		}
		this.tries += result2.tries;
		this.startTime = Math.min(this.startTime, result2.startTime);

		return this;
	}
	
	public CryptoBoxResult merge(MatrixState state) {
		if (state.score > 0 && state.score >= this.maxScore) {

			if (state.score > this.maxScore) {
				this.maxScorersSet.clear();
			}

			this.maxScore = state.score;
			this.maxScorersSet.add(new MatrixState(state));

			this.foundTime = System.currentTimeMillis();

//			System.out.println("join: new (local) max score " + this.maxScore + " for: " + state);
			
			long maxGlobal = maxScoreGlobal.get();
			if (state.score >= maxGlobal) {
				// we have to make sure another thread hasn't found a different high
				// score which might get overwritten by us
				if (maxScoreGlobal.compareAndSet(maxGlobal, state.score)) {
					long now = System.currentTimeMillis();
					System.out.println("serial: new max score " + state);
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
		if (maxScorersSet.isEmpty()) return null;
		
		return (MatrixState)maxScorersSet.toArray()[0];
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
		result.append(" maxScorers: " + this.maxScorersSet);
		
		return result.toString();
				
	}
	
}


