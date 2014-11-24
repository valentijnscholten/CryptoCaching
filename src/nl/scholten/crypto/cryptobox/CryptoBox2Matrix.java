package nl.scholten.crypto.cryptobox;

import java.io.IOException;

public class CryptoBox2Matrix extends CryptoBoxMatrix{
	
	private static String INPUT = "GOEDAIEAANFEANARDVIJIENGUREENVIERNDADUNTJVIERVPIEVFENNRLRGRULADETIEIENNPUNNDRGENAEGENNEJFVGLERGVITIN";
	public static String[] HITS = new String[]{"GOED", "GEDAAN", "NOORD", "OOST", "PUNT", "GRADEN", "MINUTEN", "NUL", "EEN", "TWEE", "DRIE", "VIER", "VIJF", "ZES", "ZEVEN", "ACHT", "NEGEN", "TIEN"};
	private static int SIZE = 10;
	private static int STEPS = 8;
	
	public CryptoBox2Matrix() {
		super(INPUT, SIZE, STEPS, HITS);
	}
	
	public CryptoBox2Matrix(String input) {
		super(input, SIZE, STEPS, HITS);
	}
	
	protected boolean isSolved() {
		boolean solved = true;
		
		solved = getRow(0).startsWith("NOORD");
		if (!solved) return false;
		
		return solved;
	}

	public static void main(String[] args) throws IOException {
		Matrix org = new CryptoBox2Matrix(INPUT);
		Matrix m = new CryptoBox2Matrix(INPUT);
		
		solve(org, m);
	}
	
}
