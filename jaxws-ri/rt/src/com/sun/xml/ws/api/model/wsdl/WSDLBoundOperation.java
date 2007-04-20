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

package com.sun.xml.ws.api.model.wsdl;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;

import javax.jws.WebParam.Mode;
import javax.xml.namespace.QName;
import java.util.Map;

/**
 * Abstracts wsdl:binding/wsdl:operation. It can be used to determine the parts and their binding.
 *
 * @author Vivek Pandey
 */
public interface WSDLBoundOperation extends WSDLObject, WSDLExtensible {
    /**
     * Short-cut for {@code getOperation().getName()}
     */
    @NotNull QName getName();

    /**
     * Gives soapbinding:operation@soapAction value. soapbinding:operation@soapAction is optional attribute.
     * If not present an empty String is returned as per BP 1.1 R2745.
     */
    @NotNull String getSOAPAction();

    /**
     * Gets the wsdl:portType/wsdl:operation model - {@link WSDLOperation},
     * associated with this binding operation.
     *
     * @return always same {@link WSDLOperation}
     */
    @NotNull WSDLOperation getOperation();

    /**
     * Gets the soapbinding:binding/operation/wsaw:Anonymous. A default value of OPTIONAL is returned.
     *
     * @return Anonymous value of the operation
     */
    ANONYMOUS getAnonymous();

    enum ANONYMOUS { optional, required, prohibited }

    /**
     * Gets {@link WSDLPart} for the given wsdl:input or wsdl:output part
     *
     * @return null if no part is found
     */
    @Nullable WSDLPart getPart(@NotNull String partName, @NotNull Mode mode);

    /**
     * Gets all inbound {@link WSDLPart} by its {@link WSDLPart#getName() name}.
     */
    @NotNull Map<String,WSDLPart> getInParts();

    /**
     * Gets all outbound {@link WSDLPart} by its {@link WSDLPart#getName() name}.
     */
    @NotNull Map<String,WSDLPart> getOutParts();

    /**
     * Gets the payload QName of the request message.
     *
     * <p>
     * It's possible for an operation to define no body part, in which case
     * this method returns null. 
     */
    @Nullable QName getReqPayloadName();

}
