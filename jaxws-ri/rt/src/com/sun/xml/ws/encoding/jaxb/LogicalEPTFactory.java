/*
 * $Id: LogicalEPTFactory.java,v 1.3 2005-07-23 04:10:02 kohlert Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */
package com.sun.xml.ws.encoding.jaxb;

import com.sun.xml.ws.encoding.internal.InternalEncoder;
import com.sun.xml.ws.encoding.soap.SOAPDecoder;
import com.sun.xml.ws.encoding.soap.SOAPEncoder;

/**
 * Change the name of this class to JaxwsEPTFactory or something else. OR
 * split into multiple factories.
 */
public interface LogicalEPTFactory {
	public LogicalEncoder getLogicalEncoder();

    public InternalEncoder getInternalEncoder();
    
	/**
	 * @return the SOAPEncoder
	 */
	public SOAPEncoder getSOAPEncoder();
	/**
	 * @return the SOAPDecoder
	 */
	public SOAPDecoder getSOAPDecoder();
}
