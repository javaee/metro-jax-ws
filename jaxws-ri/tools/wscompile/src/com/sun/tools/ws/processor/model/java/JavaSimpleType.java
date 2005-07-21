/*
 * $Id: JavaSimpleType.java,v 1.3 2005-07-21 01:59:08 vivekp Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.processor.model.java;

import com.sun.codemodel.JType;
import com.sun.tools.xjc.api.TypeAndAnnotation;
import com.sun.tools.ws.processor.model.jaxb.JAXBTypeAndAnnotation;

/**
 *
 * @author WS Development Team
 */
public class JavaSimpleType extends JavaType {

    public JavaSimpleType() {}

    public JavaSimpleType(String name, String initString) {
        super(name, true, initString);
    }

    public JavaSimpleType(JAXBTypeAndAnnotation jtype) {
        super(jtype);
    }

}
