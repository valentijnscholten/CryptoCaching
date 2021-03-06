package nl.scholten.crypto.cryptobox.scorer;

import java.util.List;

import nl.scholten.crypto.cryptobox.data.CryptoBoxMatrix;

public class EndingExtraScorer implements CryptoBoxScorer {

	private List<String> endings;
	private boolean forceEndMatch;

	public EndingExtraScorer(List<String> endings) {
		this.endings = endings;
	}

	@Override
	public int score(CryptoBoxMatrix matrix) {
		int result = 0;
		boolean endMatch = false;
		for (String ending : endings) {
			if (matrix.data.endsWith(ending)) {
				result += 10 * ending.length() * ending.length();
				endMatch = true;
				break;
			}
		}

		if (forceEndMatch && !endMatch)
			return -1;
		
		return result;
	}

	public String getResult() {
		return "";
	}

	
}
