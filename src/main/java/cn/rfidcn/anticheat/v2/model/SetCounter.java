package cn.rfidcn.anticheat.v2.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

public class SetCounter<I>  extends Slot<I>{

	HashSet<I> set;
	
	public SetCounter(){
		set = new HashSet<I>();
	}
	
	@Override
	public void increase(I i) {
		System.out.println("***"+i);
		set.add(i);
	}


	@Override
	public void clear() {
		set.clear();
	}


	@Override
	public int sum(Countable<I>[] cs) {
		for(Countable c: cs){
			set.addAll((Collection<? extends I>) c.getContent());
		}
		return set.size();
	}


	@Override
	public String printString() {
		Iterator itr = set.iterator();
		StringBuilder sb = new StringBuilder();
		while(itr.hasNext()){
			sb.append(itr.next()).append(" ");
		}
		return sb.append("|").toString();
	}

	@Override
	public Object getContent() {
		return set;
	}
	
}
