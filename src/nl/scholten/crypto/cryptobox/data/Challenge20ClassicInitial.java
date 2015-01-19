package nl.scholten.crypto.cryptobox.data;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nl.scholten.crypto.cryptobox.scorer.AhoCorasickSquareScorer;
import nl.scholten.crypto.cryptobox.scorer.CombinedScorer;
import nl.scholten.crypto.cryptobox.solver.CryptoBoxFJSerialSolver;
import nl.scholten.crypto.cryptobox.solver.CryptoBoxSolver;
import nl.scholten.crypto.cryptobox.util.AhoCorasick;

@SuppressWarnings("serial")
public class Challenge20ClassicInitial extends CryptoBoxMatrix {

	private static String INPUT = "RLITAEKXWWWHETTSRYESNAHWRTRDNWSOWECWEAAEEIOYHETHWKRSGPYISYSEOESHEDSPSCEWTAAEXTWEETRSXNDEHEEETEWHONXW";


	public static String[] HITS = new String[] {

		"here",
		"what", 
		"sweet", 
		"there", 
		"they", 
		"other", 
		"thee", 
		"were", 
		"another", 
		"none", 
		"taste", 
		"where", 
		"eyes", 
		"wish", 
		"send", 
		"letter", 
		"sake", 
		"whether", 
		"this", 
		"rest", 
		"news", 
		"done", 
		"said", 
		"wished", 
		"when", 
		"note", 
		"knew", 
		"read", 
		"then", 
		"these", 
		"seen", 
		"three", 
		"worth", 
		"says", 
		"seat", 
		"than", 
		"wine", 
		"sort"	
	};
	
	public static String[] BEGINNINGS = new String[] { };
	public static String[] ENDINGS = new String[] { "XX", "XXX" };

	public static final Set<List<OperationInstance>> PREFIXES = new HashSet<List<OperationInstance>>();
	static {
	
	}

	private static int SIZE = 10;
	private static int STEPS = 20;

	public Challenge20ClassicInitial() {
		super(INPUT, SIZE);
	}

	public Challenge20ClassicInitial(String input) {
		super(input, SIZE);
	}

	public static void main(String[] args) throws IOException {
		// System.in.read();
		CryptoBoxMatrix m = new Challenge20ClassicInitial(INPUT);

		CombinedScorer scorer = new CombinedScorer();
//		scorer.addScorer(new CountMatchesSquareScorer(Arrays.asList(HITS)));
		scorer.addScorer(new AhoCorasickSquareScorer(AhoCorasick.createTrie(Arrays.asList(HITS))));		
//		scorer.addScorer(new BeginningLengthScorer(Arrays.asList(ENDINGS)));		
//		scorer.addScorer(new EndingLengthScorer(Arrays.asList(ENDINGS)));		

//		CryptoBoxSolver solver = new CryptoBoxStrategicSolver();
		CryptoBoxSolver solver = new CryptoBoxFJSerialSolver();
//		CryptoBoxFJSerialSolver solver = new CryptoBoxFJSerialSolver();
//		CryptoBoxSolver solver = new CryptoBoxStrategicSolver();
		solver.setScorer(scorer).setStartMatrix(m).setSteps(STEPS).setPrefixes(PREFIXES).solve();
//		solver.setPermuSources(Collections.singleton(permuSource)).setScorer(scorer).setStartMatrix(m).setSteps(STEPS).solve();

		
//		CryptoBoxPermuSolver solver = new CryptoBoxPermuSolver();
//		solver.setPermuSource(permuSource).setScorer(scorer).setStartMatrix(m).setSteps(STEPS).solve();
		

		// System.in.read();
	}

}
