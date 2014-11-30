package nl.scholten.crypto.cryptobox.scorer;

import java.util.List;

import nl.scholten.crypto.cryptobox.data.CryptoBoxMatrix;

import org.apache.commons.lang3.StringUtils;

public class CountMatchesScorer implements CryptoBoxScorer {

	private List<String> hits;
	private boolean rewardFirstOccurence;

	@Override
	public int score(CryptoBoxMatrix matrix) {
		int result = 0;
		for (String hit : hits) {
			int count = StringUtils.countMatches(matrix.data, hit);

			// extra score for first occurrence to stimulate more different
			// words
			if (rewardFirstOccurence && count > 0)
				result += 10;

			result += (count * 10); // 10 pts per word
		}
		return result;
	}

}
