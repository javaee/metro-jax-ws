/*
 * $Id: Parameter.java,v 1.3 2005-10-12 23:33:20 vivekp Exp $
 */

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */
package com.sun.tools.ws.wsdl.document.jaxws;

import javax.xml.namespace.QName;

/**
 * @author Vivek Pandey
 *
 * class representing jaxws:parameter
 *
 */
public class Parameter {
    private String part;
    private QName element;
    private String name;
    private String messageName;

    /**
     * @param part
     * @param element
     * @param name
     */
    public Parameter(String msgName, String part, QName element, String name) {
        this.part = part;
        this.element = element;
        this.name = name;
        this.messageName = msgName;
    }

    public String getMessageName() {
        return messageName;
    }

    public void setMessageName(String messageName) {
        this.messageName = messageName;
    }

    /**
     * @return Returns the element.
     */
    public QName getElement() {
        return element;
    }

    /**
     * @param element The element to set.
     */
    public void setElement(QName element) {
        this.element = element;
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

    /**
     * @return Returns the part.
     */
    public String getPart() {
        return part;
    }

    /**
     * @param part The part to set.
     */
    public void setPart(String part) {
        this.part = part;
    }
}
