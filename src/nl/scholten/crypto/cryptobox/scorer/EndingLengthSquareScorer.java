package nl.scholten.crypto.cryptobox.scorer;

import java.util.List;

import nl.scholten.crypto.cryptobox.data.CryptoBoxMatrix;

public class EndingLengthSquareScorer implements CryptoBoxScorer {

	private List<String> endings;
	private boolean forceEndMatch;

	public EndingLengthSquareScorer(List<String> endings) {
		this.endings = endings;
	}

	@Override
	public int score(CryptoBoxMatrix matrix) {
		int result = 0;
		boolean endMatch = false;
		for (String ending : endings) {
			if (matrix.data.endsWith(ending)) {
				result += (ending.length() + 10) * (ending.length() + 10) ;
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
