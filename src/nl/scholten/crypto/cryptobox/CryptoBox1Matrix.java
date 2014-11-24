package nl.scholten.crypto.cryptobox;

import java.util.ArrayList;
import java.util.List;

public class CryptoBox1Matrix extends CryptoBoxMatrix {
	
	private static String INPUT = "OORXVEJFENENGDADUNVIERNRLPNNTZESAUHTOULMINUCENAOSTVIETGREDENTWERVINRPUNTZEVEMDRIUENEEENXINTENGXXIXXX";
	public static String[] HITS = new String[]{"GOED", "GEDAAN", "NOORD", "OOST", "PUNT", "GRADEN", "MINUTEN", "NUL", "EEN", "TWEE", "DRIE", "VIER", "VIJF", "ZES", "ZEVEN", "ACHT", "NEGEN", "TIEN"};
	private static int SIZE = 10;
	private static int STEPS = 4;
	
	public CryptoBox1Matrix() {
		super(INPUT, SIZE, STEPS, HITS);
	}
	
	public CryptoBox1Matrix(char[][] input) {
		super(input, SIZE, STEPS, HITS);
	}
	
	public CryptoBox1Matrix(String inputString) {
		super(inputString, SIZE, STEPS, HITS);
	}

	protected boolean isSolved() {
		String row0 = String.valueOf(data[0]);
		boolean solved = true;

		String row9 = String.valueOf(data[9]);
		solved = row9.endsWith("XXXXXXX");
		if (!solved) return false;
		
		solved = row0.startsWith("NOORDVIJFE");
		if (!solved) return false;
		
		String row1 = String.valueOf(data[1]);
		solved = row1.startsWith("ENGRADENVI");
		
		return solved;
	}

	public static void main(String[] args) {
		Matrix org = new CryptoBox1Matrix(INPUT);
		Matrix m = new CryptoBox1Matrix(INPUT);
		
		List<String> opsLog = new ArrayList<>();
		solve(org, m);
	}
	
}
