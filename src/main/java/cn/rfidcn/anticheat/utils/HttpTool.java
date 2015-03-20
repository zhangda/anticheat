package cn.rfidcn.anticheat.utils;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

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

import com.alibaba.fastjson.JSON;

public class HttpTool {
	
	public static HttpResponse getJSONByPostWithStringEntity(HttpClient httpclient, String postUrl,
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


	public static HttpClient getNewHttpClient() throws Exception {
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


	public static class MySSLSocketFactory extends SSLSocketFactory {
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
