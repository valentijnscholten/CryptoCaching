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
public class Crypto4 extends CryptoBoxMatrix {

	private static String INPUT = "UAEIOMETEREIRRHOHNTGTHEVPROJSLEETIVPNAECHIERDCAOOFNTPTDRIERENARONTZEDEENUADENEVENGCJFZENENBVAATMETVE";
	public static String[] HITS = new String[] { "PROJECTEER", "VANAF", "HIER", "RECHTDOOR", "METER", "HONDERD", "HOOG", "EN", "JACHT", "TROFEE", "ACHTER", "UIT", "NAAR",
		"GRADEN", "MINUTEN", "NERGENS", "GETAL", "HET", "GAAT", "VIND", "DRIEEENPUNTZEVENGRADEN",  "NEGENVIJFZEVENACHTMETER", "VANUIT", "LAATSTE", "MET",
		"LEER", "OM", "TE", "GAAN",
		"PUNT", "NUL", "EEN", "TWEE", "DRIE", "VIER", "VIJF",
		"ZES", "ZEVEN", "ACHT", "NEGEN" };

	public static String[] BEGINNINGS = new String[] { // "UIT", "BAROMETER" 
		}; //?
	public static String[] ENDINGS = new String[] { "EENVIJFZEVENACHTMETER", "METER"
		}; //?
	
	public static final Set<List<OperationInstance>> PREFIXES = new HashSet<List<OperationInstance>>();
	static {
//		List<OperationInstance> headStart = new ArrayList<Matrix.OperationInstance>();
//		headStart.add(new Matrix.OperationInstance(OPERATION.ROW_LEFT, 8));
//		headStart.add(new Matrix.OperationInstance(OPERATION.COL_UP, 3));
//		
//		HEAD_STARTS.add(headStart);
	}

	private static int SIZE = 10;
//	private static int STEPS = 14; // hint from Cache owner
	private static int STEPS = 16;

	public Crypto4() {
		super(INPUT, SIZE);
	}

	public Crypto4(String input) {
		super(input, SIZE);
	}

	public static void main(String[] args) throws IOException {
		CryptoBoxMatrix m = new Crypto4(INPUT);


		CombinedScorer scorer = new CombinedScorer();
//		scorer.addScorer(new IndexOfScorer(Arrays.asList(HITS)));
//		scorer.addScorer(new IndexOfLengthScorer(Arrays.asList(HITS)));
//		scorer.addScorer(new IndexOfLengthSquareScorer(Arrays.asList(HITS)));
		scorer.addScorer(new CountMatchesSquareScorer(Arrays.asList(HITS)));
//		scorer.addScorer(new BeginningScorer(Arrays.asList(BEGINNINGS)));
		scorer.addScorer(new BeginningLengthScorer(Arrays.asList(BEGINNINGS)));
//		scorer.addScorer(new EndingScorer(Arrays.asList(ENDINGS)));
		scorer.addScorer(new EndingLengthScorer(Arrays.asList(ENDINGS)));

//		CryptoBoxSolver solver = new CryptoBoxSerialSolver();
//		CryptoBoxSolver solver = new CryptoBoxFJSerialSolver();
		CryptoBoxSolver solver = new CryptoBoxStrategicSolver();
		solver.setScorer(scorer).setStartMatrix(m).setSteps(STEPS).setPrefixes(PREFIXES).solve();
		
	}

}
