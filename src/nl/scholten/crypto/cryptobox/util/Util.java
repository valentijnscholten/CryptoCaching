package nl.scholten.crypto.cryptobox.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.scholten.crypto.cryptobox.data.OPERATION;
import nl.scholten.crypto.cryptobox.data.OperationInstance;

import org.apache.commons.lang3.Validate;

public class Util {

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByDescendingValue(
			Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			@Override
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		Map<K, V> result = new LinkedHashMap<>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
	
	
	public static <T> T[] permutation(long k, T[] opsLog) {
		T[] s = Arrays.copyOf(opsLog, opsLog.length);
		for(int j = 1; j < s.length; ++j) 
	    {
	        //swap(s, i, j) swaps position i and j of the string s.
			swap(s, (int) k % (j + 1), j); 
	        k = k / (j + 1);
	    }
		
		return s;
	}
	
	public static <T> void swap(T[] s, int i, int j) {
		T temp = s[i];
		s[i] = s[j];
		s[j] = temp;
	}
	
	/**
	 * returns true if list1 is a permutation of list2
	 * @param list1
	 * @param list2
	 * @return
	 */
	public static <T extends Comparable> boolean isPermutationOf(List<T> list1, List<T> list2) {
		Validate.notNull(list1);
		Validate.notNull(list2);
		if (list1.size() != list2.size()) return false;

		List<T> ordered1 = new LinkedList<>(list1);
		Collections.sort(ordered1);
		
		List<T> ordered2 = new LinkedList<>(list2);
		Collections.sort(ordered2);
		
//		System.out.println("1: " + ordered1);
//		System.out.println("2: " + ordered2);
		
		return ordered1.equals(ordered2);
	}
	
	public static <T> Set<Set<T>> subSet(Set<T> source, int segments) {
		int segmentSize = source.size() / segments;
		int leftOver = source.size() - segments * segmentSize;
		int currentSegmentLimit = segmentSize;
		if (leftOver > 0) {
			currentSegmentLimit++;
			leftOver--;
		}
		Set<Set<T>> subsets = new HashSet<Set<T>>();
		Set<T> subset = new HashSet<T>(); 
		for(T item: source) {
			subset.add(item);
			if (subset.size() == currentSegmentLimit) {

				subsets.add(subset);
				
				subset = new HashSet<T>();
				currentSegmentLimit = segmentSize;
				if (leftOver > 0) {
					currentSegmentLimit++;
					leftOver--;
				}
			}
		}
		return subsets;

	}
	
	public static boolean containsPermutationOf(List<OperationInstance> opsLog,
			Set<List<OperationInstance>> topScorersOpsLogs) {
		for (List<OperationInstance> topScorerOpsLog: topScorersOpsLogs) {
			if (isPermutationOf(opsLog, topScorerOpsLog)) {
				return true;
			}
		}
		return false;
	}	
	
	public static long doubleFactorial(int n) {
		Validate.isTrue(n >= -1);
		
		if (n == -1) return 1;
		if (n == 0) return 1;
				
		return n * doubleFactorial(n - 2);
	}
	
	public static String test(int i) {
		//N 51 [a].[i-x2][j-n-x2][n-z-x2] E 04 [b].[k-n][j-i+x1][j-k]
		return "N51 40." + (i-2) + "70 E04 24.8"  + (17-i+1) + "1";
	}
	
	public static void main(String[] args) {
		
		Set<OperationInstance> set1 = new HashSet<OperationInstance>();
		set1.add(new OperationInstance(OPERATION.RL, 1));
		set1.add(new OperationInstance(OPERATION.RL, 2));
		set1.add(new OperationInstance(OPERATION.RL, 3));
		set1.add(new OperationInstance(OPERATION.RL, 4));
		set1.add(new OperationInstance(OPERATION.RL, 5));
		set1.add(new OperationInstance(OPERATION.RL, 6));
		set1.add(new OperationInstance(OPERATION.RL, 7));
		set1.add(new OperationInstance(OPERATION.RL, 8));
		set1.add(new OperationInstance(OPERATION.RL, 9));
		set1.add(new OperationInstance(OPERATION.RL, 10));
		set1.add(new OperationInstance(OPERATION.RL, 11));
		set1.add(new OperationInstance(OPERATION.RL, 12));
		
		System.out.println(subSet(set1, 130));
		
		List<OperationInstance> opsLog1 = new LinkedList<OperationInstance>();
		opsLog1.add(new OperationInstance(OPERATION.RL, 1));
		opsLog1.add(new OperationInstance(OPERATION.RL, 2));
		opsLog1.add(new OperationInstance(OPERATION.RL, 3));
		opsLog1.add(new OperationInstance(OPERATION.RL, 4));
		opsLog1.add(new OperationInstance(OPERATION.RL, 1));
		opsLog1.add(new OperationInstance(OPERATION.RL, 2));
		opsLog1.add(new OperationInstance(OPERATION.RL, 3));
		opsLog1.add(new OperationInstance(OPERATION.RL, 4));
		opsLog1.add(new OperationInstance(OPERATION.RL, 1));
		opsLog1.add(new OperationInstance(OPERATION.RL, 2));
		opsLog1.add(new OperationInstance(OPERATION.RL, 3));
		opsLog1.add(new OperationInstance(OPERATION.RL, 4));
		
		
		
		System.out.print("Calculating permutations: ");
		long start = System.currentTimeMillis();

//		Set<List<OperationInstance>> perms = new HashSet<>();
//		for (int k = 0; k < CombinatoricsUtils.factorial(opsLog1.size()); k++) {
//			OperationInstance[] perm = permutation(k, (OperationInstance[])opsLog1.toArray()); 
//			System.out.println(k + ": " + perm + " isPerm: " + isPermutationOf(perm, opsLog1));
//			System.out.println(k + ": " + perm);
//		}

//		System.out.println(System.currentTimeMillis() - start);

		
		List<OperationInstance> opsLog2 = new LinkedList<OperationInstance>();
		opsLog2.add(new OperationInstance(OPERATION.RL, 1));
		opsLog2.add(new OperationInstance(OPERATION.RL, 2));
		opsLog2.add(new OperationInstance(OPERATION.RL, 3));
		opsLog2.add(new OperationInstance(OPERATION.RL, 5));
		opsLog2.add(new OperationInstance(OPERATION.RL, 4));
		opsLog2.add(new OperationInstance(OPERATION.RR, 5));
		opsLog2.add(new OperationInstance(OPERATION.CD, 4));
		opsLog2.add(new OperationInstance(OPERATION.RR, 5));

		System.out.println(opsLog1 + " permOf " + opsLog2 + " :" + isPermutationOf(opsLog1, opsLog2));
		
		List<OperationInstance> opsLog3 = new LinkedList<OperationInstance>();
		opsLog3.add(new OperationInstance(OPERATION.CU, 6));
		opsLog3.add(new OperationInstance(OPERATION.RL, 1));
		opsLog3.add(new OperationInstance(OPERATION.CD, 0));
		opsLog3.add(new OperationInstance(OPERATION.CU, 5));
		opsLog3.add(new OperationInstance(OPERATION.RL, 4));
		opsLog3.add(new OperationInstance(OPERATION.RR, 5));
		opsLog3.add(new OperationInstance(OPERATION.CD, 4));
		opsLog3.add(new OperationInstance(OPERATION.RR, 5));

		System.out.println(opsLog3);
		Collections.sort(opsLog3);
		System.out.println(opsLog3);
		
		
	}



	
}
