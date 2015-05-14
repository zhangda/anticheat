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
		set.add(i);
	}


	@Override
	public void clear() {
		set.clear();
	}


	@Override
	public int sum(Slot<I>[] cs) {
		for(Slot c: cs){
			set.addAll((Collection<? extends I>) ((SetCounter)c).getSet());
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

	public HashSet<I> getSet() {
		return set;
	}

	public void setSet(HashSet<I> set) {
		this.set = set;
	}

	
	
}
