/*
 * $Id: LogicalEPTFactory.java,v 1.2 2005-05-25 20:52:05 kohlert Exp $
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
	 * @return
	 */
	public SOAPEncoder getSOAPEncoder();
	/**
	 * @return
	 */
	public SOAPDecoder getSOAPDecoder();
}
