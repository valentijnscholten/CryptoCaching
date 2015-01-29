package nl.scholten.crypto.cryptobox.data;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nl.scholten.crypto.cryptobox.scorer.AhoCorasickSquareScorer;
import nl.scholten.crypto.cryptobox.scorer.CombinedScorer;
import nl.scholten.crypto.cryptobox.solver.CryptoBoxFJSerialSolver;
import nl.scholten.crypto.cryptobox.solver.CryptoBoxSolver;
import nl.scholten.crypto.cryptobox.util.AhoCorasick;

@SuppressWarnings("serial")
public class Challenge20Classic extends CryptoBoxMatrix {

	private static String INPUT = "RLITAEKXWWWHETTSRYESNAHWRTRDNWSOWECWEAAEEIOYHETHWKRSGPYISYSEOESHEDSPSCEWTAAEXTWEETRSXNDEHEEETEWHONXW";


	public static String[] HITS = new String[] {
		"it",
		"a",
		"ye",
		"yes",
		"we",
		"he",
		"is",
		"the",
		"who",
		"on",
		"ah",
		"see",
		"she",
		"an",
		"so",
		"let",
		"no",
		"thee",
		"one",
		"new",
		"knew",
		"saw",
		"its",
		"at",
		"oh",
		"now",
		"try",
		"set",
		"as",
		"hot",
		"ran",
		"ask",
		"her",
		"do",
		"sea",
		"to",
		"and",
		"yet",
		"ten",
		"end",
		"had",
		"ay",
		"send",
		"tea",
		"here",
		"there",
		"eye",
		"eyes",
		"next",
		"lie",
		"not",
		"go",
		"letter",
		"his",
		"sweet",
		"yet",
		"were",
		"eat",
		"was",
		"sad",
		"why",
		"when",
		"say",
		"get",
		"or",
		"how",
		"send",
		"easy",
		"pass",
		"any",
		"week",
		"know",
		"has",
		"sit",
		"three",
		"cry",
		"hear",
		"hand",
		"news",
		"red",
		"ears",
		"none",
		"nay",
		"another",
		"other",
		"knows",
		"hat",
		"done",
		"own",
		"where",
		"head",
		"year",
		"are",
		"then",
		"went",
		"son",
		"tone",
		"sent",
		"read",
		"sort",
		"rest",
		"we",
		"keep",
		"sat",
		"sir",
		"two",
		"they",
		"sake",
		"said",
		"either",
		"stay",
		"ready",
		"day",
		"days",
		"take",
		"hands",
		"wait",
		"age",
		"those",
		"than",
		"latter",
		"what",
		"in",
		"way",
		"art",
		"wish",
		"this",
		"air",
		"top",
		"act",
		"paid",
		"want",
		"says",
		"worth",
		"die",
		"trees",
		"white",
		"seen",
		"dear",
		"with",
		"near",
		"pay",
		"write",
		"water",
		"note",
		"these",
		"late",
		"aware",
		"dare",
		"whether",
		"stand",
		"seat",
		"nor",
		"rose",
		"share",
		"else",
		"that",
		"six",
		"work",
		"low",
		"drew",
		"hope",
		"law",
		"taste",
		"god",
		"away",
		"short",
		"secret",
		"tired",
		"green",
		"less",
		"wrote",
		"sense",
		"asked",
		"letters",
		"does",
		"ago",
		"heart"	};
	
	public static String[] BEGINNINGS = new String[] { };
	public static String[] ENDINGS = new String[] { };

	public static final Set<List<OperationInstance>> PREFIXES = new HashSet<List<OperationInstance>>();
	static {
	}

	private static int SIZE = 10;
	private static int STEPS = 20;

	public Challenge20Classic() {
		super(INPUT, SIZE);
	}

	public Challenge20Classic(String input) {
		super(input, SIZE);
	}

	public static void main(String[] args) throws IOException {
		// System.in.read();
		CryptoBoxMatrix m = new Challenge20Classic(INPUT);

		CombinedScorer scorer = new CombinedScorer();
//		scorer.addScorer(new CountMatchesSquareScorer(Arrays.asList(HITS)));
//		scorer.addScorer(new BeginningLengthScorer(Arrays.asList(ENDINGS)));		
//		scorer.addScorer(new EndingLengthScorer(Arrays.asList(ENDINGS)));		
		scorer.addScorer(new AhoCorasickSquareScorer(AhoCorasick.createEnglishTrie("1000words.txt")));
//		scorer.addScorer(new AhoCorasickSquareScorer(AhoCorasick.createTrie(Arrays.asList(HITS))));
		
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
