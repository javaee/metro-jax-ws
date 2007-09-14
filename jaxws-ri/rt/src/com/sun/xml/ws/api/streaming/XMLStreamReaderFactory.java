/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

package com.sun.xml.ws.api.streaming;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.streaming.XMLReaderException;
import org.xml.sax.InputSource;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.logging.Logger;

/**
 * Factory for {@link XMLStreamReader}.
 *
 * <p>
 * This wraps {@link XMLInputFactory} and allows us to reuse {@link XMLStreamReader} instances
 * when appropriate.
 * 
 * @author Kohsuke Kawaguchi
 */
public abstract class XMLStreamReaderFactory {

    private static final Logger LOGGER = Logger.getLogger(XMLStreamReaderFactory.class.getName());

    /**
     * Singleton instance.
     */
    private static volatile @NotNull XMLStreamReaderFactory theInstance;

    static {
        XMLInputFactory xif = null;
        if (Boolean.getBoolean(XMLStreamReaderFactory.class.getName()+".woodstox")) {
            try {
                xif = (XMLInputFactory)Class.forName("com.ctc.wstx.stax.WstxInputFactory").newInstance();
            } catch (Exception e) {
                // Ignore and fallback to default XMLInputFactory
            }
        }
        if (xif == null) {
            xif = XMLInputFactory.newInstance();
        }
        xif.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);

        XMLStreamReaderFactory f=null;

        // this system property can be used to disable the pooling altogether,
        // in case someone hits an issue with pooling in the production system.
        if(!Boolean.getBoolean(XMLStreamReaderFactory.class.getName()+".noPool"))
            f = Zephyr.newInstance(xif);

        if(f==null) {
            // is this Woodstox?
            if(xif.getClass().getName().equals("com.ctc.wstx.stax.WstxInputFactory"))
                f = new Woodstox(xif);
        }

        if(f==null)
            f = new Default(xif);

