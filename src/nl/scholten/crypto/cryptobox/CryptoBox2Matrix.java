package nl.scholten.crypto.cryptobox;

import java.io.IOException;

@SuppressWarnings("serial")
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
	
	@Override
	protected int score() {
		if (
				data.endsWith("PUNTNEGENNEGENVIJFVALERNIETIN") 
				&& 
				data.startsWith("GOEDGEDAANGANAARVIJF")
				) { 
			return super.score(); 
		} else return 0;
	}

	@Override
	protected Matrix copy() {
		Matrix result = new CryptoBox2Matrix();
		result.init(this);
		return result;
	}
	
	public static void main(String[] args) throws IOException {
		Matrix m = new CryptoBox2Matrix(INPUT);
		
//		m.solveSerially();
		m.solveFJ();

	}
	
}
