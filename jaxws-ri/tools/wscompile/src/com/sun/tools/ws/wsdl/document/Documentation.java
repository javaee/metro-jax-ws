/*
 * $Id: Documentation.java,v 1.1 2005-05-24 14:00:44 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.wsdl.document;

/**
 * Entity corresponding to the "documentation" WSDL element.
 *
 * @author JAX-RPC Development Team
 */
public class Documentation {

    public Documentation(String s) {
        content = s;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String s) {
        content = s;
    }

    public void accept(WSDLDocumentVisitor visitor) throws Exception {
        visitor.visit(this);
    }

    private String content;
}
