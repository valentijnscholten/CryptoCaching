package nl.scholten.crypto.cryptobox.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class RGGG {

	public static List<String> convertFormulasToExcel() throws IOException {
		List<String> result = new LinkedList<>();
		ClassLoader classLoader = RGGG.class.getClassLoader();
    	File file = new File(classLoader.getResource("RGGG.txt").getFile());
    	BufferedReader buf = new BufferedReader(new FileReader(file));
		
		for(String line = buf.readLine(); line != null; line = buf.readLine()) {
			String seperator = "\t";
			String[] parts = line.split(seperator);
			
			//part 0 = solution number (1-11)
			//part 3 = org formula for example N 51 14.(a)(a+b)(e)
			String newFormula = appendLetters(parts[3], parts[0]);
			
			String newLine = parts[0] + seperator + parts[1] + seperator + parts[2] + seperator + newFormula;
			
			
			result.add(newLine);
		}
		
		return result;
	}
	
	private static String appendLetters(String formula, String appendix) {
		String result = ""; 

		boolean informula = false;
		String formula_part = "";
		char prev_letter = ' ';
		for(char letter: formula.toCharArray()) {
			if (letter == '.') {
				informula = true;
				result += letter;
				result += "\t=";
			} else if (informula) {
				if (letter == ' ') {
					informula = false;
					
					result += appendLettersInFormulaPart(formula_part, appendix);
					
					formula_part = "";
					
					result += "\t";
					result+=letter;
				} else if (letter == '('){
					if (prev_letter == ')') {
						formula_part += "\t=";
					}
					formula_part += letter;
				} else if (letter == ')'){
					formula_part += letter;
				} else if (informula && letter >= 'A' && letter <= 'M'){
					formula_part+=letter + appendix;
				} else {
					formula_part+=letter;
				}
			} else {
				result+=letter;
			}
			prev_letter = letter;
		}
		result+=appendLettersInFormulaPart(formula_part, appendix);
		
		return result;
	}

	private static String appendLettersInFormulaPart(String formula_part,
			String appendix) {
			String result = "";
			String[] parts = formula_part.split("\\)\\(");
			for (String part: parts) {
				for(char letter: part.toCharArray()) {
					
				}
		}
		return formula_part;
	}

	public static void main(String[] args) throws IOException {
		for (String line: convertFormulasToExcel()) {
			System.out.println(line);
		}
	}
	
}
