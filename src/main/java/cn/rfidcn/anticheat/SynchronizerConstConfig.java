package cn.rfidcn.anticheat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.toucha.factory.common.util.SettingsHelper;

public class SynchronizerConstConfig {

    private static SettingsHelper setting = null;
    private static Logger logger = LoggerFactory.getLogger(SynchronizerConstConfig.class);
    static {
        String path = "/synchronizer-const.properties";
        setting = new SettingsHelper();
        try {
            setting.load(path);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    // json key
    public static final String RESP_STATUS_KEY = "respStatus"; // 返回状态码键
    public static final String REQ_HEADER_KEY = "header";
    public static final String PRODUCTLIST_KEY = "list"; // 产品列表键
    public static final String DISTRIBUTORLIST_KEY = "data"; // 经销商列表键


    public static final String PCODE_KEY = "pcode"; // 省级编码
    public static final String CCODE_KEY = "ccode"; // 市级编码
    public static final String XCODE_KEY = "xcode"; // 县级编码

    // AuthServer configure
    public static final String KeyStoreName = setting.getValue("KeyStoreName");
    public static final String KeyStorePass = setting.getValue("KeyStorePass");
    public static final String Alias = setting.getValue("Alias");
    public static final String AliasPass = setting.getValue("AliasPass");
    public static final String ClientUserName = setting.getValue("ClientUserName");
    public static final String ClientPassWord = setting.getValue("ClientPassWord");

}
