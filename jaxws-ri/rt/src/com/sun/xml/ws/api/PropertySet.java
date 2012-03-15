/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.xml.ws.api;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.util.ReadOnlyPropertyException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A set of "properties" that can be accessed via strongly-typed fields
 * as well as reflexibly through the property name.
 *
 * @author Kohsuke Kawaguchi
 */
@SuppressWarnings("SuspiciousMethodCalls")
public abstract class PropertySet implements org.jvnet.ws.message.PropertySet {

    /**
     * Creates a new instance of TypedMap.
     */
    protected PropertySet() {
    }

    private Map<String,Object> mapView;

    /**
     * Represents the list of strongly-typed known propertyies
     * (keyed by property names.)
     *
     * <p>
     * Just giving it an alias to make the use of this class more fool-proof.
     */
    protected static final class PropertyMap extends HashMap<String,Accessor> {}

    /**
     * Map representing the Fields and Methods annotated with {@link Property}.
     * Model of {@link PropertySet} class.
     *
     * <p>
     * At the end of the derivation chain this method just needs to be implemented
     * as:
     *
     * <pre>
     * private static final PropertyMap model;
     * static {
     *   model = parse(MyDerivedClass.class);
     * }
     * protected PropertyMap getPropertyMap() {
     *   return model;
     * }
     * </pre>
     */
    protected abstract PropertyMap getPropertyMap();

    // maybe we can use this some time
    ///**
    // * If all the properties defined on this {@link PropertySet} has a certain prefix
    // * (such as, say, "javax.xml.ws.http."), then return it.
    // *
    // * <p>
    // * Returning a non-null name from this method allows methods like
    // * {@link #get(Object)} and {@link #put(String, Object)} to work faster.
    // * This is especially so with {@link DistributedPropertySet}, so implementations
    // * are encouraged to set a common prefix, as much as possible.
    // *
    // * <p>
    // * Currently, this is used only by {@link DistributedPropertySet}.
    // *
    // * @return
    // *      Null if no such common prefix exists. Otherwise string like
    // *      "javax.xml.ws.http." (the dot at the last is usually preferrable,
    // *      so that properties like "javax.xml.ws.https.something" won't match.
    // */
    //protected abstract String getPropertyPrefix();

    /**
     * This method parses a class for fields and methods with {@link Property}.
     */
    protected static PropertyMap parse(final Class clazz) {
        // make all relevant fields and methods accessible.
        // this allows runtime to skip the security check, so they runs faster.
        return AccessController.doPrivileged(new PrivilegedAction<PropertyMap>() {
            public PropertyMap run() {
                PropertyMap props = new PropertyMap();
                for( Class c=clazz; c!=null; c=c.getSuperclass()) {
                    for (Field f : c.getDeclaredFields()) {
                        Property cp = f.getAnnotation(Property.class);
                        if(cp!=null) {
                            for(String value : cp.value()) {
                                props.put(value, new FieldAccessor(f, value));
                            }
                        }
                    }
                    for (Method m : c.getDeclaredMethods()) {
                        Property cp = m.getAnnotation(Property.class);
                        if(cp!=null) {
                            String name = m.getName();
                            assert name.startsWith("get") || name.startsWith("is");

                            String setName = name.startsWith("is") ? "set"+name.substring(3) : // isFoo -> setFoo 
                            	's'+name.substring(1);   // getFoo -> setFoo
                            Method setter;
                            try {
                                setter = clazz.getMethod(setName,m.getReturnType());
                            } catch (NoSuchMethodException e) {
                                setter = null; // no setter
                            }
                            for(String value : cp.value()) {
                                props.put(value, new MethodAccessor(m, setter, value));
                            }
                        }
                    }
                }

                return props;
            }
        });
    }

    /**
     * Represents a typed property defined on a {@link PropertySet}.
     */
    protected interface Accessor {
        String getName();
        boolean hasValue(PropertySet props);
        Object get(PropertySet props);
        void set(PropertySet props, Object value);
    }

    static final class FieldAccessor implements Accessor {
        /**
         * Field with the annotation.
         */
        private final Field f;

