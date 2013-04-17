/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004-2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package fromjava.nosei_bare_apt.server;

import java.lang.Float;
import java.lang.Integer;
import java.lang.String;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "FooException", namespace = "urn:test:types")
public class FooException {

    @XmlElement(name = "varString", namespace = "", type = String.class)
    protected String varString;
    @XmlElement(name = "varInt", namespace = "", type = Integer.class)
    protected int varInt;
    @XmlElement(name = "varFloat", namespace = "", type = Float.class)
    protected float varFloat;

    /**
     * Gets the value of the varString property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String}
     */
    @XmlTransient
    public String getVarString() {
        return varString;
    }

    /**
     * Sets the value of the varString property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String}
     */
    public void setVarString(String value) {
        this.varString = value;
    }

    /**
     * Gets the value of the varInt property.
     * 
     */
    @XmlTransient
    public int getVarInt() {
        return varInt;
    }

    /**
     * Sets the value of the varInt property.
     * 
     */
    public void setVarInt(int value) {
        this.varInt = value;
    }

    /**
     * Gets the value of the varFloat property.
     * 
     */
    @XmlTransient
    public float getVarFloat() {
        return varFloat;
    }

    /**
     * Sets the value of the varFloat property.
     * 
     */
    public void setVarFloat(float value) {
        this.varFloat = value;
    }

}
