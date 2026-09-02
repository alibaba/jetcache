package com.alicp.jetcache.support;

final class KryoClassResolverUtil {

    private static final String[] PRIMITIVE_TYPE_NAMES = {
            "boolean", "byte", "char", "short", "int", "long", "float", "double", "void"
    };

    static void checkAllowed(Class<?> type, DecodeFilter decodeFilter) {
        if (type != null && !type.isPrimitive()) {
            checkAllowed(type.getName(), decodeFilter);
        }
    }

    static void checkAllowed(String className, DecodeFilter decodeFilter) {
        if (isPrimitiveTypeName(className)) {
            return;
        }
        if (decodeFilter.isEnabled() && !decodeFilter.isAllowed(className)) {
            throw new DecodeFilterException(className);
        }
    }

    static Class<?> loadClass(String className, ClassLoader primary, ClassLoader fallback) throws ClassNotFoundException {
        try {
            return Class.forName(className, false, primary);
        } catch (ClassNotFoundException e) {
            return Class.forName(className, false, fallback);
        }
    }

    private static boolean isPrimitiveTypeName(String className) {
        if (className == null) {
            return false;
        }
        for (String primitiveTypeName : PRIMITIVE_TYPE_NAMES) {
            if (primitiveTypeName.equals(className)) {
                return true;
            }
        }
        return false;
    }
}
