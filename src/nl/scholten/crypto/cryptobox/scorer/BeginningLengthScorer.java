package nl.scholten.crypto.cryptobox.scorer;

import java.util.List;

import nl.scholten.crypto.cryptobox.data.CryptoBoxMatrix;

public class BeginningLengthScorer implements CryptoBoxScorer {

	private List<String> beginnings;
	private boolean forceBeginMatch;

	public BeginningLengthScorer(List<String> beginnings) {
		this.beginnings = beginnings;
	}

	@Override
	public int score(CryptoBoxMatrix matrix) {
		int result = 0;
		boolean beginMatch = false;
		for (String beginning : beginnings) {
			if (matrix.data.startsWith(beginning)) {
				result += beginning.length();
				beginMatch = true;
				break;
			}
		}

		if (forceBeginMatch && !beginMatch)
			return -1;
		
		return result;
	}

	public String getResult() {
		return "";
	}

}
