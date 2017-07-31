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

package com.sun.xml.ws.spi.db;

import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBException;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.bind.attachment.AttachmentUnmarshaller;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;

/**
 * The XMLBridge is a Glassfish JAXWS side of Glassfish JAXB Bridge.
 * 
 * Mini-marshaller/unmarshaller that is specialized for a particular
 * element name and a type.
 *
 * <p>
 * Instances of this class is stateless and multi-thread safe.
 * They are reentrant.
 *
 * <p>
 * All the marshal operation generates fragments.
 *
 * <p>
 * <b>Subject to change without notice</b>.
 *
 * @since JAXB 2.0 EA1
 * @author Kohsuke Kawaguchi
 * @author shih-chang.chen@oracle.com
 */
public interface XMLBridge<T> {
    /**
     * Gets the {@link BindingContext} to which this object belongs.
     *
     * @since 2.1
     */
    public @NotNull BindingContext context();

    /**
     *
     * @throws JAXBException
     *      if there was an error while marshalling.
     *
     * @since 2.0 EA1
     */
//    public void marshal(T object,XMLStreamWriter output) throws JAXBException;
    
    public void marshal(T object,XMLStreamWriter output, AttachmentMarshaller am) throws JAXBException;

    /**
     * Marshals the specified type object with the implicit element name
     * associated with this instance of {@link Bond}.
     *
     * @param nsContext
     *      if this marshalling is done to marshal a subelement, this {@link NamespaceContext}
     *      represents in-scope namespace bindings available for that element. Can be null,
     *      in which case JAXB assumes no in-scope namespaces.
     * @throws JAXBException
     *      if there was an error while marshalling.
     *
     * @since 2.0 EA1
     */
//    public void marshal(T object,OutputStream output, NamespaceContext nsContext) throws JAXBException;
    
    /**
     * @since 2.0.2
     */
    public void marshal(T object,OutputStream output, NamespaceContext nsContext, AttachmentMarshaller am) throws JAXBException ;
//
////    public void marshal(@NotNull BridgeContext context,T object,OutputStream output, NamespaceContext nsContext) throws JAXBException;
//
//    public void marshal(@NotNull Marshaller m,T object,OutputStream output, NamespaceContext nsContext) throws JAXBException;


    public void marshal(T object,Node output) throws JAXBException ;
//
////    public void marshal(@NotNull BridgeContext context,T object,Node output) throws JAXBException ;
//
//    public void marshal(@NotNull Marshaller m,T object,Node output) throws JAXBException;


    /**
     * @since 2.0 EA4
     */
//    public void marshal(T object, ContentHandler contentHandler) throws JAXBException;
    /**
     * @since 2.0.2
     */
    public void marshal(T object, ContentHandler contentHandler, AttachmentMarshaller am) throws JAXBException ;
    
////    public void marshal(@NotNull BridgeContext context,T object, ContentHandler contentHandler) throws JAXBException;
//    
//    public void marshal(@NotNull Marshaller m,T object, ContentHandler contentHandler) throws JAXBException;

    /**
     * @since 2.0 EA4
     */
    public void marshal(T object, Result result) throws JAXBException;
    
////    public void marshal(@NotNull BridgeContext context,T object, Result result) throws JAXBException;
//    public void marshal(@NotNull Marshaller m,T object, Result result) throws JAXBException;



    /**
     * Unmarshals the specified type object.
     *
     * @param in
     *      the parser must be pointing at a start tag
     *      that encloses the XML type that this {@link Bond} is
     *      instanciated for.
     *
     * @return
     *      never null.
     *
     * @throws JAXBException
     *      if there was an error while unmarshalling.
     *
     * @since 2.0 EA1
     */
//    public @NotNull T unmarshal(@NotNull XMLStreamReader in) throws JAXBException ;
    /**
     * @since 2.0.3
     */
    public @NotNull T unmarshal(@NotNull XMLStreamReader in, @Nullable AttachmentUnmarshaller au) throws JAXBException;
//    public @NotNull T unmarshal(@NotNull BridgeContext context, @NotNull XMLStreamReader in) throws JAXBException ;
//    public @NotNull T unmarshal(@NotNull Unmarshaller u, @NotNull XMLStreamReader in) throws JAXBException;

    /**
     * Unmarshals the specified type object.
     *
     * @param in
     *      the parser must be pointing at a start tag
     *      that encloses the XML type that this {@link Bond} is
     *      instanciated for.
     *
     * @return
     *      never null.
     *
     * @throws JAXBException
     *      if there was an error while unmarshalling.
     *
     * @since 2.0 EA1
     */
//    public @NotNull T unmarshal(@NotNull Source in) throws JAXBException ;
    /**
     * @since 2.0.3
     */
    public @NotNull T unmarshal(@NotNull Source in, @Nullable AttachmentUnmarshaller au) throws JAXBException;
//    public @NotNull T unmarshal(@NotNull BridgeContext context, @NotNull Source in) throws JAXBException;
//    public @NotNull T unmarshal(@NotNull Unmarshaller u, @NotNull Source in) throws JAXBException;

    /**
     * Unmarshals the specified type object.
     *
     * @param in
     *      the parser must be pointing at a start tag
     *      that encloses the XML type that this {@link XMLBridge} is
     *      instanciated for.
     *
     * @return
     *      never null.
     *
     * @throws JAXBException
     *      if there was an error while unmarshalling.
     *
     * @since 2.0 EA1
     */
    public @NotNull T unmarshal(@NotNull InputStream in) throws JAXBException ;
    
//    public @NotNull T unmarshal(@NotNull BridgeContext context, @NotNull InputStream in) throws JAXBException ;
    
//    public @NotNull T unmarshal(@NotNull Unmarshaller u, @NotNull InputStream in) throws JAXBException;

    /**
     * Unmarshals the specified type object.
     *
     * @param n
     *      Node to be unmarshalled.
     *
     * @return
     *      never null.
     *
     * @throws JAXBException
     *      if there was an error while unmarshalling.
     *
     * @since 2.0 FCS
     */
//    public @NotNull T unmarshal(@NotNull Node n) throws JAXBException ;
    /**
     * @since 2.0.3
     */
    public @NotNull T unmarshal(@NotNull Node n, @Nullable AttachmentUnmarshaller au) throws JAXBException;
//    public @NotNull T unmarshal(@NotNull BridgeContext context, @NotNull Node n) throws JAXBException;
//    public @NotNull T unmarshal(@NotNull Unmarshaller context, @NotNull Node n) throws JAXBException;

    /**
     * Gets the {@link TypeInfo} from which this bridge was created.
     */
    public TypeInfo getTypeInfo();

    /**
     * This can be used to determine whether XMLStreamWriter or OutputStream is
     * prefered by the implementation.
     * 
     * @return true if marshall to OutputStream is supported in the 
     * implementation.
     */
    public boolean supportOutputStream();
}
