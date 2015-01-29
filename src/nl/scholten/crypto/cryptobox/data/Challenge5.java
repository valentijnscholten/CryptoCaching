package nl.scholten.crypto.cryptobox.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nl.scholten.crypto.cryptobox.scorer.AhoCorasickSquareScorer;
import nl.scholten.crypto.cryptobox.scorer.CombinedScorer;
import nl.scholten.crypto.cryptobox.solver.CryptoBoxSolver;
import nl.scholten.crypto.cryptobox.solver.CryptoBoxStrategicSolver;
import nl.scholten.crypto.cryptobox.util.AhoCorasick;

@SuppressWarnings("serial")
public class Challenge5 extends CryptoBoxMatrix {

	private static String INPUT = "STARTHFHRSEEREESIOASQCARYITNNOSTUASACLSEAEEREREUORDRARFDAWCINSLENTTNRIGGTURTIOTHHECLETLEEESSHECIGSTX";

	public static String[] HITS = new String[] { "NOORD", "OOST", "GRADEN",
			"MINUTEN", "PUNT", "NUL", "EEN", "TWEE", "DRIE", "VIER", "VIJF",
			"ZES", "ZEVEN", "ACHT", "NEGEN" };

	public static String[] BEGINNINGS = new String[] { "NOORDVIJFEENGRADENVI" };
	public static String[] ENDINGS = new String[] {};

	public static final Set<List<OperationInstance>> PREFIXES = new HashSet<List<OperationInstance>>();
	static {
		// solution = [RL_8, CU_3, RR_0, CD_6]
		List<OperationInstance> prefix = new ArrayList<OperationInstance>();
		prefix.add(new OperationInstance(OPERATION.RL, 8));
		prefix.add(new OperationInstance(OPERATION.CU, 3));

//		PREFIXES.add(prefix);
	}

	private static int SIZE = 10;
	private static int STEPS = 5;

	public Challenge5() {
		super(INPUT, SIZE);
	}

	public Challenge5(String input) {
		super(input, SIZE);
	}

	public static void main(String[] args) throws IOException {
		// System.in.read();
		CryptoBoxMatrix m = new Challenge5(INPUT);

		CombinedScorer scorer = new CombinedScorer();
		scorer.addScorer(new AhoCorasickSquareScorer(AhoCorasick.createEnglishTrie("1000words.txt")));		

		CryptoBoxSolver solver = new CryptoBoxStrategicSolver();
//		CryptoBoxFJSerialSolver solver = new CryptoBoxFJSerialSolver();
//		CryptoBoxSolver solver = new CryptoBoxStrategicSolver();
		solver.setScorer(scorer).setStartMatrix(m).setSteps(STEPS).setPrefixes(PREFIXES).solve();
//		solver.setPermuSources(Collections.singleton(permuSource)).setScorer(scorer).setStartMatrix(m).setSteps(STEPS).solve();

		
//		CryptoBoxPermuSolver solver = new CryptoBoxPermuSolver();
//		solver.setPermuSource(permuSource).setScorer(scorer).setStartMatrix(m).setSteps(STEPS).solve();
		

		// System.in.read();
	}

}
