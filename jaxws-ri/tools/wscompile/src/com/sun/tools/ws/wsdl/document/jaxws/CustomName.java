/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
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
