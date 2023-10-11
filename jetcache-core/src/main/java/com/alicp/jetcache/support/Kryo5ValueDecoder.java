package com.alicp.jetcache.support;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.Input;

import java.io.ByteArrayInputStream;

/**
 * Created on 2016/10/4.
 *
 * @author huangli
 */
public class Kryo5ValueDecoder extends AbstractValueDecoder {

    public static final Kryo5ValueDecoder INSTANCE = new Kryo5ValueDecoder(true);

    public Kryo5ValueDecoder(boolean useIdentityNumber) {
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
        Kryo5ValueEncoder.Kryo5Cache kryoCache = null;
        try {
            kryoCache = Kryo5ValueEncoder.kryoCacheObjectPool.borrowObject();
            Kryo kryo = kryoCache.getKryo();
            ClassLoader classLoader = Kryo5ValueDecoder.class.getClassLoader();
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
                Kryo5ValueEncoder.kryoCacheObjectPool.returnObject(kryoCache);
            }
        }
    }
}
