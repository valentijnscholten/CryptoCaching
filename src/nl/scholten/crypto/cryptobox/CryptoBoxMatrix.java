package nl.scholten.crypto.cryptobox;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.eaio.stringsearch.BoyerMooreHorspoolRaita;
import com.eaio.stringsearch.StringSearch;

@SuppressWarnings("serial")
public abstract class CryptoBoxMatrix extends Matrix {

	private static final boolean DO_BEGIN_MATCH = true;
	private static final boolean DO_END_MATCH = true;
	private static final boolean DO_X_PAD_MATCH = true;

	private static final boolean FORCE_BEGIN_MATCH = false;
	private static final boolean FORCE_END_MATCH = false;
	private static final boolean FORCE_X_PAD_MATCH = false;

	private static final boolean USE_ARRAY_FOR_HITS = false;

	private static final boolean SCORE_BY_SCAN_FROM_START = false;
	// /TODO calculate these ? or make sure all words match these sizes by
	// splitting them up if needed?
	private static final int MIN_WORD_SIZE = 3;
	private static final int MAX_WORD_SIZE = 6;

	private static final boolean SCORE_BY_COUNT_MATCHES = true;
//	private static final boolean SCORE_BY_COUNT_MATCHES = false;
	private static final boolean REWARD_FIRST_OCCURRENCE_OF_WORD = false; // slows
																			// down
	private static final boolean SCORE_JAVA_REGEX = false;
//	private static final boolean SCORE_JAVA_REGEX = true;
	private static final boolean SCORE_EXPERIMENTAL = false;

	protected String[] hits;
	protected Set<String> hitsSet;
	protected Object[] hitsPatterns;
	protected Pattern hitsPattern;
	protected String[] beginnings;
	protected String[] endings;
	protected int x_paddings;
	protected String x_padding;

	protected Set<String> words;
	protected StringSearch ss;

	// private ShiftOrMismatches ss;

	public CryptoBoxMatrix(String input, int size, int maxSteps, String[] hits,
			String[] beginnings, String[] endings, List<List<OperationInstance>> headStarts) {
		super(input, size, maxSteps, headStarts);
		this.hits = hits;
		this.hitsSet = new HashSet<String>(Arrays.asList(hits));

		ss = new BoyerMooreHorspoolRaita();
		this.hitsPatterns = new Object[hits.length];
		for (int i = 0; i < hits.length; i++) {
			hitsPatterns[i] = ss.processString(hits[i]);
		}

		this.words = new HashSet<String>();
		this.beginnings = beginnings;

		this.x_paddings = StringUtils.countMatches(input, "X");
		this.x_padding = StringUtils.rightPad("", x_paddings, "X");

		this.endings = endings;// copy all first
		for (int i = 0; i < endings.length; i++) {
			// remove xpads
			while (this.endings[i].endsWith("X")) {
				this.endings[i] = StringUtils.removeEnd(this.endings[i], "X");
			}
			// add fixed xpads
			this.endings[i] += x_padding;
		}

		String hitsPatternString = "(";
		for (String hit : hits) {
			if (hitsPatternString.length() > 1)
				hitsPatternString += "|";
			hitsPatternString += hit;
		}
		hitsPatternString += ")";
		this.hitsPattern = Pattern.compile(hitsPatternString);
//		System.out.println("Pattern = '" + hitsPatternString + "'");

		this.maxScore = this.score();
		this.maxScorersSet.add(this);
		this.maxScoreGlobal.set(this.maxScore);

	}

	public CryptoBoxMatrix(CryptoBoxMatrix m) {
		super(m);
		this.headStarts = m.headStarts;
		this.hits = m.hits;
		this.hitsSet = m.hitsSet;

		ss = new BoyerMooreHorspoolRaita();
		this.hitsPatterns = m.hitsPatterns;

		this.words = new HashSet<String>(m.words);
		this.beginnings = m.beginnings;

		this.x_paddings = m.x_paddings;
		this.x_padding = m.x_padding;

		this.endings = m.endings;// copy all first

		this.hitsPattern = m.hitsPattern;

		this.maxScore = m.maxScore;
		this.maxScorersSet = new HashSet<Matrix>(m.maxScorersSet);
		
	}

