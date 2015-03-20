package cn.rfidcn.anticheat.bolt;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import cn.rfidcn.anticheat.model.ManagementEvent;

import com.alibaba.fastjson.JSON;

public class FilterBolt extends BaseRichBolt {

	static final Logger logger = Logger.getLogger(FilterBolt.class);
	
	private OutputCollector collector;
	 
	@Override
	public void prepare(Map stormConf, TopologyContext context,
			OutputCollector collector) {
		this.collector = collector;
	}

	@Override
	public void execute(Tuple input) {
		List<Object> appJsonList = (List)input.getValue(0);
		for(Object obj : appJsonList){
			ManagementEvent event = new ManagementEvent();
			try {
				BeanUtils.copyProperties(event,obj);
			} catch (IllegalAccessException | InvocationTargetException e) {
				e.printStackTrace();
			}
			
			String json = JSON.toJSONString(obj);
			logger.info(json);
			if((int)event.getHae()==8100 || (int)event.getHae()==8111 || (int)event.getHae()==8115){
				String msg = event.getMsg();
				String oid=null;
				String tid=null;
				Pattern p = Pattern.compile("hid:\\[\\[(.*)\\]\\].*tbUid:\\[\\[(.*)\\]\\]");
				Matcher m = p.matcher(msg);
				while(m.find()){
					oid = m.group(1);
					tid = m.group(2);
				}
				logger.info("oid: "+oid+" tid: "+tid);
				if(oid!=null && tid!=null){
					collector.emit(new Values(oid, tid));
				}
			}
		}
		collector.ack(input);
		
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		 declarer.declare(new Fields("oid", "tid"));
	}

}