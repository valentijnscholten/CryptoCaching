package nl.scholten.crypto.cryptobox.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("serial")
public class Crypto4 extends CryptoBoxMatrix {

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

	public Crypto4() {
		super(INPUT, SIZE, STEPS, Arrays.asList(HITS), Arrays.asList(BEGINNINGS), Arrays.asList(ENDINGS), HEAD_STARTS);
	}

	public Crypto4(String input) {
		super(input, SIZE, STEPS, Arrays.asList(HITS), Arrays.asList(BEGINNINGS), Arrays.asList(ENDINGS), HEAD_STARTS);
	}

	public static void main(String[] args) throws IOException {
		CryptoBoxMatrix org = new Crypto4(INPUT);
		CryptoBoxMatrix m = new Crypto4(INPUT);

		// m.solveSerially();
		m.solveFJ();
	}

}
