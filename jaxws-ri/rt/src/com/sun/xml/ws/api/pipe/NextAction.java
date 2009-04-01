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

package com.sun.xml.ws.api.pipe;

import com.sun.xml.ws.api.message.Packet;

/**
 * Indicates what shall happen after {@link Tube#processRequest(Packet)} or
 * {@link Tube#processResponse(Packet)} returns.
 *
 * <p>
 * To allow reuse of this object, this class is mutable.
 *
 * @author Kohsuke Kawaguchi
 */
public final class NextAction {
    int kind;
    Tube next;
    Packet packet;
    /**
     * Really either {@link RuntimeException} or {@link Error}.
     */
    Throwable throwable;

    // public enum Kind { INVOKE, INVOKE_AND_FORGET, RETURN, SUSPEND }

    static final int INVOKE = 0;
    static final int INVOKE_AND_FORGET = 1;
    static final int RETURN = 2;
    static final int THROW = 3;
    static final int SUSPEND = 4;

    private void set(int k, Tube v, Packet p, Throwable t) {
        this.kind = k;
        this.next = v;
        this.packet = p;
        this.throwable = t;
    }

    /**
     * Indicates that the next action should be to
     * invoke the next tube's {@link Tube#processRequest(Packet)},
     * then later invoke the current tube's {@link Tube#processResponse(Packet)}
     * with the response packet.
     */
    public void invoke(Tube next, Packet p) {
        set(INVOKE, next, p, null);
    }

    /**
     * Indicates that the next action should be to
     * invoke the next tube's {@link Tube#processRequest(Packet)},
     * but the current tube doesn't want to receive the response packet to
     * its {@link Tube#processResponse(Packet)}.
     */
    public void invokeAndForget(Tube next, Packet p) {
        set(INVOKE_AND_FORGET, next, p, null);
    }

    /**
     * Indicates that the next action is to flip the processing direction
     * and starts response processing.
     */
    public void returnWith( Packet response ) {
        set(RETURN, null, response, null);
    }

    /**
     * Indicates that the next action is to flip the processing direction
     * and starts exception processing.
     *
     * @param t
     *      Either {@link RuntimeException} or {@link Error}, but defined to
     *      take {@link Throwable} because {@link Tube#processException(Throwable)}
     *      takes {@link Throwable}.
     */
    public void throwException(Throwable t) {
        assert t instanceof RuntimeException || t instanceof Error;
        set(THROW,null,null,t);
    }

    /**
     * Indicates that the fiber should be suspended.
     * Once {@link Fiber#resume(Packet) resumed}, return the response processing.
     */
    public void suspend() {
        set(SUSPEND, null, null, null);
    }

    /**
     * Indicates that the fiber should be suspended.
     * Once {@link Fiber#resume(Packet) resumed}, resume with the
     * {@link Tube#processRequest(Packet)} on the given next tube.
     */
    public void suspend(Tube next) {
        set(SUSPEND, next, null, null);
    }

    public Tube getNext() {
        return next;
    }

    public Packet getPacket() {
        return packet;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    /**
     * Dumps the contents to assist debugging.
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(super.toString()).append(" [");
        buf.append("kind=").append(getKindString()).append(',');
        buf.append("next=").append(next).append(',');
        buf.append("packet=").append(packet).append(',');
        buf.append("throwable=").append(throwable).append(']');
        return buf.toString();
    }

    /**
     * Returns {@link #kind} in a human readable string, to assist debugging.
     */
    public String getKindString() {
        switch(kind) {
        case INVOKE:            return "INVOKE";
        case INVOKE_AND_FORGET: return "INVOKE_AND_FORGET";
        case RETURN:            return "RETURN";
        case THROW:             return "THROW";
        case SUSPEND:           return "SUSPEND";
        default:                throw new AssertionError(kind);
        }
    }
}
