package org.jvnet.ws.message;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

import javax.xml.ws.handler.MessageContext;

/**
 * A set of "properties" that can be accessed via strongly-typed fields
 * as well as reflexibly through the property name.
 *
 * @author Kohsuke Kawaguchi
 */
public interface PropertySet {

    /**
     * Marks a field on {@link PropertySet} as a
     * property of {@link MessageContext}.
     *
     * <p>
     * To make the runtime processing easy, this annotation
     * must be on a public field (since the property name
     * can be set through {@link Map} anyway, you won't be
     * losing abstraction by doing so.)
     *
     * <p>
     * For similar reason, this annotation can be only placed
     * on a reference type, not primitive type.
     *
     * @author Kohsuke Kawaguchi
     */
    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD,ElementType.METHOD})
    public @interface Property {
        /**
         * Name of the property.
         */
        String[] value();
    }

    public boolean containsKey(Object key);

    /**
     * Gets the name of the property.
     *
     * @param key
     *      This field is typed as {@link Object} to follow the {@link Map#get(Object)}
     *      convention, but if anything but {@link String} is passed, this method
     *      just returns null.
     */
    public Object get(Object key);

    /**
     * Sets a property.
     *
     * <h3>Implementation Note</h3>
     * This method is slow. Code inside JAX-WS should define strongly-typed
     * fields in this class and access them directly, instead of using this.
     *
     * @see Property
     */
    public Object put(String key, Object value);

    /**
     * Checks if this {@link PropertySet} supports a property of the given name.
     */
    public boolean supports(Object key);

    public Object remove(Object key);

    /**
     * Creates a {@link Map} view of this {@link PropertySet}.
     *
     * <p>
     * This map is partially live, in the sense that values you set to it
     * will be reflected to {@link PropertySet}.
     *
     * <p>
     * However, this map may not pick up changes made
     * to {@link PropertySet} after the view is created.
     *
     * @return
     *      always non-null valid instance.
     */
    public Map<String,Object> createMapView();
}