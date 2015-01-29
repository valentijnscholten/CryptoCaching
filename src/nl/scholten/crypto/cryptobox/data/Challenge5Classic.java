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
import nl.scholten.crypto.cryptobox.scorer.EndingLengthSquareScorer;
import nl.scholten.crypto.cryptobox.solver.CryptoBoxSolver;
import nl.scholten.crypto.cryptobox.solver.CryptoBoxStrategicSolver;

@SuppressWarnings("serial")
public class Challenge5Classic extends CryptoBoxMatrix {

	private static String INPUT = "STARTHFHRSEEREESIOASQCARYITNNOSTUASACLSEAEEREREUORDRARFDAWCINSLENTTNRIGGTURTIOTHHECLETLEEESSHECIGSTX";

	public static String[] HITS = new String[] { "SEARCH", "FOR", "SECRETS",
			"IN", "A", "SQUARE", "ITS", "NOT", "EASY", "CLUES", "ARE", "RARE",
			"DANCING", "LEFT", "UNTIL", "RIGHT", "SEES", "THE", "LIGHT" };
	
	public static String[] BEGINNINGS = new String[] {};
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

	public Challenge5Classic() {
		super(INPUT, SIZE);
	}

	public Challenge5Classic(String input) {
		super(input, SIZE);
	}

	public static void main(String[] args) throws IOException {
		// System.in.read();
		CryptoBoxMatrix m = new Challenge5Classic(INPUT);

		CombinedScorer scorer = new CombinedScorer();
		scorer.addScorer(new CountMatchesSquareScorer(Arrays.asList(HITS)));
		scorer.addScorer(new BeginningLengthScorer(Arrays.asList(ENDINGS)));		
		scorer.addScorer(new EndingLengthSquareScorer(Arrays.asList(ENDINGS)));		

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
