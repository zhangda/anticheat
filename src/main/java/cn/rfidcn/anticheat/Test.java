package cn.rfidcn.anticheat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import storm.kafka.BrokerHosts;
import storm.kafka.KafkaSpout;
import storm.kafka.SpoutConfig;
import storm.kafka.StringScheme;
import storm.kafka.ZkHosts;
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.spout.SchemeAsMultiScheme;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;
import cn.rfidcn.anticheat.bolt.TestBolt;
import cn.rfidcn.anticheat.utils.ConfReader;
import cn.rfidcn.anticheat.v2.ban.BanUsers;
import cn.rfidcn.anticheat.v2.bolt.GroupingBoltV2;
import cn.rfidcn.anticheat.v2.job.DetectJob;
import cn.rfidcn.anticheat.v2.model.IntegerCounter;
import cn.rfidcn.anticheat.v2.model.SetCounter;

public class Test {
	
     public static void main(String args[]) {	
		ConfReader confReader = ConfReader.getConfReader();
		
		BrokerHosts zk = new ZkHosts("192.168.8.104:2181");	 
		Config conf = new Config(); 
//		conf.put(Config.TOPOLOGY_TRIDENT_BATCH_EMIT_INTERVAL_MILLIS, Integer.parseInt(confReader.getProperty("emitTimeInt")));
		conf.put(Config.TOPOLOGY_WORKERS, 1);
//		conf.put(Config.TOPOLOGY_ACKER_EXECUTORS, Integer.parseInt(confReader.getProperty("num_workers")));
		
		
		SpoutConfig  appSpoutConf = new SpoutConfig(zk, "test2", "/test2",UUID.randomUUID().toString());
        appSpoutConf.fetchSizeBytes = 1 * 1024 * 1024;
        appSpoutConf.bufferSizeBytes = 1 * 1024 * 1024;
//        appSpoutConf.scheme = new SchemeAsMultiScheme(new AvroScheme());
        appSpoutConf.scheme = new SchemeAsMultiScheme(new StringScheme());
        
        KafkaSpout kafkaSpout = new KafkaSpout(appSpoutConf);
        
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("appspout", kafkaSpout, 1);
        builder.setBolt("filterBolt", new TestBolt(), 2).shuffleGrouping("appspout");
 
        List<DetectJob> jobs = new ArrayList<DetectJob>();
        DetectJob jobsuccess = new DetectJob();
        jobsuccess.setJobid(DetectJob.ID.taoId_too_many_success).setEmitFrequencyInSecAndWindowLengthInSec(10, 60)
        	.setThreshold(3).setCountableType(SetCounter.class).setBanable(new BanUsers("https://26859e06572b49b5a1467c80df93c52a.chinacloudapp.cn/config/banusers"));
        
        
    
        DetectJob jobfailure = new DetectJob();
        jobfailure.setJobid(DetectJob.ID.taoId_too_many_failure).setEmitFrequencyInSecAndWindowLengthInSec(15, 60)
        	.setThreshold(2).setCountableType(IntegerCounter.class).setBanable(new BanUsers("https://26859e06572b49b5a1467c80df93c52a.chinacloudapp.cn/config/banusers"));
       
        
        jobs.add(jobsuccess);
        jobs.add(jobfailure);
        
        builder.setBolt("groupingBolt", new GroupingBoltV2(jobs),1).fieldsGrouping("filterBolt", new Fields("key"));
        		
        
        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("anticheat-test", conf,  builder.createTopology());  
     
    }
}