/**
 * $Id: Binding.java,v 1.5 2005-08-18 19:11:42 kohlert Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.wsdl.writer.document;

import javax.xml.namespace.QName;
import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlAttribute;
import com.sun.xml.txw2.annotation.XmlElement;
import com.sun.xml.ws.wsdl.writer.document.soap.SOAPBinding;

/**
 *
 * @author WS Development Team
 */
@XmlElement("binding")
public interface Binding
    extends TypedXmlWriter, StartWithExtensionsType
{


    @XmlAttribute
    public com.sun.xml.ws.wsdl.writer.document.Binding type(QName value);

    @XmlAttribute
    public com.sun.xml.ws.wsdl.writer.document.Binding name(String value);

    @XmlElement
    public BindingOperationType operation();

    @XmlElement(value="binding",ns="http://schemas.xmlsoap.org/wsdl/soap/")
    public SOAPBinding soapBinding();

    @XmlElement(value="binding",ns="http://schemas.xmlsoap.org/wsdl/soap12/")
    public com.sun.xml.ws.wsdl.writer.document.soap12.SOAPBinding soap12Binding();

}
