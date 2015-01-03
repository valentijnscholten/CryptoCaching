package nl.scholten.crypto.cryptobox.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nl.scholten.crypto.cryptobox.data.OPERATION;
import nl.scholten.crypto.cryptobox.data.OperationInstance;

public class HeapPermute<T> {

	private static <T> void swap(T[] v, int i, int j) {
		T t = v[i]; v[i] = v[j]; v[j] = t;
	}

	public static <T> void permute(T[] v, int n, Set<List<T>> result) {
		System.out.print("Calculating permutations: ");
		long start = System.currentTimeMillis();
		permuteInternal(v, n, result);
		System.out.println(System.currentTimeMillis() - start);
	}
	
	public static <T> void permuteInternal(T[] v, int n, Set<List<T>> result) {
		if (n == 1) {
//			System.out.println(Arrays.copyOf(v, v.length) + "--" + Arrays.toString(v));
			List<T> perm = Arrays.asList(Arrays.copyOf(v, v.length));
			result.add(perm);
		} else {
			for (int i = 0; i < n; i++) {
				permuteInternal(v, n-1, result);
				if (n % 2 == 1) {
					swap(v, 0, n-1);
				} else {
					swap(v, i, n-1);
				}
			}
		}
	}

	public static void main(String[] args) {
		Integer[] ns = {1, 2, 3, 4};
		
		Set<List<Integer>> result = new HashSet<List<Integer>>();
		HeapPermute.permute(ns, ns.length, result);
//		System.out.println(result);
		
		OperationInstance[] ois = {new OperationInstance(OPERATION.CU, 0), new OperationInstance(OPERATION.CU, 1), new OperationInstance(OPERATION.CU, 2), new OperationInstance(OPERATION.CU, 3)};
		
		Set<List<OperationInstance>> result2 = new HashSet<List<OperationInstance>>();
		HeapPermute.permute(ois, ois.length, result2);
//		System.out.println(result2);

	}

}

