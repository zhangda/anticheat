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
	
	public static int SUCCESS =0;
	public static int FAIL= 1;
	
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

			Integer hae = event.getHae();
			if(hae!=null && (hae == 8100 || hae ==8111 || hae == 8115 || hae == 8700) ){
				String msg = event.getMsg();
				String oid= null;
				String tid= null;
				Pattern p = Pattern.compile("hid:\\[\\[(.*)\\]\\].*tbUid:\\[\\[(.*)\\]\\]");
				Matcher m = p.matcher(msg);
				while(m.find()){
					oid = m.group(1);
					tid = m.group(2);
				}
				logger.info("oid: "+oid+" tid: "+tid);
				if(oid!=null && tid!=null && !oid.trim().equals("") && !tid.trim().equals("")){
					if(hae == 8700){
						collector.emit(new Values(oid, tid, SUCCESS));
					}else{
						collector.emit(new Values(oid, tid, FAIL));
					}
				}
			}
		}
		collector.ack(input);
		
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		 declarer.declare(new Fields("oid", "tid", "flag"));
	}

}
