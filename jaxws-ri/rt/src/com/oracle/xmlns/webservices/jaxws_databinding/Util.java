package com.oracle.xmlns.webservices.jaxws_databinding;
import com.sun.xml.ws.model.RuntimeModelerException;

/**
 * Simple util to handle default values.
 *
 * @author miroslav.kos@oracle.com
 */
class Util {

    static String nullSafe(String value) {
        return value == null ? "" : value;
    }

    static <T> T nullSafe(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

    @SuppressWarnings("unchecked")
    static <T extends Enum> T nullSafe(Enum value, T defaultValue) {
        return value == null ? defaultValue : (T) T.valueOf(defaultValue.getClass(), value.toString());
    }

    public static Class<?> findClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeModelerException("runtime.modeler.external.metadata.generic", e);
        }
    }
}
