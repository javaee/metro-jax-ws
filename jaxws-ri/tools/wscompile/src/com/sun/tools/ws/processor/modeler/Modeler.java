/*
 * $Id: Modeler.java,v 1.3 2005-07-29 19:54:49 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */


package com.sun.tools.ws.processor.modeler;

import com.sun.tools.ws.processor.model.Model;

/**
 * A Modeler is used to create a Model of a Web Service from a particular Web 
 * Web Service description such as a WSDL
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
