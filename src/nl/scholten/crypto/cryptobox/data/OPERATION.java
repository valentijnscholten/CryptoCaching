package nl.scholten.crypto.cryptobox.data;

public enum OPERATION {
	CD(false, false, "CD", 0), CU(false, true, "CU", 1), RL(true, false, "RL", 2), RR(
			true, true, "RR", 3);

	public boolean isRow;
	public boolean isPositive;
	public String value;
	public int ordinal;

	private OPERATION(boolean isRow, boolean isPositive, String value, int ordinal) {
		this.isRow = isRow;
		this.isPositive = isPositive;
		this.value = value;
		this.ordinal = ordinal;
	}

	public static OPERATION parseValue(String value) {
		for (OPERATION op : OPERATION.values()) {
			if (op.getValue().equals(value))
				return op;
		}
		return null;
	}

	public String getValue() {
		return this.value;
	}

	public int getIndex() {
		return ordinal;
	}
	
	public String toString() {
		return getValue();
	}
	
	public static OPERATION fromIndex(int ordinal) {
		for(OPERATION op: OPERATION.values()) {
			if (op.ordinal == ordinal)
				return op;
		}
		return null;
	}
	
}