package nl.scholten.crypto.cryptobox.data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import nl.scholten.crypto.cryptobox.util.Util;

import org.apache.commons.lang3.StringUtils;

public class CryptoBoxResult {
	public long tries;
	public long startTime;
	public long foundTime;	

	public int maxScore;
	public Set<MatrixState> maxScorerStates;
	public Map<String, MatrixState> maxScorersUniqueResults;
	public SortedMap<Integer, List<MatrixState>> topScorersMap;
	
	
	public static Long bruteTries;
	
	public CryptoBoxResult(int size, Long bruteTries) {
		tries = 0;
		startTime = System.currentTimeMillis();
		foundTime = -1;
		maxScore = -1;
		maxScorerStates = new HashSet<MatrixState>();
		maxScorersUniqueResults = new HashMap<String, MatrixState>();
		this.bruteTries = bruteTries;

//		this.topScorersMap = new LinkedHashMap<Integer, List<MatrixState>>() {
//            @Override
//            protected boolean removeEldestEntry(Map.Entry<Integer, List<MatrixState>> eldest) {
//                //max last 10 highest scores
//            	return size() > 10;
//            }
//		};

		this.topScorersMap = new TreeMap<Integer, List<MatrixState>>();	
		
	}

	public CryptoBoxResult merge(MatrixState state) {
//		System.out.println("merging " + state);
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

			long maxGlobal = CounterSingletons.MAXSCORE.counter.get();
			if (state.score >= maxGlobal) {
//				// we have to make sure another thread hasn't found a different high
//				// score which might get overwritten by us
				if (CounterSingletons.MAXSCORE.counter.compareAndSet(maxGlobal, state.score)) {
					System.out.println("state: new max score " + state.toString());
				}
			}
			
		} else {
////			System.out.println("join: not (local) max score " + StringUtils.leftPad(String.valueOf(this.maxScore), 3)
//					+ " for: " + state);
			
		}
		this.tries += 1;
		this.mergeScoreMap(new MatrixState(state));
		CounterSingletons.TRIES.counter.getAndIncrement();

		return this;
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
		this.mergeScoreMap(result2.topScorersMap);
		this.tries += result2.tries;
		this.startTime = Math.min(this.startTime, result2.startTime);
	
		return this;
	}
	
	private void mergeScoreMap(MatrixState state) {
		List<MatrixState> states = this.topScorersMap.get(state.score);
		if (states == null) {
			states = new LinkedList<MatrixState>();
			this.topScorersMap.put(state.score, states);
		}
		//TODO only add if not yet exists?
		states.add(state);
		cropMap(topScorersMap);
	}

	
	private void mergeScoreMap(Map<Integer, List<MatrixState>> scoreMap2) {
		for (Map.Entry<Integer, List<MatrixState>> entry2: scoreMap2.entrySet()) {
			if (!this.topScorersMap.containsKey(entry2.getKey())) {
				this.topScorersMap.put(entry2.getKey(), entry2.getValue());
			} else {
				this.topScorersMap.get(entry2.getKey()).addAll(entry2.getValue());
			}
			cropMap(topScorersMap);
		}
	}
	
	
	private void cropMap(SortedMap<Integer, List<MatrixState>> map) {
		//first attempt, just keep 10 top scores with all states for those.
		if (map.size() > 5) {
			map.remove(map.firstKey());
		}
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
		result.append(" scoreMap size " + this.topScorersMap.size());
		result.append(" maxScorers: " + this.maxScorerStates.size() + "(" + this.maxScorersUniqueResults.size() + " unique) ");			
		if (this.maxScorerStates.size() <= 100) {
			result.append(" data: " + this.maxScorerStates);
		}
		
		return result.toString();
				
	}

	public static CryptoBoxResult joinResults(CryptoBoxResult[] cryptoBoxResults) {
		List<CryptoBoxResult> partialResults = Arrays.asList(cryptoBoxResults);
		return joinResults(partialResults);
	}

	public Set<List<OperationInstance>> getMaxScorerOpsLogs() {
		Set<List<OperationInstance>> result = new HashSet<List<OperationInstance>>();
		for (MatrixState state: maxScorerStates) {
			result.add(state.opsLog);
		}
		return result;
	}

	public Set<List<OperationInstance>> getMaxScorerUniqueResultOpsLogs() {
		Set<List<OperationInstance>> result = new HashSet<List<OperationInstance>>();
		for (String matrixResult: maxScorersUniqueResults.keySet()) {
			result.add(maxScorersUniqueResults.get(matrixResult).opsLog);
		}
		
		return result;
	}
	
	public Set<MatrixState> getTopScorers() {
		Set<MatrixState> result = new HashSet<MatrixState>();
		for (Map.Entry<Integer, List<MatrixState>> entry: this.topScorersMap.entrySet()) {
			for(MatrixState state: entry.getValue()) {
				result.add(state);
			}
		}
		return result;
	}

	public Set<List<OperationInstance>> getTopScorersOpsLogs() {
		Set<List<OperationInstance>> result = new HashSet<List<OperationInstance>>();
		for (MatrixState state: getTopScorers()) {
			result.add(state.opsLog);
		}
		
		return result;
	}

	

	public Set<List<OperationInstance>> getTopScorersOpsLogsNoPermutations() {
		Set<List<OperationInstance>> result = new HashSet<List<OperationInstance>>();
		for (MatrixState state: getTopScorers()) {
			if (!Util.containsPermutationOf(state.opsLog, result)) {
				result.add(state.opsLog);
			}
		}
		
		return result;
	}

	public Set<OperationInstance> getTopScorerOIs() {
		Set<OperationInstance> result = new HashSet<OperationInstance>();
		for(MatrixState topScorer: getTopScorers()) {
			result.addAll(new HashSet<>(topScorer.opsLog));
		}
		return result;
	}
	
	public Map<OperationInstance, Integer> getTopScorerOICounts() {
		Map<OperationInstance, Integer> result = new HashMap<OperationInstance, Integer>();
		for(MatrixState topScorer: getTopScorers()) {
			for(OperationInstance oi: topScorer.opsLog) {
				Integer count = result.get(oi);
				if (count == null) {
					result.put(oi, new Integer(1));
				} else {
					result.put(oi, count + 1);
				}
			}
		}
		return Util.sortByDescendingValue(result);	
	}
	
	
	
}


