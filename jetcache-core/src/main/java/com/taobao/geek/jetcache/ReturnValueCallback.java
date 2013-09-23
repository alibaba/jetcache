/**
 * Created on  13-09-21 23:18
 */
package com.taobao.geek.jetcache;

/**
 * @author yeli.hl
 */
public interface ReturnValueCallback<T> {
    public T execute() throws Throwable;
}
