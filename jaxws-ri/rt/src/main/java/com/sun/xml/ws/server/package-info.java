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

/**
 * <h1>JAX-WS 2.0.1 Server Runtime</h1>
 * <P>This document describes the architecture of server side 
 * JAX-WS 2.0.1 runtime. </p>
 *
 * <h3>JAX-WS 2.0.1 Server Runtime Sequence Diagram</h3>

  * <img src='../../../../../jaxws/basic-server.seq.png'>
 
 *
 *
 * <H3>Message Flow</H3>
 * <P>A Web Service invocation starts with either the 
 * {@link com.sun.xml.ws.transport.http.servlet.WSServletDelegate WSServletDelegate}
 * or the {@link com.sun.xml.ws.transport.http.server.ServerConnectionImpl ServerConnectionImpl}.
 * Both of these classes find the appropriate {@link com.sun.xml.ws.server.RuntimeEndpointInfo RuntimeEndpointInfo}
 * and invokes the {@link com.sun.xml.ws.server.Tie#handle(com.sun.xml.ws.api.server.WSConnection,
 * com.sun.xml.ws.spi.runtime.RuntimeEndpointInfo) Tie.handle}
 * method. This method first creates a {@link com.sun.pept.ept.MessageInfo MessageInfo}
 * used to gather inforrmation about the message to be received. A
 * {@link com.sun.xml.ws.server.RuntimeContext RuntimeContext}
 * is then created with the MessageInfo and the {@link com.sun.xml.ws.api.model.SEIModel RuntimeModel}
 * retrieved from the RuntimeEndpointInfo. The RuntimeContext is then
 * stored in the MessageInfo. The {@link com.sun.pept.ept.EPTFactory EPTFactory}
 * is retrieved from the {@link com.sun.xml.ws.server.EPTFactoryFactoryBase EPTFactoryFactoryBase}
 * and also placed in the MessagInfo. A {@link com.sun.pept.protocol.MessageDispatcher MessageDispatcher}
 * is then created and the receive method is invoked. There will be two
 * types of MessageDispatchers for JAX-WS 2.0.1, SOAPMessageDispatcher
 * (one for client and one for the server) and an XMLMessageDispatcher
 * (one for the client and one for the server).</P>
 * <P>The MessageDispatcher.receive method orchestrates the receiving of
 * a Message. The SOAPMessageDispatcher first converts the MessageInfo
 * to a SOAPMessage. The SOAPMessageDispatcher then does mustUnderstand
 * processing followed by an invocation of any handlers. The SOAPMessage
 * is then converted to an InternalMessage and stored in the
 * MessageInfo. The converting of the SOAPMessage to an InternalMessage
 * is done using the decoder retrieved from the EPTFactory that is
 * contained in the MessageInfo. Once the SOAPMessage has been converted
 * to an InternalMessage the endpoint implementation is invoked via
 * reflection from the Method stored in the MessageInfo. The return
 * value of the method call is then stored in the InternalMessage. An
 * internalMessage is then created from the MessageInfo. The SOAPEncoder
 * is retrieved from the EPTFactory stored in the MessageInfo. The
 * SOAPEncoder.toSOAPMessage is then invoked to create a SOAPMessage
 * from the InternalMessage. A WSConnection is then retrieved from the
 * MessageInfo and the SOAPMessage is returned over that WSConnection.</P>
 * <P><BR>
 * </P>
 * <H3>External Interactions</H3>
 * <H4>SAAJ API</H4>
 * <UL>
 * 	<LI><P>JAX-WS creates SAAJ javax.xml.soap.SOAPMessage 
 *      from the HttpServletRequest.
 * 	At present, JAX-WS reads all the bytes from the request stream and
 * 	then creates SOAPMessage along with the HTTP headers.</P>
 * </UL>
 * <P>javax.xml.soap.MessageFactory(binding).createMessage(MimeHeaders, InputStream)</P>
 * <UL>
 * 	<LI><P>SOAPMessage parses the content from the stream including MIME
 * 	data</P>
 * 	<LI><P>com.sun.xml.ws.server.SOAPMessageDispatcher::checkHeadersPeekBody()</P>
 * 	<P>SOAPMessage.getSOAPHeader() is used for mustUnderstand processing
 * 	of headers. It further uses
 * 	javax.xml.soap.SOAPHeader.examineMustUnderstandHeaderElements(role)</P>
 * 	<P>SOAPMessage.getSOAPBody().getFistChild() is used for guessing the
 * 	MEP of the request</P>
 * 	<LI><P>com.sun.xml.ws.handler.HandlerChainCaller:insertFaultMessage()</P>
 * 	<P>SOAPMessage.getSOAPPart().getEnvelope() and some other SAAJ calls
 * 	are made to create a fault in the SOAPMessage</P>
 * 	<LI><P>com.sun.xml.ws.handler.LogicalMessageImpl::getPayload()
 * 	interacts with SAAJ to get body from SOAPMessage</P>
 * 	<LI><P>com.sun.xml.ws.encoding.soap.SOAPEncoder.toSOAPMessage(com.sun.xml.ws.encoding.soap.internal.InternalMessage,
 * 	SOAPMessage). There is a scenario where there is SOAPMessage and a
 * 	logical handler sets payload as Source. To write to the stream,
 * 	SOAPMessage.writeTo() is used but before that the body needs to be
 * 	updated with logical handler' Source. Need to verify if this
 * 	scenario is still happening since Handler.close() is changed to take
 * 	MessageContext.</P>
 * 	<LI><P>com.sun.xml.ws.handlerSOAPMessageContextImpl.getHeaders()
 * 	uses SAAJ API to get headers.</P>
 * 	<LI><P>SOAPMessage.writeTo() is used to write response. At present,
 * 	it writes into byte[] and this byte[] is written to
 * 	HttpServletResponse.</P>
 * </UL>
 * <H4>JAXB API</H4>
 * <P>JAX-WS RI uses the JAXB API to marshall/unmarshall user created
 * JAXB objects with user created {@link javax.xml.bind.JAXBContext JAXBContext}. 
 * Handler, Dispatch in JAX-WS API provide ways for the user to specify his/her own
 * JAXBContext. {@link com.sun.xml.ws.encoding.jaxb.JAXBTypeSerializer JAXBTypeSerializer} class uses all these methods.</P>
 * <UL>
 * 	<LI><p>{@link javax.xml.bind.Marshaller#marshal(Object,XMLStreamWriter) Marshaller.marshal(Object,XMLStreamWriter)}</p>
 * 	<LI><P>{@link javax.xml.bind.Marshaller#marshal(Object,Result) Marshaller.marshal(Object, DomResult)}</P>
 * 	<LI><P>{@link javax.xml.bind.Unmarshaller#unmarshal(XMLStreamReader) Object Unmarshaller.unmarshal(XMLStreamReader)}</P>
 * 	<LI><P>{@link javax.xml.bind.Unmarshaller#unmarshal(Source) Object Unmarshaller.unmarshal(Source)}</P>
 * </UL>
 * The following two JAXB classes are implemented by JAX-WS to enable/implement MTOM and XOP
 * <UL>
 *      <LI><P>{@link javax.xml.bind.attachment.AttachmentMarshaller AttachmentMarshaller}</P>
 *      <LI><P>{@link javax.xml.bind.attachment.AttachmentUnmarshaller AttachmentUnmarshaller}</P>
 * </UL>
 * <H4>JAXB Runtime-API (private contract)</H4>
 * <P>JAX-WS RI uses these private API for serialization/deserialization
 * purposes. This private API is used to serialize/deserialize method
 * parameters at the time of JAXBTypeSerializer class uses all
 * these methods.</P>
 * <UL>
 * 	<LI><P>{@link com.sun.xml.bind.api.Bridge#marshal(BridgeContext, Object, XMLStreamWriter) Bridge.marshal(BridgeContext, Object, XMLStreamWriter)}</P>
 * 	<LI><P>{@link com.sun.xml.bind.api.Bridge#marshal(BridgeContext, Object, Node) Bridge.marshal(BridgeContext, Object, Node)}</P>
 * 	<LI><P>{@link com.sun.xml.bind.api.Bridge#unmarshal(BridgeContext, XMLStreamReader) Object Bridge.unmarshal(BridgeContext, XMLStreamReader)}</P>
 * </UL>
 * 
 * @ArchitectureDocument
 **/
package com.sun.xml.ws.server;

import com.sun.xml.bind.api.BridgeContext;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.transform.Result;

import org.w3c.dom.Node;
