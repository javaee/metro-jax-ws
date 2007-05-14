/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.xml.ws.api.streaming;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.streaming.XMLReaderException;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.WebServiceException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Factory for {@link XMLStreamWriter}.
 *
 * <p>
 * This wraps {@link XMLOutputFactory} and allows us to reuse {@link XMLStreamWriter} instances
 * when appropriate.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class XMLStreamWriterFactory {

    /**
     * Singleton instance.
     */
    private static volatile @NotNull XMLStreamWriterFactory theInstance;


    static {
        XMLOutputFactory  xof = null;
        if (Boolean.getBoolean(XMLStreamWriterFactory.class.getName()+".woodstox")) {
            try {
                xof = (XMLOutputFactory)Class.forName("com.ctc.wstx.stax.WstxOutputFactory").newInstance();
            } catch (Exception e) {
                // Ignore and fallback to default XMLOutputFactory
            }
        }
        if (xof == null) {
            xof = XMLOutputFactory.newInstance();
        }

        XMLStreamWriterFactory f=null;

        // this system property can be used to disable the pooling altogether,
        // in case someone hits an issue with pooling in the production system.
        if(!Boolean.getBoolean(XMLStreamWriterFactory.class.getName()+".noPool"))
            f = Zephyr.newInstance(xof);
        if(f==null)
            f = new Default(xof);

        theInstance = f;
    }

    /**
     * See {@link #create(OutputStream)} for the contract.
     * This method may be invoked concurrently.
     */
    public abstract XMLStreamWriter doCreate(OutputStream out);

    /**
     * See {@link #create(OutputStream,String)} for the contract.
     * This method may be invoked concurrently.
     */
    public abstract XMLStreamWriter doCreate(OutputStream out, String encoding);

    /**
     * See {@link #recycle(XMLStreamWriter)} for the contract.
     * This method may be invoked concurrently.
     */
    public abstract void doRecycle(XMLStreamWriter r);

    /**
     * Should be invoked when the code finished using an {@link XMLStreamWriter}.
     *
     * <p>
     * If the recycled instance implements {@link RecycleAware},
     * {@link RecycleAware#onRecycled()} will be invoked to let the instance
     * know that it's being recycled.
     *
     * <p>
     * It is not a hard requirement to call this method on every {@link XMLStreamReader}
     * instance. Not doing so just reduces the performance by throwing away
     * possibly reusable instances. So the caller should always consider the effort
     * it takes to recycle vs the possible performance gain by doing so.
     *
     * <p>
     * This method may be invked by multiple threads concurrently.
     *
     * @param r
     *      The {@link XMLStreamReader} instance that the caller finished using.
     *      This could be any {@link XMLStreamReader} implementation, not just
     *      the ones that were created from this factory. So the implementation
     *      of this class needs to be aware of that.
     */
    public static void recycle(XMLStreamWriter r) {
        get().doRecycle(r);
    }

    /**
     * Interface that can be implemented by {@link XMLStreamWriter} to
     * be notified when it's recycled.
     *
     * <p>
     * This provides a filtering {@link XMLStreamWriter} an opportunity to
     * recycle its inner {@link XMLStreamWriter}.
     */
    public interface RecycleAware {
        void onRecycled();
    }

    /**
     * Gets the singleton instance.
     */
    public static @NotNull XMLStreamWriterFactory get() {
        return theInstance;
    }

    /**
     * Overrides the singleton {@link XMLStreamWriterFactory} instance that
     * the JAX-WS RI uses.
     *
     * @param f
     *      must not be null.
     */
    public static void set(@NotNull XMLStreamWriterFactory f) {
        if(f==null) throw new IllegalArgumentException();
        theInstance = f;
    }

    /**
     * Short-cut for {@link #create(OutputStream, String)} with UTF-8.
     */
    public static XMLStreamWriter create(OutputStream out) {
        return get().doCreate(out);
    }

    public static XMLStreamWriter create(OutputStream out, String encoding) {
        return get().doCreate(out, encoding);
    }

    /**
     * @deprecated
     *      Use {@link #create(OutputStream)}
     */
    public static XMLStreamWriter createXMLStreamWriter(OutputStream out) {
        return create(out);
    }

    /**
     * @deprecated
     *      Use {@link #create(OutputStream, String)}
     */
    public static XMLStreamWriter createXMLStreamWriter(OutputStream out, String encoding) {
        return create(out, encoding);
    }

    /**
     * @deprecated
     *      Use {@link #create(OutputStream, String)}. The boolean flag was unused anyway.
     */
    public static XMLStreamWriter createXMLStreamWriter(OutputStream out, String encoding, boolean declare) {
        return create(out,encoding);
    }

    /**
     * Default {@link XMLStreamWriterFactory} implementation
     * that can work with any {@link XMLOutputFactory}.
     *
     * <p>
     * {@link XMLOutputFactory} is not required to be thread-safe, so the
     * create method on this implementation is synchronized.
     */
    public static final class Default extends XMLStreamWriterFactory {
        private final XMLOutputFactory xof;

        public Default(XMLOutputFactory xof) {
            this.xof = xof;
        }

        public XMLStreamWriter doCreate(OutputStream out) {
            return doCreate(out,"UTF-8");
        }

        public synchronized XMLStreamWriter doCreate(OutputStream out, String encoding) {
            try {
                return xof.createXMLStreamWriter(out,encoding);
            } catch (XMLStreamException e) {
                throw new XMLReaderException("stax.cantCreate",e);
            }
        }

        public void doRecycle(XMLStreamWriter r) {
            // no recycling
        }
    }

    /**
     * {@link XMLStreamWriterFactory} implementation for Sun's StaX implementation.
     *
     * <p>
     * This implementation supports instance reuse.
     */
    public static final class Zephyr extends XMLStreamWriterFactory {
        private final XMLOutputFactory xof;
        private final ThreadLocal<XMLStreamWriter> pool = new ThreadLocal<XMLStreamWriter>();
        private final Method resetMethod;
        private final Method setOutputMethod;
        private final Class zephyrClass;

        public static XMLStreamWriterFactory newInstance(XMLOutputFactory xof) {
            try {
                Class<?> clazz = xof.createXMLStreamWriter(new StringWriter()).getClass();

                if(!clazz.getName().startsWith("com.sun.xml.stream."))
                return null;    // nope

                return new Zephyr(xof,clazz);
            } catch (XMLStreamException e) {
                return null;    // impossible
            } catch (NoSuchMethodException e) {
                return null;    // this xof wasn't Zephyr
            }
        }

        private Zephyr(XMLOutputFactory xof, Class clazz) throws NoSuchMethodException {
            this.xof = xof;

            zephyrClass = clazz;
            setOutputMethod = clazz.getMethod("setOutput", StreamResult.class, String.class);
            resetMethod = clazz.getMethod("reset");
        }

        /**
         * Fetchs an instance from the pool if available, otherwise null.
         */
        private @Nullable XMLStreamWriter fetch() {
            XMLStreamWriter sr = pool.get();
            if(sr==null)    return null;
            pool.set(null);
            return sr;
        }

        public XMLStreamWriter doCreate(OutputStream out) {
            return doCreate(out,"UTF-8");
        }

        public XMLStreamWriter doCreate(OutputStream out, String encoding) {
            XMLStreamWriter xsw = fetch();
            if(xsw!=null) {
                // try to reuse
                try {
                    resetMethod.invoke(xsw);
                    setOutputMethod.invoke(xsw,new StreamResult(out),encoding);
                    return xsw;
                } catch (IllegalAccessException e) {
                    throw new XMLReaderException("stax.cantCreate",e);
                } catch (InvocationTargetException e) {
                    throw new XMLReaderException("stax.cantCreate",e);
                }
            }

            // create a new instance
            try {
                return xof.createXMLStreamWriter(out,encoding);
            } catch (XMLStreamException e) {
                throw new XMLReaderException("stax.cantCreate",e);
            }
        }

        public void doRecycle(XMLStreamWriter r) {
            if(zephyrClass.isInstance(r)) {
                // this flushes the underlying stream, so it might cause chunking issue 
                try {
                    r.close();
                } catch (XMLStreamException e) {
                    throw new WebServiceException(e);
                }
                pool.set(r);
            }
            if(r instanceof RecycleAware)
                ((RecycleAware)r).onRecycled();
        }
    }
}
