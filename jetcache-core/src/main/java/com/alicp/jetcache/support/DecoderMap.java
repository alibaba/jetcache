/**
 * Created on 2018/3/20.
 */
package com.alicp.jetcache.support;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author huangli
 */
public class DecoderMap {

    public static final int IDENTITY_NUMBER_JAVA = 0x4A953A80;

    // int IDENTITY_NUMBER_FASTJSON = 0x4A953A81; not used since 2.5+
    // removed in 2.8.0
    // int IDENTITY_NUMBER_KRYO4 = 0x4A953A82;

    /**
     * @since 2.7
     */
    public static final int IDENTITY_NUMBER_KRYO5 = 0xF6E0A5C0;

    /**
     * fastjson2 encoder/decoder is implemented but not register by default.
     * This is because json is not good serializable util for java and has many compatible problems.
     *
     * @see com.alicp.jetcache.anno.support.DefaultEncoderParser
     * @see DecoderMap
     * @since 2.7
     */
    public static final int IDENTITY_NUMBER_FASTJSON2 = 0xF6E0A5C1;

    /**
     * jackson3 encoder/decoder is implemented but not register by default.
     * This is because json is not good serializable util for java and has many compatible problems.
     *
     * @see com.alicp.jetcache.anno.support.DefaultEncoderParser
     * @see DecoderMap
     * @since 2.8
     */
    public static final int IDENTITY_NUMBER_JACKSON3 = 0xF6E0A5C2;

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
        decoderMap.put(identityNumber, decoder);
    }

    public void clear() {
        decoderMap.clear();
    }

    public ReentrantLock getLock() {
        return reentrantLock;
    }

    public void setInited(boolean inited) {
        this.inited = inited;
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
            register(IDENTITY_NUMBER_JAVA, defaultJavaValueDecoder());
            try {
                Class.forName("com.esotericsoftware.kryo.kryo5.Kryo");
                register(IDENTITY_NUMBER_KRYO5, Kryo5ValueDecoder.INSTANCE);
            } catch (ClassNotFoundException e) {
                // the com.esotericsoftware:kryo should be 5+
                try {
                    Class.forName("com.esotericsoftware.kryo.Kryo");
                    register(IDENTITY_NUMBER_KRYO5, KryoValueDecoder.INSTANCE);
                } catch (ClassNotFoundException e2) {
                    // kryo is not on the classpath, skip registration
                }
            }
            // register(IDENTITY_NUMBER_FASTJSON2, Fastjson2ValueDecoder.INSTANCE);
            // register(IDENTITY_NUMBER_JACKSON3, Jackson3ValueDecoder.INSTANCE);
            inited = true;
        } finally {
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
