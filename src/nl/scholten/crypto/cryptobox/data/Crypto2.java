package nl.scholten.crypto.cryptobox.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nl.scholten.crypto.cryptobox.scorer.BeginningLengthScorer;
import nl.scholten.crypto.cryptobox.scorer.CombinedScorer;
import nl.scholten.crypto.cryptobox.scorer.CountMatchesSquareScorer;
import nl.scholten.crypto.cryptobox.scorer.EndingLengthScorer;
import nl.scholten.crypto.cryptobox.solver.CryptoBoxSolver;
import nl.scholten.crypto.cryptobox.solver.CryptoBoxStrategicSolver;

@SuppressWarnings("serial")
public class Crypto2 extends CryptoBoxMatrix {
								 //GOEDGEDAANGANAARVIJFEENGRADENVIERNULPUNTVIERDRIEVIJFENVIERGRADENDRIENULPUNTNEGENNEGENVIJFVALERNIETIN
	private static String INPUT = "GOEDAIEAANFEANARDVIJIENGUREENVIERNDADUNTJVIERVPIEVFENNRLRGRULADETIEIENNPUNNDRGENAEGENNEJFVGLERGVITIN";
	public static String[] HITS = new String[] { "GOED", "GEDAAN", "VALERNIETIN", "GA", "NAAR", "VIERGRADENDRIENUL", "VIJFEENGRADEN",
			"NOORD", "OOST", "GRADEN", "MINUTEN", 
			"PUNT", "NUL", "EEN", "TWEE", "DRIE",
			"VIER", "VIJF", "ZES", "ZEVEN", "ACHT", "NEGEN" };

	public static String[] BEGINNINGS = new String[] { "GOEDGEDAANGANAARVIJF", "GOED" };
	public static String[] ENDINGS = new String[] { "PUNTNEGENNEGENVIJFVALERNIETIN", "VALERNIETIN"};

	public static final Set<List<OperationInstance>> PREFIXES = new HashSet<List<OperationInstance>>();
	static {
		//solution [RR_5, CU_6, RL_1, CD_0, RL_4, CD_4, CU_5, RL_6]
		List<OperationInstance> prefix = new ArrayList<OperationInstance>();
//		headStart.add(new OperationInstance(OPERATION.RR, 5));
//		headStart.add(new OperationInstance(OPERATION.CU, 6));
//		headStart.add(new OperationInstance(OPERATION.RL, 1));
//		headStart.add(new OperationInstance(OPERATION.CD, 0));
//		headStart.add(new OperationInstance(OPERATION.RL, 4));
//		headStart.add(new OperationInstance(OPERATION.CD, 4));
//		HEAD_STARTS.add(prefix);
		
		List<OperationInstance> prefix2 = new ArrayList<OperationInstance>();
		prefix2.add(new OperationInstance(OPERATION.CD, 0));
		prefix2.add(new OperationInstance(OPERATION.CU, 6));
		prefix2.add(new OperationInstance(OPERATION.RL, 1));
		prefix2.add(new OperationInstance(OPERATION.CD, 4));
		prefix2.add(new OperationInstance(OPERATION.RR, 1));
		prefix2.add(new OperationInstance(OPERATION.CU, 0));
		prefix2.add(new OperationInstance(OPERATION.RL, 1));
		prefix2.add(new OperationInstance(OPERATION.RR, 5));
		
//		PREFIXES.add(prefix2);

		prefix2 = new ArrayList<OperationInstance>();
		prefix2.add(new OperationInstance(OPERATION.CD, 0));
		prefix2.add(new OperationInstance(OPERATION.CU, 6));
		prefix2.add(new OperationInstance(OPERATION.RL, 1));
		prefix2.add(new OperationInstance(OPERATION.CD, 4));
		prefix2.add(new OperationInstance(OPERATION.RR, 1));
		prefix2.add(new OperationInstance(OPERATION.CU, 0));
		prefix2.add(new OperationInstance(OPERATION.RL, 1));
		prefix2.add(new OperationInstance(OPERATION.CU, 5));
		
//		PREFIXES.add(prefix2);

		
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
//		scorer.addScorer(new IndexOfScorer(Arrays.asList(HITS)));
//		scorer.addScorer(new IndexOfLengthScorer(Arrays.asList(HITS)));
//		scorer.addScorer(new IndexOfLengthSquareScorer(Arrays.asList(HITS)));
		scorer.addScorer(new CountMatchesSquareScorer(Arrays.asList(HITS)));
//		scorer.addScorer(new BeginningScorer(Arrays.asList(BEGINNINGS)));
		scorer.addScorer(new BeginningLengthScorer(Arrays.asList(BEGINNINGS)));
//		scorer.addScorer(new EndingScorer(Arrays.asList(ENDINGS)));
		scorer.addScorer(new EndingLengthScorer(Arrays.asList(ENDINGS)));

		CryptoBoxSolver solver = new CryptoBoxStrategicSolver();
//		CryptoBoxSolver solver = new CryptoBoxFJSerialSolver();
		solver.setScorer(scorer).setStartMatrix(m).setSteps(STEPS).setPrefixes(PREFIXES).solve();



	}

}
