/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.xml.ws.api.pipe;

import javax.xml.ws.Dispatch;

import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.oracle.webservices.api.message.BasePropertySet;
import com.oracle.webservices.api.message.PropertySet;

/**
 * When using {@link Dispatch}<{@link Packet}> and the invocation completes with a Throwable, it is
 * useful to be able to inspect the Packet in addition to the Throwable as the Packet contains 
 * meta-data about the request and/or response.  However, the default behavior is that the caller
 * only receives the Throwable.
 *
 * This {@link PropertySet} is part of the implementation that allows a completing Fiber to return
 * the Throwable to the caller as part of the Packet.
 *
 */
public class ThrowableContainerPropertySet extends BasePropertySet {

    public ThrowableContainerPropertySet(final Throwable throwable) {
        this.throwable = throwable;
    }

    ////////////////////////////////////////////////////
    //
    // The original throwable
    //
    public static final String FIBER_COMPLETION_THROWABLE = "com.sun.xml.ws.api.pipe.fiber-completion-throwable";
    private Throwable throwable;
    @Property(FIBER_COMPLETION_THROWABLE)
    public Throwable getThrowable() {
        return throwable;
    }
    public void setThrowable(final Throwable throwable) {
        this.throwable = throwable;
    }

    ////////////////////////////////////////////////////
    //
    // The FAULT message created in WsaServerTube or WSEndpointImpl
    //
    public static final String FAULT_MESSAGE = "com.sun.xml.ws.api.pipe.fiber-completion-fault-message";
    private Message faultMessage;
    @Property(FAULT_MESSAGE)
    public Message getFaultMessage() {
        return faultMessage;
    }
    public void setFaultMessage(final Message faultMessage) {
        this.faultMessage = faultMessage;
    }

    ////////////////////////////////////////////////////
    //
    // The response Packet seen in WsaServerTube.processException or WSEndpointImpl
    //
    public static final String RESPONSE_PACKET = "com.sun.xml.ws.api.pipe.fiber-completion-response-packet";
    private Packet responsePacket;
    @Property(RESPONSE_PACKET)
    public Packet getResponsePacket() {
        return responsePacket;
    }
    public void setResponsePacket(final Packet responsePacket) {
        this.responsePacket = responsePacket;
    }

    ////////////////////////////////////////////////////
    //
    // If the fault representation of the exception has already been created
    //
    public static final String IS_FAULT_CREATED = "com.sun.xml.ws.api.pipe.fiber-completion-is-fault-created";
    private boolean isFaultCreated = false;
    @Property(IS_FAULT_CREATED)
    public boolean isFaultCreated() {
        return isFaultCreated;
    }
    public void setFaultCreated(final boolean isFaultCreated) {
        this.isFaultCreated = isFaultCreated;
    }

    //
    // boilerplate
    //

    @Override
    protected PropertyMap getPropertyMap() {
        return model;
    }

    private static final PropertyMap model;
    static {
        model = parse(ThrowableContainerPropertySet.class);
    }
}
