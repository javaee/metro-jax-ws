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

package com.sun.xml.ws.api.handler;

import com.sun.istack.Nullable;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;

import javax.xml.ws.handler.MessageContext;
import java.util.Set;

/**
 * The <code>MessageHandlerContext</code> interface extends
 * <code>MessageContext</code> to provide easy access to the contained message.
 *
 * This context provides access to RI's <code>Message</code> model for efficient access
 * to various things like accessing headers etc. It also provides access to
 * binding information as <code>WSBinding</code>.
 *
 * @author Rama Pulavarthi
 * @since JAX-WS 2.1.3
 */
public interface MessageHandlerContext extends MessageContext {
    /**
     * Gets the message from this message context
     *
     * @return The contained message; returns <code>null</code> if no
     *         message is present in this message context
     */
    public Message getMessage();

    
    /**
     * Sets the message in this message context
     */
    public void setMessage(Message message);

    /**
     * @see javax.xml.ws.handler.soap.SOAPMessageContext#getRoles()
     */
     public Set<String> getRoles();


    /**
     * Provides access to <code>WSBinding</code> which can be used in various ways.
     * for example: <code>WSBinding#getSOAPVersion</code> to get SOAP version of the binding.
     *              <code>WSBinding#isFeatureEnabled(AddressingFeature)</code> to check if addressing is enabled
     */
    public WSBinding getWSBinding();

    /**
     * Provides access to <code>SEIModel</code>.
     */
    public @Nullable SEIModel getSEIModel();

    /**
     * Gets the {@link WSDLPort} that represents the port.
     * @return
     *      returns the WSDLModel of the port that the client/endpoint binds to.
     *      null when the Service is not configured with WSDL information.
     */
    public @Nullable WSDLPort getPort();
   
}
