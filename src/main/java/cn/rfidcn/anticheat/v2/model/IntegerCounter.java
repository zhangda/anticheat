package cn.rfidcn.anticheat.v2.model;



public class IntegerCounter<I>  extends Slot<I> {

	int count;
	
	public IntegerCounter(){
		count = 0;
	}
	
	@Override
	public void increase(I i) {
		count++;
	}

	@Override
	public int sum(Slot<I>[] cs) {
		int c = 0;
		for(Slot ct: cs){
			c += ((IntegerCounter)ct).getCount();
		}
		return c;
	}

	@Override
	public void clear() {
		count = 0;
		
	}

	@Override
	public String printString() {
		return count+"|";
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
	
	

}
