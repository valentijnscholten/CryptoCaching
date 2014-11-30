package nl.scholten.crypto.cryptobox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class CryptoBox4Matrix extends CryptoBoxMatrix {

	private static String INPUT = "UAEIOMETEREIRRHOHNTGTHEVPROJSLEETIVPNAECHIERDCAOOFNTPTDRIERENARONTZEDEENUADENEVENGCJFZENENBVAATMETVE";
	public static String[] HITS = new String[] { "NOORD", "OOST", 
		"GRADEN", "MINUTEN", 
		"PUNT", "NUL", "EEN", "TWEE", "DRIE", "VIER", "VIJF",
		"ZES", "ZEVEN", "ACHT", "NEGEN" };

	public static String[] BEGINNINGS = new String[] {};
	public static String[] ENDINGS = new String[] {};
	
	public static final List<List<OperationInstance>> HEAD_STARTS = new ArrayList<List<OperationInstance>>();
	static {
//		List<OperationInstance> headStart = new ArrayList<Matrix.OperationInstance>();
//		headStart.add(new Matrix.OperationInstance(OPERATION.ROW_LEFT, 8));
//		headStart.add(new Matrix.OperationInstance(OPERATION.COL_UP, 3));
//		
//		HEAD_STARTS.add(headStart);
	}

	private static int SIZE = 10;
	private static int STEPS = 14; // hint from Cache owner
//	private static int STEPS = 16;

	public CryptoBox4Matrix() {
		super(INPUT, SIZE, STEPS, HITS, BEGINNINGS, ENDINGS, HEAD_STARTS);
	}

	public CryptoBox4Matrix(String input) {
		super(input, SIZE, STEPS, HITS, BEGINNINGS, ENDINGS, HEAD_STARTS);
	}

	public CryptoBox4Matrix(CryptoBox4Matrix m) {
		super(m);
	}

	protected boolean isSolved() {
		boolean solved = true;

		solved = getRow(0).startsWith("NOORD");
		if (!solved)
			return false;

		return solved;
	}

	@Override
	protected Matrix copy() {
		Matrix result = new CryptoBox4Matrix(this);
		return result;
	}

	public static void main(String[] args) throws IOException {
		Matrix org = new CryptoBox4Matrix(INPUT);
		Matrix m = new CryptoBox4Matrix(INPUT);

		// m.solveSerially();
		m.solveFJ();
	}

}