        theInstance = f;
        LOGGER.fine("XMLStreamReaderFactory instance is = "+theInstance);
    }

    /**
     * Overrides the singleton {@link XMLStreamReaderFactory} instance that
     * the JAX-WS RI uses.
     */
    public static void set(XMLStreamReaderFactory f) {
        if(f==null) throw new IllegalArgumentException();
        theInstance = f;
    }

    public static XMLStreamReaderFactory get() {
        return theInstance;
    }

    public static XMLStreamReader create(InputSource source, boolean rejectDTDs) {
        try {
            // Char stream available?
            if (source.getCharacterStream() != null) {
                return get().doCreate(source.getSystemId(), source.getCharacterStream(), rejectDTDs);
            }

            // Byte stream available?
            if (source.getByteStream() != null) {
                return get().doCreate(source.getSystemId(), source.getByteStream(), rejectDTDs);
            }

            // Otherwise, open URI
            return get().doCreate(source.getSystemId(), new URL(source.getSystemId()).openStream(),rejectDTDs);
        } catch (IOException e) {
            throw new XMLReaderException("stax.cantCreate",e);
        }
    }

    public static XMLStreamReader create(@Nullable String systemId, InputStream in, boolean rejectDTDs) {
        return get().doCreate(systemId,in,rejectDTDs);
    }

    public static XMLStreamReader create(@Nullable String systemId, InputStream in, @Nullable String encoding, boolean rejectDTDs) {
        return (encoding == null)
                ? create(systemId, in, rejectDTDs)
                : get().doCreate(systemId,in,encoding,rejectDTDs);
    }

    public static XMLStreamReader create(@Nullable String systemId, Reader reader, boolean rejectDTDs) {
        return get().doCreate(systemId,reader,rejectDTDs);
    }

    /**
     * Should be invoked when the code finished using an {@link XMLStreamReader}.
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
    public static void recycle(XMLStreamReader r) {
        get().doRecycle(r);
    }

    // implementations

    public abstract XMLStreamReader doCreate(String systemId, InputStream in, boolean rejectDTDs);

    private XMLStreamReader doCreate(String systemId, InputStream in, @NotNull String encoding, boolean rejectDTDs) {
        Reader reader;
        try {
            reader = new InputStreamReader(in, encoding);
        } catch(UnsupportedEncodingException ue) {
            throw new XMLReaderException("stax.cantCreate", ue);
        }
        return doCreate(systemId, reader, rejectDTDs);
    }

    public abstract XMLStreamReader doCreate(String systemId, Reader reader, boolean rejectDTDs);

    public abstract void doRecycle(XMLStreamReader r);

    /**
     * Interface that can be implemented by {@link XMLStreamReader} to
     * be notified when it's recycled.
     *
     * <p>
     * This provides a filtering {@link XMLStreamReader} an opportunity to
     * recycle its inner {@link XMLStreamReader}.
     */
    public interface RecycleAware {
        void onRecycled();
    }

    /**
     * {@link XMLStreamReaderFactory} implementation for SJSXP/JAXP RI.
     */
    public static final class Zephyr extends XMLStreamReaderFactory {
        private final XMLInputFactory xif;

        private final ThreadLocal<XMLStreamReader> pool = new ThreadLocal<XMLStreamReader>();

        /**
         * Sun StAX impl <code>XMLReaderImpl.setInputSource()</code> method via reflection.
         */
        private final Method setInputSourceMethod;

        /**
         * Sun StAX impl <code>XMLReaderImpl.reset()</code> method via reflection.
         */
        private final Method resetMethod;

        /**
         * The Sun StAX impl's {@link XMLStreamReader} implementation clas.
         */
        private final Class zephyrClass;

        /**
         * Creates {@link Zephyr} instance if the given {@link XMLInputFactory} is the one
         * from Zephyr.
         */
        public static @Nullable
        XMLStreamReaderFactory newInstance(XMLInputFactory xif) {
            // check if this is from Zephyr
            try {
                Class<?> clazz = xif.createXMLStreamReader(new StringReader("<foo/>")).getClass();

                if(!clazz.getName().startsWith("com.sun.xml.stream."))
                    return null;    // nope

                return new Zephyr(xif,clazz);
            } catch (NoSuchMethodException e) {
                return null;    // this factory is not for zephyr
            } catch (XMLStreamException e) {
                return null;    // impossible to fail to parse <foo/>, but anyway
            }
        }

        public Zephyr(XMLInputFactory xif, Class clazz) throws NoSuchMethodException {
            zephyrClass = clazz;
            setInputSourceMethod = clazz.getMethod("setInputSource", InputSource.class);
            resetMethod = clazz.getMethod("reset");

            try {
                // Turn OFF internal factory caching in Zephyr.
                // Santiago told me that this makes it thread-safe.
                xif.setProperty("reuse-instance", false);
            } catch (IllegalArgumentException e) {
                // falls through
            }
            this.xif = xif;
        }

        /**
         * Fetchs an instance from the pool if available, otherwise null.
         */
        private @Nullable XMLStreamReader fetch() {
            XMLStreamReader sr = pool.get();
            if(sr==null)    return null;
            pool.set(null);
            return sr;
        }

        public void doRecycle(XMLStreamReader r) {
            if(zephyrClass.isInstance(r))
                pool.set(r);
            if(r instanceof RecycleAware)
                ((RecycleAware)r).onRecycled();
        }

        public XMLStreamReader doCreate(String systemId, InputStream in, boolean rejectDTDs) {
            try {
                XMLStreamReader xsr = fetch();
                if(xsr==null)
                    return xif.createXMLStreamReader(systemId,in);

                // try re-using this instance.
                InputSource is = new InputSource(systemId);
                is.setByteStream(in);
                reuse(xsr,is);
                return xsr;
            } catch (IllegalAccessException e) {
                throw new XMLReaderException("stax.cantCreate",e);
            } catch (InvocationTargetException e) {
                throw new XMLReaderException("stax.cantCreate",e);
            } catch (XMLStreamException e) {
                throw new XMLReaderException("stax.cantCreate",e);
            }
        }

        public XMLStreamReader doCreate(String systemId, Reader in, boolean rejectDTDs) {
            try {
                XMLStreamReader xsr = fetch();
                if(xsr==null)
                    return xif.createXMLStreamReader(systemId,in);

                // try re-using this instance.
                InputSource is = new InputSource(systemId);
                is.setCharacterStream(in);
                reuse(xsr,is);
                return xsr;
            } catch (IllegalAccessException e) {
                throw new XMLReaderException("stax.cantCreate",e);
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (cause == null) {
                    cause = e;
                }
                throw new XMLReaderException("stax.cantCreate", cause);
            } catch (XMLStreamException e) {
                throw new XMLReaderException("stax.cantCreate",e);
            }
        }

        private void reuse(XMLStreamReader xsr, InputSource in) throws IllegalAccessException, InvocationTargetException {
            resetMethod.invoke(xsr);
            setInputSourceMethod.invoke(xsr,in);
        }
    }

    /**
     * Default {@link XMLStreamReaderFactory} implementation
     * that can work with any {@link XMLInputFactory}.
     *
     * <p>
     * {@link XMLInputFactory} is not required to be thread-safe, so the
     * create method on this implementation is synchronized.
     */
    public static final class Default extends NoLock {
        public Default(XMLInputFactory xif) {
            super(xif);
        }

        public synchronized XMLStreamReader doCreate(String systemId, InputStream in, boolean rejectDTDs) {
            return super.doCreate(systemId, in, rejectDTDs);
        }

        public synchronized XMLStreamReader doCreate(String systemId, Reader in, boolean rejectDTDs) {
            return super.doCreate(systemId, in, rejectDTDs);
        }
    }

    /**
     * Similar to {@link Default} but doesn't do any synchronization.
     *
     * <p>
     * This is useful when you know your {@link XMLInputFactory} is thread-safe by itself.
     */
    public static class NoLock extends XMLStreamReaderFactory {
        private final XMLInputFactory xif;

        public NoLock(XMLInputFactory xif) {
            this.xif = xif;
        }

        public XMLStreamReader doCreate(String systemId, InputStream in, boolean rejectDTDs) {
            try {
                return xif.createXMLStreamReader(systemId,in);
            } catch (XMLStreamException e) {
                throw new XMLReaderException("stax.cantCreate",e);
            }
        }

        public XMLStreamReader doCreate(String systemId, Reader in, boolean rejectDTDs) {
            try {
                return xif.createXMLStreamReader(systemId,in);
            } catch (XMLStreamException e) {
                throw new XMLReaderException("stax.cantCreate",e);
            }
        }

        public void doRecycle(XMLStreamReader r) {
            // there's no way to recycle with the default StAX API.
        }
    }

    /**
     * Handles Woodstox's XIF but set properties to do the string interning.
     * Woodstox {@link XMLInputFactory} is thread safe.
     */
    public static final class Woodstox extends NoLock {
        public Woodstox(XMLInputFactory xif) {
            super(xif);
            xif.setProperty("org.codehaus.stax2.internNsUris",true);
        }

        public XMLStreamReader doCreate(String systemId, InputStream in, boolean rejectDTDs) {
            return super.doCreate(systemId, in, rejectDTDs);
        }

        public XMLStreamReader doCreate(String systemId, Reader in, boolean rejectDTDs) {
            return super.doCreate(systemId, in, rejectDTDs);
        }
    }
}
