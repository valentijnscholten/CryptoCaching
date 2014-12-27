package nl.scholten.crypto.cryptobox.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nl.scholten.crypto.cryptobox.scorer.BeginningLengthScorer;
import nl.scholten.crypto.cryptobox.scorer.CombinedScorer;
import nl.scholten.crypto.cryptobox.scorer.EndingLengthScorer;
import nl.scholten.crypto.cryptobox.scorer.IndexOfLengthSquareScorer;
import nl.scholten.crypto.cryptobox.solver.CryptoBoxSerialSolver;
import nl.scholten.crypto.cryptobox.solver.CryptoBoxSolver;

@SuppressWarnings("serial")
public class Crypto3UK extends CryptoBoxMatrix {

	private static String INPUT = "CCHGRTOUFXNOINNSOXOADTOEBNNGEIHTHDXEISXTRORONDYIOTEEENRTHFUSVZEROZEROREEEHTPYREEWIGAPGXSXTROHINATXLA"; //7 X-es
	
//	public static String[] HITS = new String[] { "ZOEK", "GOED", "TEVREDEN",
//			"GEFELICITEERD", "SUCCESS", "MET", "ZOEKEN", "NAAR", "VIND",
//			"PUNT", "GRADEN", "NUL", "EEN", "TWEE", "DRIE", "VIER", "VIJF",
//			"ZES", "ZEVEN", "ACHT", "NEGEN", "TIEN" };

	public static String[] HITS = new String[] {  "CONGRATULATIONS", "TOFINDTHEBOX", "TO", "FIND", "THE", "BOX", "HAPPY", "SEARCHING", "SEARCH", "RDX", "RDY", //"GA", "NAAR", "NEGENDRIEEEN",  
		"DOT", "ZERO", "NUL", "ONE", "TWO", "THREE", "FOUR", "FIVE",
		"SIX", "SEVEN", "EIGHT", "NINE" };
	
	public static String[] BEGINNINGS = new String[] { "CONGRATULATIONSTOFINDTHEBOXGOTO", "CONGRATULATIONS" };
	public static String[] ENDINGS = new String[] { "EIGHTZEROTWOHAPPYSEARCHINGXXX", "HAPPYSEARCHINGXXX", "XXX", "XXX", "XX", "X" }; //exclude paddings

	private static int SIZE = 10;
	private static int STEPS = 4;
	
	public static final Set<List<OperationInstance>> HEAD_STARTS = new HashSet<List<OperationInstance>>();
	static {
		
		///after 1 night running, two keus with score == 100: [CU_9, RL_6, CU_7, RR_2, CU_4, RL_6, CD_2, RR_5, CU_5, RR_0, RR_3, RR_7][CU_9, RL_0, CD_8, RR_9, CU_1, CU_2, RL_1, RL_7, CD_5, CD_6, RR_1, RL_3]
		
		List<OperationInstance> headStart = new ArrayList<OperationInstance>();
//		headStart.add(new OperationInstance(OPERATION.CU, 9));
//		headStart.add(new OperationInstance(OPERATION.RL, 6));
//		headStart.add(new OperationInstance(OPERATION.CU, 7));
//		headStart.add(new OperationInstance(OPERATION.RR, 2));
//		headStart.add(new Matrix.OperationInstance(OPERATION.CU, 4));
//		headStart.add(new Matrix.OperationInstance(OPERATION.RL, 6));
//		headStart.add(new Matrix.OperationInstance(OPERATION.CD, 2));
//		headStart.add(new Matrix.OperationInstance(OPERATION.RR, 5));
//		headStart.add(new Matrix.OperationInstance(OPERATION.CU, 5));
//		headStart.add(new Matrix.OperationInstance(OPERATION.RR, 0));
//		headStart.add(new Matrix.OperationInstance(OPERATION.RR, 3));
//		headStart.add(new Matrix.OperationInstance(OPERATION.RR, 7));
//		HEAD_STARTS.add(headStart);
		
		List<OperationInstance> headStart2 = new ArrayList<OperationInstance>();
		headStart2.add(new OperationInstance(OPERATION.CU, 1));
		headStart2.add(new OperationInstance(OPERATION.CU, 2));
		headStart2.add(new OperationInstance(OPERATION.CD, 5));
		headStart2.add(new OperationInstance(OPERATION.CD, 6));
		headStart2.add(new OperationInstance(OPERATION.CD, 8));
		headStart2.add(new OperationInstance(OPERATION.CU, 9));
		headStart2.add(new OperationInstance(OPERATION.RL, 9));
		headStart2.add(new OperationInstance(OPERATION.CD, 2));
		headStart2.add(new OperationInstance(OPERATION.RL, 1));
		headStart2.add(new OperationInstance(OPERATION.RR, 9));
		headStart2.add(new OperationInstance(OPERATION.CU, 2));
		headStart2.add(new OperationInstance(OPERATION.CU, 6));
		headStart2.add(new OperationInstance(OPERATION.RL, 3));
		headStart2.add(new OperationInstance(OPERATION.CD, 1));
		headStart2.add(new OperationInstance(OPERATION.CD, 6));
		headStart2.add(new OperationInstance(OPERATION.RL, 6));
		headStart2.add(new OperationInstance(OPERATION.CU, 1));
//		headStart2.add(new OperationInstance(OPERATION.RL, 3));
//		HEAD_STARTS.add(headStart2);

		headStart2 = new ArrayList<OperationInstance>();
		headStart2.add(new OperationInstance(OPERATION.CU, 1));
		headStart2.add(new OperationInstance(OPERATION.CU, 2));
		headStart2.add(new OperationInstance(OPERATION.CD, 8));
		headStart2.add(new OperationInstance(OPERATION.CU, 9));
		headStart2.add(new OperationInstance(OPERATION.RL, 1));
		headStart2.add(new OperationInstance(OPERATION.CD, 5));
		headStart2.add(new OperationInstance(OPERATION.RR, 1));
		headStart2.add(new OperationInstance(OPERATION.RL, 1));
		headStart2.add(new OperationInstance(OPERATION.CU, 2));
		headStart2.add(new OperationInstance(OPERATION.RR, 8));
//		headStart2.add(new OperationInstance(OPERATION.RL, 1));
//		headStart2.add(new OperationInstance(OPERATION.CU, 2));
//		headStart2.add(new OperationInstance(OPERATION.RL, 3));
//		headStart2.add(new OperationInstance(OPERATION.CD, 1));
//		headStart2.add(new OperationInstance(OPERATION.CD, 6));
//		headStart2.add(new OperationInstance(OPERATION.RL, 6));
//		headStart2.add(new OperationInstance(OPERATION.CU, 1));
//		headStart2.add(new OperationInstance(OPERATION.RL, 3));
//		HEAD_STARTS.add(headStart2);
	}
	
