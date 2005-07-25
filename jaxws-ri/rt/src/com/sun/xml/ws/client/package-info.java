/**
 *  <h1>JAX-WS 2.0 Client Runtime</h1>
 * <P>This document describes the architecture of client side 
 * JAX-WS 2.0 runtime. 
 *
 * <h3>JAX-WS 2.0 Server Client Sequence Diagram</h3>
 * {@SequenceDiagram
 *    pobject(U,"user");
 *    object(A,"EndpointIFInvocationHandler");
 *    object(B,"DelegateBase");
 *    object(C,"MessageDispatcher");
 *    object(D,"Encoder");
 *    object(E,"Decoder");
 *    object(F,"WSConnection");
 *    step();

 *    message(U,A,"invoke Web Service");
 *    active(A);
 *    message(A,A,"invoke");
 *    active(A);
 *    step();
 *    inactive(A);
 *
 *    active(A);
 *    message(A,A,"implementSEIMethod");
 *    step();
 *    inactive(A);
 *
 *    message(A,B,"send");
 *    active(B);
 *    step();
 *    inactive(A);
 *
 *    message(B,B,"getContactInfo");
 *    active(B);
 *    step();
 *    inactive(B);
 *
 *    message(B,B,"getMessageDispatcher");
 *    active(B);
 *    step();
 *    inactive(B);
 *
 *    message(B,C,"send");
 *    active(C);
 *    step();
 *    inactive(B);
 * 
 *    active(C);
 *    message(C,C,"doSend");
 *    inactive(C);
 *
 *    message(C,D,"toInternalMessage");
 *    active(D);
 *    step();
 *    inactive(C);
 *
 *    message(C,D,"toSOAPMessage");
 *    complete(D);
 *
 *    message(C,F,"setHeaders");
 *    active(F);
 *    step();
 *    inactive(C);
 *
 *    message(C,F,"getOutput");
 *    active(F);
 *    step();
 *    inactive(C);
 *
 *    message(C,F,"writeTo");
 *    active(F);
 *    step();
 *    inactive(C);
 *
 *    active(D);
 *    message(C,C,"receive");
 *    inactive(C);
 *
 *    active(C);
 *    message(C,C,"doSendAsync");
 *    inactive(C);
 *
 *    active(C);
 *    message(C,C,"sendAsyncReceive");
 *    inactive(C);
 *
 *    complete(A);
 * }
 *
 * <H3>Message Flow</H3>
 * {@link com.sun.xml.ws.client.WebService} provides client view of a Web service.
 * WebService.getPort returns an instance of {@link com.sun.xml.ws.client.EndpointIFInvocationHandler}
 * with {@link com.sun.pept.ept.ContactInfoList} and {@link com.sun.pept.Delegate} initialized. 
 * A method invocation on the port, obtained from WebService, invokes
 * {@link com.sun.xml.ws.client.EndpointIFInvocationHandler#invoke}. This method then creates a 
 * {@link com.sun.pept.ept.MessageInfo} and populates the data (parameters specified by the user) and metadata such as
 * RuntimeContext, RequestContext, Message Exchange Pattern into this MessageInfo. This method then
 * invokes {@link com.sun.pept.Delegate#send} and returns the response.
 * <P></P>
 * Delegate.send iterates through the ContactInfoList and picks up the correct {@link com.sun.pept.ept.ContactInfo}
 * based upon binding id of the {@link javax.xml.ws.BindingProvider} and sets it on
 * the MessageInfo. After the Delegate obtains a specific ContactInfo it uses that ContactInfo
 * to obtain a protocol-specific {@link com.sun.pept.protocol.MessageDispatcher}. There will be two types of 
 * client-side MessageDispatchers for JAX-WS 2.0 FCS, {@link com.sun.xml.ws.protocol.soap.client.SOAPMessageDispatcher} 
 * and an {@link com.sun.xml.ws.protocol.xml.client.XMLMessageDispatcher}. The Delegate then invokes
 * {@link com.sun.pept.protocol.MessageDispatcher#send}. A different method is invoked depending upon whether 
 * the request is synchronous or asynchronous.
 * 
 * The MessageDispatcher uses ContactInfo to obtain
 * a {@com.sun.xml.ws.encoding.soap.client.SOAPXMLEncoder} which converts the MessageInfo to 
 * {@link com.sun.xml.ws.encoding.soap.internal.InternalMessage}. The MessageDispatcher invokes 
 * any configured handlers and use the encoder to convert the InternalMessage to a {@link javax.xml.soap.SOAPMessage}. 
 * The metadata from the MessageInfo is classified into {@link javax.xml.soap.MimeHeaders} of this SOAPMessage and 
 * transport context. The SOAPMessge is then written to the output stream of the obtained WSConnection.
 *
 * The MessageDispatcher.receive handles the response. The SOAPMessageDispatcher extracts the 
 * SOAPMessage from the input stream of WSConnection and performs the mustUnderstand processing followed
 * by invocation of any handlers. The MessageDispatcher uses ContactInfo to obtain a {@com.sun.xml.ws.encoding.soap.client.SOAPXMLDecoder} 
 * which converts the SOAPMessage to InternalMessage and then InternalMessage to MessageInfo. The response
 * is returned back to the client code via Delegate.
 *
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