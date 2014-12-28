package nl.scholten.crypto.cryptobox.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nl.scholten.crypto.cryptobox.scorer.CombinedScorer;
import nl.scholten.crypto.cryptobox.scorer.EndingScorer;
import nl.scholten.crypto.cryptobox.scorer.IndexOfScorer;
import nl.scholten.crypto.cryptobox.solver.CryptoBoxSolver;
import nl.scholten.crypto.cryptobox.solver.CryptoBoxStrategicSolver;

@SuppressWarnings("serial")
public class Crypto1 extends CryptoBoxMatrix {

	private static String INPUT = "OORXVEJFENENGDADUNVIERNRLPNNTZESAUHTOULMINUCENAOSTVIETGREDENTWERVINRPUNTZEVEMDRIUENEEENXINTENGXXIXXX";

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
	private static int STEPS = 4;

	public Crypto1() {
		super(INPUT, SIZE);
	}

	public Crypto1(String input) {
		super(input, SIZE);
	}

	public static void main(String[] args) throws IOException {
		// System.in.read();
		CryptoBoxMatrix org = new Crypto1(INPUT);
		CryptoBoxMatrix m = new Crypto1(INPUT);

//		m.shiftRowLeft(0);
//		System.out.println(CryptoBoxMatrix.toStringSideBySide(org, m));
		// System.out.println();
		// m.shiftRowRight(0);
		// System.out.println(Matrix.toStringSideBySide(org, m));
		// System.out.println();
		// m.shiftColumnDown(0);
		// System.out.println(Matrix.toStringSideBySide(org, m));
		// System.out.println();
		// m.shiftColumnUp(0);
		// System.out.println(Matrix.toStringSideBySide(org, m));
		// System.out.println();

		// m.shiftRowLeft(9);
		// m.shiftColumnUp(0);
		// m.shiftColumnUp(0);
		// m.shiftColumnUp(0);
		// System.out.println(Matrix.toStringSideBySide(org, m));
		//
		// System.out.println(m.data);
		// System.out.println("EORXVEJFENINGDADUNVIVRNRLPNNTZTSAUHTOULMNNUCENAOSTUIETGREDENEWERVINRPUOTZEVEMDRIEENEEENXINENGXXIXXXT");

		CombinedScorer scorer = new CombinedScorer();
		scorer.addScorer(new IndexOfScorer(Arrays.asList(HITS)));
		scorer.addScorer(new EndingScorer(Arrays.asList(ENDINGS)));		

//		CryptoBoxSolver solver = new CryptoBoxSerialSolver();
//		CryptoBoxSolver solver = new CryptoBoxFJSerialSolver();
		CryptoBoxSolver solver = new CryptoBoxStrategicSolver();
//		solver.setScorer(scorer).setStartMatrix(m).setSteps(STEPS).solve();
		solver.setScorer(scorer).setStartMatrix(m).setSteps(STEPS).setPrefixes(PREFIXES).solve();
//		solver.setScorer(scorer).setStartMatrix(m).setSteps(STEPS).setPostfixes(PREFIXES).solve();
		


		// System.in.read();
	}

}
