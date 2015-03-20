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

    // AuthServer configure
    public static final String KeyStoreName = setting.getValue("KeyStoreName");
    public static final String KeyStorePass = setting.getValue("KeyStorePass");
    public static final String Alias = setting.getValue("Alias");
    public static final String AliasPass = setting.getValue("AliasPass");
    public static final String ClientUserName = setting.getValue("ClientUserName");
    public static final String ClientPassWord = setting.getValue("ClientPassWord");

}
