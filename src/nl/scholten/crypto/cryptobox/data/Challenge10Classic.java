package nl.scholten.crypto.cryptobox.data;

import java.io.IOException;
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
public class Challenge10Classic extends CryptoBoxMatrix {

	private static String INPUT = "HTHLYTHXUGTTHETINEHEBOXAECRLTSEVSRWIULCNOMEEUTBYTASMAOTGUEBROKERHEKDYADSTREADALNLTHPWORXSOXUTEOUDOTX";

//state: new max score 3886 [RR_3, RR_7, CU_3, CD_7, RL_0, CU_7, CU_7, RL_9, CD_7, CD_7](3R7RDUHD0LHUHU9LHDHD) THEYTHOUGHTTHATINTHEBOXSECRETSNEVERWILLCOMEOUTBUTASMARTGUYBROKETHEKEYANDSPREADALLTHEWORDSOUTLOUDXXXX

	//THEYTHOUGHTTHATINXHEBOXSECRETSNEVERWILLCOMEOUTBUTASMARTGUYBROKETHEKEYANDSPREADALLTHEWORDSOXUTLOUDXTX
	//THEYTHOUGHTTHATINTHEBOXSECRETSNEVERWILLCOMEOUTBUTASMARTGUYBROKETHEKEYANDSPREADALLTHEWORDSOUTLOUDXXXX

	public static String[] HITS = new String[] {
		"THEYTHOUGHTTHATINTHEBOXSECRETS",
		"NEVERWILLCOMEOUTBUTASMARTGUY",
		"BROKETHEKEYANDSPREADALLTHEWORDSOUTLOUDXXXX",
		
		"THEY", "THOUGHT", "THAT", "INTHE"
				, "BOX"
				, "SECRETS"
				, "NEVER"
				, "WILL"
				, "COME"
				, "OUT"
				, "BUTA"
				, "SMART"
				, "GUY"
				, "BROKE"
//				, "THE"
				, "KEY"
				, "AND"
				, "SPREAD"
				, "ALL"
//				, "THE"
				, "WORDS"
				, "OUT"
				, "LOUD"
	};
	
	public static String[] BEGINNINGS = new String[] { "THEYTHOUGHTTHATINTHE"};
	public static String[] ENDINGS = new String[] { "XX", "XXX", "XXXX", "LOUDX", "LOUDXX", "LOUDXXX", "LOUDXXXX" };

	public static final Set<List<OperationInstance>> PREFIXES = new HashSet<List<OperationInstance>>();
	static {
	}

	private static int SIZE = 10;
	private static int STEPS = 10;

	public Challenge10Classic() {
		super(INPUT, SIZE);
	}

	public Challenge10Classic(String input) {
		super(input, SIZE);
	}

	public static void main(String[] args) throws IOException {
		// System.in.read();
		CryptoBoxMatrix m = new Challenge10Classic(INPUT);

		CombinedScorer scorer = new CombinedScorer();
		scorer.addScorer(new CountMatchesSquareScorer(Arrays.asList(HITS)));
		scorer.addScorer(new BeginningLengthScorer(Arrays.asList(ENDINGS)));		
		scorer.addScorer(new EndingLengthScorer(Arrays.asList(ENDINGS)));		

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
