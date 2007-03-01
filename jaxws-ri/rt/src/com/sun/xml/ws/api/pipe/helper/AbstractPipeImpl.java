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

package com.sun.xml.ws.api.pipe.helper;

import com.sun.xml.ws.api.pipe.Pipe;
import com.sun.xml.ws.api.pipe.PipeCloner;

/**
 * Partial default implementation of {@link Pipe}.
 *
 * <p>
 * To be shielded from potentail changes in JAX-WS,
 * please consider extending from this class, instead
 * of implementing {@link Pipe} directly.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class AbstractPipeImpl implements Pipe {

    /**
     * Do-nothing constructor.
     */
    protected AbstractPipeImpl() {
    }

    /**
     * Basis for the copy constructor.
     *
     * <p>
     * This registers the newly created {@link Pipe} with the {@link PipeCloner}
     * through {@link PipeCloner#add(Pipe, Pipe)}.
     */
    protected AbstractPipeImpl(Pipe that, PipeCloner cloner) {
        cloner.add(that,this);
    }

    public void preDestroy() {
        // noop
    }
}
