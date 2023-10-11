/**
 * Created on 2022/07/27.
 */
package com.alicp.jetcache.support;

import com.alicp.jetcache.CacheValueHolder;
import com.alicp.jetcache.anno.SerialPolicy;

import java.nio.charset.StandardCharsets;

/**
 * @author huangli
 */
public abstract class AbstractJsonEncoder extends AbstractValueEncoder {
    public AbstractJsonEncoder(boolean useIdentityNumber) {
        super(useIdentityNumber);
    }

    protected abstract byte[] encodeSingleValue(Object value);

    @Override
    public byte[] apply(Object value) {
        try {
            JsonData[] data = encode(value);
            int len = len(data);
            byte[] buffer = useIdentityNumber ? new byte[len + 4] : new byte[len];
            int index = 0;
            if (useIdentityNumber) {
                index = writeInt(buffer, index, SerialPolicy.IDENTITY_NUMBER_FASTJSON2);
            }
            if (data == null) {
                writeShort(buffer, index, -1);
            } else {
                index = writeShort(buffer, index, data.length);
                for (JsonData d : data) {
                    if (d == null) {
                        index = writeShort(buffer, index, -1);
                    } else {
                        index = writeShort(buffer, index, d.getClassName().length);
                        index = writeBytes(buffer, index, d.getClassName());
                        index = writeInt(buffer, index, d.getData().length);
                        index = writeBytes(buffer, index, d.getData());
                    }
                }
            }
            return buffer;
        } catch (Throwable e) {
            StringBuilder sb = new StringBuilder("Fastjson Encode error. ");
            sb.append("msg=").append(e.getMessage());
            throw new CacheEncodeException(sb.toString(), e);
        }
    }

    private int len(JsonData[] data) {
        if (data == null) {
            return 2;
        }
        int x = 2;
        for (JsonData d : data) {
            if (d == null) {
                x += 2;
            } else {
                x += 2 + d.getClassName().length + 4 + d.getData().length;
            }
        }
        return x;
    }

    private int writeInt(byte[] buf, int index, int value) {
        buf[index] = (byte) (value >> 24 & 0xFF);
        buf[index + 1] = (byte) (value >> 16 & 0xFF);
        buf[index + 2] = (byte) (value >> 8 & 0xFF);
        buf[index + 3] = (byte) (value & 0xFF);
        return index + 4;
    }

    private int writeShort(byte[] buf, int index, int value) {
        buf[index] = (byte) (value >> 8 & 0xFF);
        buf[index + 1] = (byte) (value & 0xFF);
        return index + 2;
    }

    private int writeBytes(byte[] buf, int index, byte[] data) {
        System.arraycopy(data, 0, buf, index, data.length);
        return index + data.length;
    }

    private JsonData[] encode(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof CacheValueHolder) {
            CacheValueHolder h = (CacheValueHolder) value;
            Object bizObject = h.getValue();
            h.setValue(null);
            JsonData[] result = new JsonData[2];
            result[0] = encodeJsonData(h);
            result[1] = encodeJsonData(bizObject);
            h.setValue(bizObject);
            return result;
        } else if (value instanceof CacheMessage) {
            CacheMessage cm = (CacheMessage) value;
            Object[] keys = cm.getKeys();
            cm.setKeys(null);
            JsonData[] result = keys == null ? new JsonData[1] : new JsonData[keys.length + 1];
            result[0] = encodeJsonData(cm);
            if (keys != null) {
                for (int i = 0; i < keys.length; i++) {
                    result[i + 1] = encodeJsonData(keys[i]);
                }
            }
            cm.setKeys(keys);
            return result;
        } else {
            return new JsonData[]{encodeJsonData(value)};
        }
    }

    private JsonData encodeJsonData(Object value) {
        if (value == null) {
            return null;
        }
        JsonData jsonData = new JsonData();
        jsonData.setClassName(value.getClass().getName().getBytes(StandardCharsets.UTF_8));
        jsonData.setData(encodeSingleValue(value));
        return jsonData;
    }

    private static class JsonData {
        private byte[] className;
        private byte[] data;

        public byte[] getClassName() {
            return className;
        }

        public void setClassName(byte[] className) {
            this.className = className;
        }

        public byte[] getData() {
            return data;
        }

        public void setData(byte[] data) {
            this.data = data;
        }
    }
}
