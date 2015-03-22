package nl.scholten.crypto.cryptobox.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class Test {

	public static Set<String> allCombos(Set<String> source1, Set<String> source2, Set<String> source3) {
		Set<String> result = new HashSet<String>();
		for (String cijfer: source1) {
			for (String cijfer2: source2) {
				for (String cijfer3: source3) {
					result.add(cijfer + cijfer2 + cijfer3);
				}
			}
		}
		return result;
	}
	
	public static void main(String[] args) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

//		"NUL",
//		"EEN",
//		"TWEE",
//		"DRIE",
//		"VIER",
//		"VIJF",
//		"ZES",
//		"ZEVEN",
//		"ACHT",
//		"NEGEN"

		
		Set<String> length3 = new HashSet<>(Arrays.asList(new String[]{
				"NUL",
				"EEN",
				"ZES",
				"VYF"
		}));
		Set<String> length4 = new HashSet<>(Arrays.asList(new String[]{
				"TWEE",
				"DRIE",
				"VIER",
				"VIJF",
				"ACHT",
		}));
		Set<String> length5 = new HashSet<>(Arrays.asList(new String[]{
				"ZEVEN",
				"NEGEN"
		}));

		
		Set<String> alles = new HashSet<String>();
		
		alles.addAll(allCombos(length3, length3, length5));
		alles.addAll(allCombos(length3, length5, length3));
		alles.addAll(allCombos(length5, length3, length3));

		alles.addAll(allCombos(length4, length4, length3));
		alles.addAll(allCombos(length3, length4, length4));
		alles.addAll(allCombos(length4, length3, length4));

		for (String combo: alles) {
			System.out.println(combo);
		}
		
		
	}

}
