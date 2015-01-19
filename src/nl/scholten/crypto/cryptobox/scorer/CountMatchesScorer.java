package nl.scholten.crypto.cryptobox.scorer;

import java.util.LinkedList;
import java.util.List;

import nl.scholten.crypto.cryptobox.data.CryptoBoxMatrix;

import org.apache.commons.lang3.StringUtils;

public class CountMatchesScorer implements CryptoBoxScorer {

	private boolean rewardFirstOccurence;

	private List<String> hits = new LinkedList<>();

	public CountMatchesScorer(List<String> hits) {
		for (String hit: hits) {
			this.hits.add(hit.toUpperCase());
		}
	}

	
	@Override
	public int score(CryptoBoxMatrix matrix) {
		int result = 0;
		for (String hit : hits) {
			int count = StringUtils.countMatches(matrix.data, hit);

			// extra score for first occurrence to stimulate more different
			// words
			if (rewardFirstOccurence && count > 0)
				result += 10;

			result += (count * hit.length()); // 10 pts per word
		}
		return result;
	}
	
	public String getResult() {
		return "";
	}


}
