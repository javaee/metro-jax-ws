/**
 *
 * <P>This document describes the architecture of JAX-WS 2.0 runtime. JAX-WS is
 * the aggregating component of what is called the integrated Stack
 * (I-Stack). The I-Stack consists of JAX-WS, JAX-B, StAX, SAAJ and Fast
 * Infoset. JAX-B is the databinding component of the stack. StAX is the
 * Streaming XML parser used by the stack. SAAJ is used for its
 * attachment support with SOAP messages and to allow handler developers
 * to gain access to the SOAP message via a standard interface. Fast
 * Infoset is a binary encoding of XML that can improve performance.</P>
 * <P>JAX-WS 2.0 was originally called JAX-RPC 2.0 and was hence
 * developed from the JAX-RPC 1.1 code base. JAX-WS has been
 * significanlty rearchitected to for extensibility going forward by
 * incorporating and implementing concepts from <A HREF="pept.html">PEPT</A>.
 * PEPT presents four main components presentation, encoding, protocol
 * and transport. Ideally each of these components would not be
 * dependent on another, however, due to time constraints an optimal
 * PEPT implementation was not possible and thus some of the PEPT
 * boundaries have been blurred. Future versions of JAX-WS may sharpen
 * these boundaries when possible.</P>
 * <P>The remainder of this document will describe the JAX-WS runtime
 * architecture from the client and server perspectives.</P>
 *
 * <p>
 * <dl>
 *  <dt>{@link com.sun.xml.ws.server Server}
 *  <dd>
 *    The server side portion of the JAX-WS runtime.
 *
 *  <dt>{@link com.sun.xml.ws.client Client}
 *  <dd>
 *    The client side portion of the JAX-WS runtime.
 *
 * </dl>
 * 
 * @ArchitectureDocument
 **/

