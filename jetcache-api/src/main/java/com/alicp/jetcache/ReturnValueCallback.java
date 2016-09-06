/**
 * Created on  13-09-21 23:18
 */
package com.alicp.jetcache;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public interface ReturnValueCallback<T> {
    public T execute() throws Throwable;
}
