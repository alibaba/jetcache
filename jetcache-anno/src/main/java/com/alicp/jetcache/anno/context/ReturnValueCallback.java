/**
 * Created on  13-09-21 23:18
 */
package com.alicp.jetcache.anno.context;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
@Deprecated
public interface ReturnValueCallback<T> {
    public T execute() throws Throwable;
}
