/**
 * Created on  13-09-23 12:08
 */
package com.alicp.jetcache.test.support;

import java.io.Serializable;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class DynamicQuery implements Serializable {
    private static final long serialVersionUID = 2446193386760839620L;
    private long id;
    private String name;
    private String email;

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
}
