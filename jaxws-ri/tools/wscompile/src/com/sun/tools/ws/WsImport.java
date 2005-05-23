/**
 * $Id: WsImport.java,v 1.1 2005-05-23 23:10:41 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws;

import com.sun.tools.ws.wscompile.CompileTool;

/**
 * @author Vivek Pandey
 */

public class WsImport {
    public static void main(String[] args) {
        CompileTool tool = new CompileTool(System.out, "wsimport");
        System.exit(tool.run(args) ? 0 : 1);
    }
}
