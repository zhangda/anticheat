package cn.rfidcn.anticheat.v2.job;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import cn.rfidcn.anticheat.v2.ban.Banable;
import cn.rfidcn.anticheat.v2.model.Slot;

public class DetectJob<K, V>  implements Serializable{
	
	public enum ID { taoId_too_many_failure, taoId_too_many_success }
	
	static final Logger logger = Logger.getLogger(DetectJob.class);
	
	private HashMap<K, Slot[]> map;
	private int threshold;
	private int emitFrequencyInSec;
	private int windowLengthInSec;
	private int size;
	private int current;
	private Banable banable;
	private ID jobid;
	private Class countableType;
	private int currentTick;
	
	
	public void doRecord(ID jobid, K key, V value){
		if(this.jobid != jobid) return;		
		Slot[] slots = map.get(key);
		if(slots == null){
			slots = new Slot[size];
			for(int i=0; i<size; i++){
			   try {
				slots[i] = (Slot)countableType.newInstance();
			   } catch (InstantiationException | IllegalAccessException e) {
				   e.printStackTrace();
			   }
		    }
			map.put(key, slots);
		}
		map.get(key)[current].increase(value);
	}
	
	public void doEmit(){
		current = (current+1)%size;
		Iterator<K> itr = map.keySet().iterator();
		List<K> toBan = new ArrayList<K>();
		List<K> toClean = new ArrayList<K>();
		while(itr.hasNext()){
			K key =itr.next();
			Slot[]  slots = map.get(key);
			int count = slots[current].sum(slots);
			if(count>= threshold){
				System.out.println("found bad gay, emit >>>>>>>>  "+key);
				toBan.add(key);
			}
			if(count==0){
				toClean.add(key);
			}
			map.get(key)[current].clear();
		}
		if(toBan.size()>0) 
			banable.doBan(toBan);
	    for(K k:toClean){
		   map.remove(k);
	    }
	}
	
	
	public void doPrint(){
		  Iterator<K> mItr = map.keySet().iterator();
		  while(mItr.hasNext()){
			 K key = mItr.next();
		     System.out.println("@@@@@@" + key);
		     Slot[]  slots = map.get(key);
			 for(int i=0; i<size;i++){
				if(i==current) System.out.print("--->");
				System.out.println(slots[i].printString());
			 }
		  }
	}


	public DetectJob(){
		this.current = 0;
		this.map = new HashMap<K, Slot[]>();
		this.currentTick = 0;
	}
	
	
	public void doTick(int baseImitFreq){
		this.currentTick +=  baseImitFreq;
	}
	
	public boolean isToEmit(){
		if (currentTick == emitFrequencyInSec){
			currentTick = 0;
			logger.info("time to find out the bad guys!!");
			return true;
		}else{
			logger.info("not yet to emit!!");
			return false;
		}
	}
	
	public DetectJob setThreshold(int threshold) {
		this.threshold = threshold;
		return this;
	}

	
	public DetectJob setEmitFrequencyInSecAndWindowLengthInSec(int emitFrequencyInSec,int windowLengthInSec ) {
		this.emitFrequencyInSec = emitFrequencyInSec;
		this.windowLengthInSec = windowLengthInSec;
		this.size = windowLengthInSec / emitFrequencyInSec;	
		if(windowLengthInSec<emitFrequencyInSec || windowLengthInSec % emitFrequencyInSec>0){
			logger.error("window length and emit frequency are not good values!!!!");
		}
		return this;
	}

	public DetectJob setBanable(Banable banable) {
		this.banable = banable;
		return this;
	}


	public DetectJob setJobid(ID jobid) {
		this.jobid = jobid;
		return this;
	}
	

	public DetectJob setCountableType(Class countableType) {
		this.countableType = countableType;
		return this;
	}
	
	
}
