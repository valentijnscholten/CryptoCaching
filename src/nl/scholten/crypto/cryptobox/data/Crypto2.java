package nl.scholten.crypto.cryptobox.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("serial")
public class Crypto2 extends CryptoBoxMatrix {

	private static String INPUT = "GOEDAIEAANFEANARDVIJIENGUREENVIERNDADUNTJVIERVPIEVFENNRLRGRULADETIEIENNPUNNDRGENAEGENNEJFVGLERGVITIN";
	public static String[] HITS = new String[] { "GOED", "GEDAAN", "VALERNIETIN", "GA", "NAAR",
			"NOORD", "OOST", "GRADEN", "MINUTEN", 
			"PUNT", "NUL", "EEN", "TWEE", "DRIE",
			"VIER", "VIJF", "ZES", "ZEVEN", "ACHT", "NEGEN" };

	public static String[] BEGINNINGS = new String[] { "GOEDGEDAANGANAARVIJF" };
	public static String[] ENDINGS = new String[] { "PUNTNEGENNEGENVIJFVALERNIETIN" };

	public static final List<List<OperationInstance>> HEAD_STARTS = new ArrayList<List<OperationInstance>>();
	static {
		//solution [RR_5, CU_6, RL_1, CD_0, RL_4, CD_4, CU_5, RL_6]
		List<OperationInstance> headStart = new ArrayList<CryptoBoxMatrix.OperationInstance>();
		headStart.add(new CryptoBoxMatrix.OperationInstance(OPERATION.RR, 5));
		headStart.add(new CryptoBoxMatrix.OperationInstance(OPERATION.CU, 6));
		headStart.add(new CryptoBoxMatrix.OperationInstance(OPERATION.RL, 1));
		headStart.add(new CryptoBoxMatrix.OperationInstance(OPERATION.CD, 0));
		headStart.add(new CryptoBoxMatrix.OperationInstance(OPERATION.RL, 4));
		headStart.add(new CryptoBoxMatrix.OperationInstance(OPERATION.CD, 4));
		
//		HEAD_STARTS.add(headStart);
	}

	
	private static int SIZE = 10;
	private static int STEPS = 8;

	public Crypto2() {
		super(INPUT, SIZE, STEPS, Arrays.asList(HITS), Arrays.asList(BEGINNINGS), Arrays.asList(ENDINGS), HEAD_STARTS);
	}

	public Crypto2(String input) {
		super(input, SIZE, STEPS, Arrays.asList(HITS), Arrays.asList(BEGINNINGS), Arrays.asList(ENDINGS), HEAD_STARTS);
	}

	public static void main(String[] args) throws IOException {
		CryptoBoxMatrix m = new Crypto2(INPUT);

//		 m.solveSerially();
		m.solveFJ();

	}

}
