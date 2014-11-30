package nl.scholten.crypto.cryptobox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class CryptoBox1Matrix extends CryptoBoxMatrix {

	private static String INPUT = "OORXVEJFENENGDADUNVIERNRLPNNTZESAUHTOULMINUCENAOSTVIETGREDENTWERVINRPUNTZEVEMDRIUENEEENXINTENGXXIXXX";

	public static String[] HITS = new String[] { "NOORD", "OOST", 
			"GRADEN", "MINUTEN", 
			"PUNT", "NUL", "EEN", "TWEE", "DRIE", "VIER", "VIJF",
			"ZES", "ZEVEN", "ACHT", "NEGEN" };

	public static String[] BEGINNINGS = new String[] { "NOORDVIJFEENGRADENVI" };
	public static String[] ENDINGS = new String[] {};

	public static final List<List<OperationInstance>> HEAD_STARTS = new ArrayList<List<OperationInstance>>();
	static {
		//solution = [RL_8, CU_3, RR_0, CD_6]
		List<OperationInstance> headStart = new ArrayList<Matrix.OperationInstance>();
		headStart.add(new Matrix.OperationInstance(OPERATION.ROW_LEFT, 8));
		headStart.add(new Matrix.OperationInstance(OPERATION.COL_UP, 3));
		
		HEAD_STARTS.add(headStart);
	}
	
	private static int SIZE = 10;
	private static int STEPS = 4;

	public CryptoBox1Matrix() {
		super(INPUT, SIZE, STEPS, HITS, BEGINNINGS, ENDINGS, HEAD_STARTS);
	}

	public CryptoBox1Matrix(String input) {
		super(input, SIZE, STEPS, HITS, BEGINNINGS, ENDINGS, HEAD_STARTS);
	}

	public CryptoBox1Matrix(CryptoBox1Matrix matrix) {
		super(matrix);
	}

	protected boolean isSolved() {
		boolean solved = false;
		return solved;
	}

	@Override
	protected Matrix copy() {
		Matrix result = new CryptoBox1Matrix(this);
		return result;
	}

	public static void main(String[] args) throws IOException {
//		System.in.read();
		Matrix m = new CryptoBox1Matrix(INPUT);

		Matrix m2 = new CryptoBox1Matrix(INPUT);

		// m.shiftRowLeft(0);
		// System.out.println(Matrix.toStringSideBySide(org, m));
		// System.out.println();
		// m.shiftRowRight(0);
		// System.out.println(Matrix.toStringSideBySide(org, m));
		// System.out.println();
		// m.shiftColumnDown(0);
		// System.out.println(Matrix.toStringSideBySide(org, m));
		// System.out.println();
		// m.shiftColumnUp(0);
		// System.out.println(Matrix.toStringSideBySide(org, m));
		// System.out.println();

		// m.shiftRowLeft(9);
		// m.shiftColumnUp(0);
		// m.shiftColumnUp(0);
		// m.shiftColumnUp(0);
		// System.out.println(Matrix.toStringSideBySide(org, m));
		//
		// System.out.println(m.data);
		// System.out.println("EORXVEJFENINGDADUNVIVRNRLPNNTZTSAUHTOULMNNUCENAOSTUIETGREDENEWERVINRPUOTZEVEMDRIEENEEENXINENGXXIXXXT");

		 m.solveSerially();
//		m2.solveFJ();

//		 System.in.read();
	}

}
