package nl.scholten.crypto.cryptobox.data;


public class OperationInstance implements Comparable<OperationInstance>{
	public OPERATION op;
	public int index;

	public OperationInstance(OPERATION op, int index) {
		this.op = op;
		this.index = index;
	}

	public String toString() {
		return op.toString() + "_" + index;
	}

	// TODO not used?
	public boolean equals(OperationInstance oi) {
		return this.op == oi.op && this.index == oi.index;
	}

	@Override
	public int compareTo(OperationInstance oi) {
		int opResult = op.compareTo(oi.op);
		
		if (opResult != 0) return opResult;

		return Integer.compare(index, oi.index);
		
	}
	
	
}