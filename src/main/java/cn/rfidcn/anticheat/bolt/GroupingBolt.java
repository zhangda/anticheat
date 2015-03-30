package cn.rfidcn.anticheat.bolt;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.log4j.Logger;

import backtype.storm.Config;
import backtype.storm.Constants;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import cn.rfidcn.anticheat.utils.AuthTool;
import cn.rfidcn.anticheat.utils.HttpTool;

import com.toucha.factory.common.cache.service.CacheInit;
import com.toucha.factory.common.model.PlatformRequestHeader;
import com.toucha.factory.common.util.RandomUtil;

public class GroupingBolt extends BaseRichBolt{

	static final Logger logger = Logger.getLogger(GroupingBolt.class);
	
	private int threshold;
	private int emitFrequencyInSec;
	private int windowLengthInSec;
	private int size;
	private int current;
	private HashMap<String, HashSet<String>[]> map;
	private String banUrl;
	
	private int successWinLength;
	private int successSize;
	private int successThreshold;
	private int successCurrent;
	private HashMap<String, HashSet<String>[]> successMap;
	
	private OutputCollector collector;
	
	static {
		try {
			AuthTool.initAuthCache();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	
	public GroupingBolt(int emitFrequencyInSec, int windowLengthInSec, int threshold, int successWinLenth, int successThreshold, String banUrl){
		this.emitFrequencyInSec = emitFrequencyInSec;
		this.windowLengthInSec = windowLengthInSec;
		this.threshold = threshold;
		this.map = new HashMap<String,HashSet<String>[]>();
		this.size = windowLengthInSec / emitFrequencyInSec;
		this.current = 0;
		this.banUrl = banUrl;
		
		this.successWinLength = successWinLenth; //60*30;
		this.successSize = successWinLength / emitFrequencyInSec;
		this.successThreshold = successThreshold; //40;
		this.successCurrent = 0;
		this.successMap = new HashMap<String,HashSet<String>[]>();
	}

	
	@Override
	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		this.collector = collector;
	}

	@Override
	public void execute(Tuple input) {
		if(isTickTuple(input)){
			logger.info("time's up, find out bad guys!!!");
			doEmit();
		}else{
			logger.info("new event comming in...");
			doRecord(input);
		}
	}
	
	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields());
	}
	
	
	@Override
	public Map<String, Object> getComponentConfiguration() {
		Map<String, Object> conf = new HashMap<String, Object>();
	    conf.put(Config.TOPOLOGY_TICK_TUPLE_FREQ_SECS, emitFrequencyInSec);
	    return conf;
	}
	
	
	private void doRecord(String _oid, String _tid, HashMap<String, HashSet<String>[]> _map, int _size, int _current){
		HashSet<String>[] slots = _map.get(_tid);
		if(slots==null){
			slots = (HashSet<String>[]) Array.newInstance(HashSet.class, _size);
			for(int i=0; i<_size; i++){
			   slots[i]= new HashSet<String>();
		    }
			_map.put(_tid, slots);
		}
		_map.get(_tid)[_current].add(_oid);
		
		logger.info("new record ==> "+_oid);
		logger.info("************************");
		print(_map, _current, _size);
		logger.info("************************");
	}
	
	private void doRecord(Tuple input){
		String oid = input.getString(0);
		String tid = input.getString(1);
		int flag = input.getInteger(2);
		
		if(flag == FilterBolt.FAIL){
			doRecord(oid, tid, map, size, current);
		}else if (flag == FilterBolt.SUCCESS){
			doRecord(oid, tid, successMap, successSize, successCurrent);
		}else{
			logger.error("FLAG IS WRONG "+flag);
		}	
		collector.ack(input);
	}
	
	
	private void doEmit(HashMap<String, HashSet<String>[]> _map, int _size, int _current, int _threshold){

		Iterator itr = _map.keySet().iterator();
		List<String> users =  new ArrayList<String>();
		List<String> toClean = new ArrayList<String>();
		while(itr.hasNext()){
			String tid = (String)itr.next();
			HashSet<String>[]  slots = _map.get(tid);
			
			Set<String> all = new HashSet<String>();
			for(int i=0;i<_size;i++){
				all.addAll(slots[i]);
			}
			if(all.size()>= _threshold){
				// collector.emit(new Values(tid));
				logger.info("found bad gay, emit >>>>>>>>  "+tid);
				users.add(tid);
			}
			if(all.size()==0){
				toClean.add(tid);
			}
			_map.get(tid)[_current].clear();
	   }
		print(_map, _current, _size);
		logger.info("===========================");
	   
	   for(String tid:toClean){
		   _map.remove(tid);
	   }
	   
	   if(!users.isEmpty()){
			try {
				doBan(banUrl, users);
			} catch (Exception e) {
				e.printStackTrace();
		    }
	    }
	   
	}

	private void doEmit(){
		logger.info("the current window status... ");
		logger.info("===========================");
		print(map, current, size);
		logger.info("===========================");
		logger.info(".........................");
		print(successMap, successCurrent, successSize);
		logger.info(".........................");
		
		current = (current+1)%size;
		successCurrent = (successCurrent+1)%successSize;
		
		doEmit(map, size, current, threshold);
		doEmit(successMap, successSize, successCurrent, successThreshold);
		
	   }
	
	private boolean isTickTuple(Tuple tuple) {
	    return tuple.getSourceComponent().equals(Constants.SYSTEM_COMPONENT_ID) && tuple.getSourceStreamId().equals(
	        Constants.SYSTEM_TICK_STREAM_ID);
	  }

	
	private void print(HashMap<String, HashSet<String>[]> map, int current, int size){
	  Iterator mItr = map.keySet().iterator();
	  while(mItr.hasNext()){
		 String tid = (String)mItr.next();
	     logger.info("@@@@@@"+tid);
	     HashSet<String>[]  slots = map.get(tid);
		 for(int i=0; i<size;i++){
			if(i==current) System.out.print("--->");
			Set set = slots[i];
			Iterator itr = set.iterator();
			String s="";
			while(itr.hasNext()){
				s += itr.next()+" ";
			}
			System.out.print(s+"|");
		}
		System.out.println();
	 }	
	}
	
	
	private void doBan(String url, List<String> ids) throws Exception{
		 HttpClient httpclient = HttpTool.getNewHttpClient();
	     Map<String, Object> headerParamsMap = new HashMap<String, Object>();
	     headerParamsMap.put("authorization", "Bearer " + CacheInit.getCache().getAuthAccessToken().getAccessToken());
	     PlatformRequestHeader header = new PlatformRequestHeader();
	     header.setRequestId(RandomUtil.getRandomUUID());
	     header.setUserIp(InetAddress.getLocalHost().getHostAddress());
	     Map<String, Object> param = new HashMap<String, Object>();
	     param.put("users",ids);
	     HttpResponse res = HttpTool.getJSONByPostWithStringEntity(httpclient, url, param, headerParamsMap);
	     logger.info(res);
	}


	
}
