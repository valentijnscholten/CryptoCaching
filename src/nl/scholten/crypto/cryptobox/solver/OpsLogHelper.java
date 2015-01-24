package nl.scholten.crypto.cryptobox.solver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.scholten.crypto.cryptobox.data.OPERATION;
import nl.scholten.crypto.cryptobox.data.OperationInstance;

public class OpsLogHelper {

	public static Map<String, String> challengeValues = new HashMap<String, String>();
	static {
		challengeValues.put(new OperationInstance(OPERATION.RL, 0).toString().toString(), "0L");
		challengeValues.put(new OperationInstance(OPERATION.RL, 1).toString(), "1L");
		challengeValues.put(new OperationInstance(OPERATION.RL, 2).toString(), "2L");
		challengeValues.put(new OperationInstance(OPERATION.RL, 3).toString(), "3L");
		challengeValues.put(new OperationInstance(OPERATION.RL, 4).toString(), "4L");
		challengeValues.put(new OperationInstance(OPERATION.RL, 5).toString(), "5L");
		challengeValues.put(new OperationInstance(OPERATION.RL, 6).toString(), "6L");
		challengeValues.put(new OperationInstance(OPERATION.RL, 7).toString(), "7L");
		challengeValues.put(new OperationInstance(OPERATION.RL, 8).toString(), "8L");
		challengeValues.put(new OperationInstance(OPERATION.RL, 9).toString(), "9L");

		challengeValues.put(new OperationInstance(OPERATION.RR, 0).toString(), "0R");
		challengeValues.put(new OperationInstance(OPERATION.RR, 1).toString(), "1R");
		challengeValues.put(new OperationInstance(OPERATION.RR, 2).toString(), "2R");
		challengeValues.put(new OperationInstance(OPERATION.RR, 3).toString(), "3R");
		challengeValues.put(new OperationInstance(OPERATION.RR, 4).toString(), "4R");
		challengeValues.put(new OperationInstance(OPERATION.RR, 5).toString(), "5R");
		challengeValues.put(new OperationInstance(OPERATION.RR, 6).toString(), "6R");
		challengeValues.put(new OperationInstance(OPERATION.RR, 7).toString(), "7R");
		challengeValues.put(new OperationInstance(OPERATION.RR, 8).toString(), "8R");
		challengeValues.put(new OperationInstance(OPERATION.RR, 9).toString(), "9R");
		
		challengeValues.put(new OperationInstance(OPERATION.CU, 0).toString(), "AU");
		challengeValues.put(new OperationInstance(OPERATION.CU, 1).toString(), "BU");
		challengeValues.put(new OperationInstance(OPERATION.CU, 2).toString(), "CU");
		challengeValues.put(new OperationInstance(OPERATION.CU, 3).toString(), "DU");
		challengeValues.put(new OperationInstance(OPERATION.CU, 4).toString(), "EU");
		challengeValues.put(new OperationInstance(OPERATION.CU, 5).toString(), "FU");
		challengeValues.put(new OperationInstance(OPERATION.CU, 6).toString(), "GU");
		challengeValues.put(new OperationInstance(OPERATION.CU, 7).toString(), "HU");
		challengeValues.put(new OperationInstance(OPERATION.CU, 8).toString(), "IU");
		challengeValues.put(new OperationInstance(OPERATION.CU, 9).toString(), "JU");
		
		challengeValues.put(new OperationInstance(OPERATION.CD, 0).toString(), "AD");
		challengeValues.put(new OperationInstance(OPERATION.CD, 1).toString(), "BD");
		challengeValues.put(new OperationInstance(OPERATION.CD, 2).toString(), "CD");
		challengeValues.put(new OperationInstance(OPERATION.CD, 3).toString(), "DD");
		challengeValues.put(new OperationInstance(OPERATION.CD, 4).toString(), "ED");
		challengeValues.put(new OperationInstance(OPERATION.CD, 5).toString(), "FD");
		challengeValues.put(new OperationInstance(OPERATION.CD, 6).toString(), "GD");
		challengeValues.put(new OperationInstance(OPERATION.CD, 7).toString(), "HD");
		challengeValues.put(new OperationInstance(OPERATION.CD, 8).toString(), "ID");
		challengeValues.put(new OperationInstance(OPERATION.CD, 9).toString(), "JD");
		
	}
	
	public static boolean isMeaningfull(OperationInstance[] perm) {
		for (int i = 0; i < perm.length; i++) {
			OperationInstance prevOIA = null;
			OperationInstance prevOIB = null;

			if (i > 0) prevOIA = perm[i - 1];
			if (i > 1) prevOIB = perm[i - 2];

			if (!isMeaningfull(prevOIA, prevOIB, perm[i])) {
				return false;
			}
		}
		return true;
	}

	public static boolean isMeaningfull(OperationInstance prevOIA,
			OperationInstance prevOIB, OperationInstance oi) {

		// no more than 2 of the same oi adjacent
//		if (true || prevOIA != prevOIB || (oi != prevOIA || oi != prevOIB)) {
		if (prevOIA != prevOIB || (oi != prevOIA || oi != prevOIB)) {

			if ( // make sure all segments of the same type (row/col) are in
					// ascending order. Order doesn't matter, so only
					// calculate for those with ascending order)
			(prevOIA == null) // no previous OI, so always go.
					|| (oi.op.isRow != prevOIA.op.isRow) // row after col or
															// col after row
															// always go
					|| (oi.op.isRow == prevOIA.op.isRow && oi.index > prevOIA.index) // row
																						// after
																						// row
																						// should
																						// ascend,
																						// same
																						// for
																						// col
																						// after
																						// col.
																						// leaves
																						// only
																						// 43%
																						// of
																						// tries!
					|| (oi.op.isRow == prevOIA.op.isRow
							&& oi.index == prevOIA.index && (oi.op.isPositive == prevOIA.op.isPositive)) // CU_0
																											// CD_0
																											// not
																											// useful
																											// as
																											// they
																											// compensate
																											// eachother.
																											// leaves
																											// 39%
																											// of
																											// tries

			) {
				return true;
			} else {
				return false;
			}

		} else {
			return false;
		}

	}

	public static boolean isMeaningfull(List<OperationInstance> opsLog) {
		return isMeaningfull(opsLog.toArray(new OperationInstance[0]));
	}

	public static String getChallengeDisplayString(List<OperationInstance> opsLog) {
		String result = "";
		for (OperationInstance oi: opsLog) {
			result += challengeValues.get(oi.toString());
		}
		return result;
	}

	
	//will shift the operation by shiftSize steps.
	// CD_0 with shiftSize 4 becomes CU_4
	// CD_0 with shiftSize 14 and matrix size 10 becomes CU_4
	// RR_0 with shiftSize 34 and matrix size 10 becomes CD_4
	public static OperationInstance shift(OperationInstance oi,
			int shiftSize, int matrixSize) {
		int nextIndex = oi.index + shiftSize;
		
		int nextOpIndex = oi.op.index + nextIndex / (matrixSize);
		nextIndex = nextIndex % (matrixSize);
		
		return new OperationInstance(OPERATION.fromIndex(nextOpIndex % 4), nextIndex);
	}

	public static void main(String[] args) {
		System.out.println(shift(new OperationInstance(OPERATION.CD, 0), 4, 10));
		System.out.println(shift(new OperationInstance(OPERATION.CD, 0), 14, 10));
		System.out.println(shift(new OperationInstance(OPERATION.RR, 0), 34, 10));
		System.out.println(shift(new OperationInstance(OPERATION.RR, 0), 94, 10));
	}
	
	
}
