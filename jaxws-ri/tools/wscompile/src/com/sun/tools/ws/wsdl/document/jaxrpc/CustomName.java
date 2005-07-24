/*
 * $Id: CustomName.java,v 1.2 2005-07-24 01:37:50 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.ws.wsdl.document.jaxws;


/**
 * @author Vivek Pandey
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class CustomName {


    /**
     *
     */
    public CustomName() {
    }

    /**
     *
     */
    public CustomName(String name, String javaDoc) {
        this.name = name;
        this.javaDoc = javaDoc;
    }

    /**
     * @return Returns the javaDoc.
     */
    public String getJavaDoc() {
        return javaDoc;
    }
    /**
     * @param javaDoc The javaDoc to set.
     */
    public void setJavaDoc(String javaDoc) {
        this.javaDoc = javaDoc;
    }
    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }
    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    private String javaDoc;
    private String name;
}
