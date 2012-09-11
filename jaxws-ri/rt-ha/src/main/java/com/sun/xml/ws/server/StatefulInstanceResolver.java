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
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
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

package com.sun.xml.ws.server;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.bind.marshaller.SAX2DOMEx;
import com.sun.xml.ws.api.ha.HighAvailabilityProvider;
import com.sun.xml.ws.api.ha.HighAvailabilityProvider.StoreType;
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.server.InstanceResolver;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.api.server.WSWebServiceContext;
import com.sun.xml.ws.developer.EPRRecipe;
import com.sun.xml.ws.developer.StatefulWebServiceManager;
import com.sun.xml.ws.resources.ServerMessages;
import com.sun.xml.ws.util.DOMUtil;
import com.sun.xml.ws.util.InjectionPlan;
import com.sun.xml.ws.util.xml.XmlUtil;
import org.glassfish.ha.store.api.BackingStore;
import org.glassfish.ha.store.api.Storeable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXResult;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link InstanceResolver} that looks at JAX-WS cookie header to
 * determine the instance to which a message will be routed.
 *
 * <p>
 * See {@link StatefulWebServiceManager} for more about user-level semantics.
 *
 * @author Kohsuke Kawaguchi
 * @author Jitendra Kotamraju (added high availability)
 */
public final class StatefulInstanceResolver<T> extends AbstractMultiInstanceResolver<T> implements StatefulWebServiceManager<T> {
    /**
     * This instance is used for serving messages that have no cookie
     * or cookie value that the server doesn't recognize.
     */
    private volatile @Nullable T fallback;

    private HAMap haMap;

    // time out control. 0=disabled
    private volatile long timeoutMilliseconds = 0;
    private volatile Callback<T> timeoutCallback;
    /**
     * Timer that controls the instance time out. Lazily created.
     */
    private volatile Timer timer;

    // Application classloader(typically web app classloader), needed for
    // deserialization of web service class
    private final ClassLoader appCL;

    private final boolean haEnabled;

    // Used for {@link BackingStore#load()} and {@link BackingStore#save()}
    // Keep this a static class, otherwise enclosed object will be pulled in
    // during serialization
    private static final class HAInstance<T> implements Storeable {
        transient @NotNull T instance;
        private byte[] buf;

        private long lastAccess = 0L;

        private boolean isNew = false;

        // unless the request gives a version somehow, this cannot be used
        // to find out the dirty active cache entry
        private long version = -1;

        private long maxIdleTime;

        public HAInstance() {
            // Storeable objects require public no-arg constructor
        }

        public HAInstance(T instance, long timeout) {
            this.instance = instance;
            lastAccess = System.currentTimeMillis();
            maxIdleTime = timeout;
        }

