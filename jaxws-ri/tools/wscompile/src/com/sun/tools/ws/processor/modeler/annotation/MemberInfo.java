/**
 * $Id: MemberInfo.java,v 1.1 2005-05-23 23:23:50 bbissett Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.ws.processor.modeler.annotation;

import javax.xml.namespace.QName;
import com.sun.tools.ws.processor.modeler.annotation.*;
/**
 *
 * @author  dkohlert
 */
public class MemberInfo {
    int paramIndex;
    String paramType;
    String paramName;
    QName elementName;
//    TypeMoniker typeMoniker;

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
