/**
 * Created on  13-10-31 22:51
 */
package com.taobao.geek.jetcache.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;
import com.taobao.geek.jetcache.SerialPolicy;

import java.io.*;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
//TODO enhance performance
class SerializeUtil {
    private static final byte JAVA_HEAD = 'J';
    private static final byte FASTJSON_HEAD = 'F';
    private static final byte KRYO_HEAD = 'K';

    public static byte[] encode(Object value, SerialPolicy serialPolicy) throws Exception {
        switch (serialPolicy) {
            case FASTJSON: {
                byte[] bs = JSON.toJSONBytes(value, SerializerFeature.WriteClassName);
                byte[] bs2 = new byte[bs.length + 1];
                bs2[0] = FASTJSON_HEAD;
                System.arraycopy(bs, 0, bs2, 1, bs.length);
                return bs2;
            }
            case JAVA: {
                ByteArrayOutputStream bos = new ByteArrayOutputStream(512);
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                oos.writeObject(value);
                oos.flush();
                byte[] bs = bos.toByteArray();
                byte[] bs2 = new byte[bs.length + 1];
                System.arraycopy(bs, 0, bs2, 1, bs.length);
                bs2[0] = JAVA_HEAD;
                return bs2;
            }
            case KRYO: {
                Kryo kryo = new Kryo();
                kryo.setDefaultSerializer(CompatibleFieldSerializer.class);
                ByteArrayOutputStream bos = new ByteArrayOutputStream(128);
                Output output = new Output(bos);
                output.write(KRYO_HEAD);
                kryo.writeClassAndObject(output, value);
                output.close();
                return bos.toByteArray();
            }
            default:
                throw new IllegalArgumentException(serialPolicy.toString());
        }
    }

    public static Object decode(byte[] buffer) throws Exception {
        switch (buffer[0]) {
            case FASTJSON_HEAD: {
                byte[] bs = new byte[buffer.length - 1];
                System.arraycopy(buffer, 1, bs, 0, bs.length);
                return JSON.parse(bs);
            }
            case JAVA_HEAD: {
                ByteArrayInputStream in = new ByteArrayInputStream(buffer, 1, buffer.length - 1);
                ObjectInputStream ois = new ObjectInputStream(in);
                return ois.readObject();
            }
            case KRYO_HEAD: {
                ByteArrayInputStream in = new ByteArrayInputStream(buffer, 1, buffer.length - 1);
                Input input = new Input(in);
                Kryo kryo = new Kryo();
                kryo.setDefaultSerializer(CompatibleFieldSerializer.class);
                return kryo.readClassAndObject(input);
            }
            default:
                throw new IllegalArgumentException(buffer[0] + "");
        }
    }
}
