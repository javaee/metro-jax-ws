/*
 * $Id: EntityReferenceValidator.java,v 1.1 2005-05-24 14:04:11 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.wsdl.framework;

import javax.xml.namespace.QName;

/**
 * An interface implemented by a class that is capable of validating
 * a QName/Kind pair referring to an external entity.
 *
 * @author JAX-RPC Development Team
 */
public interface EntityReferenceValidator {
    public boolean isValid(Kind kind, QName name);
}
