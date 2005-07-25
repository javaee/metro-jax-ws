/**
 *  <h1>JAX-WS 2.0 Client Runtime</h1>
 * <P>This document describes the architecture of client side 
 * JAX-WS 2.0 runtime. 
 *
 * <h3>JAX-WS 2.0 Server Client Sequence Diagram</h3>
 *
 * <H3>Message Flow</H3>
 * <H3>External Interactions</H3>
 * <H4>SAAJ API</H4>
 * <UL>
 * 	<LI><P>JAX-WS creates SAAJ SOAPMessage from the HttpServletRequest.
 * 	At present, JAX-WS reads all the bytes from the request stream and
 * 	then creates SOAPMessage along with the HTTP headers.</P>
 * </UL>
 * <P>MessageFactory(binding).createMessage(MimeHeaders, InputStream)</P>
 * <UL>
 * 	<LI><P>SOAPMessage parses the content from the stream including MIME
 * 	data</P>
 * 	<LI><P>com.sun.xml.ws.server.SOAPMessageDispatcher::checkHeadersPeekBody()</P>
 * 	<P>SOAPMessage.getSOAPHeader() is used for mustUnderstand processing
 * 	of headers. It further uses
 * 	SOAPHeader.examineMustUnderstandHeaderElements(role)</P>
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
 * JAXB objects with user created JAXBContext.Handler, Dispatch in
 * JAX-WS API provide ways for the user to specify his/her own
 * JAXBContext. JAXBTypeSerializer class uses all these methods.</P>
 * <UL>
 * 	<LI><p>Marshaller.marshal(Object,XMLStreamWriter)</p>
 * 	<LI><P>Marshaller.marshal(Object, DomResult)</P>
 * 	<LI><P>Object Unmarshaller.unmarshal(XMLStreamReader)</P>
 * 	<LI><P><Object Unmarshaller.unmarshal(Source)</P>
 * </UL>
 * <H4>JAXB Runtime-API (private contract)</H4>
 * <P>JAX-WS RI uses these private API for serialization/deserialization
 * purposes. This private API is used to serialize/deserialize method
 * parameters at the time of binding.JAXBTypeSerializer class uses all
 * these methods.</P>
 * <UL>
 * 	<LI><P>Bridge.marshal(BridgeContext, Object, XMLStreamWriter)</P>
 * 	<LI><P>Bridge.marshal(BridgeContext, Object, Node)</P>
 * 	<LI><P>Bridge.unmarshal(BridgeContext, XMLStreamReader)</P>
 * </UL>
 * 
 * @ArchitectureDocument
 **/
package com.sun.xml.ws.client;

import javax.xml.ws.Binding;