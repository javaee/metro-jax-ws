/**
 * $Id: SOAPBinding.java,v 1.4 2005-09-10 19:47:50 kohsuke Exp $
 */

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */
package com.sun.xml.ws.model.soap;

import com.sun.xml.ws.encoding.soap.SOAPVersion;

/**
 * Binding object that represents soap:binding
 *
  * @author Vivek Pandey
 */
public class SOAPBinding {
    public SOAPBinding() {
    }

    /**
     * @return Returns the use.
     */
    public Use getUse() {
        return use;
    }

    /**
     * @param use
     *            The use to set.
     */
    public void setUse(Use use) {
        this.use = use;
    }

    /**
     * @return Returns the style.
     */
    public Style getStyle() {
        return style;
    }

    /**
     * @param style
     *            The style to set.
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
     * @return the SOAPVersion of this SOAPBinding
     */
    public SOAPVersion getSOAPVersion() {
        return soapVersion;
    }

    /**
     * @return true if this is a document/literal SOAPBinding
     */
    public boolean isDocLit() {
        return style.equals(Style.DOCUMENT) && use.equals(Use.LITERAL);
    }

    /**
     * @return true if this is a rpc/literal SOAPBinding
     */
    public boolean isRpcLit() {
        return style.equals(Style.RPC) && use.equals(Use.LITERAL);
    }

    /**
     * @return the soapAction header value. It's always non-null. soap
     *         message serializer needs to generated SOAPAction HTTP header with
     *         the return of this method enclosed in quotes("").
     */
    public String getSOAPAction() {
        if (soapAction == null)
            return "";
        return soapAction;
    }

    /**
     * @param soapAction
     *            The soapAction to set.
     */
    public void setSOAPAction(String soapAction) {
        this.soapAction = soapAction;
    }

    private Style style = Style.DOCUMENT;

    private Use use = Use.LITERAL;

    private SOAPVersion soapVersion = SOAPVersion.SOAP_11;

    private String soapAction;

}
