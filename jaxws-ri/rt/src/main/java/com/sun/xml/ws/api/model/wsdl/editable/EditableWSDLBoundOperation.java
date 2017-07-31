/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.xml.ws.api.model.wsdl.editable;

import java.util.Map;

import javax.jws.WebParam.Mode;
import javax.jws.soap.SOAPBinding.Style;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;

public interface EditableWSDLBoundOperation extends WSDLBoundOperation {
	
	@Override
    @NotNull EditableWSDLOperation getOperation();

	@Override
    @NotNull EditableWSDLBoundPortType getBoundPortType();

	@Override
    @Nullable EditableWSDLPart getPart(@NotNull String partName, @NotNull Mode mode);

	@Override
    @NotNull Map<String,? extends EditableWSDLPart> getInParts();

	@Override
    @NotNull Map<String,? extends EditableWSDLPart> getOutParts();
    
	@Override
    @NotNull Iterable<? extends EditableWSDLBoundFault> getFaults();

	/**
	 * Add Part
	 * @param part Part
	 * @param mode Mode
	 */
    public void addPart(EditableWSDLPart part, Mode mode);

    /**
     * Add Fault
     * @param fault Fault
     */
    public void addFault(@NotNull EditableWSDLBoundFault fault);

    /**
     * Sets the soapbinding:binding/operation/wsaw:Anonymous.
     *
     * @param anonymous Anonymous value of the operation
     */
	public void setAnonymous(ANONYMOUS anonymous);
	
	/**
	 * Sets input explicit body parts
	 * @param b True, if input body part is explicit
	 */
	public void setInputExplicitBodyParts(boolean b);
	
	/**
	 * Sets output explicit body parts
	 * @param b True, if output body part is explicit
	 */
	public void setOutputExplicitBodyParts(boolean b);
	
	/**
	 * Sets fault explicit body parts
	 * @param b True, if fault body part is explicit
	 */
	public void setFaultExplicitBodyParts(boolean b);
	
	/**
	 * Set request namespace
	 * @param ns Namespace
	 */
	public void setRequestNamespace(String ns);
	
	/**
	 * Set response namespace
	 * @param ns Namespace
	 */
	public void setResponseNamespace(String ns);
	
	/**
	 * Set SOAP action
	 * @param soapAction SOAP action
	 */
	public void setSoapAction(String soapAction);
	
	/**
	 * Set parameter style
	 * @param style Style
	 */
	public void setStyle(Style style);
	
	/**
	 * Freezes WSDL model to prevent further modification
	 * @param root WSDL Model
	 */
	public void freeze(EditableWSDLModel root);
}
