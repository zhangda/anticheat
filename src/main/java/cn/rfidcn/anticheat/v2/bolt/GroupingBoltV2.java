package cn.rfidcn.anticheat.v2.bolt;

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
import cn.rfidcn.anticheat.bolt.FilterBolt;
import cn.rfidcn.anticheat.utils.AuthTool;
import cn.rfidcn.anticheat.utils.HttpTool;
import cn.rfidcn.anticheat.v2.job.DetectJob;

import com.toucha.factory.common.cache.service.CacheInit;
import com.toucha.factory.common.model.PlatformRequestHeader;
import com.toucha.factory.common.util.RandomUtil;

public class GroupingBoltV2 extends BaseRichBolt{

	static final Logger logger = Logger.getLogger(GroupingBoltV2.class);
	static final int BASE_EMIT_FREQ_IN_SEC = 5;
	
	static {
		try {
			AuthTool.initAuthCache();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	private OutputCollector collector;
	private List<DetectJob> jobs;
	
	public GroupingBoltV2(List<DetectJob> jobs){
		this.jobs = jobs;
	}
	
	@Override
	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		this.collector = collector;
	}

	@Override
	public void execute(Tuple input) {
		if(isTickTuple(input)){
			allDoTick();
			allDoEmit();
			allDoPrint();
		}else{
			logger.info("new event comming in...");
			allDoRecord((DetectJob.ID)input.getValue(0),input.getValue(1),input.getValue(2));
			allDoPrint();
		}
	}
	
	private void allDoRecord(DetectJob.ID jobid, Object key, Object value){
		for(DetectJob job: jobs){
			job.doRecord(jobid, key, value);
		}
	}
	
	private void allDoEmit(){
		for(DetectJob job: jobs){
			if(job.isToEmit())
				job.doEmit();
		}
	}

	private void allDoPrint(){
		for(DetectJob job: jobs){
			job.doPrint();
		}
	}
	
	private void allDoTick(){
		for(DetectJob job: jobs){
			job.doTick(BASE_EMIT_FREQ_IN_SEC);
		}
	}
	
	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields());
	}
	
	
	@Override
	public Map<String, Object> getComponentConfiguration() {
		Map<String, Object> conf = new HashMap<String, Object>();
	    conf.put(Config.TOPOLOGY_TICK_TUPLE_FREQ_SECS, BASE_EMIT_FREQ_IN_SEC);
	    return conf;
	}
	
	private boolean isTickTuple(Tuple tuple) {
	    return tuple.getSourceComponent().equals(Constants.SYSTEM_COMPONENT_ID) && tuple.getSourceStreamId().equals(
	        Constants.SYSTEM_TICK_STREAM_ID);
	  }

}
