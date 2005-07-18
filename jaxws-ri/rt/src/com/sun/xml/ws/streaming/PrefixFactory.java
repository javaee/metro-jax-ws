/*
 * $Id: PrefixFactory.java,v 1.2 2005-07-18 16:52:23 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.streaming;

/**
 * <p> Interface for prefix factories. </p>
 *
 * <p> A prefix factory is able to create a new prefix for a URI that
 * was encountered for the first time when writing a document
 * using an XMLWriter. </p>
 *
 * @author WS Development Team
 */
public interface PrefixFactory {
    /**
     * Return a brand new prefix for the given URI.
     */
    public String getPrefix(String uri);
}
