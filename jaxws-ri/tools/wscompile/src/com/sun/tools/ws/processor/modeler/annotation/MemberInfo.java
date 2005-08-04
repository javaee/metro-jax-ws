/**
 * $Id: MemberInfo.java,v 1.2 2005-08-04 21:48:37 kohlert Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.ws.processor.modeler.annotation;

import javax.xml.namespace.QName;
import com.sun.tools.ws.processor.modeler.annotation.*;
/**
 *
 * @author  WS Development Team
 */
public class MemberInfo {
    int paramIndex;
    String paramType;
    String paramName;
    QName elementName;

    public MemberInfo(int paramIndex, String paramType, String paramName,
        QName elementName) {
        this.paramIndex = paramIndex;
        this.paramType = paramType;
        this.paramName = paramName;
        this.elementName = elementName;
    }

    public int getParamIndex() {
        return paramIndex;
    }

    public String getParamType() {
        return paramType;
    }

    public String getParamName() {
        return paramName;
    }

    public QName getElementName() {
        return elementName;
    }
}
