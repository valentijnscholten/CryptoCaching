package nl.scholten.crypto.cryptobox.data;

import java.util.ArrayList;
import java.util.List;

import nl.scholten.crypto.cryptobox.solver.OpsLogHelper;

import org.apache.commons.lang3.StringUtils;

public class MatrixState implements Comparable<MatrixState> {

	public List<OperationInstance> opsLog;
	public CryptoBoxMatrix matrix;

	public long stepsLeft;
	protected long steps;
	public int score;
	public long generation;
	
	public MatrixState(CryptoBoxMatrix m, long stepsLeft) {
		this.matrix = new CryptoBoxMatrix(m);
		this.steps = 0;
		this.stepsLeft = stepsLeft;
		this.opsLog = new ArrayList<OperationInstance>();
		this.generation = 0;
	}

	public MatrixState(MatrixState state2) {
		this.steps = state2.steps;
		this.stepsLeft = state2.stepsLeft;
		this.opsLog = new ArrayList<OperationInstance>(state2.opsLog);
		this.matrix = new CryptoBoxMatrix(state2.matrix);
		this.score = state2.score;
		this.generation = state2.generation;
	}
	
	public String toString() {
		return StringUtils.leftPad(String.valueOf(score), 4) + " " + StringUtils.leftPad(String.valueOf(steps), 3) + " " + opsLog + "(" + OpsLogHelper.getChallengeDisplayString(opsLog) + ")" + " " + matrix.toString();
	}

	public String toStringPretty() {
		return StringUtils.leftPad(String.valueOf(score), 4) + "\n" + StringUtils.leftPad(String.valueOf(steps), 3) + " " + opsLog + "(" + OpsLogHelper.getChallengeDisplayString(opsLog) + ")" + "\n" + matrix.toStringPretty();
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
		if (!this.opsLog.equals(((MatrixState)state2).opsLog))
			return false;
		return true;
	}

	public void copyFrom(MatrixState newState) {
		this.matrix = new CryptoBoxMatrix(newState.matrix);
		this.opsLog = new ArrayList<OperationInstance>(newState.opsLog);
		this.score = newState.score;
		this.steps = newState.steps;
		this.stepsLeft = newState.stepsLeft;
		this.generation = newState.generation;
	}

	@Override
	public int compareTo(MatrixState o) {
		return new Integer(score).compareTo(o.score);
	}

	
}
