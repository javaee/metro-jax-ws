/*
 * $Id: ProcessorException.java,v 1.3 2005-07-18 18:13:54 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.processor;

import com.sun.xml.ws.util.exception.JAXWSExceptionBase;
import com.sun.xml.ws.util.localization.Localizable;

/**
 * ProcessorException represents an exception that occurred while processing
 * a web service.
 * 
 * @see com.sun.xml.rpc.util.exception.JAXWSExceptionBase
 * 
 * @author WS Development Team
 */
public class ProcessorException extends JAXWSExceptionBase {

    public ProcessorException(String key) {
        super(key);
    }

    public ProcessorException(String key, String arg) {
        super(key, arg);
    }

    public ProcessorException(String key, Object[] args) {
        super(key, args);
    }

    public ProcessorException(String key, Localizable arg) {
        super(key, arg);
    }

    public String getResourceBundleName() {
        return "com.sun.tools.ws.resources.processor";
    }
}
