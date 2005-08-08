/**
 * <h1>JAX-WS 2.0 Handler Runtime</h1>
 * <p>This document describes the architecture of the handler code
 * in the JAX-WS 2.0 runtime.
 *
 * <p>A user may specifiy handlers at tool time with wsgen or wsimport. A
 * handler chain file exists as a Java annotation (created from a wsdl
 * customization when starting from wsdl) and this file is read at
 * runtime to create the handler chain on the binding. On the server side,
 * a customization to the sun-jaxws.xml file can be used to add handlers.
 * These would be parsed first and handlers specified on the port as 
 * annotations would be ignored.
 *
 * <p>At runtime, a {@link HandlerChainCaller} is created by the binding on
 * client side or by a message dispatcher on the server side. The handler
 * chain caller does the handler invocation and controls the flow of the
 * handlers. For details of the code that calls the handler chain caller,
 * see {@link com.sun.xml.ws.protocol.soap.client.SOAPMessageDispatcher}
 * on the client side and 
 * {@link com.sun.xml.ws.protocol.soap.server.SOAPMessageDispatcher} and
 * {@link com.sun.xml.ws.protocol.xml.server.XMLMessageDispatcher} on the
 * server side.
 *
 * @ArchitectureDocument
 */
package com.sun.xml.ws.handler;
