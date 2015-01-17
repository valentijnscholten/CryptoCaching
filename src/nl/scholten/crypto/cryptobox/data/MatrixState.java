package nl.scholten.crypto.cryptobox.data;

import java.util.ArrayList;
import java.util.List;

import nl.scholten.crypto.cryptobox.solver.OpsLogHelper;

import org.apache.commons.lang3.StringUtils;

public class MatrixState {

	public List<OperationInstance> opsLog;
	public CryptoBoxMatrix matrix;

	public long stepsLeft;
	protected long steps;
	public int score;
	
	public MatrixState(CryptoBoxMatrix m, long stepsLeft) {
		this.matrix = new CryptoBoxMatrix(m);
		this.steps = 0;
		this.stepsLeft = stepsLeft;
		this.opsLog = new ArrayList<OperationInstance>();
	}

	public MatrixState(MatrixState state2) {
		this.steps = state2.steps;
		this.stepsLeft = state2.stepsLeft;
		this.opsLog = new ArrayList<OperationInstance>(state2.opsLog);
		this.matrix = new CryptoBoxMatrix(state2.matrix);
		this.score = state2.score;
	}
	
	public String toString() {
		return StringUtils.leftPad(String.valueOf(score), 4) + " " + opsLog + "(" + OpsLogHelper.getChallengeDisplayString(opsLog) + ")" + " " + matrix.toString();
	}

	public String toStringPretty() {
		return StringUtils.leftPad(String.valueOf(score), 4) + "\n" + opsLog + "(" + OpsLogHelper.getChallengeDisplayString(opsLog) + ")" + "\n" + matrix.toStringPretty();
	}

	public void apply(List<OperationInstance> opsLog) {
		for (OperationInstance operationInstance: opsLog) {
			this.apply(operationInstance);
		}
	}

	public void apply(OperationInstance[] opsLog) {
		for (OperationInstance operationInstance: opsLog) {
			this.apply(operationInstance);
		}
	}

	public void apply(OperationInstance operationInstance) {
//		System.out.println("applying " + operationInstance + " opsLog is now: " + opsLog);
		
//		System.out.println("BEFORE:" + matrix.data);
		this.matrix.apply(operationInstance);
		this.opsLog.add(operationInstance);
		steps++;
		stepsLeft--;
//		System.out.println(" AFTER:" + matrix.data);
		
	}

	public void unapply(List<OperationInstance> opsLog) {
		this.unapply(opsLog.toArray(new OperationInstance[0]));
	}

	public void unapply(OperationInstance[] opsLog) {
		//unapply in reverse order!
		for (int i = opsLog.length - 1; i >= 0; i--) { 
			this.unapply(opsLog[i]);
		}
	}
	
	public void unapply(OperationInstance operationInstance) {
//		System.out.println("applying " + operationInstance + " opsLog is now: " + opsLog);
		
		this.matrix.unapply(operationInstance);
		opsLog.remove(opsLog.size() - 1);
		steps--;
		stepsLeft++;
	}
	
	@Override
	public boolean equals(Object state2) {
		if (state2 == null)
			return false;
		if (!(state2 instanceof MatrixState))
			return false;
		// for now only compare opsLog
		if (this.opsLog != ((MatrixState)state2).opsLog)
			return false;
		return true;
	}

	
}
