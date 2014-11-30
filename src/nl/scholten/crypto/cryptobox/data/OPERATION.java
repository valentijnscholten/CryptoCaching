package nl.scholten.crypto.cryptobox.data;

public enum OPERATION {
	CU(false, true, "CU"), CD(false, false, "CD"), RL(true, false, "RL"), RR(
			true, true, "RR");

	public boolean isRow;
	public boolean isPositive;
	public String value;

	private OPERATION(boolean isRow, boolean isPositive, String value) {
		this.isRow = isRow;
		this.isPositive = isPositive;
		this.value = value;
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

	public String toString() {
		return getValue();
	}
}