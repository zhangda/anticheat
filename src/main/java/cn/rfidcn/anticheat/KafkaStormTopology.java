package cn.rfidcn.anticheat;

import java.util.UUID;

import org.apache.log4j.Logger;

import storm.kafka.BrokerHosts;
import storm.kafka.KafkaSpout;
import storm.kafka.SpoutConfig;
import storm.kafka.ZkHosts;
import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.spout.SchemeAsMultiScheme;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;
import cn.rfidcn.anticheat.bolt.FilterBolt;
import cn.rfidcn.anticheat.bolt.GroupingBolt;
import cn.rfidcn.anticheat.scheme.AvroScheme;
import cn.rfidcn.anticheat.utils.ConfReader;

public class KafkaStormTopology {
	
	static final Logger logger = Logger.getLogger(KafkaStormTopology.class);
	
	public static void main(String args[]) {
		
		ConfReader confReader = ConfReader.getConfReader();
		
		BrokerHosts zk = new ZkHosts(confReader.getProperty("zkHosts"));	 
		Config conf = new Config(); 
//		conf.put(Config.TOPOLOGY_TRIDENT_BATCH_EMIT_INTERVAL_MILLIS, Integer.parseInt(confReader.getProperty("emitTimeInt")));
		conf.put(Config.TOPOLOGY_WORKERS, Integer.parseInt(confReader.getProperty("num_workers")));
//		conf.put(Config.TOPOLOGY_ACKER_EXECUTORS, Integer.parseInt(confReader.getProperty("num_workers")));
		
		
		SpoutConfig  appSpoutConf = new SpoutConfig(zk, confReader.getProperty("appeventTopic"), "/"+confReader.getProperty("appeventTopic"),UUID.randomUUID().toString());
        appSpoutConf.fetchSizeBytes = 5 * 1024 * 1024;
        appSpoutConf.bufferSizeBytes = 5 * 1024 * 1024;
        appSpoutConf.scheme = new SchemeAsMultiScheme(new AvroScheme());
        
        KafkaSpout kafkaSpout = new KafkaSpout(appSpoutConf);
        
      TopologyBuilder builder = new TopologyBuilder();
      builder.setSpout("appspout", kafkaSpout, 2);
      builder.setBolt("filterBolt", new FilterBolt(), 4).shuffleGrouping("appspout");
      builder.setBolt("groupingBolt", new GroupingBolt(1*60,5*60,5), 4).fieldsGrouping("filterBolt", new Fields("tid"));
          
        try {
			StormSubmitter.submitTopology("anticheat", conf, builder.createTopology());
		} catch (AlreadyAliveException e) {
			logger.error("AlreadyAliveException", e);
		} catch (InvalidTopologyException e) {
			logger.error("InvalidTopologyException", e);
		}
        
	}
	

}
