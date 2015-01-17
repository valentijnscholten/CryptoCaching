package nl.scholten.crypto.cryptobox.scorer;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import nl.scholten.crypto.cryptobox.data.CryptoBoxMatrix;
import nl.scholten.crypto.cryptobox.util.AhoCorasick;

import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;

public class AhoCorasickSquareScorer implements CryptoBoxScorer {

	private Trie trie;

	private Set<String> matches = new HashSet<>();
	
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
				if (!matches.contains(hitString)) {
					System.out.println("AHO_HIT=" + hitString);
					matches.add(hit.getKeyword());
				}
				result += (hit.size() * hit.size()); 
			}
			return result;
		} catch (NullPointerException ne) {
			System.out.println("Score Problem for: " + matrix);
			return -1;
		}
	}

}
