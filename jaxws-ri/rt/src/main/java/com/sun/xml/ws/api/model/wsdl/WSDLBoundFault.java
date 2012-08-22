/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
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
import com.sun.istack.Nullable;

import javax.xml.namespace.QName;

/**
 * Abstracts wsdl:binding/wsdl:operation/wsdl:fault
 *
 * @author Vivek Pandey
 */
public interface WSDLBoundFault extends WSDLObject, WSDLExtensible {

    /**
     * Gives the wsdl:binding/wsdl:operation/wsdl:fault@name value
     */
    public
    @NotNull
    String getName();

    /**
     * Gives the qualified name associated with the fault. the namespace URI of the bounded fault
     * will be the one derived from wsdl:portType namespace.
     *
     * Maybe null if this method is called before the model is completely build (frozen), if a binding fault has no
     * corresponding fault in abstractwsdl:portType/wsdl:operation then the namespace URI of the fault will be that of
     * the WSDBoundPortType.
     */
    public @Nullable QName getQName();

    /**
     * Gives the associated abstract fault from
     * wsdl:portType/wsdl:operation/wsdl:fault. It is only available after
     * the WSDL parsing is complete and the entire model is frozen.
     * <p/>
     * Maybe null if a binding fault has no corresponding fault in abstract
     * wsdl:portType/wsdl:operation
     */
    public
    @Nullable
    WSDLFault getFault();

    /**
     * Gives the owner {@link WSDLBoundOperation}
     */
    @NotNull WSDLBoundOperation getBoundOperation();
}