        /**
         * One of the values in {@link Property} annotation on {@link #f}.
         */
        private final String name;

        protected FieldAccessor(Field f, String name) {
            this.f = f;
            f.setAccessible(true);
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public boolean hasValue(PropertySet props) {
            return get(props)!=null;
        }

        public Object get(PropertySet props) {
            try {
                return f.get(props);
            } catch (IllegalAccessException e) {
                throw new AssertionError();
            }
        }

        public void set(PropertySet props, Object value) {
            try {
                f.set(props,value);
            } catch (IllegalAccessException e) {
                throw new AssertionError();
            }
        }
    }

    static final class MethodAccessor implements Accessor {
        /**
         * Getter method.
         */
        private final @NotNull Method getter;
        /**
         * Setter method.
         * Some property is read-only.
         */
        private final @Nullable Method setter;

        /**
         * One of the values in {@link Property} annotation on {@link #getter}.
         */
        private final String name;

        protected MethodAccessor(Method getter, Method setter, String value) {
            this.getter = getter;
            this.setter = setter;
            this.name = value;
            getter.setAccessible(true);
            if(setter!=null)
                setter.setAccessible(true);
        }

        public String getName() {
            return name;
        }

        public boolean hasValue(PropertySet props) {
            return get(props)!=null;
        }

        public Object get(PropertySet props) {
            try {
                return getter.invoke(props);
            } catch (IllegalAccessException e) {
                throw new AssertionError();
            } catch (InvocationTargetException e) {
                handle(e);
                return 0;   // never reach here
            }
        }

        public void set(PropertySet props, Object value) {
            if(setter==null)
                throw new ReadOnlyPropertyException(getName());
            try {
                setter.invoke(props,value);
            } catch (IllegalAccessException e) {
                throw new AssertionError();
            } catch (InvocationTargetException e) {
                handle(e);
            }
        }

        /**
         * Since we don't expect the getter/setter to throw a checked exception,
         * it should be possible to make the exception propagation transparent.
         * That's what we are trying to do here.
         */
        private Exception handle(InvocationTargetException e) {
            Throwable t = e.getTargetException();
            if(t instanceof Error)
                throw (Error)t;
            if(t instanceof RuntimeException)
                throw (RuntimeException)t;
            throw new Error(e);
        }
    }

    /**
     * Class allowing to work with PropertySet object as with a Map; it doesn't only allow to read properties from
     * the map but also to modify the map in a way it is in sync with original strongly typed fields. It also allows
     * (if necessary) to store additional properties those can't be found in strongly typed fields.
     *
     * @see com.sun.xml.ws.api.PropertySet#asMap() method
     */
    class MapView extends HashMap<String, Object> {

        // flag if it should allow store also different properties
        // than the from strongly typed fields
        boolean extensible;

        MapView(boolean extensible) {
            super(getPropertyMap().entrySet().size());
            this.extensible = extensible;
            initialize();
        }

        public void initialize() {
            for (final Entry<String, Accessor> e : getPropertyMap().entrySet()) {
                super.put(e.getKey(), e.getValue());
            }
        }

        @Override
        public Object get(Object key) {

            Object o = super.get(key);
            if (o instanceof Accessor) {
                return ((Accessor) o).get(PropertySet.this);
            } else {
                return o;
            }
        }

        @Override
        public Set<Entry<String, Object>> entrySet() {
            Set<Entry<String, Object>> entries = new HashSet<Entry<String, Object>>();
            for (String key : keySet()) {
                entries.add(new SimpleImmutableEntry<String, Object>(key, get(key)));
            }
            return entries;
        }

        @Override
        public Object put(String key, Object value) {

            Object o = super.get(key);
            if (o != null && o instanceof Accessor) {

                Object oldValue = ((Accessor) o).get(PropertySet.this);
                ((Accessor) o).set(PropertySet.this, value);
                return oldValue;

            } else {

                if (extensible) {
                    return super.put(key, value);
                } else {
                    throw new IllegalStateException("Unknown property [" + key + "] for PropertySet [" +
                            PropertySet.this.getClass().getName() + "]");
                }
            }
        }

