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

package com.sun.xml.ws.server.sei;

import com.sun.xml.ws.api.model.Parameter;
import com.sun.xml.ws.model.ParameterImpl;

import javax.xml.ws.Holder;

/**
 * Moves a Java value unmarshalled from a response message
 * to the right place.
 *
 * <p>
 * Sometimes values are returned as a return value, and
 * others are returned in the {@link Holder} value. Instances
 * of this interface abstracts this detail.
 *
 * <p>
 * {@link EndpointValueSetter} is a stateless behavior encapsulation.
 *
 * @author Jitendra Kotamraju
 */
abstract class EndpointValueSetter {
    private EndpointValueSetter() {}

    /**
     * Moves the value to the expected place.
     *
     * @param obj
     *      The unmarshalled object.
     * @param args
     *      The arguments that need to be given to the Java method invocation. If <tt>obj</tt>
     *      is supposed to be returned as a {@link Holder} value, a suitable
     *      {@link Holder} is obtained from this argument list and <tt>obj</tt>
     *      is set.
     *
     */
    abstract void put(Object obj, Object[] args);

    /**
     * {@link Param}s with small index numbers are used often,
     * so we pool them to reduce the footprint.
     */
    private static final EndpointValueSetter[] POOL = new EndpointValueSetter[16];

    static {
        for( int i=0; i<POOL.length; i++ )
            POOL[i] = new Param(i);
    }

    /**
     * Returns a {@link EndpointValueSetter} suitable for the given {@link Parameter}.
     */
    public static EndpointValueSetter get(ParameterImpl p) {
        int idx = p.getIndex();
        if (p.isIN()) {
            if (idx<POOL.length) {
                return POOL[idx];
            } else {
                return new Param(idx);
            }
        } else {
            return new HolderParam(idx);
        }
    }

    static class Param extends EndpointValueSetter {
        /**
         * Index of the argument to put the value to.
         */
        protected final int idx;

        public Param(int idx) {
            this.idx = idx;
        }

        void put(Object obj, Object[] args) {
            if (obj != null) {
                args[idx] = obj;
            }
        }
    }
    
    static final class HolderParam extends Param {

        public HolderParam(int idx) {
            super(idx);
        }

        @Override
        void put(Object obj, Object[] args) {
            Holder holder = new Holder();
            if (obj != null) {
                holder.value = obj;
            }
            args[idx] = holder;
        }
    }
}
