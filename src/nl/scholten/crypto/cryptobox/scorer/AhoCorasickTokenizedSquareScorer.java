package nl.scholten.crypto.cryptobox.scorer;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import nl.scholten.crypto.cryptobox.data.CryptoBoxMatrix;
import nl.scholten.crypto.cryptobox.util.AhoCorasick;
import nl.scholten.crypto.cryptobox.util.Util;

import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Token;
import org.ahocorasick.trie.Trie;

public class AhoCorasickTokenizedSquareScorer implements CryptoBoxScorer {

	private Trie trie;

	public Map<String, Integer> matches = new ConcurrentHashMap<>();

	private Set<String> bonusHits;
	
	public AhoCorasickTokenizedSquareScorer(Trie trie, List<String> bonusHits) {
		this.trie = trie;
		this.bonusHits = new HashSet<>(bonusHits);
		trie.removeOverlaps();

	}

	@Override
	public int score(CryptoBoxMatrix matrix) {
		try {
			Collection<Emit> hits = AhoCorasick.getMatches(matrix.data, trie);
			
			int result = 0;
			Emit prevMatch = null;
			for(Token token: trie.tokenize(matrix.data)) {
				if (token.isMatch()) {
					Emit hit = token.getEmit();
					String hitString = hit.getKeyword();
					Integer count = matches.get(hitString);
					if (hitString.length() > 2) {
						if (count == null) {
	//					System.out.println("AHO_HIT=" + hitString);
						count = 0;
						}
						matches.put(hitString, count + 1);
					} 
					
					result += (hit.size() * hit.size());
					
					//bonus hits more points
					if (bonusHits.contains(hitString)) result += hit.size() * hit.size();
					
					//start word extra points
					if (hit.getStart() == 0 && hit.size() > 2) result +=  hit.size();
//					if (hit.getStart() == 0 && hitString.equals("LET")) result += 100 * hit.size();
//					if (hit.getStart() == 0 && hitString.equals("LETTER")) result += 100 * hit.size();
//					if (hit.getStart() == 0 && hitString.equals("LETTERS")) result += 100 * hit.size();
//					if (hit.getStart() == 0 && hitString.equals("WHETHER")) result += 100 * hit.size();
					
					//if two matches adjacent => more mpoints
					if (prevMatch != null) result += prevMatch.size() * hit.size();
					prevMatch = hit;
				} else {
					prevMatch = null;
				}
			}
			
			return result;
		} catch (NullPointerException ne) {
			System.out.println("Score Problem for: " + matrix);
			return -1;
		}
	}
	
	public String getResult() {
		return Util.sortByDescendingValue(matches).toString();
	}

	public static void main(String[] args) {
		System.out.println(new AhoCorasickTokenizedSquareScorer(AhoCorasick.createEnglishTrie("1000words.txt"), new LinkedList<String>()).score(new CryptoBoxMatrix("SWISHEDNWWAHERTOKXENESHERTSYNWEIOTECRDAASGOTHEREWKCEPWISWHEREOWSEATYSSETTYWEDPWEWEAYXNSTHERELTWAHXEX",  10)));
	}
	
}
