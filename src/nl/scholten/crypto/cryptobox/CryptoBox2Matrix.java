package nl.scholten.crypto.cryptobox;

import java.io.IOException;

public class CryptoBox2Matrix extends CryptoBoxMatrix{
	
	private static String INPUT = "GOEDAIEAANFEANARDVIJIENGUREENVIERNDADUNTJVIERVPIEVFENNRLRGRULADETIEIENNPUNNDRGENAEGENNEJFVGLERGVITIN";
	private static int SIZE = 10;
	private static int STEPS = 8;
	
	public CryptoBox2Matrix() {
		super(INPUT, SIZE, STEPS, HITS);
	}
	
	public CryptoBox2Matrix(char[][] input) {
		super(input, SIZE, STEPS, HITS);
	}
	
	public CryptoBox2Matrix(String inputString) {
		super(inputString, SIZE, STEPS, HITS);
	}

	protected boolean isSolved() {
		String row0 = String.valueOf(data[0]);
		boolean solved = true;

//		String row9 = String.valueOf(data[9]);
//		solved = row9.endsWith("XXXXXXX");
//		if (!solved) return false;
		
		solved = row0.startsWith("NOORD");
		if (!solved) return false;
		
//		String row1 = String.valueOf(data[1]);
//		solved = row1.startsWith("ENGRADENVI");
		
		return solved;
	}

//	@Override
//	protected int score() {
		
//		int result = super.score();
//		return result;
//	}
	
	public static void main(String[] args) throws IOException {
		Matrix org = new CryptoBox2Matrix(INPUT);
		Matrix m = new CryptoBox2Matrix(INPUT);
		
		solve(org, m);
	}
	
}
