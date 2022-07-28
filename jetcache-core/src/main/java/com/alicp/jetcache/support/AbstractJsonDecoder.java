/**
 * Created on 2022/07/27.
 */
package com.alicp.jetcache.support;

import com.alicp.jetcache.CacheValueHolder;

import java.nio.charset.StandardCharsets;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public abstract class AbstractJsonDecoder extends AbstractValueDecoder {

    public AbstractJsonDecoder(boolean useIdentityNumber) {
        super(useIdentityNumber);
    }

    @Override
    protected Object doApply(byte[] buffer) throws Exception {
        int[] indexHolder = new int[1];
        indexHolder[0] = isUseIdentityNumber() ? 4 : 0;
        short objCount = readShort(buffer, indexHolder[0]);
        indexHolder[0] = indexHolder[0] + 2;
        if (objCount < 0) {
            return null;
        }
        Object obj = readObject(buffer, indexHolder);
        if (obj == null) {
            return null;
        }
        if (obj instanceof CacheValueHolder) {
            CacheValueHolder h = (CacheValueHolder) obj;
            h.setValue(readObject(buffer, indexHolder));
            return h;
        } else if (obj instanceof CacheMessage) {
            CacheMessage cm = (CacheMessage) obj;
            if (objCount > 1) {
                Object[] keys = new Object[objCount - 1];
                for (int i = 0; i < objCount - 1; i++) {
                    keys[i] = readObject(buffer, indexHolder);
                }
                cm.setKeys(keys);
            }
            return cm;
        } else {
            return obj;
        }
    }

    private int readInt(byte[] buf, int index) {
        int x = buf[index] & 0xFF;
        x = (x << 8) | (buf[index + 1] & 0xFF);
        x = (x << 8) | (buf[index + 2] & 0xFF);
        x = (x << 8) | (buf[index + 3] & 0xFF);
        return x;
    }

    private short readShort(byte[] buf, int index) {
        int x = buf[index] & 0xFF;
        x = (x << 8) | (buf[index + 1] & 0xFF);
        return (short) x;
    }

    private Object readObject(byte[] buf, int[] indexHolder) throws Exception {
        int index = indexHolder[0];
        short classNameLen = readShort(buf, index);
        index += 2;
        if (classNameLen < 0) {
            indexHolder[0] = index;
            return null;
        } else {
            String className = new String(buf, index, classNameLen, StandardCharsets.UTF_8);
            index += classNameLen;
            Class<?> clazz = Class.forName(className);
            int size = readInt(buf, index);
            index += 4;
            Object obj = parseObject(buf, index, size, clazz);
            index += size;
            indexHolder[0] = index;
            return obj;
        }
    }

    protected abstract Object parseObject(byte[] buffer, int index, int len, Class clazz);
}
