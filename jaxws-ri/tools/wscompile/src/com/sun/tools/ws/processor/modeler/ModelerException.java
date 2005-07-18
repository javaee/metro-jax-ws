/*
 * $Id: ModelerException.java,v 1.2 2005-07-18 18:14:03 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.processor.modeler;

import com.sun.tools.ws.processor.ProcessorException;
import com.sun.xml.ws.util.localization.Localizable;

/**
 * ModelerException represents an exception that occurred while
 * visiting service model.
 *
 * @see com.sun.xml.rpc.util.exception.JAXRPCExceptionBase
 *
 * @author WS Development Team
*/
public class ModelerException extends ProcessorException {

    public ModelerException(String key) {
        super(key);
    }

    public ModelerException(String key, String arg) {
        super(key, arg);
    }

    public ModelerException(String key, Object[] args) {
        super(key, args);
    }

    public ModelerException(String key, Localizable arg) {
        super(key, arg);
    }

    public ModelerException(Localizable arg) {
        super("modeler.nestedModelError", arg);
    }

    public String getResourceBundleName() {
        return "com.sun.tools.ws.resources.modeler";
    }

}
