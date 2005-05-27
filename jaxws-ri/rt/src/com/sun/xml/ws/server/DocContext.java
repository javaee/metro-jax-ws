/*
 * $Id: DocContext.java,v 1.1 2005-05-27 02:50:30 jitu Exp $
 *
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.server;

public interface DocContext {
    public String getAbsolutePath(String abs, String rel);
}
