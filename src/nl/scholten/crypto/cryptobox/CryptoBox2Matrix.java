package nl.scholten.crypto.cryptobox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class CryptoBox2Matrix extends CryptoBoxMatrix {

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
		List<OperationInstance> headStart = new ArrayList<Matrix.OperationInstance>();
		headStart.add(new Matrix.OperationInstance(OPERATION.ROW_RIGHT, 5));
		headStart.add(new Matrix.OperationInstance(OPERATION.COL_UP, 6));
		headStart.add(new Matrix.OperationInstance(OPERATION.ROW_LEFT, 1));
		headStart.add(new Matrix.OperationInstance(OPERATION.COL_DOWN, 0));
		headStart.add(new Matrix.OperationInstance(OPERATION.ROW_LEFT, 4));
		headStart.add(new Matrix.OperationInstance(OPERATION.COL_DOWN, 4));
		
		HEAD_STARTS.add(headStart);
	}

	
	private static int SIZE = 10;
	private static int STEPS = 8;

	public CryptoBox2Matrix() {
		super(INPUT, SIZE, STEPS, HITS, BEGINNINGS, ENDINGS, HEAD_STARTS);
	}

	public CryptoBox2Matrix(String input) {
		super(input, SIZE, STEPS, HITS, BEGINNINGS, ENDINGS, HEAD_STARTS);
	}

	public CryptoBox2Matrix(CryptoBox2Matrix m) {
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
		Matrix result = new CryptoBox2Matrix(this);
		return result;
	}

	public static void main(String[] args) throws IOException {
		Matrix m = new CryptoBox2Matrix(INPUT);

		 m.solveSerially();
//		m.solveFJ();

	}

}
