package nl.scholten.crypto.cryptobox.data;

/**
 * Performance notes:
 * - making startswith a hit helps, 10x performance/
 * - making startswith known hit mandatory helps most, doubling performance
 * - doing no score helps, doubling performance again.
 * - doing no apply does help, but not a lot (20%).
 * - fork join with SERIAL_STEPS == 3 == size-11 helps 400% 
 * - hype threading does help about 50%.
 * 
 * 
 * 
 * 
 * - all cu/cd exclusive -> order doesn't matter
 * [CU_9, CU_6, CU_5, CD_1], 
 * [CU_9, CU_6, CD_1, CU_5]
 * - also if first (or last?) operation is rl/rr but constant, and rest cu/cd exclusive:
 * 	[CU_8, CD_1, CU_1, RL_4], 
 *	[CU_8, CD_0, CU_0, RL_4], 
 * - also vice versa rl/rr exclusive, also with other parts constant
 * - rr compensate rl and cu compensate cl if index is equal and exclusive
 * - rr direct after rl same index does nothing
 * 
 * 
 * 973714 unique keys out of 40^4 = 2560000  = 0,38 = 38%, so over 60% should be dropped by optimize
 * 
 * - use intern to filter duplicates. helps a bit serially, but no gain with FJ.
 * - maintain hashmap with scores, slows it down a lot.
 * - score by scan from start is very slow
 * - use indexof instead of countmatches speeds up 200%
 * - leave out keys with more than 2 equal oi adjacent save little <1% at 4 steps or 5 steps
 * - make sure all segments of adjacent row or segments of adjacent col are in ascending order as the order doesn't matter -> saves 40% at 4 steps.
 * 
 * 
 * 
 * Faster string search:
 * BoyerMooreHorspool 10x slower on single pattern match.
 * BNDM also 10x slower
 * shiftormismatch even slower and bad results.
 * plain java regex is 10 times slower!
 * 
 */import java.lang.reflect.Field;
import java.util.List;


@SuppressWarnings("serial")
public class CryptoBoxMatrix {

	public String data;
	private char[] dataArray;

	public int size;

	public CryptoBoxMatrix(String input, int size) {
		init(size, 
				input);
	}
	
	public void init(	int size, 
						String data) {
		this.size = size;

		// deep copy
		this.data = new String(data.toCharArray());
		// hacketiy hack to get access to the char[] as well.
		Field field;
		try {
			field = this.data.getClass().getDeclaredField("value");
			field.setAccessible(true);
			this.dataArray = ((char[]) field.get(this.data));
		} catch (NoSuchFieldException | SecurityException
				| IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException();
		}
	}
	
	public CryptoBoxMatrix(CryptoBoxMatrix m) {
		this.init(m.size, 
				m.data);
	}

	public String toString() {
		return data;
	}

	public String toStringPretty() {
		StringBuilder result = new StringBuilder();
		String[] rows = data.split("(?<=\\G.{" + size + "})");

		for (String row : rows) {
			if (result.length() > 0)
				result.append("\n");
			result.append(row);
		}
		return result.toString();
	}
	
	// TODO deduplicate shifts
	/**
	 * x = row, y = col
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public void setXY(int x, int y, char c) {
		dataArray[(x * size) + y] = c;
	}

	/**
	 * x = row, y = col
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public char getXY(int x, int y) {
		return dataArray[(x * size) + y];
	}

	public void shiftColumnUp(int column) {
		char temp = getXY(0, column);

		for (int row = 0; row < size - 1; row++) {
			setXY(row, column, getXY(row + 1, column));
		}

		setXY(size - 1, column, temp);
	}

	public void shiftColumnDown(int column) {
		char temp = getXY(size - 1, column);

		for (int row = size - 1; row > 0; row--) {
			setXY(row, column, getXY(row - 1, column));
		}

		setXY(0, column, temp);
	}

	public void shiftRowLeft(int row) {
		char temp = getXY(row, 0);

		System.arraycopy(dataArray, (row * size) + 1, dataArray, (row * size),
				(size - 1));

		setXY(row, size - 1, temp);

	}

	public void shiftRowRight(int row) {
		char temp = getXY(row, size - 1);

		System.arraycopy(dataArray, (row * size), dataArray, (row * size) + 1,
				(size - 1));

		setXY(row, 0, temp);

	}

	public void apply(OperationInstance oi) {
		switch (oi.op) {

		case CU:
			shiftColumnUp(oi.index);
			break;
		case CD:
			shiftColumnDown(oi.index);
			break;
		case RL:
			shiftRowLeft(oi.index);
			break;
		case RR:
			shiftRowRight(oi.index);
			break;

		default:
			throw new IllegalStateException("Unknown operation " + oi.op);
		}
	}

	public void unapply(OperationInstance oi) {

		switch (oi.op) {

		case CU:
			shiftColumnDown(oi.index);
			break;
		case CD:
			shiftColumnUp(oi.index);
			break;
		case RL:
			shiftRowRight(oi.index);
			break;
		case RR:
			shiftRowLeft(oi.index);
			break;

		default:
			throw new IllegalStateException("Unknown operation " + oi.op);
		}
	}

	public void apply(List<OperationInstance> headStart) {
		for (OperationInstance oi : headStart) {
			apply(oi);
		}
	}
	
	public String getRow(int i) {
		return data.substring(i * size, (i * size) + size);
	}

	public static String toStringSideBySide(CryptoBoxMatrix... matrices) {

		if (matrices.length == 0)
			return "";
		StringBuilder result = new StringBuilder();

		for (int row = 0; row < matrices[0].size; row++) {
			for (CryptoBoxMatrix m : matrices) {
				
				if (result.length() > 0
						&& result.charAt(result.length() - 1) != '\n')
					result.append("\t");
				result.append(m.getRow(row));
			}
			result.append("\n");
		}
		return result.toString();
	}

	@Override
	public boolean equals(Object matrix2) {
		if (matrix2 == null)
			return false;
		if (!(matrix2 instanceof CryptoBoxMatrix))
			return false;
		CryptoBoxMatrix other = (CryptoBoxMatrix) matrix2;
		return this.data.equals(other.data) && this.size == other.size;
	}
	
}