/*
 * <H2>2.0 Client side Runtime</H2>
 * <P><BR><BR>
 * </P>
<H2>3.0 Server Side Runtime</H2>
<H3>3.1 Important classes</H3>
<H4>3.1.1 <A HREF="javadocs/com/sun/xml/ws/server/Tie.html">com.sun.xml.ws.server.Tie</A></H4>
<H4>3.1.2 <A HREF="javadocs/com/sun/xml/ws/server/EPTFactoryFactoryBase.html">com.sun.xml.ws.server.EPTFactoryFactoryBase</A></H4>
<H4>3.1.3 <A HREF="javadocs/com/sun/xml/ws/server/RuntimeEndpointInfo.html">com.sun.xml.ws.server.RuntimeEndpointInfo</A></H4>
<H4>3.1.4 <A HREF="javadocs/com/sun/xml/ws/spi/runtime/WSConnection.html">com.sun.xml.ws.spi.runtime.WSConnection</A></H4>
<H4><FONT FACE="Thorndale, serif">3.1.<FONT FACE="Thorndale, serif">5
</FONT></FONT><A HREF="javadocs/com/sun/xml/ws/transport/http/servlet/JAXRPCContextListener.html"><FONT FACE="Thorndale, serif">com.sun.xml.ws.transport.http.servlet.WSContextListener</FONT></A></H4>
<P STYLE="margin-bottom: 0in; text-decoration: none"><FONT FACE="Times, serif"><FONT SIZE=3>Class
needs to be renamed to WSContextListener</FONT></FONT></P>
<H4><FONT FACE="Thorndale, serif">3.1.6
<A HREF="javadocs/com/sun/xml/ws/transport/http/servlet/ServletConnectionImpl.html">com.sun.xml.ws.transport.http.servlet.<FONT FACE="Times, serif">ServletConnectionImpl</FONT></A></FONT></H4>
<H4><FONT FACE="Thorndale, serif">3.1.7
<A HREF="javadocs/com/sun/xml/ws/transport/http/servlet/JAXRPCServletDelegate.html">com.sun.xml.ws.transport.http.servlet.WS<FONT FACE="Times, serif">ServletDelegate</FONT></A></FONT></H4>
<P STYLE="margin-bottom: 0in"><A HREF="./javadocs\com\sun\xml\ws\transport\http\servlet\JAXRPCServletDelegate.html"><SPAN STYLE="text-decoration: none"><FONT FACE="Times, serif"><FONT COLOR="#000000">Class
needs to be renamed to WSServletDelegate</FONT></FONT></SPAN></A></P>
<H4>3.1.8 <A HREF="javadocs/com/sun/xml/ws/transport/http/server/ServerConnectionImpl.html">com.sun.xml.ws.transport.http.server.ServerConnectionImpl</A></H4>
<H3>3.3 Message Flow</H3>
<P>A Web Service invocation starts with either the <A HREF="javadocs/com/sun/xml/ws/transport/http/servlet/JAXRPCServletDelegate.html"><FONT FACE="Times, serif">WSServletDelegate</FONT></A>
or the <A HREF="javadocs/com/sun/xml/ws/transport/http/server/ServerConnectionImpl.html">ServerConnectionImpl</A>.
Both of these classes find the appropriate</P>
<P><A HREF="javadocs/com/sun/xml/ws/server/RuntimeEndpointInfo.html">RuntimeEndpointInfo</A>
and invokes the <A HREF="javadocs/com/sun/xml/ws/server/Tie.html#handle(com.sun.xml.ws.spi.runtime.WSConnection, com.sun.xml.ws.spi.runtime.RuntimeEndpointInfo)">Tie.handle</A>
method. This method first creates a <A HREF="javadocs">MessageInfo</A>
used to gather inforrmation about the message to be received. A
<A HREF="javadocs/com/sun/xml/ws/server/RuntimeContext.html">RuntimeContext</A>
is then created with the MessageInfo and the <A HREF="javadocs/com/sun/xml/ws/model/RuntimeModel.html">RuntimeModel</A>
retrieved from the RuntimeEndpointInfo. The RuntimeContext is then
stored in the MessageInfo. The <A HREF="javadocs/com/sun/pept/ept/EPTFactory.html">EPTFactory</A>
is retrieved from the <A HREF="javadocs/com/sun/xml/ws/server/EPTFactoryFactoryBase.html">EPTFactoryFactoryBase</A>
and also placed in the MessagInfo. A <A HREF="javadocs/com/sun/pept/protocol/MessageDispatcher.html">MessageDispatcher</A>
is then created and the receive method is invoked. There will be two
types of MessageDispatchers for JAX-WS 2.0 FCS, SOAPMessageDispatcher
(one for client and one for the server) and an XMLMessageDispatcher
(one for the client and one for the server).</P>
<P>The MessageDispatcher.receive method orchestrates the receiving of
a Message. The SOAPMessageDispatcher first converts the MessageInfo
to a SOAPMessage. The SOAPMessageDispatcher then does mustUnderstand
processing followed by an invocation of any handlers. The SOAPMessage
is then converted to an InternalMessage and stored in the
MessageInfo. The converting of the SOAPMessage to an InternalMessage
is done using the decoder retrieved from the EPTFactory that is
contained in the MessageInfo. Once the SOAPMessage has been converted
to an InternalMessage the endpoint implementation is invoked via
reflection from the Method stored in the MessageInfo. The return
value of the method call is then stored in the InternalMessage. An
internalMessage is then created from the MessageInfo. The SOAPEncoder
is retrieved from the EPTFactory stored in the MessageInfo. The
SOAPEncoder.toSOAPMessage is then invoked to create a SOAPMessage
from the InternalMessage. A WSConnection is then retrieved from the
MessageInfo and the SOAPMessage is returned over that WSConnection.</P>
<P><BR><BR>
</P>
<H3>3.3 External Interactions</H3>
<H4>3.3.1 SAAJ API</H4>
<UL>
	<LI><P>JAX-WS creates SAAJ SOAPMessage from the HttpServletRequest.
	At present, JAX-WS reads all the bytes from the request stream and
	then creates SOAPMessage along with the HTTP headers.</P>
</UL>
<P>MessageFactory(binding).createMessage(MimeHeaders, InputStream)</P>
<UL>
	<LI><P>SOAPMessage parses the content from the stream including MIME
	data</P>
	<LI><P>com.sun.xml.ws.server.SOAPMessageDispatcher::checkHeadersPeekBody()</P>
	<P>SOAPMessage.getSOAPHeader() is used for mustUnderstand processing
	of headers. It further uses
	SOAPHeader.examineMustUnderstandHeaderElements(role)</P>
	<P>SOAPMessage.getSOAPBody().getFistChild() is used for guessing the
	MEP of the request</P>
	<LI><P>com.sun.xml.ws.handler.HandlerChainCaller:insertFaultMessage()</P>
	<P>SOAPMessage.getSOAPPart().getEnvelope() and some other SAAJ calls
	are made to create a fault in the SOAPMessage</P>
	<LI><P>com.sun.xml.ws.handler.LogicalMessageImpl::getPayload()
	interacts with SAAJ to get body from SOAPMessage</P>
	<LI><P>com.sun.xml.ws.encoding.soap.SOAPEncoder.toSOAPMessage(com.sun.xml.ws.encoding.soap.internal.InternalMessage,
	SOAPMessage). There is a scenario where there is SOAPMessage and a
	logical handler sets payload as Source. To write to the stream,
	SOAPMessage.writeTo() is used but before that the body needs to be
	updated with logical handler' Source. Need to verify if this
	scenario is still happening since Handler.close() is changed to take
	MessageContext.</P>
	<LI><P>com.sun.xml.ws.handlerSOAPMessageContextImpl.getHeaders()
	uses SAAJ API to get headers.</P>
	<LI><P>SOAPMessage.writeTo() is used to write response. At present,
	it writes into byte[] and this byte[] is written to
	HttpServletResponse.</P>
</UL>
<H4>3.3.2 JAXB API</H4>
<P>JAX-WS RI uses the JAXB API to marshall/unmarshall user created
JAXB objects with user created JAXBContext.Handler, Dispatch in
JAX-WS API provide ways for the user to specify his/her own
JAXBContext. JAXBTypeSerializer class uses all these methods.</P>
<UL>
	<LI><H5 STYLE="font-weight: medium">Marshaller.marshal(Object,
	XMLStreamWriter)</H5>
	<LI><P><FONT FACE="Times, serif">Marshaller.marshal(Object,
	DomResult)</FONT></P>
	<LI><P><FONT FACE="Times, serif">Object
	Unmarshaller.unmarshal(XMLStreamReader)</FONT></P>
	<LI><P><FONT FACE="Times, serif">Object
	Unmarshaller.unmarshal(Source)</FONT></P>
</UL>
<H4>3.3.3 JAXB Runtime-API (private contract)</H4>
<P>JAX-WS RI uses these private API for serialization/deserialization
purposes. This private API is used to serialize/deserialize method
parameters at the time of binding.JAXBTypeSerializer class uses all
these methods.</P>
<UL>
	<LI><P ALIGN=LEFT><FONT FACE="Times, serif">Bridge.marshal(BridgeContext,
	Object, XMLStreamWriter)</FONT></P>
	<LI><P ALIGN=LEFT><FONT FACE="Times, serif">Bridge.marshal(BridgeContext,
	Object, Node)</FONT></P>
	<LI><P ALIGN=LEFT><FONT FACE="Times, serif">Bridge.unmarshal(BridgeContext,
	XMLStreamReader)</FONT></P>
</UL>
*/
package com.sun.xml.ws;

//import javax.xml.ws.Binding;