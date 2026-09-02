package com.alicp.jetcache.support;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.Registration;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.util.DefaultClassResolver;
import com.esotericsoftware.kryo.util.IntMap;
import com.esotericsoftware.kryo.util.ObjectMap;

/**
 * DecodeFilter-aware class resolver for the legacy Maven coordinate {@code com.esotericsoftware:kryo}.
 * <p>
 * Despite the class name, this is NOT for Kryo4. Since JetCache 2.8.0, the
 * {@code com.esotericsoftware:kryo} dependency is also 5.x (just a different Maven coordinate
 * from {@code com.esotericsoftware.kryo:kryo5}). This resolver handles the
 * {@code com.esotericsoftware.kryo} package path.
 * <p>
 * Filter check logic is in {@link KryoClassResolverUtil}.
 *
 * @author huangli
 */
class KryoClassResolver extends DefaultClassResolver {
    private final DecodeFilter decodeFilter;

    public KryoClassResolver(DecodeFilter decodeFilter) {
        this.decodeFilter = decodeFilter;
    }

    @Override
    public Registration readClass(Input input) {
        Registration registration = super.readClass(input);
        if (registration != null) {
            KryoClassResolverUtil.checkAllowed(registration.getType(), decodeFilter);
        }
        return registration;
    }

    // Copied from DefaultClassResolver.readName() (Kryo 5.x, com.esotericsoftware:kryo)
    // with filter check inserted after reading className string and after class loading.
    // If Kryo upgrades, this method must be reviewed for consistency.
    @Override
    protected Registration readName(Input input) {
        int nameId = input.readVarInt(true);
        if (nameIdToClass == null) {
            nameIdToClass = new IntMap<>();
        }

        Class<?> type = nameIdToClass.get(nameId);
        if (type == null) {
            String className = input.readString();
            KryoClassResolverUtil.checkAllowed(className, decodeFilter);
            type = super.getTypeByName(className);
            if (type == null) {
                try {
                    type = KryoClassResolverUtil.loadClass(className, kryo.getClassLoader(), Kryo.class.getClassLoader());
                } catch (ClassNotFoundException e) {
                    throw new KryoException("Unable to find class: " + className, e);
                }
                if (nameToClass == null) {
                    nameToClass = new ObjectMap<>();
                }
                nameToClass.put(className, type);
            }
            nameIdToClass.put(nameId, type);
        }
        KryoClassResolverUtil.checkAllowed(type, decodeFilter);
        return kryo.getRegistration(type);
    }

    @Override
    protected Class<?> getTypeByName(String className) {
        KryoClassResolverUtil.checkAllowed(className, decodeFilter);
        return super.getTypeByName(className);
    }
}
