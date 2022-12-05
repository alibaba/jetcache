/**
 * Created on 2022/07/30.
 */
package com.alicp.jetcache.template;

import com.alicp.jetcache.CacheLoader;
import com.alicp.jetcache.RefreshPolicy;
import com.alicp.jetcache.anno.CacheConsts;
import com.alicp.jetcache.anno.CacheType;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class QuickConfig {
    private String area = CacheConsts.DEFAULT_AREA;
    private String name;
    private Duration expire;
    private Duration localExpire;
    private Integer localLimit;
    private CacheType cacheType;
    private Boolean syncLocal;
    private Function<Object, Object> keyConvertor;
    private Function<Object, byte[]> valueEncoder;
    private Function<byte[], Object> valueDecoder;
    private Boolean cacheNullValue;
    private Boolean useAreaInPrefix;
    private Boolean penetrationProtect;
    private Duration penetrationProtectTimeout;
    private RefreshPolicy refreshPolicy;
    private CacheLoader<? extends Object, ? extends Object> loader;

    private QuickConfig() {
    }

    public static Builder newBuilder(String name) {
        return new Builder(name);
    }

    public static Builder newBuilder(String area, String name) {
        return new Builder(area, name);
    }

    public static class Builder {
        private String area = CacheConsts.DEFAULT_AREA;
        private final String name;
        private Duration expire;
        private Duration localExpire;
        private Integer localLimit;
        private CacheType cacheType;
        private Boolean syncLocal;
        private Function<Object, Object> keyConvertor;
        private Function<Object, byte[]> valueEncoder;
        private Function<byte[], Object> valueDecoder;
        private Boolean cacheNullValue;
        private Boolean useAreaInPrefix;
        private Boolean penetrationProtect;
        private Duration penetrationProtectTimeout;
        private RefreshPolicy refreshPolicy;
        private CacheLoader<? extends Object, ? extends Object> loader;

        Builder(String name) {
            Objects.requireNonNull(name);
            this.name = name;
        }

        Builder(String area, String name) {
            Objects.requireNonNull(area);
            Objects.requireNonNull(name);
            this.area = area;
            this.name = name;
        }

        public QuickConfig build() {
            QuickConfig c = new QuickConfig();
            c.area = area;
            c.name = name;
            c.expire = expire;
            c.localExpire = localExpire;
            c.localLimit = localLimit;
            c.cacheType = cacheType;
            c.syncLocal = syncLocal;
            c.keyConvertor = keyConvertor;
            c.valueEncoder = valueEncoder;
            c.valueDecoder = valueDecoder;
            c.cacheNullValue = cacheNullValue;
            c.useAreaInPrefix = useAreaInPrefix;
            c.penetrationProtect = penetrationProtect;
            c.penetrationProtectTimeout = penetrationProtectTimeout;
            c.refreshPolicy = refreshPolicy;
            c.loader = loader;
            return c;
        }

        public Builder expire(Duration expire) {
            this.expire = expire;
            return this;
        }

        public Builder localExpire(Duration localExpire) {
            this.localExpire = localExpire;
            return this;
        }

        public Builder localLimit(Integer localLimit) {
            this.localLimit = localLimit;
            return this;
        }

        public Builder cacheType(CacheType cacheType) {
            this.cacheType = cacheType;
            return this;
        }

        public Builder syncLocal(Boolean syncLocal) {
            this.syncLocal = syncLocal;
            return this;
        }

        public Builder keyConvertor(Function<Object, Object> keyConvertor) {
            this.keyConvertor = keyConvertor;
            return this;
        }

        public Builder valueEncoder(Function<Object, byte[]> valueEncoder) {
            this.valueEncoder = valueEncoder;
            return this;
        }

        public Builder valueDecoder(Function<byte[], Object> valueDecoder) {
            this.valueDecoder = valueDecoder;
            return this;
        }

        public Builder cacheNullValue(Boolean cacheNullValue) {
            this.cacheNullValue = cacheNullValue;
            return this;
        }

        public Builder useAreaInPrefix(Boolean useAreaInPrefix) {
            this.useAreaInPrefix = useAreaInPrefix;
            return this;
        }

        public Builder penetrationProtect(Boolean penetrationProtect) {
            this.penetrationProtect = penetrationProtect;
            return this;
        }

        public Builder penetrationProtectTimeout(Duration penetrationProtectTimeout) {
            this.penetrationProtectTimeout = penetrationProtectTimeout;
            return this;
        }

        public Builder refreshPolicy(RefreshPolicy refreshPolicy) {
            this.refreshPolicy = refreshPolicy;
            return this;
        }

        public <K, V> Builder loader(CacheLoader<K, V> loader) {
            this.loader = loader;
            return this;
        }
    }

    public String getArea() {
        return area;
    }

    public String getName() {
        return name;
    }

    public Duration getExpire() {
        return expire;
    }

    public Duration getLocalExpire() {
        return localExpire;
    }

    public CacheType getCacheType() {
        return cacheType;
    }

    public Integer getLocalLimit() {
        return localLimit;
    }

    public Boolean getSyncLocal() {
        return syncLocal;
    }

    public Function<Object, Object> getKeyConvertor() {
        return keyConvertor;
    }

    public Function<Object, byte[]> getValueEncoder() {
        return valueEncoder;
    }

    public Function<byte[], Object> getValueDecoder() {
        return valueDecoder;
    }

    public Boolean getCacheNullValue() {
        return cacheNullValue;
    }

    public Boolean getUseAreaInPrefix() {
        return useAreaInPrefix;
    }

    public Boolean getPenetrationProtect() {
        return penetrationProtect;
    }

    public Duration getPenetrationProtectTimeout() {
        return penetrationProtectTimeout;
    }

    public RefreshPolicy getRefreshPolicy() {
        return refreshPolicy;
    }

    public <K, V> CacheLoader<K, V> getLoader() {
        return (CacheLoader<K, V>) loader;
    }
}
