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
