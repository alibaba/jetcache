/**
 * Created on  13-10-17 22:34
 */
package com.alicp.jetcache.embedded;

import org.checkerframework.checker.units.qual.K;

import java.util.Collection;
import java.util.Map;
import java.util.SortedMap;

/**
 * @author huangli
 */
public interface ExtraInnerMap extends InnerMap {

    SortedMap<Object, Object> prefixMap(final Object key);
}
