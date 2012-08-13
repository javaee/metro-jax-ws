/**
 * $Id: TestProtocolException.java,v 1.1 2007-09-22 00:39:23 ramapulavarthi Exp $
 *
 * Copyright 2005 Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package handler.handler_processing.common;

import javax.xml.ws.ProtocolException;

/**
 * This exception class is used by some tests to make sure that
 * the 
 */
public class TestProtocolException extends ProtocolException {

    public TestProtocolException() {
        super();
    }
    
    public TestProtocolException(String message) {
        super(message);
    }
    
}