        public T getInstance(final ClassLoader cl) {
            if (instance == null) {
                try {
                    ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buf)) {
                        @Override
                        protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
                            Class<?> clazz = cl.loadClass(desc.getName());
                            if (clazz == null) {
                                clazz = super.resolveClass(desc);
                            }
                            return clazz;
                        }
                    };
                    instance = (T) in.readObject();
                    in.close();
                } catch (Exception ioe) {
                    throw new WebServiceException(ioe);
                }
            }
            return instance;
        }

        @Override
        public long _storeable_getVersion() {
            return version;
        }

        @Override
        public void _storeable_setVersion(long version) {
            this.version = version;
        }

        @Override
        public long _storeable_getLastAccessTime() {
            return lastAccess;
        }

        @Override
        public void _storeable_setLastAccessTime(long time) {
            lastAccess = time;
        }

        @Override
        public long _storeable_getMaxIdleTime() {
            return maxIdleTime;
        }

        @Override
        public void _storeable_setMaxIdleTime(long time) {
            maxIdleTime = time;
        }

        @Override
        public String[] _storeable_getAttributeNames() {
            return new String[0];
        }

        @Override
        public boolean[] _storeable_getDirtyStatus() {
            return new boolean[0];
        }

        @Override
        public void _storeable_writeState(OutputStream os) throws IOException {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream boos = new ObjectOutputStream(bos);
            boos.writeObject(instance);
            boos.close();
            this.buf = bos.toByteArray();        // convert instance to byte[]

            ObjectOutputStream oos = new ObjectOutputStream(os);
            oos.writeLong(version);
            oos.writeLong(lastAccess);
            oos.writeLong(maxIdleTime);
            oos.writeBoolean(isNew);
            oos.writeInt(buf.length);
            oos.write(buf);
            oos.close();
        }

        @Override
        public void _storeable_readState(InputStream is) throws IOException {
            ObjectInputStream ois = new ObjectInputStream(is);
            version = ois.readLong();
            lastAccess = ois.readLong();
            maxIdleTime = ois.readLong();
            isNew = ois.readBoolean();
            int len = ois.readInt();
            buf = new byte[len];
            ois.readFully(buf);
            ois.close();
        }
    }

    /**
     * Maintains the stateful service instance and its time-out timer.
     */
    private final class Instance {
        final @NotNull T instance;
        volatile TimerTask task;

        public Instance(T instance) {
            this.instance = instance;
        }

        /**
         * Resets the timer.
         */
        public synchronized void restartTimer() {
            cancel();
            if (timeoutMilliseconds == 0) {
                return;
            } // no timer

            task = new TimerTask() {
                @Override
                public void run() {
                    try {
                        Callback<T> cb = timeoutCallback;
                        if (cb != null) {
                            if (logger.isLoggable(Level.FINEST)) {
                                logger.log(Level.FINEST, "Invoking timeout callback for instance/timeouttask = [ {0} / {1} ]", new Object[]{instance, this});
                            }
                            cb.onTimeout(instance, StatefulInstanceResolver.this);
                            return;
                        }
                        // default operation is to unexport it.
                        unexport(instance);
                    } catch (Throwable e) {
                        // don't let an error in the code kill the timer thread
                        logger.log(Level.SEVERE, "time out handler failed", e);
                    }
                }
            };
            timer.schedule(task, timeoutMilliseconds);
        }

        /**
         * Cancels the timer.
         */
        public synchronized void cancel() {
            if (task != null) {
                boolean result = task.cancel();
                if (logger.isLoggable(Level.FINEST)) {
                    logger.log(Level.FINEST, "Timeout callback CANCELED for instance/timeouttask/cancel result = [ {0} / {1} / {2} ]", new Object[]{instance, this, result});
                }
            } else {
                if (logger.isLoggable(Level.FINEST)) {
                    logger.log(Level.FINEST, "Timeout callback NOT CANCELED for instance = [ {0} ]; task is null ...", instance);
                }
            }
            task = null;
        }
        
        public synchronized void setTask(TimerTask t) {
            this.task = t;
        }

    }


    @SuppressWarnings("unchecked")
    public StatefulInstanceResolver(Class<T> clazz) {
        super(clazz);
        appCL = clazz.getClassLoader();

        boolean ha = false;
        if (HighAvailabilityProvider.INSTANCE.isHaEnvironmentConfigured()) {
            if (Serializable.class.isAssignableFrom(clazz)) {
                logger.log(Level.WARNING,"{0}" + " doesn''t implement Serializable. High availibility is disabled i.e." +
                        "if a failover happens, stateful instance state is not failed over.", clazz);
                ha = true;
            }
        }
        haEnabled = ha;

    }

    @Override
    public @NotNull T resolve(Packet request) {

        HeaderList headers = request.getMessage().getHeaders();
        Header header = headers.get(COOKIE_TAG, true);
        String id = null;
        if (header != null) {
            // find the instance
            id = header.getStringContent();
            Instance o = haMap.get(id);
            if (o != null) {
                if (logger.isLoggable(Level.FINEST)) {
                    logger.log(Level.FINEST, "Restarting timer for objectId/Instance = [ {0} / {1} ]", new Object[]{id, o});
                }
                o.restartTimer();
                return o.instance;
            }

            // huh? what is this ID?
            logger.log(Level.INFO, "Request had an unrecognized object ID {0}", id);
        } else {
            logger.fine("No objectId header received");
        }

        // need to fallback
        T flbk = this.fallback;
        if (flbk != null) {
            return flbk;
        }

        if (id == null) {
            throw new WebServiceException(ServerMessages.STATEFUL_COOKIE_HEADER_REQUIRED(COOKIE_TAG));
        } else {
            throw new WebServiceException(ServerMessages.STATEFUL_COOKIE_HEADER_INCORRECT(COOKIE_TAG, id));
        }
    }

    /*
     * Changed stateful web service instance is pushed to backing store
     * after the invocation of service.
     */
    @Override
    public void postInvoke(@NotNull Packet request, @NotNull T servant) {
        haMap.put(servant);
    }

    @Override
    public void start(WSWebServiceContext wsc, WSEndpoint endpoint) {
        super.start(wsc, endpoint);

        haMap = new HAMap();

        if (endpoint.getBinding().getAddressingVersion() == null) {
            throw new WebServiceException(ServerMessages.STATEFUL_REQURES_ADDRESSING(clazz));
        }

        // inject StatefulWebServiceManager.
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getType() == StatefulWebServiceManager.class) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    throw new WebServiceException(ServerMessages.STATIC_RESOURCE_INJECTION_ONLY(StatefulWebServiceManager.class, field));
                }
                new InjectionPlan.FieldInjectionPlan<T, StatefulWebServiceManager>(field).inject(null, this);
            }
        }

        for (Method method : clazz.getDeclaredMethods()) {
            Class[] paramTypes = method.getParameterTypes();
            if (paramTypes.length != 1) {
                continue;
            }   // not what we are looking for

            if (paramTypes[0] == StatefulWebServiceManager.class) {
                if (!Modifier.isStatic(method.getModifiers())) {
                    throw new WebServiceException(ServerMessages.STATIC_RESOURCE_INJECTION_ONLY(StatefulWebServiceManager.class, method));
                }

                new InjectionPlan.MethodInjectionPlan<T, StatefulWebServiceManager>(method).inject(null, this);
            }
        }
    }

    @Override
    public void dispose() {
        synchronized (haMap) {
            for (Instance t : haMap.values()) {
                t.cancel();
                dispose(t.instance);
            }
            haMap.destroy();
        }
        if (fallback != null) {
            dispose(fallback);
            fallback = null;
        }
        stopTimer();
    }

    @NotNull
    @Override
    public W3CEndpointReference export(T o) {
        return export(W3CEndpointReference.class, o);
    }

    @NotNull
    @Override
    public <EPR extends EndpointReference> EPR export(Class<EPR> epr, T o) {
        return export(epr, o, null);
    }

    @Override
    public <EPR extends EndpointReference> EPR export(Class<EPR> epr, T o, EPRRecipe recipe) {
        return export(epr, InvokerTube.getCurrentPacket(), o, recipe);
    }

    @NotNull
    @Override
    public <EPR extends EndpointReference> EPR export(Class<EPR> epr, WebServiceContext context, T o) {
        if (context instanceof WSWebServiceContext) {
            WSWebServiceContext wswsc = (WSWebServiceContext) context;
            return export(epr, wswsc.getRequestPacket(), o);
        }

        throw new WebServiceException(ServerMessages.STATEFUL_INVALID_WEBSERVICE_CONTEXT(context));
    }

    @NotNull
    @Override
    public <EPR extends EndpointReference> EPR export(Class<EPR> adrsVer, @NotNull Packet currentRequest, T o) {
        return export(adrsVer, currentRequest, o, null);
    }

    @Override
    public <EPR extends EndpointReference> EPR export(Class<EPR> adrsVer, @NotNull Packet currentRequest, T o, EPRRecipe recipe) {
        return export(adrsVer, currentRequest.webServiceContextDelegate.getEPRAddress(currentRequest, owner),
                currentRequest.webServiceContextDelegate.getWSDLAddress(currentRequest, owner), o, recipe);
    }

    @NotNull
    @Override
    public <EPR extends EndpointReference> EPR export(Class<EPR> adrsVer, String endpointAddress, T o) {
        return export(adrsVer, endpointAddress, null, o, null);
    }

    @NotNull
    public <EPR extends EndpointReference> EPR export(Class<EPR> adrsVer, String endpointAddress, String wsdlAddress, T o, EPRRecipe recipe) {
        if (endpointAddress == null) {
            throw new IllegalArgumentException("No address available");
        }

        String key = haMap.get(o);

        if (key != null) {
            return createEPR(key, adrsVer, endpointAddress, wsdlAddress, recipe);
        }

        // not exported yet.
        synchronized (this) {
            // double check now in the synchronization block to
            // really make sure that we can export.
            key = haMap.get(o);
            if (key != null) {
                return createEPR(key, adrsVer, endpointAddress, wsdlAddress, recipe);
            }

            if (o != null) {
                prepare(o);
            }
            key = UUID.randomUUID().toString();
            Instance instance = new Instance(o);
            if (logger.isLoggable(Level.FINEST)) {
                logger.log(Level.FINEST, "Storing instance ID/Instance/Object/TimerTask = [ {0} / {1} / {2} / {3} ]", new Object[]{key, instance, instance.instance, instance.task});
            }
            haMap.put(key, instance);
            if (timeoutMilliseconds != 0) {
                instance.restartTimer();
            }
        }

        return createEPR(key, adrsVer, endpointAddress, wsdlAddress, recipe);
    }

    /*
     * Creates an EPR that has the right key.
     */

    private <EPR extends EndpointReference> EPR createEPR(String key,
                                                          Class<EPR> eprClass, String address, String wsdlAddress, EPRRecipe recipe) {

        List<Element> referenceParameters = new ArrayList<Element>();
        List<Element> metadata = new ArrayList<Element>();

        Document doc = DOMUtil.createDom();
        Element cookie =
                doc.createElementNS(COOKIE_TAG.getNamespaceURI(),
                        COOKIE_TAG.getPrefix() + ":" + COOKIE_TAG.getLocalPart());
        cookie.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:"
                + COOKIE_TAG.getPrefix(), COOKIE_TAG.getNamespaceURI());
        cookie.setTextContent(key);
        referenceParameters.add(cookie);

        if (recipe != null) {
            for (Header h : recipe.getReferenceParameters()) {
                doc = DOMUtil.createDom();
                SAX2DOMEx s2d = new SAX2DOMEx(doc);
                try {
                    h.writeTo(s2d, XmlUtil.DRACONIAN_ERROR_HANDLER);
                    referenceParameters.add((Element) doc.getLastChild());
                } catch (SAXException e) {
                    throw new WebServiceException("Unable to write EPR Reference parameters " + h, e);
                }
            }
            Transformer t = XmlUtil.newTransformer();
            for (Source s : recipe.getMetadata()) {
                try {
                    DOMResult r = new DOMResult();
                    t.transform(s, r);
                    Document d = (Document) r.getNode();
                    metadata.add(d.getDocumentElement());
                } catch (TransformerException e) {
                    throw new IllegalArgumentException("Unable to write EPR metadata " + s, e);
                }
            }

        }

        return eprClass.cast(owner.getEndpointReference(eprClass, address, wsdlAddress, metadata, referenceParameters));
    }

    @Override
    public void unexport(@Nullable T o) {
        if (o == null) {
            return;
        }
        Instance i = haMap.remove(o);
        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, "Removed Instance = [ {0} ], remaining instance keys = [ {1} ]", new Object[]{o, haMap.instances.keySet()});
        }
        if (i != null) {
            i.cancel();
        }
    }

    @Override
    public T resolve(EndpointReference epr) {
        class CookieSniffer extends DefaultHandler {
            StringBuilder buf = new StringBuilder();
            boolean inCookie = false;

            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                if (localName.equals(COOKIE_TAG.getLocalPart()) && uri.equals(COOKIE_TAG.getNamespaceURI())) {
                    inCookie = true;
                }
            }

            @Override
            public void characters(char ch[], int start, int length) throws SAXException {
                if (inCookie) {
                    buf.append(ch, start, length);
                }
            }

            @Override
            public void endElement(String uri, String localName, String qName) throws SAXException {
                inCookie = false;
            }
        }
        CookieSniffer sniffer = new CookieSniffer();
        epr.writeTo(new SAXResult(sniffer));

        Instance o = haMap.get(sniffer.buf.toString());
        if (o != null) {
            return o.instance;
        }
        return null;
    }

    @Override
    public void setFallbackInstance(T o) {
        if (o != null) {
            prepare(o);
        }
        this.fallback = o;
    }

    @Override
    public void setTimeout(long milliseconds, Callback<T> callback) {
        if (milliseconds < 0) {
            throw new IllegalArgumentException();
        }
        this.timeoutMilliseconds = milliseconds;
        this.timeoutCallback = callback;
        haMap.getExpiredTask().cancel();
        if (timeoutMilliseconds > 0) {
            startTimer();
            timer.schedule(haMap.newExpiredTask(), timeoutMilliseconds, timeoutMilliseconds);
        } else {
            stopTimer();
        }
    }

    @Override
    public void touch(T o) {
        Instance i = haMap.touch(o);
        if (i != null) {
            i.restartTimer();
        }
    }


    private synchronized void startTimer() {
        if (timer == null) {
            timer = new Timer("JAX-WS stateful web service timeout timer");
        }
    }

    private synchronized void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private class HAMap {
        // cookie --> Instance
        final Map<String, Instance> instances = new HashMap<String, Instance>();
        // object --> cookie
        final Map<T, String> reverseInstances = new HashMap<T, String>();
        final BackingStore<String, HAInstance> bs;
        // Removes expired entrees from BackingStore
        TimerTask expiredTask;

        HAMap() {
            StoreType type = haEnabled ? StoreType.IN_MEMORY : StoreType.NOOP;
            bs = HighAvailabilityProvider.INSTANCE.createBackingStore(
                    HighAvailabilityProvider.INSTANCE.getBackingStoreFactory(type),
                    owner.getServiceName() + ":" + owner.getPortName() + ":STATEFUL_WEB_SERVICE",
                    String.class,
                    HAInstance.class);
            expiredTask = newExpiredTask();
        }

        TimerTask getExpiredTask() {
            return expiredTask;
        }

        private TimerTask newExpiredTask() {
            expiredTask = new TimerTask() {
                @Override
                public void run() {
                    HighAvailabilityProvider.removeExpired(bs);
                }
            };
            return expiredTask;
        }

        synchronized String get(T t) {
            return reverseInstances.get(t);
        }

        synchronized Instance touch(T t) {
            String id = get(t);
            if (id != null) {
                Instance i = get(id);
                if (i != null) {
                    put(id, i);
                    return i;
                }
            }
            return null;
        }

        synchronized Instance get(String id) {
            Instance i = instances.get(id);
            if (i == null) {
                HAInstance<T> hai = HighAvailabilityProvider.loadFrom(bs, id, null);
                if (hai != null) {
                    T t = hai.getInstance(appCL);
                    i = new Instance(t);
                    instances.put(id, i);
                    reverseInstances.put(t, id);
                }
            }
            return i;
        }

        synchronized void put(String id, Instance newi) {
            Instance oldi = instances.get(id);
            boolean isNew = oldi == null;
            if (!isNew) {
                reverseInstances.remove(oldi.instance);
                // reuse the original timeout task
                newi.setTask(oldi.task);
            }

            instances.put(id, newi);
            reverseInstances.put(newi.instance, id);
            HAInstance<T> hai = new HAInstance<T>(newi.instance, timeoutMilliseconds);
            HighAvailabilityProvider.saveTo(bs, id, hai, isNew);
        }

        synchronized void put(T t) {
            String id = reverseInstances.get(t);
            if (id != null) {
                put(id, new Instance(t));
            }
        }

        synchronized void remove(String id) {
            Instance i = instances.get(id);
            if (i != null) {
                instances.remove(id);
                reverseInstances.remove(i.instance);
                HighAvailabilityProvider.removeFrom(bs, id);
            }
        }

        synchronized Instance remove(T t) {
            String id = reverseInstances.get(t);
            if (id != null) {
                reverseInstances.remove(t);
                Instance i = instances.remove(id);
                HighAvailabilityProvider.removeFrom(bs, id);
                return i;
            }
            return null;
        }

        synchronized void destroy() {
            instances.clear();
            reverseInstances.clear();
            HighAvailabilityProvider.destroy(bs);
        }

        Collection<Instance> values() {
            return instances.values();
        }

    }


    private static final QName COOKIE_TAG = new QName("http://jax-ws.dev.java.net/xml/ns/", "objectId", "jaxws");

    private static final Logger logger =
            Logger.getLogger(com.sun.xml.ws.util.Constants.LoggingDomain + ".server");
}
