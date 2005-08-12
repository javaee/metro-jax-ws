/**
 * $Id: SOAP12Binding.java,v 1.1 2005-08-12 16:46:32 vivekp Exp $
 */

/**
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.ws.wsdl.document.soap;

import javax.xml.namespace.QName;

public class SOAP12Binding extends SOAPBinding{
    @Override public QName getElementName() {
        return SOAP12Constants.QNAME_BINDING;
    }
}
