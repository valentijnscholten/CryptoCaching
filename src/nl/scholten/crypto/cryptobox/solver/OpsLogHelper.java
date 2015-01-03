package nl.scholten.crypto.cryptobox.solver;

import java.util.List;

import nl.scholten.crypto.cryptobox.data.OperationInstance;

public class OpsLogHelper {

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

}
