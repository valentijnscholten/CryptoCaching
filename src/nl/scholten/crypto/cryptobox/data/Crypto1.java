package nl.scholten.crypto.cryptobox.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("serial")
public class Crypto1 extends CryptoBoxMatrix {

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
		List<OperationInstance> headStart = new ArrayList<CryptoBoxMatrix.OperationInstance>();
		headStart.add(new CryptoBoxMatrix.OperationInstance(OPERATION.RL, 8));
		headStart.add(new CryptoBoxMatrix.OperationInstance(OPERATION.CU, 3));
		
//		HEAD_STARTS.add(headStart);
	}
	
	private static int SIZE = 10;
	private static int STEPS = 4;

	public Crypto1() {
		super(INPUT, SIZE, STEPS, Arrays.asList(HITS), Arrays.asList(BEGINNINGS), Arrays.asList(ENDINGS), HEAD_STARTS);
	}

	public Crypto1(String input) {
		super(input, SIZE, STEPS, Arrays.asList(HITS), Arrays.asList(BEGINNINGS), Arrays.asList(ENDINGS), HEAD_STARTS);
	}

	public static void main(String[] args) throws IOException {
//		System.in.read();
		CryptoBoxMatrix m = new Crypto1(INPUT);

		CryptoBoxMatrix m2 = new Crypto1(INPUT);

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
