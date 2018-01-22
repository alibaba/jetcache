/**
 * Created on 2018/1/22.
 */
package com.alicp.jetcache.anno.support;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class CacheAnnoConfig {
    private String area;
    private String name;
    private String key;
    private String condition;

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }
}