	/**
	 * Based on opsLog we can immediately see some solutions that are duplicates
	 * of others. 1: if all operations are row operations on mutual exclusive
	 * columns, the order doesn't matter. So only use the one where indexes are
	 * ascending.
	 * 
	 * @return
	 */
	public boolean isDuplicate() {
		if (!CACHE_SCORES && !USE_INTERN)
			return false;

		if (CACHE_SCORES && duplicates.containsKey(data))
			return true;

		if (USE_INTERN) {
			String newData = new String(data.toCharArray());
			// if this data value was interned before, it was scored before so
			// duplicate
			if (newData.intern() != newData)
				return true;
		}
		return false;

	}

	public boolean isHit(String s) {
		if (USE_ARRAY_FOR_HITS) {
			for (String hit : hits) {
				if (hit.equals(s)) {
					return true;
				}
			}
			return false;
		} else {
			return hitsSet.contains(s);
		}
	}

	protected int scoreByScanFromStart(int result) {
		int prev_b = 0;
		int a = 0;
		int wordsTotalSize = 0;

		while (a < (size * size) - MIN_WORD_SIZE) {

			// TODO ignore last XXXXs if there's a match
			for (int b = Math.min(a + MAX_WORD_SIZE, (size * size)); b >= a
					+ MIN_WORD_SIZE; b--) {
				String s = data.substring(a, b);

				if (isHit(s)) {
					// the substring is a word, hooray!
					if (a == prev_b) {
						// word is adjecent to previous word, hallelujah
						result += 100;
					}
					// another contains should be fast as hashcode is cache for
					// s.
					if (REWARD_FIRST_OCCURRENCE_OF_WORD && !words.contains(s)) {
						// first time word is found
						result += 10;
						words.add(s);
					}

					// add point for each hit.
					result += 1;
					wordsTotalSize += s.length();
					// System.out.println("found: " + s + " wordTotalSize " +
					// wordsTotalSize);

					a = b - 1;// pre-compensate for a++ in outer loop.
					prev_b = b;
					break;
				}
			}
			a++;
		}
		// if
		// (data.equals("NOORDVIJFEENGRADENVIERNULPUNTZESACHTNULMINUTENOOSTVIERGRADENTWEEVIERPUNTZEVENDRIENEGENMINUTENXXXXXXX"))
		// {
		// System.out.println("VALENTIJN");
		// }
		if (wordsTotalSize == ((size * size) - x_paddings)) {
			// all words adjacent, means it is solved (should be).
			System.out.println("Solved: " + data);
			result += 10000;
		}
		return result;
	}

	@Override
	protected int score() {
		return scoreInteral(false);
	}

	protected int scoreInteral(boolean detailed) {
		// System.out.println("scoring: " + opsLog + " data: " + data);
		// TODO more subtle optimization needed. This drops to many options, but
		// since the key is random, chances are it's ok.
		// TODO idea: make sure all segments of row ops and segments of col ops
		// are in ascending order of index, because within a segment, the order
		// doesn't matter.
		// if (isAllRow() || isAllCol()) return 0;
		if (isDuplicate())
			return 0;
		int result = 0;

		if (DO_BEGIN_MATCH) {
			boolean beginMatch = false;
			for (String beginning : beginnings) {
				if (data.startsWith(beginning)) {
					result += 100;
					beginMatch = true;
					break;
				}
			}

			if (FORCE_BEGIN_MATCH && !beginMatch)
				return 0;
		}

		if (DO_END_MATCH) {
			boolean endMatch = false;
			for (String ending : endings) {
				if (data.endsWith(ending)) {
					result += 100;
					endMatch = true;
					break;
				}
			}

			if (FORCE_END_MATCH && !endMatch)
				return 0;
		}

		if (DO_X_PAD_MATCH) {
			boolean padMatch = false;

			String end = data.substring(data.length() - x_paddings,
					data.length());
			int count = StringUtils.countMatches(end, "X");

			// System.out.println(count + " x_pads for: " + data);
			result += count;
			padMatch = count == x_paddings;

			if (padMatch)
				result += 10;

			if (FORCE_X_PAD_MATCH && !padMatch)
				return 0;
		}

		if (SCORE_BY_COUNT_MATCHES)
			return scoreByCountMatches(result, detailed);
		if (SCORE_BY_SCAN_FROM_START)
			return scoreByScanFromStart(result);
		if (SCORE_JAVA_REGEX)
			return scoreJavaRegex(result);

		if (SCORE_EXPERIMENTAL)
			return scoreExpiremental(result);

		throw new IllegalStateException("Select a score method");
	}

