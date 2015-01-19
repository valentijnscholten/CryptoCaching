package nl.scholten.crypto.cryptobox.scorer;

import java.util.LinkedList;
import java.util.List;

import nl.scholten.crypto.cryptobox.data.CryptoBoxMatrix;

import org.apache.commons.lang3.StringUtils;

public class CountMatchesSquareScorer implements CryptoBoxScorer {

	private List<String> hits = new LinkedList<>();

	public CountMatchesSquareScorer(List<String> hits) {
		for (String hit: hits) {
			this.hits.add(hit.toUpperCase());
		}
	}

	@Override
	public int score(CryptoBoxMatrix matrix) {
		int result = 0;
		for (String hit : hits) {
			int count = StringUtils.countMatches(matrix.data, hit);

			result += (count * hit.length() * hit.length()); 
		}
		return result;
	}

	public String getResult() {
		return "";
	}
	
}
