/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2013 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.api.model.wsdl;

import com.sun.istack.NotNull;

import javax.xml.namespace.QName;

/**
 * Abstraction of wsdl:portType/wsdl:operation/wsdl:output
 *
 * @author Vivek Pandey
 */
public interface WSDLOutput extends WSDLObject, WSDLExtensible {
    /**
     * Gives the wsdl:portType/wsdl:operation/wsdl:output@name
     */
    String getName();

    /**
     * Gives the WSDLMessage corresponding to wsdl:output@message
     * <p/>
     * This method should not be called before the entire WSDLModel is built. Basically after the WSDLModel is built
     * all the references are resolve in a post processing phase. IOW, the WSDL extensions should
     * not call this method.
     *
     * @return Always returns null when called from inside WSDL extensions.
     */
    WSDLMessage getMessage();

    /**
     * Gives the Action Message Addressing Property value for
     * {@link WSDLOutput} message.
     * <p/>
     * This method provides the correct value irrespective of
     * whether the Action is explicitly specified in the WSDL or
     * implicitly derived using the rules defined in WS-Addressing. 
     *
     * @return Action
     */
    String getAction();

    /**
     * Gives the owning {@link WSDLOperation}
     */
    @NotNull
    WSDLOperation getOperation();

    /**
     * Gives qualified name of the wsdl:output 'name' attribute value. If there is no name, then it computes the name from:
     *
     * wsdl:operation@name+"Response", which is local name of {@link WSDLOperation#getName()} + "Response"
     * <p/>
     *
     * The namespace uri is determined from the enclosing wsdl:operation.
     */
    @NotNull
    QName getQName();
    
    /**
     * Checks if the Action value is implicitly derived using the rules defined in WS-Addressing.
     * 
     * @return true if the Action value is implicitly derived using the rules defined in WS-Addressing.
     */
    public boolean isDefaultAction();
}
