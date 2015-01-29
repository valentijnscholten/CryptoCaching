package nl.scholten.crypto.cryptobox.data;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

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
	public boolean equals(Object object) {
		if (!(object instanceof OperationInstance)) return false;
		if (object == null) return false;
		OperationInstance oi = (OperationInstance)object;
				
		return this.op.equals(oi.op) && this.index == oi.index;
	}

	@Override
	public int compareTo(OperationInstance oi) {
		int opResult = op.compareTo(oi.op);
		
		if (opResult != 0) return opResult;

		return Integer.compare(index, oi.index);
	}

    public static boolean listEquals(List<OperationInstance> list1, List<OperationInstance> list2) {
        if (list2 == list1)
            return true;
        if (!(list2 instanceof List))
            return false;

        ListIterator<OperationInstance> e1 = list1.listIterator();
        ListIterator<OperationInstance> e2 = ((List) list2).listIterator();
        while (e1.hasNext() && e2.hasNext()) {
        	OperationInstance o1 = e1.next();
        	OperationInstance o2 = e2.next();
            System.out.println(o1 + " " + o2 );
            if (!(o1==null ? o2==null : o1.equals(o2)))
                return false;
        }
        return !(e1.hasNext() || e2.hasNext());
    }
	
	public static void main(String[] args) {

		System.out.println(new OperationInstance(OPERATION.CD, 1).equals(new OperationInstance(OPERATION.CD, 1)));
		
		List<OperationInstance> opsLog1 = new ArrayList<OperationInstance>();
		opsLog1.add(new OperationInstance(OPERATION.RR, 3));
		opsLog1.add(new OperationInstance(OPERATION.RL, 1));
		opsLog1.add(new OperationInstance(OPERATION.RR, 7));
		opsLog1.add(new OperationInstance(OPERATION.CU, 4));
		opsLog1.add(new OperationInstance(OPERATION.RL, 6));
		opsLog1.add(new OperationInstance(OPERATION.CD, 5));
		opsLog1.add(new OperationInstance(OPERATION.RR, 2));
		opsLog1.add(new OperationInstance(OPERATION.CU, 8));
		opsLog1.add(new OperationInstance(OPERATION.RL, 9));
		
		List<OperationInstance> opsLog2 = new ArrayList<OperationInstance>();
		opsLog2.add(new OperationInstance(OPERATION.RR, 3));
		opsLog2.add(new OperationInstance(OPERATION.RL, 1));
		opsLog2.add(new OperationInstance(OPERATION.RR, 7));
		opsLog2.add(new OperationInstance(OPERATION.CU, 4));
		opsLog2.add(new OperationInstance(OPERATION.RL, 6));
		opsLog2.add(new OperationInstance(OPERATION.CD, 5));
		opsLog2.add(new OperationInstance(OPERATION.RR, 2));
		opsLog2.add(new OperationInstance(OPERATION.CU, 8));
		opsLog2.add(new OperationInstance(OPERATION.RL, 9));
		
		System.out.println(opsLog1.equals(opsLog2));
		System.out.println(OperationInstance.listEquals(opsLog1, opsLog2));
	}
	
}