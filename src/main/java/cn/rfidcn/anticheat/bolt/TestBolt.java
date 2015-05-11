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
import cn.rfidcn.anticheat.v2.job.DetectJob;

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
		//flag,oid,tid
		String s = input.getString(0).trim();
		System.out.println("=================>"+s);
		String ss[] = s.split(",");
		if(ss[0].equals("1")){
			collector.emit(new Values(DetectJob.ID.taoId_too_many_failure,ss[1],ss[2]));
		}else{
			collector.emit(new Values(DetectJob.ID.taoId_too_many_success, ss[1],ss[2]));
		}
		collector.ack(input);
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("oid", "tid", "flag"));
		
	}
	

}
