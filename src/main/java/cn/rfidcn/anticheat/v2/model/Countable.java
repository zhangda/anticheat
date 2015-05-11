package cn.rfidcn.anticheat.v2.model;

public interface Countable<I> {
	
	public void increase(I i);
	
	public int sum(Countable<I>[] cs);

	public void clear();
	
	public String printString();
	
	public Object getContent();
	
}
