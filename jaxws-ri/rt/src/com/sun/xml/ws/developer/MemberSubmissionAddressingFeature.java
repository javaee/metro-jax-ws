/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

package com.sun.xml.ws.developer;

import com.sun.xml.ws.api.FeatureConstructor;

import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.WebServiceFeature;

/**
 * Addressing Feature representing MemberSubmission Version.
 *
 * @author Rama Pulavarthi
 */

public class MemberSubmissionAddressingFeature extends WebServiceFeature {
    /**
     * Constant value identifying the MemberSubmissionAddressingFeature
     */
    public static final String ID = "http://java.sun.com/xml/ns/jaxws/2004/08/addressing";

    /**
     * Constant ID for the <code>required</code> feature parameter
     */
    public static final String IS_REQUIRED = "ADDRESSING_IS_REQUIRED";
    
    private boolean required;

    /**
     * Create an MemberSubmissionAddressingFeature
     * The instance created will be enabled.
     */
    public MemberSubmissionAddressingFeature() {
        this.enabled = true;
    }

    /**
     * Create an MemberSubmissionAddressingFeature
     *
     * @param enabled specifies whether this feature should
     *                be enabled or not.
     */
    public MemberSubmissionAddressingFeature(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Create an <code>MemberSubmissionAddressingFeature</code>
     *
     * @param enabled specifies whether this feature should
     * be enabled or not.
     * @param required specifies the value that will be used
     * for the <code>required</code> attribute on the
     * <code>wsaw:UsingAddressing</code> element.
     */
    @FeatureConstructor({"enabled","required"})
    public MemberSubmissionAddressingFeature(boolean enabled, boolean required) {
        this.enabled = enabled;
        this.required = required;
    }

    public String getID() {
        return ID;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }
}
