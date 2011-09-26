/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
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

package com.sun.xml.ws.api;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SAAJMetaFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;

/**
 * Factory SPI for SAAJ implementations
 * 
 * @since 2.2.6
 */
public class SAAJFactory {
    /**
     * Creates a new <code>MessageFactory</code> object that is an instance
     * of the specified implementation.  May be a dynamic message factory,
     * a SOAP 1.1 message factory, or a SOAP 1.2 message factory. A dynamic
     * message factory creates messages based on the MIME headers specified
     * as arguments to the <code>createMessage</code> method.
     *
     * This method uses the SAAJMetaFactory to locate the implementation class 
     * and create the MessageFactory instance.
     * 
     * @return a new instance of a <code>MessageFactory</code>
     *
     * @param protocol  a string constant representing the class of the
     *                   specified message factory implementation. May be
     *                   either <code>DYNAMIC_SOAP_PROTOCOL</code>,
     *                   <code>DEFAULT_SOAP_PROTOCOL</code> (which is the same
     *                   as) <code>SOAP_1_1_PROTOCOL</code>, or
     *                   <code>SOAP_1_2_PROTOCOL</code>.
     *
     * @exception SOAPException if there was an error in creating the
     *            specified implementation of  <code>MessageFactory</code>.
     * @see SAAJMetaFactory
     */
	public MessageFactory createMessageFactory(String saajFactoryString) throws SOAPException {
		return MessageFactory.newInstance(saajFactoryString);
	}
	
    /**
     * Creates a new <code>SOAPFactory</code> object that is an instance of
     * the specified implementation, this method uses the SAAJMetaFactory to 
     * locate the implementation class and create the SOAPFactory instance.
     *
     * @return a new instance of a <code>SOAPFactory</code>
     *
     * @param protocol  a string constant representing the protocol of the
     *                   specified SOAP factory implementation. May be
     *                   either <code>DYNAMIC_SOAP_PROTOCOL</code>,
     *                   <code>DEFAULT_SOAP_PROTOCOL</code> (which is the same
     *                   as) <code>SOAP_1_1_PROTOCOL</code>, or
     *                   <code>SOAP_1_2_PROTOCOL</code>.
     *
     * @exception SOAPException if there was an error creating the
     *            specified <code>SOAPFactory</code>
     * @see SAAJMetaFactory
     */
	public SOAPFactory createSOAPFactory(String saajFactoryString) throws SOAPException {
		return SOAPFactory.newInstance(saajFactoryString);
	}
}
