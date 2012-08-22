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

package com.sun.xml.ws.api.model.soap;

import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.model.JavaMethod;

import javax.jws.soap.SOAPBinding.Style;
import javax.jws.soap.SOAPBinding.Use;
import javax.jws.WebMethod;

/**
 * Models soap:binding in a WSDL document or a {@link javax.jws.soap.SOAPBinding} annotation. This
 * can be the return of {@link JavaMethod#getBinding()}.
 *
 * @author Vivek Pandey
 */
abstract public class SOAPBinding {
    protected Use use = Use.LITERAL;
    protected Style style = Style.DOCUMENT;
    protected SOAPVersion soapVersion = SOAPVersion.SOAP_11;
    protected String soapAction = "";

    /**
     * Get {@link Use} such as <code>literal</code> or <code>encoded</code>.
     */
    public Use getUse() {
        return use;
    }

    /**
     * Get {@link Style} - such as <code>document</code> or <code>rpc</code>.
     */
    public Style getStyle() {
        return style;
    }

    /**
     * Get the {@link SOAPVersion}
     */
    public SOAPVersion getSOAPVersion() {
        return soapVersion;
    }

    /**
     * Returns true if its document/literal
     */
    public boolean isDocLit() {
        return style == Style.DOCUMENT && use == Use.LITERAL;
    }

    /**
     * Returns true if this is a rpc/literal binding
     */
    public boolean isRpcLit() {
        return style == Style.RPC && use == Use.LITERAL;
    }

    /**
     * Value of <code>wsdl:binding/wsdl:operation/soap:operation@soapAction</code> attribute or
     * {@link WebMethod#action()} annotation.
     * <pre>
     * For example:
     * &lt;wsdl:binding name="HelloBinding" type="tns:Hello">
     *   &lt;soap:binding style="document" transport="http://schemas.xmlsoap.org/soap/http"/>
     *   &lt;wsdl:operation name="echoData">
     *       &lt;soap12:operation soapAction=""/>
     * ...
     * </pre>
     * It's always non-null. soap message serializer needs to generated SOAPAction HTTP header with
     * the return of this method enclosed in quotes("").
     *
     * @see com.sun.xml.ws.api.message.Packet#soapAction
     */
    public String getSOAPAction() {
        return soapAction;
    }
}
