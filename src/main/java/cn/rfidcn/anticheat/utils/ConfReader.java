package cn.rfidcn.anticheat.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Properties;

import org.apache.log4j.Logger;

public class ConfReader implements Serializable{
	
	static final Logger logger = Logger.getLogger(ConfReader.class);
	private Properties p;
	private volatile static ConfReader confReader;
	
	private ConfReader(){
		InputStream in = null;
		in = ClassLoader.getSystemResourceAsStream("config.properties");	
		p = new Properties();
		try {
			p.load(in);
		} catch (IOException e) {
			logger.error("IOException", e);
		}
	}
	
	public static ConfReader getConfReader(){
		if(confReader==null){
			 synchronized (ConfReader.class) {  
				  if (confReader == null) {  
				       confReader = new ConfReader();  
				  }  
			 }
		}
		return confReader;
	}
	
	public String getProperty(String key){
		return p.getProperty(key);
	}

}

