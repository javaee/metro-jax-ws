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

package com.sun.xml.ws.api.pipe.helper;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Fiber;
import com.sun.xml.ws.api.pipe.NextAction;
import com.sun.xml.ws.api.pipe.Pipe;
import com.sun.xml.ws.api.pipe.PipeCloner;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;

/**
 * {@link Tube} that invokes {@link Pipe}.
 *
 * <p>
 * This can be used to make a {@link Pipe} look like a {@link Tube}.
 *
 * @author Kohsuke Kawaguchi
 * @author Jitendra Kotamraju
 */
public class PipeAdapter extends AbstractTubeImpl {
    private final Pipe next;

    public static Tube adapt(Pipe p) {
        if (p instanceof Tube) {
            return (Tube) p;
        } else {
            return new PipeAdapter(p);
        }
    }

    public static Pipe adapt(Tube p) {
        if (p instanceof Pipe) {
            return (Pipe) p;
        } else {
            class TubeAdapter extends AbstractPipeImpl {
                private final Tube t;

                public TubeAdapter(Tube t) {
                    this.t = t;
                }

                private TubeAdapter(TubeAdapter that, PipeCloner cloner) {
                    super(that, cloner);
                    this.t = cloner.copy(that.t);
                }

                public Packet process(Packet request) {
                    return Fiber.current().runSync(t,request);
                }

                public Pipe copy(PipeCloner cloner) {
                    return new TubeAdapter(this,cloner);
                }
            }

            return new TubeAdapter(p);
        }
    }


    private PipeAdapter(Pipe next) {
        this.next = next;
    }

    /**
     * Copy constructor
     */
    private PipeAdapter(PipeAdapter that, TubeCloner cloner) {
        super(that,cloner);
        this.next = ((PipeCloner)cloner).copy(that.next);
    }

    /**
     * Uses the current fiber and runs the whole pipe to the completion
     * (meaning everything from now on will run synchronously.)
     */
    public @NotNull NextAction processRequest(@NotNull Packet p) {
        return doReturnWith(next.process(p));
    }

    public @NotNull NextAction processResponse(@NotNull Packet p) {
        throw new IllegalStateException();
    }

    @NotNull
    public NextAction processException(@NotNull Throwable t) {
        throw new IllegalStateException();
    }

    public void preDestroy() {
        next.preDestroy();
    }

    public PipeAdapter copy(TubeCloner cloner) {
        return new PipeAdapter(this,cloner);
    }

    public String toString() {
        return super.toString()+"["+next.toString()+"]";
    }
}
