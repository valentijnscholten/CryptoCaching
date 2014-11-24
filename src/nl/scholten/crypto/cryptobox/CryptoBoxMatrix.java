package nl.scholten.crypto.cryptobox;

import org.apache.commons.lang3.StringUtils;


public abstract class CryptoBoxMatrix extends Matrix {

	public static String[] HITS = new String[]{"NOORD", "OOST", "PUNT", "GRADEN", "MINUTEN", "NUL", "EEN", "TWEE", "DRIE", "VIER", "VIJF", "ZES", "ZEVEN", "ACHT", "NEGEN", "TIEN"};
	private String[] hits;
	
	public CryptoBoxMatrix(String input, int size, int maxSteps, String[] hits) {
		super(input, size, maxSteps);
	}

	public CryptoBoxMatrix(char[][] input, int size, int maxSteps, String[] hits) {
		super(input, size, maxSteps);
		this.hits = hits;
	}

	public CryptoBoxMatrix(CryptoBoxMatrix m1) {
		super(m1);
		this.hits = m1.hits;
	}

	@Override
	protected int score() {
		String plain = toStringOneline();
		int result = 0;
		for (String hit: hits) {
			int count = StringUtils.countMatches(plain, hit);
			//extra score for first occurrence to stimulate more different words
			if (count > 0) result += 10; 
			result += StringUtils.countMatches(plain, hit);
		}
//		System.out.println("Score = " + result + " for " + plain);
		return result;
	}

}