package nl.scholten.crypto.cryptobox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class CryptoBox3Matrix extends CryptoBoxMatrix {

	private static String INPUT = "GZOELDEIBXXERFOMDEGETEDINRXNNOGEVAREDENAENARIENEDEINDEVULRYZSIZRNNUDTVEEEHTCELMREACUCENSXWTESEKICXTE";
	
//	public static String[] HITS = new String[] { "ZOEK", "GOED", "TEVREDEN",
//			"GEFELICITEERD", "SUCCESS", "MET", "ZOEKEN", "NAAR", "VIND",
//			"PUNT", "GRADEN", "NUL", "EEN", "TWEE", "DRIE", "VIER", "VIJF",
//			"ZES", "ZEVEN", "ACHT", "NEGEN", "TIEN" };

	public static String[] HITS = new String[] { "GEFELICITEERD", "VIND", "GA", "NAAR", "NOORD", "OOST", 
		"GRADEN", "MINUTEN", 
		"PUNT", "NUL", "EEN", "TWEE", "DRIE", "VIER", "VIJF",
		"ZES", "ZEVEN", "ACHT", "NEGEN" };
	
	public static String[] BEGINNINGS = new String[] { "GEFELICITEERD" };
	public static String[] ENDINGS = new String[] { "SUCCESMETZOEKEN" }; //exclude paddings

	public static final List<List<OperationInstance>> HEAD_STARTS = new ArrayList<List<OperationInstance>>();
	static {
		
		///after 1 night running, two keus with score == 100: [CU_9, RL_6, CU_7, RR_2, CU_4, RL_6, CD_2, RR_5, CU_5, RR_0, RR_3, RR_7][CU_9, RL_0, CD_8, RR_9, CU_1, CU_2, RL_1, RL_7, CD_5, CD_6, RR_1, RL_3]
		
//		List<OperationInstance> headStart = new ArrayList<Matrix.OperationInstance>();
//		headStart.add(new Matrix.OperationInstance(OPERATION.ROW_LEFT, 8));
//		headStart.add(new Matrix.OperationInstance(OPERATION.COL_UP, 3));
//		headStart.add(new Matrix.OperationInstance(OPERATION.ROW_LEFT, 8));
//		headStart.add(new Matrix.OperationInstance(OPERATION.COL_UP, 3));
//		headStart.add(new Matrix.OperationInstance(OPERATION.ROW_LEFT, 8));
//		headStart.add(new Matrix.OperationInstance(OPERATION.COL_UP, 3));
//		headStart.add(new Matrix.OperationInstance(OPERATION.ROW_LEFT, 8));
//		headStart.add(new Matrix.OperationInstance(OPERATION.COL_UP, 3));
//		headStart.add(new Matrix.OperationInstance(OPERATION.ROW_LEFT, 8));
//		headStart.add(new Matrix.OperationInstance(OPERATION.COL_UP, 3));
//		headStart.add(new Matrix.OperationInstance(OPERATION.ROW_LEFT, 8));
//		headStart.add(new Matrix.OperationInstance(OPERATION.COL_UP, 3));
		
		HEAD_STARTS.add(headStart);
	}
	
	private static int SIZE = 10;
	private static int STEPS = 12;

	public CryptoBox3Matrix() {
		super(INPUT, SIZE, STEPS, HITS, BEGINNINGS, ENDINGS, HEAD_STARTS);
	}

	public CryptoBox3Matrix(String input) {
		super(input, SIZE, STEPS, HITS, BEGINNINGS, ENDINGS, HEAD_STARTS);
	}

	public CryptoBox3Matrix(CryptoBox3Matrix m) {
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
	protected int score() {
		return super.score();
	}

	@Override
	protected Matrix copy() {
		Matrix result = new CryptoBox3Matrix(this);
		return result;
	}

	public static void main(String[] args) throws IOException {
		Matrix org = new CryptoBox3Matrix(INPUT);
		Matrix m = new CryptoBox3Matrix(INPUT);

		// m.solveSerially();
		m.solveFJ();
	}

}
