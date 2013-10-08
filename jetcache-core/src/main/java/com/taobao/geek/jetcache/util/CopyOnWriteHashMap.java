/**
 * Created on  13-09-26 16:02
 */
package com.taobao.geek.jetcache.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CopyOnWriteHashMap<K, V> {

    private HashMap<K, V> map;
    private Integer initCapability;
    private float loadFactor;

    public CopyOnWriteHashMap() {
        map = new HashMap<K, V>();
    }

    public CopyOnWriteHashMap(int initCapability, float loadFactor) {
        this.initCapability = initCapability;
        this.loadFactor = loadFactor;
        map = new HashMap<K, V>(initCapability, loadFactor);
    }

    public V get(K key) {
        return map.get(key);
    }

    public synchronized V put(K key, V value) {
        HashMap<K, V> copy = (HashMap<K, V>) map.clone();
        V v = copy.put(key, value);
        map = copy;
        return v;
    }

    public Set<Map.Entry<K, V>> entrySet() {
        return map.entrySet();
    }
}