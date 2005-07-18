/*
 * $Id: EntityReferenceValidator.java,v 1.2 2005-07-18 18:14:19 kohlert Exp $
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
 * @author WS Development Team
 */
public interface EntityReferenceValidator {
    public boolean isValid(Kind kind, QName name);
}
