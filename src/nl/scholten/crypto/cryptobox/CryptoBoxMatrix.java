package nl.scholten.crypto.cryptobox;

import org.apache.commons.lang3.StringUtils;


public abstract class CryptoBoxMatrix extends Matrix {

	public static String[] HITS = new String[]{"NOORD", "OOST", "PUNT", "GRADEN", "MINUTEN", "NUL", "EEN", "TWEE", "DRIE", "VIER", "VIJF", "ZES", "ZEVEN", "ACHT", "NEGEN", "TIEN"};
	private String[] hits;
	
	public CryptoBoxMatrix(String input, int size, int maxSteps, String[] hits) {
		super(input, size, maxSteps);
		this.hits = hits;
	}

	public CryptoBoxMatrix(CryptoBoxMatrix m1) {
		super(m1);
		this.hits = m1.hits;
	}

	@Override
	protected int score() {
		int result = 0;
		//first see if the first characters are a hit, if not, score is 0.
		boolean prematch = false;
		for (String hit: hits) {
			prematch = data.startsWith(hit);
			if (prematch) break;
		}
		if (!prematch) return 0;
		
		for (String hit: hits) {
			int count = StringUtils.countMatches(data, hit);
//			int count = StringUtils.indexOf(data, hit) / 1; // doesn't help much
			//extra score for first occurrence to stimulate more different words
			if (count > 0) result += 10; 
			result += count;
		}
		return result;
	}

}