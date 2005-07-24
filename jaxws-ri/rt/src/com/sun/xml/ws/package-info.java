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
package com.sun.xml.ws;

//import javax.xml.ws.Binding;