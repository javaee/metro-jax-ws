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

/*
 * SOAPHandlerProcessor.java
 *
 * Created on February 8, 2006, 5:43 PM
 *
 *
 */

package com.sun.xml.ws.handler;

import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.encoding.soap.SOAP12Constants;
import com.sun.xml.ws.encoding.soap.SOAPConstants;
import java.util.List;
import java.util.logging.Level;
import javax.xml.namespace.QName;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.handler.Handler;

/**
 *
 * @author WS Development Team
 */
final class SOAPHandlerProcessor<C extends MessageUpdatableContext> extends HandlerProcessor<C> {

    /**
     * Creates a new instance of SOAPHandlerProcessor
     */
    public SOAPHandlerProcessor(boolean isClient, HandlerTube owner, WSBinding binding, List<? extends Handler> chain) {
        super(owner, binding, chain);
        this.isClient = isClient;
    }
    
    /**
     * Replace the message in the given message context with a
     * fault message. If the context already contains a fault
     * message, then return without changing it.
     *
     * <p>This method should only be called during a request,
     * because during a response an exception from a handler
     * is dispatched rather than replacing the message with
     * a fault. So this method can use the MESSAGE_OUTBOUND_PROPERTY
     * to determine whether it is being called on the client
     * or the server side. If this changes in the spec, then
     * something else will need to be passed to the method
     * to determine whether the fault code is client or server.
     */
    final void insertFaultMessage(C context,
        ProtocolException exception) {
        try {
            if(!context.getPacketMessage().isFault()) {
                Message faultMessage = Messages.create(binding.getSOAPVersion(),
                        exception,determineFaultCode(binding.getSOAPVersion()));
                context.setPacketMessage(faultMessage);
            }
        } catch (Exception e) {
            // severe since this is from runtime and not handler
            logger.log(Level.SEVERE,
                "exception while creating fault message in handler chain", e);
            throw new RuntimeException(e);
        }
    }
    
    /**
     * <p>Figure out if the fault code local part is client,
     * server, sender, receiver, etc. This is called by
     * insertFaultMessage.
     */
    private QName determineFaultCode(SOAPVersion soapVersion) {
        return isClient ? soapVersion.faultCodeClient : soapVersion.faultCodeServer;
    }

}
