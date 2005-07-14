/*
 * $Id: WSToolsObjectFactoryImpl.java,v 1.1 2005-07-14 01:43:40 jitu Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.ws.util;

import com.sun.tools.ws.spi.WSToolsObjectFactory;
import com.sun.tools.ws.wscompile.CompileTool;
import java.io.OutputStream;

/**
 * Factory implementation class to instantiate concrete objects for JAX-WS tools.
 *
 * @author JAX-WS Development Team
 */
public class WSToolsObjectFactoryImpl extends WSToolsObjectFactory {
    
    @Override
    public boolean wsimport(OutputStream logStream, String[] args) {
        CompileTool tool = new CompileTool(logStream, "wsimport");
        return tool.run(args);
    }
    
    @Override
    public boolean wsgen(OutputStream logStream, String[] args) {
        CompileTool tool = new CompileTool(logStream, "wsgen");
        return tool.run(args);
    }
}
