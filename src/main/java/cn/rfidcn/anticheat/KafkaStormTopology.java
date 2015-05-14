package cn.rfidcn.anticheat;

import java.util.ArrayList;
import java.util.List;
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
import cn.rfidcn.anticheat.bolt.FilterBoltV2;
import cn.rfidcn.anticheat.scheme.AvroScheme;
import cn.rfidcn.anticheat.utils.ConfReader;
import cn.rfidcn.anticheat.v2.ban.BanUsers;
import cn.rfidcn.anticheat.v2.ban.Banable;
import cn.rfidcn.anticheat.v2.bolt.GroupingBoltV2;
import cn.rfidcn.anticheat.v2.job.DetectJob;
import cn.rfidcn.anticheat.v2.model.IntegerCounter;
import cn.rfidcn.anticheat.v2.model.SetCounter;

public class KafkaStormTopology {
	
	static final Logger logger = Logger.getLogger(KafkaStormTopology.class);
	
	public static void main(String args[]) {
		
		ConfReader confReader = ConfReader.getConfReader();
		
		BrokerHosts zk = new ZkHosts(confReader.getProperty("zkHosts"));	 
		Config conf = new Config(); 
		conf.put(Config.TOPOLOGY_WORKERS, Integer.parseInt(confReader.getProperty("num_workers")));
		SpoutConfig  appSpoutConf = new SpoutConfig(zk, confReader.getProperty("appeventTopic"), "/"+confReader.getProperty("appeventTopic"), UUID.randomUUID().toString());
        appSpoutConf.fetchSizeBytes = 5 * 1024 * 1024;
        appSpoutConf.bufferSizeBytes = 5 * 1024 * 1024;
        appSpoutConf.scheme = new SchemeAsMultiScheme(new AvroScheme());
        
        KafkaSpout kafkaSpout = new KafkaSpout(appSpoutConf);
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("appspout", kafkaSpout, Integer.parseInt(confReader.getProperty("num_spouts")));
        builder.setBolt("filterBolt", new FilterBoltV2(), Integer.parseInt(confReader.getProperty("num_bolts"))).shuffleGrouping("appspout");
        
        Banable banUser = new BanUsers(confReader.getProperty("banUserUrl"));
        List<DetectJob> jobs = new ArrayList<DetectJob>();
        DetectJob jobsuccess = new DetectJob();
        jobsuccess.setJobid(DetectJob.ID.taoId_too_many_success).setEmitFrequencyInSecAndWindowLengthInSec(10, 60)
        	.setThreshold(3).setCountableType(SetCounter.class).setBanable(banUser);   
        DetectJob jobfailure = new DetectJob();
        jobfailure.setJobid(DetectJob.ID.taoId_too_many_failure).setEmitFrequencyInSecAndWindowLengthInSec(15, 60)
        	.setThreshold(2).setCountableType(IntegerCounter.class).setBanable(banUser);  
        jobs.add(jobsuccess);
        jobs.add(jobfailure);
        
        builder.setBolt("groupingBolt", new GroupingBoltV2(jobs),Integer.parseInt(confReader.getProperty("num_bolts"))).fieldsGrouping("filterBolt", new Fields("key"));

        try {
			StormSubmitter.submitTopology("anticheat", conf, builder.createTopology());
		} catch (AlreadyAliveException e) {
			logger.error("AlreadyAliveException", e);
		} catch (InvalidTopologyException e) {
			logger.error("InvalidTopologyException", e);
		}
        
	}
	

}
