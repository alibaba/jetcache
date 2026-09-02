package com.alicp.jetcache.support;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.KryoException;
import com.esotericsoftware.kryo.kryo5.Registration;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.util.DefaultClassResolver;
import com.esotericsoftware.kryo.kryo5.util.IntMap;
import com.esotericsoftware.kryo.kryo5.util.ObjectMap;

/**
 * DecodeFilter-aware Kryo5 class resolver.
 * <p>
 * Filter check logic is in {@link KryoClassResolverUtil}.
 *
 * @author huangli
 */
class Kryo5ClassResolver extends DefaultClassResolver {

    private final DecodeFilter decodeFilter;

    public Kryo5ClassResolver(DecodeFilter decodeFilter) {
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

    // Copied from DefaultClassResolver.readName() (Kryo 5.x, com.esotericsoftware.kryo:kryo5)
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
