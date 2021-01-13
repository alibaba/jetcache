package com.alicp.jetcache.autoconfigure;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created on 2017/11/20.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class ConfigTree {
    private ConfigurableEnvironment environment;
    private String prefix;

    public ConfigTree(ConfigurableEnvironment environment, String prefix) {
        Assert.notNull(environment, "environment is required");
        Assert.notNull(prefix, "prefix is required");
        this.environment = environment;
        this.prefix = prefix;
    }

    public ConfigTree subTree(String prefix) {
        return new ConfigTree(environment, fullPrefixOrKey(prefix));
    }

    private String fullPrefixOrKey(String prefixOrKey) {
        return this.prefix + prefixOrKey;
    }

    public Map<String, Object> getProperties() {
        Map<String, Object> m = new HashMap<>();
        for (PropertySource<?> source : environment.getPropertySources()) {
            if (source instanceof EnumerablePropertySource) {
                for (String name : ((EnumerablePropertySource<?>) source)
                        .getPropertyNames()) {
                    if (name != null && name.startsWith(prefix)) {
                        String subKey = name.substring(prefix.length());
                        m.put(subKey, environment.getProperty(name));
                    }
                }
            }
        }
        return m;
    }

    public boolean containsProperty(String key) {
        key = fullPrefixOrKey(key);
        return environment.containsProperty(key);
    }

    public <T> T getProperty(String key) {
        key = fullPrefixOrKey(key);
        return (T) environment.getProperty(key);
    }

    public <T> T getProperty(String key, T defaultValue) {
        if (containsProperty(key)) {
            return getProperty(key);
        } else {
            return defaultValue;
        }
    }

    public String getPrefix() {
        return prefix;
    }

    public Set<String> directChildrenKeys() {
        Map<String, Object> m = getProperties();
        return m.keySet().stream().map(
                s -> s.indexOf('.') >= 0 ? s.substring(0, s.indexOf('.')) : null)
                .filter(s -> s != null)
                .collect(Collectors.toSet());
    }
}
