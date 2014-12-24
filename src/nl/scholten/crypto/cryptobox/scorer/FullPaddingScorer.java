package nl.scholten.crypto.cryptobox.scorer;

import nl.scholten.crypto.cryptobox.data.CryptoBoxMatrix;

import org.apache.commons.lang3.StringUtils;

public class FullPaddingScorer implements CryptoBoxScorer {

	private int x_paddings;
	private boolean forceXPadMatch;

	@Override
	public int score(CryptoBoxMatrix matrix) {
		int result = 0;	
		boolean padMatch = false;

		String end = matrix.data.substring(matrix.data.length() - x_paddings,
				matrix.data.length());
		int count = StringUtils.countMatches(end, "X");

		// System.out.println(count + " x_pads for: " + data);
		result += count;
		padMatch = count == x_paddings;

		if (forceXPadMatch && !padMatch)
			return -1;

		if (padMatch)
			result += 10;
		
		return result;
	}
	
}
