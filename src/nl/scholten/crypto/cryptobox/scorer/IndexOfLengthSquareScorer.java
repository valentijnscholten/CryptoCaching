package nl.scholten.crypto.cryptobox.scorer;

import java.util.List;

import nl.scholten.crypto.cryptobox.data.CryptoBoxMatrix;

public class IndexOfLengthSquareScorer implements CryptoBoxScorer {

	private List<String> hits;

	public IndexOfLengthSquareScorer(List<String> hits) {
		super();
		this.hits = hits;
	}

	@Override
	public int score(CryptoBoxMatrix matrix) {
		int result = 0;
		for (String hit : hits) {
			if (matrix.data.indexOf(hit) > -1) {
				result += hit.length()*hit.length();// length drives score
			}
		}

//		System.out.println("scored: " + matrix.data + " " + StringUtils.leftPad(String.valueOf(result), 4));
		return result;
	}
}
