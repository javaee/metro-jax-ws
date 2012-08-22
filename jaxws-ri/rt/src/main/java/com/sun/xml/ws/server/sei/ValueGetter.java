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

package com.sun.xml.ws.server.sei;

import com.sun.xml.ws.model.ParameterImpl;
import com.sun.xml.ws.api.model.Parameter;

import javax.jws.WebParam.Mode;
import javax.xml.ws.Holder;

/**
 * Gets a value from an object that represents a parameter passed
 * as a method argument.
 *
 * <p>
 * This abstraction hides the handling of {@link Holder}.
 *
 * <p>
 * {@link ValueGetter} is a stateless behavior encapsulation.
 *
 * @author Kohsuke Kawaguchi
 */
public enum ValueGetter {
    /**
     * {@link ValueGetter} that works for {@link Mode#IN}  parameter.
     *
     * <p>
     * Since it's the IN mode, the parameter is not a {@link Holder},
     * therefore the parameter itself is a value.
     */
    PLAIN() {
        public Object get(Object parameter) {
            return parameter;
        }
    },
    /**
     * Creates {@link ValueGetter} that works for {@link Holder},
     * which is  {@link Mode#INOUT} or  {@link Mode#OUT}.
     *
     * <p>
     * In those {@link Mode}s, the parameter is a {@link Holder},
     * so the value to be sent is obtained by getting the value of the holder.
     */
    HOLDER() {
        public Object get(Object parameter) {
            if(parameter==null)
                // the user is allowed to pass in null where a Holder is expected.
                return null;
            return ((Holder)parameter).value;
        }
    };

    /**
     * Gets the value to be sent, from a parameter given as a method argument.
     */
    public abstract Object get(Object parameter);

    /**
     * Returns a {@link ValueGetter} suitable for the given {@link Parameter}.
     */
    public static ValueGetter get(ParameterImpl p) {
        // return value is always PLAIN
        if(p.getMode() == Mode.IN || p.getIndex() == -1) {
            return PLAIN;
        } else {
            return HOLDER;
        }
    }
}
