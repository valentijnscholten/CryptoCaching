package nl.scholten.crypto.cryptobox.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;

import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;

public class AhoCorasick {

	public static Trie createEnglishTrie(String filename) {
	    Trie trie = new Trie();
	    
	    try {
	    	ClassLoader classLoader = trie.getClass().getClassLoader();
	    	File file = new File(classLoader.getResource(filename).getFile());
	    	BufferedReader buf = new BufferedReader(new FileReader(file));
			
			for(String word = buf.readLine(); word != null; word = buf.readLine()) {
//				System.out.println("Adding " + word);
				if (word.length() > 1) trie.addKeyword(word);
			}
	    
			buf.close();
	    
	    } catch (IOException e) {
			throw new IllegalStateException(e);
		}
	    
	    return trie.caseInsensitive();
	}

	public static Trie createTrie(Collection<String> words) {
	    Trie trie = new Trie();

	    for(String word: words) {
			System.out.println("Adding " + word);
	    	trie.addKeyword(word);
	    }
	    
	    return trie.caseInsensitive();
	}
	
	public static Collection<Emit> getMatches(String s, Trie trie) {
		return trie.parseText(s);
	}
	
}
