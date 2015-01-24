package nl.scholten.crypto.cryptobox.scorer;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import nl.scholten.crypto.cryptobox.data.CryptoBoxMatrix;
import nl.scholten.crypto.cryptobox.util.AhoCorasick;
import nl.scholten.crypto.cryptobox.util.Util;

import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;

public class AhoCorasickSquareScorer implements CryptoBoxScorer {

	private Trie trie;

	public Map<String, Integer> matches = new ConcurrentHashMap<>();
	
	public AhoCorasickSquareScorer(Trie trie) {
		this.trie = trie;
	}

	@Override
	public int score(CryptoBoxMatrix matrix) {
		try {
			Collection<Emit> hits = AhoCorasick.getMatches(matrix.data, trie);
			
			int result = 0;
			for (Emit hit : hits) {
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
		System.out.println(new AhoCorasickSquareScorer(AhoCorasick.createEnglishTrie()).score(new CryptoBoxMatrix("SWISHEDNWWAHERTOKXENESHERTSYNWEIOTECRDAASGOTHEREWKCEPWISWHEREOWSEATYSSETTYWEDPWEWEAYXNSTHERELTWAHXEX",  10)));
	}
	
}
