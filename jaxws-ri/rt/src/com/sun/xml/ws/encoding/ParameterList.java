/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
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

package com.sun.xml.ws.encoding;

/*
 * @(#)ParameterList.java     1.10 03/02/12
 */

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
import javax.xml.ws.WebServiceException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class holds MIME parameters (attribute-value pairs).
 *
 * @version 1.10, 03/02/12
 * @author  John Mani
 */

final class ParameterList {

    private final Map<String, String> list;

    /**
     * Constructor that takes a parameter-list string. The String
     * is parsed and the parameters are collected and stored internally.
     * A ParseException is thrown if the parse fails.
     * Note that an empty parameter-list string is valid and will be
     * parsed into an empty ParameterList.
     *
     * @param	s	the parameter-list string.
     * @exception WebServiceException if the parse fails.
     */
    ParameterList(String s) {
        HeaderTokenizer h = new HeaderTokenizer(s, HeaderTokenizer.MIME);
        HeaderTokenizer.Token tk;
        int type;
        String name;

        list = new HashMap<String, String>();
        while (true) {
            tk = h.next();
            type = tk.getType();

            if (type == HeaderTokenizer.Token.EOF) // done
            return;

            if ((char)type == ';') {
                // expect parameter name
                tk = h.next();
                // tolerate trailing semicolon, even though it violates the spec
                if (tk.getType() == HeaderTokenizer.Token.EOF)
                    return;
                // parameter name must be a MIME Atom
                if (tk.getType() != HeaderTokenizer.Token.ATOM)
                    throw new WebServiceException();
                name = tk.getValue().toLowerCase();

                // expect '='
                tk = h.next();
                if ((char)tk.getType() != '=')
                    throw new WebServiceException();

                // expect parameter value
                tk = h.next();
                type = tk.getType();
                // parameter value must be a MIME Atom or Quoted String
                if (type != HeaderTokenizer.Token.ATOM &&
                    type != HeaderTokenizer.Token.QUOTEDSTRING)
                    throw new WebServiceException();

                list.put(name, tk.getValue());
            } else
                throw new WebServiceException();
        }
    }

    /**
     * Return the number of parameters in this list.
     *
     * @return  number of parameters.
     */
    int size() {
	    return list.size();
    }

    /**
     * Returns the value of the specified parameter. Note that
     * parameter names are case-insensitive.
     *
     * @param name	parameter name.
     * @return		Value of the parameter. Returns
     *			<code>null</code> if the parameter is not
     *			present.
     */
    String get(String name) {
	    return list.get(name.trim().toLowerCase());
    }


    /**
     * Return an enumeration of the names of all parameters in this
     * list.
     *
     * @return Enumeration of all parameter names in this list.
     */
    Iterator<String> getNames() {
	    return list.keySet().iterator();
    }

}
