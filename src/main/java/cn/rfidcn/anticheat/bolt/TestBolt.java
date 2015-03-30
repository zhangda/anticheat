package cn.rfidcn.anticheat.bolt;

import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;

import storm.trident.operation.TridentCollector;
import storm.trident.tuple.TridentTuple;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import cn.rfidcn.anticheat.model.AppSysLogEvent;

import com.alibaba.fastjson.JSON;

public class TestBolt  extends BaseRichBolt {

	static final Logger logger = Logger.getLogger(TestBolt.class);
	private OutputCollector collector;

	@Override
	public void prepare(Map stormConf, TopologyContext context,
			OutputCollector collector) {
		this.collector = collector;
	}

	@Override
	public void execute(Tuple input) {
		//oid,tid,flag
		String s = input.getString(0).trim();
		String ss[] = s.split(",");
		collector.emit(new Values(ss[0],ss[1],Integer.parseInt(ss[2])));
		logger.info("==="+s);
		collector.ack(input);
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("oid", "tid", "flag"));
		
	}
	

}
