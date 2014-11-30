package nl.scholten.crypto.cryptobox.scorer;

import java.util.List;

import nl.scholten.crypto.cryptobox.data.CryptoBoxMatrix;

public class CombinedScorer implements CryptoBoxScorer {

	List<CryptoBoxScorer> scorers;
	
	public CombinedScorer(List<CryptoBoxScorer> scorers) {
		this.scorers = scorers;
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
		return result;
	}

}
