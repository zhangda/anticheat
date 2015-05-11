package cn.rfidcn.anticheat.v2.ban;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.log4j.Logger;

import cn.rfidcn.anticheat.utils.HttpTool;

import com.toucha.factory.common.cache.service.CacheInit;
import com.toucha.factory.common.model.PlatformRequestHeader;
import com.toucha.factory.common.util.RandomUtil;

public class BanUsers<K> implements Banable<K> , Serializable{
	
    static final Logger logger = Logger.getLogger(BanUsers.class);
	
	public String url;
	
	public BanUsers(String url){
		this.url = url;
	}
	
	@Override
	public void doBan(List<K> ids) {
		try{
		 HttpClient httpclient = HttpTool.getNewHttpClient();
	     Map<String, Object> headerParamsMap = new HashMap<String, Object>();
	     headerParamsMap.put("authorization", "Bearer " + CacheInit.getCache().getAuthAccessToken().getAccessToken());
	     PlatformRequestHeader header = new PlatformRequestHeader();
	     header.setRequestId(RandomUtil.getRandomUUID());
	     header.setUserIp(InetAddress.getLocalHost().getHostAddress());
	     Map<String, Object> param = new HashMap<String, Object>();
	     param.put("users",ids);
	     HttpResponse res = HttpTool.getJSONByPostWithStringEntity(httpclient, url, param, headerParamsMap);
	     logger.info(res);
		}catch (Exception e){
			logger.error(e);
		}
	}

	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
}