	private boolean isAllCol() {
		for (OperationInstance oi : opsLog) {
			if (oi.op.isRow)
				return false;
		}
		return true;
	}

	private boolean isAllRow() {
		for (OperationInstance oi : opsLog) {
			if (!oi.op.isRow)
				return false;
		}
		return true;
	}

	protected int scoreByCountMatches(int result, boolean detailed) {

		for (String hit : hits) {
			if (REWARD_FIRST_OCCURRENCE_OF_WORD) {
				// extra score for first occurrence to stimulate more different
				// words
				int count = StringUtils.countMatches(data, hit);
				if (count > 0)
					result += 10;
				result += (count * 10); // 10 pts per word
			} else if (detailed) {
				//detailed scoring counts *all* matches of each hit
				int count = StringUtils.countMatches(data, hit);
				if (count > 0)
					result += 10;
				result += count; // 1 pts per word
			} else {
				// using only index doesn't help much (10%) ->> ehhh later it
				// looks like 200% gain when going parallel.
				if (data.indexOf(hit) > -1) {
					result += 10;// 10 pts per word
				}
			}
		}
		
		System.out.println("scored: " + opsLog + " score: " + StringUtils.leftPad(String.valueOf(result), 4) + " data: " + data);

		return result;
	}

	protected int scoreJavaRegex(int result) {
		Matcher matcher = this.hitsPattern.matcher(data);

		// do first match to check if it is at index 0;
		if (matcher.find()) {
			if (matcher.start() == 0) {
				// found a match at the start, which means bonus.
				result += 25;
//				System.out.println("index: " + matcher.start() + " matched: " + matcher.group() + " START_BONUS");
			} else {
				result += 10;
//				System.out.println("index: " + matcher.start() + " matched: " + matcher.group());
			}

			while (matcher.find()) {
				// skip last match as it has different logging
				int a = matcher.start();
				int b = data.length();
				int c = matcher.group().length();
				if (matcher.start() == (data.length() - matcher.group()
								.length())) {
					// found a match at the start, which means bonus.
					result += 25;
//					System.out.println("index: " + matcher.start() + " matched: " + matcher.group() + " END_BONUS");
				} else {
					result += 10;
//					System.out.println("index: " + matcher.start() + " matched: " + matcher.group());
				}
			}

		} else {
			// no matches at all
		}
		return result;
		
	}
	
	protected int scoreExpiremental(int result) {


		return result;
	}

	protected int scoreDetailed() {
		int result =  scoreInteral(true);
		System.out.println("Scored detailed for " + data + " score: " + StringUtils.leftPad(String.valueOf(result), 4));
		return result;
	}
	
	public static void main(String[] args) throws IOException {
		Matrix m = new CryptoBox2Matrix(
				StringUtils
						.rightPad(
								// "TWEEACDEENFKDRIEZEVENAT",
								"GOEDGEDAANGANAARVIJFEENGRADENVIERNULPUNTVIERDRIEVIJENNLIEGRUFADERDRIENLPUNTNEGENNEGENVIJFVALERNIETIN",
//								"NOORDVIJFEENGRADENVIERNULPUNTZESACHTNULMINUTENOOSTVIERGRADENTWEEVIERPUNTZEVENDRIENEGENMINUTENXXXXXXX",
								100, 'X'));

		System.out.println(m.score());
	}

}