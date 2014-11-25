package nl.scholten.crypto.cryptobox;

import java.io.IOException;

@SuppressWarnings("serial")
public class CryptoBox4Matrix extends CryptoBoxMatrix{
	
	private static String INPUT = "UAEIOMETEREIRRHOHNTGTHEVPROJSLEETIVPNAECHIERDCAOOFNTPTDRIERENARONTZEDEENUADENEVENGCJFZENENBVAATMETVE";
	public static String[] HITS = new String[]{"PROBEER", "SUCCESS", "MET", "ZOEKEN", "NAAR", "NOORD", "OOST", "PUNT", "GRADEN", "MINUTEN", "NUL", "EEN", "TWEE", "DRIE", "VIER", "VIJF", "ZES", "ZEVEN", "ACHT", "NEGEN", "TIEN"};
	private static int SIZE = 10;
	private static int STEPS = 12;
	
	public CryptoBox4Matrix() {
		super(INPUT, SIZE, STEPS, HITS);
	}
	
	public CryptoBox4Matrix(String input) {
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
//		if (data.endsWith("SUCCESSMETZOEKENXXX")) { 
			return super.score(); 
//		} else return 0;
	}
	
	@Override
	protected Matrix copy() {
		Matrix result = new CryptoBox4Matrix();
		result.init(this);
		return result;
	}

	
	public static void main(String[] args) throws IOException {
		Matrix org = new CryptoBox4Matrix(INPUT);
		Matrix m = new CryptoBox4Matrix(INPUT);
		
//		m.solveSerially();
		m.solveFJ();
	}
	
}
