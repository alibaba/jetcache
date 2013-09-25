/**
 * Created on  13-09-25 09:29
 */
package com.taobao.geek.jetcache.tair;

/**
 * @author yeli.hl
 */
public class TairValue {
    long e;
    Object v;

    public TairValue() {
    }

    public TairValue(long expireTime, Object value) {
        this.e = expireTime;
        this.v = value;
    }

    public long getE() {
        return e;
    }

    public void setE(long e) {
        this.e = e;
    }

    public Object getV() {
        return v;
    }

    public void setV(Object v) {
        this.v = v;
    }
}