	public static final List<OperationInstance> oisCurrent = new ArrayList<OperationInstance>();
	static {
		oisCurrent.add(new OperationInstance(OPERATION.CU, 1));
		oisCurrent.add(new OperationInstance(OPERATION.CD, 8));
		oisCurrent.add(new OperationInstance(OPERATION.CU, 9));
		oisCurrent.add(new OperationInstance(OPERATION.RL, 1));
		oisCurrent.add(new OperationInstance(OPERATION.CU, 2));
		oisCurrent.add(new OperationInstance(OPERATION.CD, 5));
		oisCurrent.add(new OperationInstance(OPERATION.RL, 3));
		oisCurrent.add(new OperationInstance(OPERATION.CD, 6));
		
		oisCurrent.add(new OperationInstance(OPERATION.RR, 8));
		oisCurrent.add(new OperationInstance(OPERATION.CD, 1));
		oisCurrent.add(new OperationInstance(OPERATION.RL, 6));
		oisCurrent.add(new OperationInstance(OPERATION.RL, 5));
		oisCurrent.add(new OperationInstance(OPERATION.RL, 7));
		
		oisCurrent.clear();
	}
	
	public Crypto3UK() {
		super(INPUT, SIZE);
	}

	public Crypto3UK(String input) {
		super(input, SIZE);
	}

	public static void main(String[] args) throws IOException {
		CryptoBoxMatrix m = new Crypto3UK(INPUT);


		CombinedScorer scorer = new CombinedScorer();
//		scorer.addScorer(new IndexOfScorer(Arrays.asList(HITS)));
//		scorer.addScorer(new IndexOfLengthScorer(Arrays.asList(HITS)));
		scorer.addScorer(new IndexOfLengthSquareScorer(Arrays.asList(HITS)));
//		scorer.addScorer(new BeginningScorer(Arrays.asList(BEGINNINGS)));
		scorer.addScorer(new BeginningLengthScorer(Arrays.asList(BEGINNINGS)));
//		scorer.addScorer(new EndingScorer(Arrays.asList(ENDINGS)));
		scorer.addScorer(new EndingLengthScorer(Arrays.asList(ENDINGS)));

		CryptoBoxSolver solver = new CryptoBoxSerialSolver();
//		CryptoBoxSolver solver = new CryptoBoxFJSerialSolver();
		solver.setScorer(scorer).setStartMatrix(m).setSteps(STEPS).setHeadStarts(HEAD_STARTS).setOisCurrent(oisCurrent).solve();
		
	}

}
