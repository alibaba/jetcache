package com.alicp.jetcache.support;

import java.util.Objects;
import java.util.function.Function;

/**
 * Created on 2016/10/4.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public abstract class AbstractValueDecoder implements Function<byte[], Object>, ValueEncoders {

    protected boolean useIdentityNumber;

    private DecoderMap decoderMap = DecoderMap.defaultInstance();

    public AbstractValueDecoder(boolean useIdentityNumber) {
        this.useIdentityNumber = useIdentityNumber;
    }

    protected int parseHeader(byte[] buf) {
        int x = 0;
        x = x | (buf[0] & 0xFF);
        x <<= 8;
        x = x | (buf[1] & 0xFF);
        x <<= 8;
        x = x | (buf[2] & 0xFF);
        x <<= 8;
        x = x | (buf[3] & 0xFF);
        return x;
    }

    protected abstract Object doApply(byte[] buffer) throws Exception;

    @Override
    public Object apply(byte[] buffer) {
        try {
            if (useIdentityNumber) {
                decoderMap.initDefaultDecoder();
                int identityNumber = parseHeader(buffer);
                AbstractValueDecoder decoder = decoderMap.getDecoder(identityNumber);
                Objects.requireNonNull(decoder, "no decoder for identity number:" + identityNumber);
                return decoder.doApply(buffer);
            } else {
                return doApply(buffer);
            }
        } catch (Throwable e) {
            throw new CacheEncodeException("decode error", e);
        }
    }

    public boolean isUseIdentityNumber() {
        return useIdentityNumber;
    }

    public void setDecoderMap(DecoderMap decoderMap) {
        this.decoderMap = decoderMap;
    }
}
