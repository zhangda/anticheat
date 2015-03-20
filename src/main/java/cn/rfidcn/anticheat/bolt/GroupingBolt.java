package cn.rfidcn.anticheat.bolt;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.log4j.Logger;

import backtype.storm.Config;
import backtype.storm.Constants;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import cn.rfidcn.anticheat.SynchronizerConstConfig;

import com.alibaba.fastjson.JSON;
import com.toucha.factory.common.cache.service.CacheInit;
import com.toucha.factory.common.config.ApplicationConfig;
import com.toucha.factory.common.model.PlatformRequestHeader;
import com.toucha.factory.common.util.RandomUtil;
import com.toucha.factory.common.util.auth.AuthParam;
import com.toucha.factory.common.util.auth.AuthenticationUtil;
import com.toucha.factory.common.util.auth.SignWay;

public class GroupingBolt extends BaseRichBolt{

	static final Logger logger = Logger.getLogger(GroupingBolt.class);
	
	private int threshold;
	private int emitFrequencyInSec;
	private int windowLengthInSec;
	private int size;
	private HashMap<String, HashMap<Integer,HashSet<String>>> map;
	 
	private OutputCollector collector;
	
	
	public GroupingBolt(int emitFrequencyInSec, int windowLengthInSec, int threshold){
		this.emitFrequencyInSec = emitFrequencyInSec;
		this.windowLengthInSec = windowLengthInSec;
		this.threshold = threshold;
		this.map = new HashMap<String,HashMap<Integer,HashSet<String>>>();
		this.size = windowLengthInSec / emitFrequencyInSec;
	}

	
	@Override
	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		this.collector = collector;
		try {
			init();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void execute(Tuple input) {
		if(isTickTuple(input)){
			logger.info("timer fires!!");
			doEmit();
		}else{
			doRecord(input);
		}
	}
	
	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("alertTid"));
		
	}
	
	
	@Override
	public Map<String, Object> getComponentConfiguration() {
		Map<String, Object> conf = new HashMap<String, Object>();
	    conf.put(Config.TOPOLOGY_TICK_TUPLE_FREQ_SECS, emitFrequencyInSec);
	    return conf;
	}
	

	private void doRecord(Tuple input){
		String oid = input.getString(0);
		String tid = input.getString(1);
		
		HashMap<Integer, HashSet<String>> slots = map.get(tid);
		if(slots==null){
			slots = new HashMap<Integer, HashSet<String>>();
			for(int i =1; i<=size; i++){
			   slots.put(i, new HashSet<String>());
		    }
			map.put(tid, slots);
		}
		
		slots.get(1).add(oid);
		logger.info("record: "+oid);
		logger.info("do record ************************ ");
		print();
		logger.info("   *************************** ");
		collector.ack(input);
	}

	private void doEmit(){
		logger.info("do emit =========================== ");
		print();
		logger.info("  =========================== ");
		Iterator itr = map.keySet().iterator();
		List<String> users =  new ArrayList<String>();
		while(itr.hasNext()){
			String tid = (String)itr.next();
			HashMap<Integer, HashSet<String>>  slots = map.get(tid);
			Set<String> all = new HashSet<String>();
			copySet(slots.get(1), all);
			
			for(int i=size; i>1;i--){
				addSet(slots.get(i), all);
				copySet(slots.get(i-1), slots.get(i));
			}
			if(all.size()>= threshold){
				collector.emit(new Values(tid));
				logger.info("emit >>>>>>>>  "+tid);
				users.add(tid);
			}
			slots.get(1).clear();
			logger.info("@@@@@@"+tid);
			print();
			logger.info("       =========================== ");
		}
		if(!users.isEmpty()){
			try {
				doBan("https://26859e06572b49b5a1467c80df93c52a.chinacloudapp.cn/config/banusers", users);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			users.clear();
		}
	}
	
	private boolean isTickTuple(Tuple tuple) {
	    return tuple.getSourceComponent().equals(Constants.SYSTEM_COMPONENT_ID) && tuple.getSourceStreamId().equals(
	        Constants.SYSTEM_TICK_STREAM_ID);
	  }


	private void print(){
	 Iterator itr1 = map.keySet().iterator();
	 while(itr1.hasNext()){
		String tid = (String)itr1.next();
		 HashMap<Integer, HashSet<String>>  slots = map.get(tid);
		for(int i=1; i<=size;i++){
			Set set = slots.get(i);
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
	private Set addSet(Set from, Set to){
		Iterator itr = from.iterator();
		while(itr.hasNext()){
			to.add(itr.next());
		}
		return to;
	}
	
	private Set copySet(Set from, Set to){
		to.clear();
		return addSet(from, to);
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private void init() throws UnsupportedEncodingException{
		AuthParam authParam = null;
	    authParam = new AuthParam();
	    authParam.setClientUserName(SynchronizerConstConfig.ClientUserName);
	    authParam.setClientPassWord(SynchronizerConstConfig.ClientPassWord);
	    authParam.setKeyStoreName(SynchronizerConstConfig.KeyStoreName);
	    authParam.setKeyStorePass(SynchronizerConstConfig.KeyStorePass);
	    authParam.setAlias(SynchronizerConstConfig.Alias);
	    authParam.setAliasPass(SynchronizerConstConfig.AliasPass);
	    authParam.setBasicHeader(AuthenticationUtil.encodeHeader(authParam.getClientUserName(), authParam.getClientPassWord()));
	    ApplicationConfig.setAuthParam(ApplicationConfig.AuthServerAccessTokenUrl, authParam, SignWay.SignWithKeyStoreFile);
	    CacheInit.initCache();
	}
	
	
	private void doBan(String url, List<String> ids) throws Exception{
		 HttpClient httpclient = getNewHttpClient();
	     Map<String, Object> headerParamsMap = new HashMap<String, Object>();
	     headerParamsMap.put("authorization", "Bearer " + CacheInit.getCache().getAuthAccessToken().getAccessToken());
	     PlatformRequestHeader header = new PlatformRequestHeader();
	     header.setRequestId(RandomUtil.getRandomUUID());
	     header.setUserIp(InetAddress.getLocalHost().getHostAddress());
	     Map<String, Object> param = new HashMap<String, Object>();
	     param.put("users",ids);
	     getJSONByPostWithStringEntity(httpclient, url, param, headerParamsMap);
	}



	private HttpResponse getJSONByPostWithStringEntity(HttpClient httpclient, String postUrl,
	        Map<String, Object> contentParamsMap, Map<String, Object> headerParamsMap) throws ClientProtocolException,
	        IOException {


	   String CONTENT_CHARSET = "utf-8";
	    String MIME_TYPE = "text/json";
	    
	    HttpResponse respInfo = null;

	    if (StringUtils.isNotBlank(postUrl)) {

	        HttpPost httpPost = new HttpPost(postUrl);

	        if (contentParamsMap != null && !contentParamsMap.isEmpty()) {
	            StringEntity entity = new StringEntity(JSON.toJSONString(contentParamsMap), MIME_TYPE, CONTENT_CHARSET);
	            // 为HttpPost设置实体数据
	            httpPost.setEntity(entity);
	        }

	        if (headerParamsMap != null && !headerParamsMap.isEmpty()) {
	            // 为HttpPost设置请求头
	            for (Map.Entry<String, Object> headerEntry : headerParamsMap.entrySet()) {
	                httpPost.addHeader(headerEntry.getKey(), headerEntry.getValue() != null ? headerEntry.getValue().toString()
	                        : "");
	            }
	        }

	        respInfo = httpclient.execute(httpPost);
	        System.out.println(respInfo);
	    }

	    return respInfo;
	}


	private HttpClient getNewHttpClient() throws Exception {
		KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());

		//不应该信任所有证书，应该验证SSL证书
		trustStore.load(null, null);
		SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
		sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

		SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("https", (SocketFactory) sf, 443));

		ClientConnectionManager ccm = new ThreadSafeClientConnManager(params,
				registry);

		HttpConnectionParams.setConnectionTimeout(params,
				5 * 1000);
		HttpConnectionParams.setSoTimeout(params, 20 * 1000);
		HttpClient client = new DefaultHttpClient(ccm, params);

		return client;
	}


	private class MySSLSocketFactory extends SSLSocketFactory {
		SSLContext sslContext = SSLContext.getInstance("TLS");

		public MySSLSocketFactory(KeyStore truststore)
				throws NoSuchAlgorithmException, KeyManagementException,
				KeyStoreException, UnrecoverableKeyException {
			super(truststore);

			TrustManager tm = new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] chain,
						String authType) throws CertificateException {
				}

				public void checkServerTrusted(X509Certificate[] chain,
						String authType) throws CertificateException {
				}

				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			};

			sslContext.init(null, new TrustManager[] { tm }, null);
		}

		@Override
		public Socket createSocket(Socket socket, String host, int port,
				boolean autoClose) throws IOException, UnknownHostException {
			return sslContext.getSocketFactory().createSocket(socket, host,
					port, autoClose);
		}

		@Override
		public Socket createSocket() throws IOException {
			return sslContext.getSocketFactory().createSocket();
		}

	}

	

	
}
