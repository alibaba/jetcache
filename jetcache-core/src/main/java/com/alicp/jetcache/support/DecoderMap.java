/**
 * Created on 2018/3/20.
 */
package com.alicp.jetcache.support;

import com.alicp.jetcache.anno.SerialPolicy;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author huangli
 */
public class DecoderMap {

    private final ConcurrentHashMap<Integer, AbstractValueDecoder> decoderMap = new ConcurrentHashMap<>();
    private volatile boolean inited = false;
    private final ReentrantLock reentrantLock = new ReentrantLock();

    private static final DecoderMap instance = new DecoderMap();

    public DecoderMap() {
    }

    public static DecoderMap defaultInstance() {
        return instance;
    }

    public AbstractValueDecoder getDecoder(int identityNumber) {
        return decoderMap.get(identityNumber);
    }

    public void register(int identityNumber, AbstractValueDecoder decoder) {
        reentrantLock.lock();
        try {
            decoderMap.put(identityNumber, decoder);
            inited = true;
        }finally {
            reentrantLock.unlock();
        }
    }

    public void clear() {
        reentrantLock.lock();
        try {
            decoderMap.clear();
            inited = true;
        }finally {
            reentrantLock.unlock();
        }
    }

    public void initDefaultDecoder() {
        if (inited) {
            return;
        }
        reentrantLock.lock();
        try {
            if (inited) {
                return;
            }
            register(SerialPolicy.IDENTITY_NUMBER_JAVA, defaultJavaValueDecoder());
            register(SerialPolicy.IDENTITY_NUMBER_KRYO4, KryoValueDecoder.INSTANCE);
            register(SerialPolicy.IDENTITY_NUMBER_KRYO5, Kryo5ValueDecoder.INSTANCE);
            // register(SerialPolicy.IDENTITY_NUMBER_FASTJSON2, Fastjson2ValueDecoder.INSTANCE);
            inited = true;
        }finally {
            reentrantLock.unlock();
        }
    }

    public static JavaValueDecoder defaultJavaValueDecoder() {
        try {
            Class.forName("org.springframework.core.ConfigurableObjectInputStream");
            return SpringJavaValueDecoder.INSTANCE;
        } catch (ClassNotFoundException e) {
            return JavaValueDecoder.INSTANCE;
        }
    }


}
