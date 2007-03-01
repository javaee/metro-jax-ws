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
import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.message.Message;

import javax.xml.namespace.QName;

/**
 * {@link WSDLPortType} bound with a specific binding.
 *
 * @author Vivek Pandey
 */
public interface WSDLBoundPortType extends WSDLFeaturedObject, WSDLExtensible {
    /**
     * Gets the name of the wsdl:binding@name attribute value as local name and wsdl:definitions@targetNamespace
     * as the namespace uri.
     */
    QName getName();

    /**
     * Gets the {@link WSDLModel} that owns this port type.
     */
    @NotNull WSDLModel getOwner();

    /**
     * Gets the {@link WSDLBoundOperation} for a given operation name
     *
     * @param operationName non-null operationName
     * @return null if a {@link WSDLBoundOperation} is not found
     */
    public WSDLBoundOperation get(QName operationName);

    /**
     * Gets the wsdl:binding@type value, same as {@link WSDLPortType#getName()}
     */
    QName getPortTypeName();

    /**
     * Gets the {@link WSDLPortType} associated with the wsdl:binding
     */
    WSDLPortType getPortType();

    /**
     * Gets the {@link WSDLBoundOperation}s
     */
    Iterable<? extends WSDLBoundOperation> getBindingOperations();

    /**
     * Returns the binding ID.
     * This would typically determined by the binding extension elements in wsdl:binding.
     */
    BindingID getBindingId();

    /**
     * Gets the bound operation in this port for a tag name. Here the operation would be the one whose
     * input part descriptor bound to soap:body is same as the tag name except for rpclit where the tag
     * name would be {@link WSDLBoundOperation#getName()}.
     *
     * <p>
     * If you have a {@link Message} and trying to figure out which operation it belongs to,
     * always use {@link Message#getOperation}, as that performs better.
     *
     * <p>
     * For example this can be used in the case when a message receipient can get the
     * {@link WSDLBoundOperation} from the payload tag name.
     *
     * <p>
     * namespaceUri and the local name both can be null to get the WSDLBoundOperation that has empty body -
     * there is no payload. According to BP 1.1 in a port there can be at MOST one operation with empty body.
     * Its an error to have namespace URI non-null but local name as null.
     *
     * @param namespaceUri namespace of the payload element.
     * @param localName local name of the payload
     * @throws NullPointerException if localName is null and namespaceUri is not.
     * @return
     *      null if no operation with the given tag name is found.
     */
    @Nullable WSDLBoundOperation getOperation(String namespaceUri, String localName);
}
