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

package com.sun.xml.ws.api.pipe.helper;

import com.sun.xml.ws.api.pipe.Pipe;
import com.sun.xml.ws.api.pipe.PipeCloner;
import com.sun.xml.ws.api.message.Packet;

/**
 * Default implementation of {@link Pipe} that is used as a filter.
 *
 * <p>
 * A filter pipe works on a {@link Packet}, then pass it onto the next pipe.
 *
 *
 * <h2>How do I implement a filter?</h2>
 * <p>
 * Filter {@link Pipe}s are ideal for those components that wish to
 * do some of the followings:
 *
 * <dl>
 * <dt><b>
 * To read an incoming message and perform some work before the
 * application (or more precisely the next pipe sees it)
 * </b>
 * <dd>
 * Implement the {@link #process} method and do some processing before
 * you pass the packet to the next pipe:
 * <pre>
 * process(request) {
 *   doSomethingWith(request);
 *   return next.process(request);
 * }
 * </pre>
 *
 *
 * <dt><b>
 * To intercept an incoming message and prevent the next pipe from seeing it.
 * </b>
 * <dd>
 * Implement the {@link #process} method and do some processing,
 * then do NOT pass the request onto the next pipe.
 * <pre>
 * process(request) {
 *   if(isSomethingWrongWith(request))
 *     return createErrorMessage();
 *   else
 *     return next.proces(request);
 * }
 * </pre>
 *
 * <dt><b>
 * To post process a reply and possibly modify a message:
 * </b>
 * <dd>
 * Implement the {@link #process} method and do some processing,
 * then do NOT pass the request onto the next pipe.
 * <pre>
 * process(request) {
 *   op = request.getMessage().getOperation();
 *   reply = next.proces(request);
 *   if(op is something I care) {
 *     reply = playWith(reply);
 *   }
 *   return reply;
 * }
 * </pre>
 *
 * </dl>
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class AbstractFilterPipeImpl extends AbstractPipeImpl {
    /**
     * Next pipe to call.
     */
    protected final Pipe next;

    protected AbstractFilterPipeImpl(Pipe next) {
        this.next = next;
        assert next!=null;
    }

    protected AbstractFilterPipeImpl(AbstractFilterPipeImpl that, PipeCloner cloner) {
        super(that, cloner);
        this.next = cloner.copy(that.next);
        assert next!=null;
    }

    public Packet process(Packet packet) {
        return next.process(packet);
    }

    @Override
    public void preDestroy() {
        next.preDestroy();
    }
}
