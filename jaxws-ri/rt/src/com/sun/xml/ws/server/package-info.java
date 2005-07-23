/**
 *  <h1>JAX-WS 2.0 Server Runtime</h1>
 * <P>This document describes the architecture of server side 
 * JAX-WS 2.0 runtime. 
 *
 * <h3>JAX-WS 2.0 Server Runtime Sequence Diagram</h3>
 * {@SequenceDiagram
 *      pobject(U,"user");
 *      object(A,"WSConnection");
 *      object(B,"Tie");
 *      object(E,"MessageDispatcher");
 *      object(F,"EPTFactoryFactoryBase");
 *      object(G,"Endpoint");
 *      step();
 *
 *      message(U,A,"invoke Web Service");
 *      active(A);
 *      message(A,A,"getRuntimeEndpointInfo");
 *      active(A);
 *      step();
 *      inactive(A);
 *
 *      message(A,B,"handle");
 *      active(B);
 *      inactive(A);
 *
 *      message(B,B,"createMessageInfo");
 *      active(B);
 *      step();
 *      inactive(B);
 *
 *      message(B,B,"createRuntimeContext");
 *      active(B);
 *      step();
 *      inactive(B);
 *
 *
 *      message(B,F,"getEPTFactory");
 *      active(F);
 *      step();
 *      inactive(F);
 *      
 *      
 *      message(B,B,"getMessageDispatcher");
 *      active(B);
 *      step();
 *      inactive(B);
 *
 *      message(B,E,"receive");
 *      active(E);
 *      inactive(B);
 *      complete(B);
 *      step();
 *      
 *      active(E);
 *      message(E,E,"createSOAPMessage");
 *      inactive(E);
 *      step();
 *
 *      active(E);
 *      message(E,E,"createInternalMessage");
 *      inactive(E);
 *
 *      message(E,G,"invoke endpoint");
 *      active(G);
 *      step();
 *      rmessage(G,E,"response");
 *      inactive(G);
 *
 *      rmessage(E,A, "response");
 *      active(A);
 *      inactive(E);
 *
 *      rmessage(A,U, "response");
 * 
 *      complete(A);
 * }

 *
 *
 
 *
 * <H3>Message Flow</H3>
 * <P>A Web Service invocation starts with either the 
 * {@link com.sun.xml.ws.transport.http.servlet.WSServletDelegate WSServletDelegate}
 * or the {@link com.sun.xml.ws.transport.http.server.ServerConnectionImpl ServerConnectionImpl}.
 * Both of these classes find the appropriate {@link com.sun.xml.ws.server.RuntimeEndpointInfo RuntimeEndpointInfo}
 * and invokes the {@link com.sun.xml.ws.server.Tie#handle(com.sun.xml.ws.spi.runtime.WSConnection, 
 * com.sun.xml.ws.spi.runtime.RuntimeEndpointInfo) Tie.handle}
 * method. This method first creates a {@link com.sun.pept.ept.MessageInfo MessageInfo}
 * used to gather inforrmation about the message to be received. A
 * {@link com.sun.xml.ws.server.RuntimeContext RuntimeContext}
 * is then created with the MessageInfo and the {@link com.sun.xml.ws.model.RuntimeModel RuntimeModel}
 * retrieved from the RuntimeEndpointInfo. The RuntimeContext is then
 * stored in the MessageInfo. The {@link com.sun.pept.ept.EPTFactory EPTFactory}
 * is retrieved from the {@link com.sun.xml.ws.server.EPTFactoryFactoryBase EPTFactoryFactoryBase}
 * and also placed in the MessagInfo. A {@link com.sun.pept.protocol.MessageDispatcher MessageDispatcher}
 * is then created and the receive method is invoked. There will be two
 * types of MessageDispatchers for JAX-WS 2.0 FCS, SOAPMessageDispatcher
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
 * JAXB objects with user created {@link javax.xml.bind.JAXBContext JAXBContext}. 
 * Handler, Dispatch in JAX-WS API provide ways for the user to specify his/her own
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
package com.sun.xml.ws.server;
