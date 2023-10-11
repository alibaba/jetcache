package com.alicp.jetcache.support;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;

import java.io.ByteArrayInputStream;

/**
 * Created on 2016/10/4.
 *
 * @author huangli
 */
public class KryoValueDecoder extends AbstractValueDecoder {

    public static final KryoValueDecoder INSTANCE = new KryoValueDecoder(true);

    public KryoValueDecoder(boolean useIdentityNumber) {
        super(useIdentityNumber);
    }

    @Override
    public Object doApply(byte[] buffer) {
        ByteArrayInputStream in;
        if (useIdentityNumber) {
            in = new ByteArrayInputStream(buffer, 4, buffer.length - 4);
        } else {
            in = new ByteArrayInputStream(buffer);
        }
        Input input = new Input(in);
        KryoValueEncoder.KryoCache kryoCache = null;
        try {
            kryoCache =  KryoValueEncoder.kryoCacheObjectPool.borrowObject();
            Kryo kryo = kryoCache.getKryo();
            ClassLoader classLoader = KryoValueDecoder.class.getClassLoader();
            Thread t = Thread.currentThread();
            if (t != null) {
                ClassLoader ctxClassLoader = t.getContextClassLoader();
                if (ctxClassLoader != null) {
                    classLoader = ctxClassLoader;
                }
            }
            kryo.setClassLoader(classLoader);
            return kryo.readClassAndObject(input);
        }finally {
            if(kryoCache != null){
                KryoValueEncoder.kryoCacheObjectPool.returnObject(kryoCache);
            }
        }
    }
}
