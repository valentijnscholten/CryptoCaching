package nl.scholten.crypto.cryptobox;



@SuppressWarnings("serial")
public class CryptoBox1Matrix extends CryptoBoxMatrix {

	private static String INPUT = "OORXVEJFENENGDADUNVIERNRLPNNTZESAUHTOULMINUCENAOSTVIETGREDENTWERVINRPUNTZEVEMDRIUENEEENXINTENGXXIXXX";
	private static int SIZE = 10;
	private static int STEPS = 4;
	
	public CryptoBox1Matrix() {
		super(INPUT, SIZE, STEPS, HITS);
	}
	
	public CryptoBox1Matrix(String input) {
		super(input, SIZE, STEPS, HITS);
	}
	
	protected boolean isSolved() {
		boolean solved = true;

		solved = getRow(9).endsWith("XXXXXXX");
		if (!solved) return false;
		
		solved = getRow(0).startsWith("NOORDVIJFE");
		if (!solved) return false;
		
		solved = getRow(1).startsWith("ENGRADENVI");
		
		return solved;
	}

	@Override
	protected Matrix copy() {
		Matrix result = new CryptoBox1Matrix();
		result.init(this);
		return result;
	}

	public static void main(String[] args) {
		Matrix m = new CryptoBox1Matrix(INPUT);

		Matrix m2 = new CryptoBox1Matrix(INPUT);
		
//		m.shiftRowLeft(0);
//		System.out.println(Matrix.toStringSideBySide(org, m));
//		System.out.println();
//		m.shiftRowRight(0);
//		System.out.println(Matrix.toStringSideBySide(org, m));
//		System.out.println();
//		m.shiftColumnDown(0);
//		System.out.println(Matrix.toStringSideBySide(org, m));
//		System.out.println();
//		m.shiftColumnUp(0);
//		System.out.println(Matrix.toStringSideBySide(org, m));
//		System.out.println();
	
//		m.shiftRowLeft(9);
//		m.shiftColumnUp(0);
//		m.shiftColumnUp(0);
//		m.shiftColumnUp(0);
//		System.out.println(Matrix.toStringSideBySide(org, m));
//		
//		System.out.println(m.data);
//		System.out.println("EORXVEJFENINGDADUNVIVRNRLPNNTZTSAUHTOULMNNUCENAOSTUIETGREDENEWERVINRPUOTZEVEMDRIEENEEENXINENGXXIXXXT");

		m.solveSerially();
//		m2.solveFJ();

		
	}

	
}
