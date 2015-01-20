/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mn.le.farcek.jbw.code;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import mn.le.farcek.jbw.api.IBundle;
import mn.le.farcek.jbw.api.IConfig;

/**
 *
 * @author Farcek
 */
public abstract class JBWConfig implements IConfig {

    private String configDir;

    @Override
    public final String getConfigDir() {
        if (configDir == null) {

            String homeDir = System.getProperty("user.home");
            if (homeDir == null)
                throw new RuntimeException("not defined System property `user.home`");

            configDir = System.getProperty("user.home") + File.separator + "." + getApplicationKey();

        }
        return configDir;
    }

    private Properties conf;

    private Properties confProperties() {
        if (conf == null) {
            conf = new Properties();
            try {
                conf.load(new FileReader(getConfigDir() + File.separator + "app.config"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return conf;
    }

    @Override
    public String getSecureToken() {
        return confProperties().getProperty("secureToken");
    }

    // --
    private File dirOfResource;

    @Override
    public File getDirOfResource() {
        if (dirOfResource == null)
            dirOfResource = new File(confProperties().getProperty("dirOfResource"));

        return dirOfResource;
    }

    // --
    private File tempDirOfResource;

    @Override
    public File getTempDirOfResource() {
        if (tempDirOfResource == null) {            
            tempDirOfResource = new File(getDirOfResource(), getTempDirName());
        }

        return tempDirOfResource;
    }
    // --
    private String tempDirName;

    @Override
    public String getTempDirName() {
        if (tempDirName == null) {
            tempDirName= confProperties().getProperty("tempDirOfResource", "_temp");
        }
        return tempDirName;
    }

    // ---
    private String webDomain;

    @Override
    public String getWebDomain() {
        if (webDomain == null) {
            webDomain = confProperties().getProperty("webDomain");
            if (webDomain == null)
                webDomain = "";
            else if (webDomain.endsWith("/"))
                webDomain = webDomain.substring(0, webDomain.length() - 1);
        }
        return webDomain;
    }

    @Override
    public String getContextPath() {
        return confProperties().getProperty("contextPath");
    }

    @Override
    public String getLoginPage() {
        return confProperties().getProperty("loginPage", "/login.html");
    }

    private Boolean debug;

    @Override
    public boolean isDebug() {
        if (debug == null)
            debug = Boolean.valueOf(confProperties().getProperty("debug", "false"));
        return debug;
    }

    private String buildVersion;

    public String getBuildVersion() {
        if (buildVersion == null) {
            buildVersion = confProperties().getProperty("build.version");
            if (buildVersion == null) buildVersion = "0";
        }
        return buildVersion;
    }

    private Locale locale;

    @Override
    public Locale getDefaultLocale() {
        if (locale == null) {
            String l = confProperties().getProperty("locale", null);
            if (l == null)
                locale = Locale.ENGLISH;
            else
                locale = new Locale(l);
        }
        return locale;
    }

    // --- 
    private Boolean templateCache;

    @Override
    public boolean isTemplateCache() {
        if (templateCache == null)
            templateCache = Boolean.valueOf(confProperties().getProperty("templateCache", "false"));
        return templateCache;
    }

    @Override
    public String getPathOfBundleHandler() {
        return confProperties().getProperty("pathOfBundleHandler", "/pages");
    }

    @Override
    public String getPathOfAssetHandler() {

        return confProperties().getProperty("pathOfAssetHandler", "/static-assets");
    }

    @Override
    public String getPathOfResourceHandler() {
        return confProperties().getProperty("pathOfResourceHandler", "/resources");
    }

    @Override
    public String getPathOfImageResourceHandler() {
        return confProperties().getProperty("pathOfImageResourceHandler", "/resource-images");
    }

    private List<Class<? extends IBundle>> bundles;

    @Override
    public List<Class<? extends IBundle>> getBundles() {
        if (bundles == null)
            setupBundles(bundles = new ArrayList<>());

        return bundles;
    }

    protected abstract void setupBundles(List<Class<? extends IBundle>> bundles);

}
