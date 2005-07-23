/*
 * $Id: XMLEPTFactory.java,v 1.1 2005-07-23 00:21:27 jitu Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */
package com.sun.xml.ws.encoding.xml;

import com.sun.xml.ws.encoding.internal.InternalEncoder;
import com.sun.xml.ws.encoding.jaxb.LogicalEncoder;

/**
 * Change the name of this class to JaxwsEPTFactory or something else. OR
 * split into multiple factories.
 */
public interface XMLEPTFactory {
	public LogicalEncoder getLogicalEncoder();
    public InternalEncoder getInternalEncoder();
    public XMLEncoder getXMLEncoder();
    public XMLDecoder getXMLDecoder();
}
