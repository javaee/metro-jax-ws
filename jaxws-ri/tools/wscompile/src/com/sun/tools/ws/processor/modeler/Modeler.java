/*
 * $Id: Modeler.java,v 1.2 2005-07-18 18:14:02 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */


package com.sun.tools.ws.processor.modeler;

import com.sun.tools.ws.processor.model.Model;

/**
 *
 * @author WS Development Team
*/
public interface Modeler {
    /**
     * Returns the top model of a Web Service. May throw a
     * ModelException if there is a problem with the model.
     *
     * @return Model - the root Node of the model of the Web Service
     *
     * @exception ModelerException
     */
    public Model buildModel();
}
