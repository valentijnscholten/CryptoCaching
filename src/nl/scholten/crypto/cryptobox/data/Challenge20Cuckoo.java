package nl.scholten.crypto.cryptobox.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nl.scholten.crypto.cryptobox.scorer.AhoCorasickTokenizedSquareScorer;
import nl.scholten.crypto.cryptobox.scorer.CombinedScorer;
import nl.scholten.crypto.cryptobox.scorer.EndingLengthSquareScorer;
import nl.scholten.crypto.cryptobox.solver.CryptoBoxSolver;
import nl.scholten.crypto.cryptobox.solver.CuckooSolver;
import nl.scholten.crypto.cryptobox.util.AhoCorasick;

import org.ahocorasick.trie.Trie;

@SuppressWarnings("serial")
public class Challenge20Cuckoo extends CryptoBoxMatrix {

	private static String INPUT = "RLITAEKXWWWHETTSRYESNAHWRTRDNWSOWECWEAAEEIOYHETHWKRSGPYISYSEOESHEDSPSCEWTAAEXTWEETRSXNDEHEEETEWHONXW";
	
	public static String[] HITS = new String[] { "SWEET", "WHEN", "EYES", "WERE", "WHEN", "TRY", "LAW", "WAIT", "LETTERS", "WHETHER", "WRITTEN", "DOWN" };

	public static String[] BEGINNINGS = new String[] { "LETTERSWEEITHER", "WRITTENDOWNLETTERS", "WRITTENDOWN", "WHETHER", "WRITTEN", "LETTERS", "ELITE" };
	public static String[] ENDINGS = new String[] { "X", "XX", "XXX"};

	public static final Set<List<OperationInstance>> PREFIXES = new HashSet<List<OperationInstance>>();
	static {
		List<OperationInstance> prefix = new ArrayList<OperationInstance>();
		prefix.add(new OperationInstance(OPERATION.RL, 8));
		prefix.add(new OperationInstance(OPERATION.CU, 3));

//		PREFIXES.add(prefix);
	}

	private static int SIZE = 10;
	private static int STEPS = 20;

	public Challenge20Cuckoo() {
		super(INPUT, SIZE);
	}

	public Challenge20Cuckoo(String input) {
		super(input, SIZE);
	}

	public static void main(String[] args) throws IOException {
		// System.in.read();
		CryptoBoxMatrix m = new Challenge20Cuckoo(INPUT);

		CombinedScorer scorer = new CombinedScorer();
		Trie trie = AhoCorasick.createEnglishTrie("5000words.txt");
		trie.addKeyword("EYESRED");
		trie.addKeyword("REDEYES");
		trie.addKeyword("ANOTHERWEEK");
		scorer.addScorer(new AhoCorasickTokenizedSquareScorer(trie, Arrays.asList(HITS)));		
//		scorer.addScorer(new BeginningLengthSquareScorer(Arrays.asList(BEGINNINGS)));		
		scorer.addScorer(new EndingLengthSquareScorer(Arrays.asList(ENDINGS)));		

		CryptoBoxSolver solver = new CuckooSolver();
//		CryptoBoxFJSerialSolver solver = new CryptoBoxFJSerialSolver();
//		CryptoBoxSolver solver = new CryptoBoxStrategicSolver();
		solver.setScorer(scorer).setStartMatrix(m).setSteps(STEPS).solve();
//		solver.setPermuSources(Collections.singleton(permuSource)).setScorer(scorer).setStartMatrix(m).setSteps(STEPS).solve();

		
//		CryptoBoxPermuSolver solver = new CryptoBoxPermuSolver();
//		solver.setPermuSource(permuSource).setScorer(scorer).setStartMatrix(m).setSteps(STEPS).solve();
		

		// System.in.read();
	}

}
