package nl.scholten.crypto.cryptobox;

import java.io.IOException;

public class CryptoBox3Matrix extends CryptoBoxMatrix{
	
	private static String INPUT = "GZOELDEIBXXERFOMDEGETEDINRXNNOGEVAREDENAENARIENEDEINDEVULRYZSIZRNNUDTVEEEHTCELMREACUCENSXWTESEKICXTE";
	public static String[] HITS = new String[]{"SUCCESS", "MET", "ZOEKEN", "NAAR", "NOORD", "OOST", "PUNT", "GRADEN", "MINUTEN", "NUL", "EEN", "TWEE", "DRIE", "VIER", "VIJF", "ZES", "ZEVEN", "ACHT", "NEGEN", "TIEN"};
	private static int SIZE = 10;
	private static int STEPS = 12;
	
	public CryptoBox3Matrix() {
		super(INPUT, SIZE, STEPS, HITS);
	}
	
	public CryptoBox3Matrix(String input) {
		super(input, SIZE, STEPS, HITS);
	}
	
	protected boolean isSolved() {
		boolean solved = true;
		
		solved = getRow(0).startsWith("NOORD");
		if (!solved) return false;
		
		return solved;
	}

	@Override
	protected int score() {
		if (data.endsWith("SUCCESSMETZOEKENXXX")) { 
			return super.score(); 
		} else return 0;
	}
	
	public static void main(String[] args) throws IOException {
		Matrix org = new CryptoBox3Matrix(INPUT);
		Matrix m = new CryptoBox3Matrix(INPUT);
		
		solve(org, m);
	}
	
}
