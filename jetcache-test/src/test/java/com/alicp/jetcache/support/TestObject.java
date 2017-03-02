package com.alicp.jetcache.support;

import java.math.BigDecimal;
import java.util.Map;
import java.io.Serializable;

/**
 * Created on 2016/10/8.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class TestObject implements Serializable {

    private static final long serialVersionUID = -2630291351236065621L;
    private long id;
    private String name;
    private String email;
    private Object data;

    private Map<String, BigDecimal> map;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Map<String, BigDecimal> getMap() {
        return map;
    }

    public void setMap(Map<String, BigDecimal> map) {
        this.map = map;
    }
}
