package nl.scholten.crypto.cryptobox.scorer;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import nl.scholten.crypto.cryptobox.data.CryptoBoxMatrix;
import nl.scholten.crypto.cryptobox.util.AhoCorasick;
import nl.scholten.crypto.cryptobox.util.Util;

import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Token;
import org.ahocorasick.trie.Trie;

public class AhoCorasickTokenizedSimpleScorer implements CryptoBoxScorer {

	private Trie trie;

	public Map<String, Integer> matches = new ConcurrentHashMap<>();

	public AhoCorasickTokenizedSimpleScorer(Trie trie) {
		this.trie = trie;
		trie.removeOverlaps(); //important
	}

	@Override
	public int score(CryptoBoxMatrix matrix) {
		try {
			Collection<Emit> hits = AhoCorasick.getMatches(matrix.data, trie);
			
			int result = 0;
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
					
					result += hit.size();
					
				} else {
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
		System.out.println(new AhoCorasickTokenizedSimpleScorer(AhoCorasick.createEnglishTrie("1000words.txt")).score(new CryptoBoxMatrix("SWISHEDNWWAHERTOKXENESHERTSYNWEIOTECRDAASGOTHEREWKCEPWISWHEREOWSEATYSSETTYWEDPWEWEAYXNSTHERELTWAHXEX",  10)));
	}
	
}

