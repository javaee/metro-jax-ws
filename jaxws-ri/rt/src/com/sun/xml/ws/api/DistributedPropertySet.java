package com.sun.xml.ws.api;

import com.sun.istack.FinalArrayList;
import com.sun.istack.NotNull;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.client.RequestContext;
import com.sun.xml.ws.client.ResponseContext;

import javax.xml.ws.WebServiceContext;
import java.util.Map.Entry;
import java.util.Set;

/**
 * {@link PropertySet} that combines properties exposed from multiple
 * {@link PropertySet}s into one.
 *
 * <p>
 * This implementation allows one {@link PropertySet} to assemble
 * all properties exposed from other "satellite" {@link PropertySet}s.
 * (A satellite may itself be a {@link DistributedPropertySet}, so
 * in general this can form a tree.)
 *
 * <p>
 * This is useful for JAX-WS because the properties we expose to the application
 * are contributed by different pieces, and therefore we'd like each of them
 * to have a separate {@link PropertySet} implementation that backs up
 * the properties. For example, this allows FastInfoset to expose its
 * set of properties to {@link RequestContext} by using a strongly-typed fields.
 *
 * <p>
 * This is also useful for a client-side transport to expose a bunch of properties
 * into {@link ResponseContext}. It simply needs to create a {@link PropertySet}
 * object with methods for each property it wants to expose, and then add that
 * {@link PropertySet} to {@link Packet}. This allows property values to be
 * lazily computed (when actually asked by users), thus improving the performance
 * of the typical case where property values are not asked.
 *
 * <p>
 * A similar benefit applies on the server-side, for a transport to expose
 * a bunch of properties to {@link WebServiceContext}.
 *
 * <p>
 * To achieve these benefits, access to {@link DistributedPropertySet} is slower
 * compared to {@link PropertySet} (such as get/set), while adding a satellite
 * object is relatively fast.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class DistributedPropertySet extends PropertySet {
    /**
     * All {@link PropertySet}s that are bundled into this {@link PropertySet}.
     */
    private final FinalArrayList<PropertySet> satellites = new FinalArrayList<PropertySet>();

    public void addSatellite(@NotNull PropertySet satellite) {
        satellites.add(satellite);
    }

    public void removeSatellite(@NotNull PropertySet satellite) {
        satellites.remove(satellite);
    }

    public void copySatelliteInto(@NotNull DistributedPropertySet r) {
        r.satellites.addAll(this.satellites);
    }

    @Override
    public Object get(Object key) {
        // check satellites
        for (PropertySet child : satellites) {
            if(child.supports(key))
                return child.get(key);
        }

        // otherwise it must be the master
        return super.get(key);
    }

    @Override
    public Object put(String key, Object value) {
        // check satellites
        for (PropertySet child : satellites) {
            if(child.supports(key))
                return child.put(key,value);
        }

        // otherwise it must be the master
        return super.put(key,value);
    }

    @Override
    public boolean supports(Object key) {
        // check satellites
        for (PropertySet child : satellites) {
            if(child.supports(key))
                return true;
        }

        return super.supports(key);
    }

    @Override
    public Object remove(Object key) {
        // check satellites
        for (PropertySet child : satellites) {
            if(child.supports(key))
                return child.remove(key);
        }

        return super.remove(key);
    }

    @Override
    /*package*/ void createEntrySet(Set<Entry<String, Object>> core) {
        super.createEntrySet(core);
        for (PropertySet child : satellites) {
            child.createEntrySet(core);
        }
    }
}