        @Override
        public void clear() {
            for (String key : keySet()) {
                remove(key);
            }
        }

        @Override
        public Object remove(Object key) {
            Object o = super.get(key);
            if (o instanceof Accessor) {
                ((Accessor)o).set(PropertySet.this, null);
            }
            return super.remove(key);
        }
    }

    public final boolean containsKey(Object key) {
        return get(key)!=null;
    }

    /**
     * Gets the name of the property.
     *
     * @param key
     *      This field is typed as {@link Object} to follow the {@link Map#get(Object)}
     *      convention, but if anything but {@link String} is passed, this method
     *      just returns null.
     */
    public Object get(Object key) {
        Accessor sp = getPropertyMap().get(key);
        if(sp!=null)
            return sp.get(this);
        throw new IllegalArgumentException("Undefined property "+key);
    }

    /**
     * Sets a property.
     *
     * <h3>Implementation Note</h3>
     * This method is slow. Code inside JAX-WS should define strongly-typed
     * fields in this class and access them directly, instead of using this.
     *
     * @throws ReadOnlyPropertyException
     *      if the given key is an alias of a strongly-typed field,
     *      and if the name object given is not assignable to the field.
     *
     * @see Property
     */
    public Object put(String key, Object value) {
        Accessor sp = getPropertyMap().get(key);
        if(sp!=null) {
            Object old = sp.get(this);
            sp.set(this,value);
            return old;
        } else {
            throw new IllegalArgumentException("Undefined property "+key);
        }
    }

    /**
     * Checks if this {@link PropertySet} supports a property of the given name.
     */
    public boolean supports(Object key) {
        return getPropertyMap().containsKey(key);
    }

    public Object remove(Object key) {
        Accessor sp = getPropertyMap().get(key);
        if(sp!=null) {
            Object old = sp.get(this);
            sp.set(this,null);
            return old;
        } else {
            throw new IllegalArgumentException("Undefined property "+key);
        }
    }

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
     * @deprecated use newer implementation {@link com.sun.xml.ws.api.PropertySet#asMap()} which produces
     * readwrite {@link Map}
     *
     * @return
     *      always non-null valid instance.
     */
    @Deprecated
    public final Map<String,Object> createMapView() {
        final Set<Entry<String,Object>> core = new HashSet<Entry<String,Object>>();
        createEntrySet(core);

        return new AbstractMap<String, Object>() {
            public Set<Entry<String,Object>> entrySet() {
                return core;
            }
        };
    }

    /**
     * Creates a modifiable {@link Map} view of this {@link PropertySet}.
     * <p/>
     * Changes done on this {@link Map} or on {@link PropertySet} object work in both directions - values made to
     * {@link Map} are reflected to {@link PropertySet} and changes done using getters/setters on {@link PropertySet}
     * object are automatically reflected in this {@link Map}.
     * <p/>
     * If necessary, it also can hold other values (not present on {@link PropertySet}) -
     * {@see PropertySet#mapAllowsAdditionalProperties}
     *
     * @return always non-null valid instance.
     */
    public Map<String, Object> asMap() {
        if (mapView == null) {
            mapView = new MapView(mapAllowsAdditionalProperties());
        }
        return mapView;
    }

    /**
     * Used when constructing the {@link MapView} for this object - it controls if the {@link MapView} servers only to
     * access strongly typed values or allows also different values
     *
     * @return true if {@link Map} should allow also properties not defined as strongly typed fields
     */
    protected boolean mapAllowsAdditionalProperties() {
        return false;
    }

    /*package*/ void createEntrySet(Set<Entry<String,Object>> core) {
        for (final Entry<String, Accessor> e : getPropertyMap().entrySet()) {
            core.add(new Entry<String, Object>() {
                public String getKey() {
                    return e.getKey();
                }

                public Object getValue() {
                    return e.getValue().get(PropertySet.this);
                }

                public Object setValue(Object value) {
                    Accessor acc = e.getValue();
                    Object old = acc.get(PropertySet.this);
                    acc.set(PropertySet.this,value);
                    return old;
                }
            });
        }
    }
}
