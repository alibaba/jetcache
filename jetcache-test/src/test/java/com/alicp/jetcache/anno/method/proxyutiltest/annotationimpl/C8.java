package com.alicp.jetcache.anno.method.proxyutiltest.annotationimpl;

import com.alicp.jetcache.anno.method.interfaces.I8;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class C8 implements I8 {
    int count;
    Map<String, Integer> m = new HashMap<>();

    @Override
    public int count(String id) {
        Integer v = m.get(id);
        if (v == null) {
            v = count++;
        }
        v++;
        m.put(id, v);
        return v;
    }

    @Override
    public void update(String theId, int value) {
        m.put(theId, value);
    }

    @Override
    public void delete(String theId) {
        m.remove(theId);
    }

    @Override
    public void update2(String theId, int value) {
        m.put(theId, value);
    }

    @Override
    public void delete2(String theId) {
        m.remove(theId);
    }

    @Override
    public int randomUpdate(String id) {
        return new Random().nextInt();
    }

    @Override
    public int randomUpdate2(String id) {
        return new Random().nextInt();
    }
}