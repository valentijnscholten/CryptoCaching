package nl.scholten.crypto.cryptobox.scorer;

import java.util.LinkedList;
import java.util.List;

import nl.scholten.crypto.cryptobox.data.CryptoBoxMatrix;

public class CombinedScorer implements CryptoBoxScorer {

	List<CryptoBoxScorer> scorers;
	
	public CombinedScorer() {
		this.scorers = new LinkedList<CryptoBoxScorer>();
	}
	
	public CombinedScorer(List<CryptoBoxScorer> scorers) {
		this.scorers = scorers;
	}
	
	public void addScorer(CryptoBoxScorer scorer) {
		this.scorers.add(scorer);
	}
	
	@Override
	public int score(CryptoBoxMatrix matrix) {
		int result = 0;
		for(CryptoBoxScorer scorer: scorers) {
			int score = scorer.score(matrix);
			
			//-1 indicates matrix is rejected
			if (score == -1) return -1;
			
			result += score;
		}
//		System.out.println("scored: " + matrix.data + " " + StringUtils.leftPad(String.valueOf(result), 4));
		return result;
	}

}
