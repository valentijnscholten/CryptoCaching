package nl.scholten.crypto.cryptobox.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("serial")
public class Crypto3 extends CryptoBoxMatrix {

	private static String INPUT = "GZOELDEIBXXERFOMDEGETEDINRXNNOGEVAREDENAENARIENEDEINDEVULRYZSIZRNNUDTVEEEHTCELMREACUCENSXWTESEKICXTE";
	
//	public static String[] HITS = new String[] { "ZOEK", "GOED", "TEVREDEN",
//			"GEFELICITEERD", "SUCCESS", "MET", "ZOEKEN", "NAAR", "VIND",
//			"PUNT", "GRADEN", "NUL", "EEN", "TWEE", "DRIE", "VIER", "VIJF",
//			"ZES", "ZEVEN", "ACHT", "NEGEN", "TIEN" };

	public static String[] HITS = new String[] { "GEFELICITEERD", "VIND", "GA", "NAAR", "NOORD", "OOST", 
		"GRADEN", "MINUTEN", 
		"PUNT", "NUL", "EEN", "TWEE", "DRIE", "VIER", "VIJF",
		"ZES", "ZEVEN", "ACHT", "NEGEN" };
	
	public static String[] BEGINNINGS = new String[] { "GEFELICITEERD", "GEFELICITEERD" };
	public static String[] ENDINGS = new String[] { "ACHTNULTWEESUCCESMETZOEKEN", "SUCCESMETZOEKEN" }; //exclude paddings

	public static final List<List<OperationInstance>> HEAD_STARTS = new ArrayList<List<OperationInstance>>();
	static {
		
		///after 1 night running, two keus with score == 100: [CU_9, RL_6, CU_7, RR_2, CU_4, RL_6, CD_2, RR_5, CU_5, RR_0, RR_3, RR_7][CU_9, RL_0, CD_8, RR_9, CU_1, CU_2, RL_1, RL_7, CD_5, CD_6, RR_1, RL_3]
		
		List<OperationInstance> headStart = new ArrayList<CryptoBoxMatrix.OperationInstance>();
		headStart.add(new CryptoBoxMatrix.OperationInstance(OPERATION.CU, 9));
		headStart.add(new CryptoBoxMatrix.OperationInstance(OPERATION.RL, 6));
		headStart.add(new CryptoBoxMatrix.OperationInstance(OPERATION.CU, 7));
		headStart.add(new CryptoBoxMatrix.OperationInstance(OPERATION.RR, 2));
//		headStart.add(new Matrix.OperationInstance(OPERATION.CU, 4));
//		headStart.add(new Matrix.OperationInstance(OPERATION.RL, 6));
//		headStart.add(new Matrix.OperationInstance(OPERATION.CD, 2));
//		headStart.add(new Matrix.OperationInstance(OPERATION.RR, 5));
//		headStart.add(new Matrix.OperationInstance(OPERATION.CU, 5));
//		headStart.add(new Matrix.OperationInstance(OPERATION.RR, 0));
//		headStart.add(new Matrix.OperationInstance(OPERATION.RR, 3));
//		headStart.add(new Matrix.OperationInstance(OPERATION.RR, 7));
		HEAD_STARTS.add(headStart);
		
		List<OperationInstance> headStart2 = new ArrayList<CryptoBoxMatrix.OperationInstance>();
		headStart2.add(new CryptoBoxMatrix.OperationInstance(OPERATION.CU, 9));
		headStart2.add(new CryptoBoxMatrix.OperationInstance(OPERATION.RL, 0));
		headStart2.add(new CryptoBoxMatrix.OperationInstance(OPERATION.CD, 8));
		headStart2.add(new CryptoBoxMatrix.OperationInstance(OPERATION.RR, 9));
//		headStart2.add(new Matrix.OperationInstance(OPERATION.CU, 1));
//		headStart2.add(new Matrix.OperationInstance(OPERATION.CU, 2));
//		headStart2.add(new Matrix.OperationInstance(OPERATION.RL, 1));
//		headStart2.add(new Matrix.OperationInstance(OPERATION.RL, 7));
//		headStart2.add(new Matrix.OperationInstance(OPERATION.CD, 5));
//		headStart2.add(new Matrix.OperationInstance(OPERATION.CD, 6));
//		headStart2.add(new Matrix.OperationInstance(OPERATION.RR, 1));
//		headStart2.add(new Matrix.OperationInstance(OPERATION.RL, 3));
		HEAD_STARTS.add(headStart2);

	}
	
	private static int SIZE = 10;
	private static int STEPS = 12;

	public Crypto3() {
		super(INPUT, SIZE, STEPS, Arrays.asList(HITS), Arrays.asList(BEGINNINGS), Arrays.asList(ENDINGS), HEAD_STARTS);
	}

	public Crypto3(String input) {
		super(input, SIZE, STEPS, Arrays.asList(HITS), Arrays.asList(BEGINNINGS), Arrays.asList(ENDINGS), HEAD_STARTS);
	}

	public static void main(String[] args) throws IOException {
		CryptoBoxMatrix org = new Crypto3(INPUT);
		CryptoBoxMatrix m = new Crypto3(INPUT);

		// m.solveSerially();
		m.solveFJ();
	}

}
