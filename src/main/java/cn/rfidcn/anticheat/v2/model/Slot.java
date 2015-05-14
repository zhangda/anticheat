package cn.rfidcn.anticheat.v2.model;

public abstract class Slot<I>{

	public void increase(I i){}

	public int sum(Slot<I>[] cs) { return 0;}

	public void clear() {}

	
	public String printString() {return null;}


}
