package nl.scholten.crypto.cryptobox.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nl.scholten.crypto.cryptobox.scorer.BeginningScorer;
import nl.scholten.crypto.cryptobox.scorer.CombinedScorer;
import nl.scholten.crypto.cryptobox.scorer.EndingScorer;
import nl.scholten.crypto.cryptobox.scorer.IndexOfScorer;
import nl.scholten.crypto.cryptobox.solver.CryptoBoxSerialSolver;
import nl.scholten.crypto.cryptobox.solver.CryptoBoxSolver;

@SuppressWarnings("serial")
public class Crypto2 extends CryptoBoxMatrix {

	private static String INPUT = "GOEDAIEAANFEANARDVIJIENGUREENVIERNDADUNTJVIERVPIEVFENNRLRGRULADETIEIENNPUNNDRGENAEGENNEJFVGLERGVITIN";
	public static String[] HITS = new String[] { "GOED", "GEDAAN", "VALERNIETIN", "GA", "NAAR",
			"NOORD", "OOST", "GRADEN", "MINUTEN", 
			"PUNT", "NUL", "EEN", "TWEE", "DRIE",
			"VIER", "VIJF", "ZES", "ZEVEN", "ACHT", "NEGEN" };

	public static String[] BEGINNINGS = new String[] { "GOEDGEDAANGANAARVIJF", "GOED" };
	public static String[] ENDINGS = new String[] { "PUNTNEGENNEGENVIJFVALERNIETIN", "VALERNIETIN"};

	public static final List<List<OperationInstance>> HEAD_STARTS = new ArrayList<List<OperationInstance>>();
	static {
		//solution [RR_5, CU_6, RL_1, CD_0, RL_4, CD_4, CU_5, RL_6]
		List<OperationInstance> headStart = new ArrayList<OperationInstance>();
//		headStart.add(new OperationInstance(OPERATION.RR, 5));
//		headStart.add(new OperationInstance(OPERATION.CU, 6));
//		headStart.add(new OperationInstance(OPERATION.RL, 1));
//		headStart.add(new OperationInstance(OPERATION.CD, 0));
//		headStart.add(new OperationInstance(OPERATION.RL, 4));
//		headStart.add(new OperationInstance(OPERATION.CD, 4));
//		HEAD_STARTS.add(headStart);
		
		List<OperationInstance> headStart2 = new ArrayList<OperationInstance>();
		headStart2.add(new OperationInstance(OPERATION.CD, 0));
		headStart2.add(new OperationInstance(OPERATION.CU, 6));
		headStart2.add(new OperationInstance(OPERATION.RL, 1));
		headStart2.add(new OperationInstance(OPERATION.CD, 4));
		headStart2.add(new OperationInstance(OPERATION.RR, 1));
		headStart2.add(new OperationInstance(OPERATION.CU, 0));
		headStart2.add(new OperationInstance(OPERATION.RL, 1));
		headStart2.add(new OperationInstance(OPERATION.RR, 5));
		
		HEAD_STARTS.add(headStart2);

		headStart2 = new ArrayList<OperationInstance>();
		headStart2.add(new OperationInstance(OPERATION.CD, 0));
		headStart2.add(new OperationInstance(OPERATION.CU, 6));
		headStart2.add(new OperationInstance(OPERATION.RL, 1));
		headStart2.add(new OperationInstance(OPERATION.CD, 4));
		headStart2.add(new OperationInstance(OPERATION.RR, 1));
		headStart2.add(new OperationInstance(OPERATION.CU, 0));
		headStart2.add(new OperationInstance(OPERATION.RL, 1));
		headStart2.add(new OperationInstance(OPERATION.CU, 5));
		
		HEAD_STARTS.add(headStart2);

		
	}

	
	private static int SIZE = 10;
	private static int STEPS = 13;
//	private static int STEPS = 8;

	public Crypto2() {
		super(INPUT, SIZE);
	}

	public Crypto2(String input) {
		super(input, SIZE);
	}

	public static void main(String[] args) throws IOException {
		CryptoBoxMatrix m = new Crypto2(INPUT);


		CombinedScorer scorer = new CombinedScorer();
		scorer.addScorer(new IndexOfScorer(Arrays.asList(HITS)));
		scorer.addScorer(new EndingScorer(Arrays.asList(ENDINGS)));
		scorer.addScorer(new BeginningScorer(Arrays.asList(BEGINNINGS)));

		CryptoBoxSolver solver = new CryptoBoxSerialSolver();
//		CryptoBoxSolver solver = new CryptoBoxFJSerialSolver();
		solver.setScorer(scorer).setStartMatrix(m).setSteps(STEPS).setHeadStarts(HEAD_STARTS).solve();
		



	}

}
