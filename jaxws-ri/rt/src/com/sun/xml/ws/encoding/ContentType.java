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

/*
 * @(#)ContentType.java       1.7 02/03/27
 */



package com.sun.xml.ws.encoding;

import javax.xml.ws.WebServiceException;

/**
 * This class represents a MIME ContentType value. It provides
 * methods to parse a ContentType string into individual components
 * and to generate a MIME style ContentType string.
 *
 * @version 1.7, 02/03/27
 * @author  John Mani
 */
public final class ContentType {

    private String primaryType;	// primary type
    private String subType;	// subtype
    private ParameterList list;	// parameter list

    /**
     * Constructor that takes a Content-Type string. The String
     * is parsed into its constituents: primaryType, subType
     * and parameters. A ParseException is thrown if the parse fails.
     *
     * @param	s	the Content-Type string.
     * @exception WebServiceException if the parse fails.
     */
    public ContentType(String s) throws WebServiceException {
        HeaderTokenizer h = new HeaderTokenizer(s, HeaderTokenizer.MIME);
        HeaderTokenizer.Token tk;

        // First "type" ..
        tk = h.next();
        if (tk.getType() != HeaderTokenizer.Token.ATOM)
            throw new WebServiceException();
        primaryType = tk.getValue();

        // The '/' separator ..
        tk = h.next();
        if ((char)tk.getType() != '/')
            throw new WebServiceException();

        // Then "subType" ..
        tk = h.next();
        if (tk.getType() != HeaderTokenizer.Token.ATOM)
            throw new WebServiceException();
        subType = tk.getValue();

        // Finally parameters ..
        String rem = h.getRemainder();
        if (rem != null)
            list = new ParameterList(rem);
    }


    /**
     * Return the primary type.
     * @return the primary type
     */
    public String getPrimaryType() {
	    return primaryType;
    }

    /**
     * Return the subType.
     * @return the subType
     */
    public String getSubType() {
	    return subType;
    }

    /**
     * Return the MIME type string, without the parameters.
     * The returned value is basically the concatenation of
     * the primaryType, the '/' character and the secondaryType.
     *
     * @return the type
     */
    public String getBaseType() {
	    return primaryType + '/' + subType;
    }

    /**
     * Return the specified parameter value. Returns <code>null</code>
     * if this parameter is absent.
     *
     * @param name parameter name
     * @return	parameter value
     */
    public String getParameter(String name) {
        if (list == null)
            return null;

        return list.get(name);
    }

    /**
     * Return a ParameterList object that holds all the available
     * parameters. Returns null if no parameters are available.
     *
     * @return	ParameterList
     */
    public ParameterList getParameterList() {
	    return list;
    }

}

