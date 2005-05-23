/*
 * $Id: ClassModelInfo.java,v 1.1 2005-05-23 23:13:23 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.processor.config;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import com.sun.tools.ws.processor.modeler.Modeler;
import com.sun.tools.ws.processor.ProcessorOptions;
import com.sun.xml.ws.util.VersionUtil;

/**
 *
 * @author JAX-RPC Development Team
 */
public class ClassModelInfo extends ModelInfo {

    public ClassModelInfo(String className) {
        this.className = className;
    }


    public Modeler getModeler(Properties properties) {
        return null;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    private String className;
}
