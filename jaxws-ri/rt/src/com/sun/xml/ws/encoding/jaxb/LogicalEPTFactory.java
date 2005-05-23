/*
 * $Id: LogicalEPTFactory.java,v 1.1 2005-05-23 22:28:41 bbissett Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */
package com.sun.xml.ws.encoding.jaxb;

import com.sun.xml.ws.encoding.internal.InternalEncoder;
import com.sun.xml.ws.encoding.soap.SOAPDecoder;
import com.sun.xml.ws.encoding.soap.SOAPEncoder;

/**
 * Change the name of this class to JaxrpcEPTFactory or something else. OR
 * split into multiple factories.
 */
public interface LogicalEPTFactory {
	public LogicalEncoder getLogicalEncoder();

    public InternalEncoder getInternalEncoder();
    
	/**
	 * @return
	 */
	public SOAPEncoder getSOAPEncoder();
	/**
	 * @return
	 */
	public SOAPDecoder getSOAPDecoder();
}
