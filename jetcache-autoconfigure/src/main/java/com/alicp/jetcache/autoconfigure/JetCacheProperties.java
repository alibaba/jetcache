package com.alicp.jetcache.autoconfigure;

import com.alicp.jetcache.support.DefaultCacheMonitorManager;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.function.Consumer;

/**
 * Created on 2016/11/23.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
@ConfigurationProperties(prefix = "jetcache")
public class JetCacheProperties {

    private String[] hidePackages;
    private int statIntervalMinutes;
    private Consumer<DefaultCacheMonitorManager.StatInfo> statCallback;

    public JetCacheProperties(){
    }

    public String[] getHidePackages() {
        return hidePackages;
    }

    public void setHidePackages(String[] hidePackages) {
        this.hidePackages = hidePackages;
    }

    public int getStatIntervalMinutes() {
        return statIntervalMinutes;
    }

    public void setStatIntervalMinutes(int statIntervalMinutes) {
        this.statIntervalMinutes = statIntervalMinutes;
    }

    public Consumer<DefaultCacheMonitorManager.StatInfo> getStatCallback() {
        return statCallback;
    }

    public void setStatCallback(Consumer<DefaultCacheMonitorManager.StatInfo> statCallback) {
        this.statCallback = statCallback;
    }
}
