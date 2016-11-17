/**
 * Created on  13-09-24 14:32
 */
package com.alicp.jetcache.anno.method;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public interface Invoker {
    Object invoke() throws Throwable;
}
