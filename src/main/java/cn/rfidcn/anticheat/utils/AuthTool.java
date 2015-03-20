package cn.rfidcn.anticheat.utils;

import java.io.UnsupportedEncodingException;

import cn.rfidcn.anticheat.SynchronizerConstConfig;

import com.toucha.factory.common.cache.service.CacheInit;
import com.toucha.factory.common.config.ApplicationConfig;
import com.toucha.factory.common.util.auth.AuthParam;
import com.toucha.factory.common.util.auth.AuthenticationUtil;
import com.toucha.factory.common.util.auth.SignWay;

public class AuthTool {

	public static void initAuthCache() throws UnsupportedEncodingException{
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
}
