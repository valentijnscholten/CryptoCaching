package nl.scholten.crypto.cryptobox.solver;

import java.util.ArrayList;
import java.util.List;

import nl.scholten.crypto.cryptobox.data.CryptoBoxMatrix;
import nl.scholten.crypto.cryptobox.data.OperationInstance;

public class MatrixState {

	public List<OperationInstance> oisLeft;
	public ArrayList<OperationInstance> opsLog;

	public CryptoBoxMatrix matrix;

	private int stepsLeft;
	private long tries;
	private int maxScore;
	
	protected long foundTime;

	public MatrixState(CryptoBoxMatrix m, int stepsLeft) {
		this.matrix = m;
		this.stepsLeft = stepsLeft;
	}

	public String toString() {
		return matrix.toString() + " " + opsLog;
	}
	
}
