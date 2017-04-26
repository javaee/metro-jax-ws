/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2013 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.util;

import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.lang.ref.SoftReference;


/**
 * General-purpose object pool.
 *
 * <p>
 * In many parts of the runtime, we need to pool instances of objects that
 * are expensive to create (such as JAXB objects, StAX parsers, {@link Tube} instances.)
 *
 * <p>
 * This class provides a default implementation of such a pool.
 *
 * TODO: improve the implementation
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class Pool<T> {

    // volatile since multiple threads may access queue reference
    private volatile SoftReference<ConcurrentLinkedQueue<T>> queue;

    /**
     * Gets a new object from the pool.
     *
     * <p>
     * If no object is available in the pool, this method creates a new one.
     *
     * @return
     *      always non-null.
     */
    public final T take() {
        T t = getQueue().poll();
        if(t==null)
            return create();
        return t;
    }

    private ConcurrentLinkedQueue<T> getQueue() {
    	SoftReference<ConcurrentLinkedQueue<T>> q = queue;
        if (q != null) {
            ConcurrentLinkedQueue<T> d = q.get();
            if (d != null)
                return d;
        }

        // overwrite the queue
        ConcurrentLinkedQueue<T> d = new ConcurrentLinkedQueue<T>();
        queue = new SoftReference<ConcurrentLinkedQueue<T>>(d);

        return d;
    }

    /**
     * Returns an object back to the pool.
     */
    public final void recycle(T t) {
        getQueue().offer(t);
    }

    /**
     * Creates a new instance of object.
     *
     * <p>
     * This method is used when someone wants to
     * {@link #take() take} an object from an empty pool.
     *
     * <p>
     * Also note that multiple threads may call this method
     * concurrently.
     */
    protected abstract T create();


    /**
     * JAXB {@link javax.xml.bind.Marshaller} pool.
     */
    public static final class Marshaller extends Pool<javax.xml.bind.Marshaller> {
        private final JAXBContext context;

        public Marshaller(JAXBContext context) {
            this.context = context;
        }

        @Override
        protected javax.xml.bind.Marshaller create() {
            try {
                return context.createMarshaller();
            } catch (JAXBException e) {
                // impossible
                throw new AssertionError(e);
            }
        }
    }

    /**
     * JAXB {@link javax.xml.bind.Marshaller} pool.
     */
    public static final class Unmarshaller extends Pool<javax.xml.bind.Unmarshaller> {
        private final JAXBContext context;

        public Unmarshaller(JAXBContext context) {
            this.context = context;
        }

        @Override
        protected javax.xml.bind.Unmarshaller create() {
            try {
                return context.createUnmarshaller();
            } catch (JAXBException e) {
                // impossible
                throw new AssertionError(e);
            }
        }
    }

    /**
     * {@link Tube} pool.
     */
    public static final class TubePool extends Pool<Tube> {
        private final Tube master;

        public TubePool(Tube master) {
            this.master = master;
            recycle(master);    // we'll use master as a part of the pool, too.
        }

        @Override
        protected Tube create() {
            return TubeCloner.clone(master);
        }
        
        /**
         *
         * @return master tubeline from pool
         * @deprecated Expected to be used in rare cases where access to master 
         * tubeline is required and safe, such as Stub.close()."
         */
        @Deprecated()
        public final Tube takeMaster() {
            return master;
        }
        
    }
}
