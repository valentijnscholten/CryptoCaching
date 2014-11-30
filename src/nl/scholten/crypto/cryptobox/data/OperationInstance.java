package nl.scholten.crypto.cryptobox.data;

public class OperationInstance {
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
}