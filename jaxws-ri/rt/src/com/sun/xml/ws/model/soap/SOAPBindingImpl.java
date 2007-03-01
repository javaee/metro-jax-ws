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
package com.sun.xml.ws.model.soap;

import com.sun.xml.ws.api.model.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import com.sun.xml.ws.api.SOAPVersion;

/**
 * A wsdl:opeartion binding object that represents soap:binding. This can be
 * the return of {@link com.sun.xml.ws.api.model.JavaMethod#getBinding()}.
 * <p/>
 * the default values are always document/literal and SoapVersion is SOAP 1.1.
 *
 * @author Vivek Pandey
 */
public class SOAPBindingImpl extends SOAPBinding {
    public SOAPBindingImpl() {
    }

    public SOAPBindingImpl(SOAPBinding sb) {
        this.use = sb.getUse();
        this.style = sb.getStyle();
        this.soapVersion = sb.getSOAPVersion();
        this.soapAction = sb.getSOAPAction();
    }

    /**
     * @param style The style to set.
     */
    public void setStyle(Style style) {
        this.style = style;
    }

    /**
     * @param version
     */
    public void setSOAPVersion(SOAPVersion version) {
        this.soapVersion = version;
    }

    /**
     * @param soapAction The soapAction to set.
     */
    public void setSOAPAction(String soapAction) {
        this.soapAction = soapAction;
    }
}
