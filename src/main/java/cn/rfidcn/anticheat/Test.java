//package cn.rfidcn.anticheat;
//
//import java.util.UUID;
//
//import storm.kafka.BrokerHosts;
//import storm.kafka.KafkaSpout;
//import storm.kafka.SpoutConfig;
//import storm.kafka.StringScheme;
//import storm.kafka.ZkHosts;
//import backtype.storm.Config;
//import backtype.storm.LocalCluster;
//import backtype.storm.spout.SchemeAsMultiScheme;
//import backtype.storm.topology.TopologyBuilder;
//import backtype.storm.tuple.Fields;
//import cn.rfidcn.anticheat.bolt.GroupingBolt;
//import cn.rfidcn.anticheat.bolt.TestBolt;
//import cn.rfidcn.anticheat.utils.ConfReader;
//
//public class Test {
//	
//	
//public static void main(String args[]) {
//		
//		ConfReader confReader = ConfReader.getConfReader();
//		
//		BrokerHosts zk = new ZkHosts("192.168.8.104:2181");	 
//		Config conf = new Config(); 
////		conf.put(Config.TOPOLOGY_TRIDENT_BATCH_EMIT_INTERVAL_MILLIS, Integer.parseInt(confReader.getProperty("emitTimeInt")));
//		conf.put(Config.TOPOLOGY_WORKERS, 1);
////		conf.put(Config.TOPOLOGY_ACKER_EXECUTORS, Integer.parseInt(confReader.getProperty("num_workers")));
//		
//		
//		SpoutConfig  appSpoutConf = new SpoutConfig(zk, "test", "/test",UUID.randomUUID().toString());
//        appSpoutConf.fetchSizeBytes = 5 * 1024 * 1024;
//        appSpoutConf.bufferSizeBytes = 5 * 1024 * 1024;
////        appSpoutConf.scheme = new SchemeAsMultiScheme(new AvroScheme());
//        appSpoutConf.scheme = new SchemeAsMultiScheme(new StringScheme());
//        
//        KafkaSpout kafkaSpout = new KafkaSpout(appSpoutConf);
//        
//        TopologyBuilder builder = new TopologyBuilder();
//        builder.setSpout("appspout", kafkaSpout, 1);
//        builder.setBolt("filterBolt", new TestBolt(), 2).shuffleGrouping("appspout");
//        builder.setBolt("groupingBolt", new GroupingBolt(5,20,2,40,3, "https://26859e06572b49b5a1467c80df93c52a.chinacloudapp.cn/config/banusers"), 1)
//        								.fieldsGrouping("filterBolt", new Fields("tid"));
//       
//        LocalCluster cluster = new LocalCluster();
//        cluster.submitTopology("anticheat-test", conf,  builder.createTopology());  
//     
//	
//	
////	try {
////		init();
////	
////	List<String> users = new ArrayList();
////	users.add("1");
////	doBan("https://tp-test-edge.chinacloudapp.cn/config/banusers", users);
////	} catch (Exception e) {
////		// TODO Auto-generated catch block
////		e.printStackTrace();
////	}
//}
//        
//
//
//
////private static void init() throws UnsupportedEncodingException{
////	AuthParam authParam = null;
////    authParam = new AuthParam();
////    authParam.setClientUserName(SynchronizerConstConfig.ClientUserName);
////    authParam.setClientPassWord(SynchronizerConstConfig.ClientPassWord);
////    authParam.setKeyStoreName(SynchronizerConstConfig.KeyStoreName);
////    authParam.setKeyStorePass(SynchronizerConstConfig.KeyStorePass);
////    authParam.setAlias(SynchronizerConstConfig.Alias);
////    authParam.setAliasPass(SynchronizerConstConfig.AliasPass);
////    authParam.setBasicHeader(AuthenticationUtil.encodeHeader(authParam.getClientUserName(), authParam.getClientPassWord()));
////    ApplicationConfig.setAuthParam(ApplicationConfig.AuthServerAccessTokenUrl, authParam, SignWay.SignWithKeyStoreFile);
////    System.out.println(ApplicationConfig.AuthServerAccessTokenUrl);
////    CacheInit.initCache();
////}
////
////
////public static void doBan(String url, List<String> ids) throws Exception{
////	 HttpClient httpclient = getNewHttpClient();
////     Map<String, Object> headerParamsMap = new HashMap<String, Object>();
////     headerParamsMap.put("authorization", "Bearer " + CacheInit.getCache().getAuthAccessToken().getAccessToken());
////     PlatformRequestHeader header = new PlatformRequestHeader();
//////     header.setRequestId(RandomUtil.getRandomUUID());
//////     header.setUserIp(InetAddress.getLocalHost().getHostAddress());
////     Map<String, Object> param = new HashMap<String, Object>();
////     param.put("users",ids);
//////     param.put(SynchronizerConstConfig.REQ_HEADER_KEY, header);
////     getJSONByPostWithStringEntity(httpclient, url, param, headerParamsMap);
////}
////
////
////
////public static HttpResponse getJSONByPostWithStringEntity(HttpClient httpclient, String postUrl,
////        Map<String, Object> contentParamsMap, Map<String, Object> headerParamsMap) throws ClientProtocolException,
////        IOException {
////
////
////   String CONTENT_CHARSET = "utf-8";
////    String MIME_TYPE = "text/json";
////    
////    HttpResponse respInfo = null;
////
////    if (StringUtils.isNotBlank(postUrl)) {
////
////        HttpPost httpPost = new HttpPost(postUrl);
////
////        if (contentParamsMap != null && !contentParamsMap.isEmpty()) {
////            StringEntity entity = new StringEntity(JSON.toJSONString(contentParamsMap), MIME_TYPE, CONTENT_CHARSET);
////            // 为HttpPost设置实体数据
////            httpPost.setEntity(entity);
////        }
////
////        if (headerParamsMap != null && !headerParamsMap.isEmpty()) {
////            // 为HttpPost设置请求头
////            for (Map.Entry<String, Object> headerEntry : headerParamsMap.entrySet()) {
////                httpPost.addHeader(headerEntry.getKey(), headerEntry.getValue() != null ? headerEntry.getValue().toString()
////                        : "");
////            }
////        }
////
////        System.out.println(headerParamsMap.values());
////        respInfo = httpclient.execute(httpPost);
////        System.out.println(respInfo);
////    }
////
////    return respInfo;
////}
////
////
////private static HttpClient getNewHttpClient() throws Exception {
////	KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
////
////	//不应该信任所有证书，应该验证SSL证书
////	trustStore.load(null, null);
////	SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
////	sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
////
////	HttpParams params = new BasicHttpParams();
////	HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
////	HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
////
////	SchemeRegistry registry = new SchemeRegistry();
////	registry.register(new Scheme("https", (SocketFactory) sf, 443));
////
////	ClientConnectionManager ccm = new ThreadSafeClientConnManager(params,
////			registry);
////
////	HttpConnectionParams.setConnectionTimeout(params,
////			5 * 1000);
////	HttpConnectionParams.setSoTimeout(params, 20 * 1000);
////	HttpClient client = new DefaultHttpClient(ccm, params);
////
////	return client;
////}
////
////
////private static class MySSLSocketFactory extends SSLSocketFactory {
////	SSLContext sslContext = SSLContext.getInstance("TLS");
////
////	public MySSLSocketFactory(KeyStore truststore)
////			throws NoSuchAlgorithmException, KeyManagementException,
////			KeyStoreException, UnrecoverableKeyException {
////		super(truststore);
////
////		TrustManager tm = new X509TrustManager() {
////			public void checkClientTrusted(X509Certificate[] chain,
////					String authType) throws CertificateException {
////			}
////
////			public void checkServerTrusted(X509Certificate[] chain,
////					String authType) throws CertificateException {
////			}
////
////			public X509Certificate[] getAcceptedIssuers() {
////				return null;
////			}
////		};
////
////		sslContext.init(null, new TrustManager[] { tm }, null);
////	}
////
////	@Override
////	public Socket createSocket(Socket socket, String host, int port,
////			boolean autoClose) throws IOException, UnknownHostException {
////		return sslContext.getSocketFactory().createSocket(socket, host,
////				port, autoClose);
////	}
////
////	@Override
////	public Socket createSocket() throws IOException {
////		return sslContext.getSocketFactory().createSocket();
////	}
////
////}
//
//
//}
