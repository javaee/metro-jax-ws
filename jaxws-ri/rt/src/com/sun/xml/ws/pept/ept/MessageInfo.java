/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */
/** Java interface "MessageInfo.java" generated from Poseidon for UML.
 *  Poseidon for UML is developed by <A HREF="http://www.gentleware.com">Gentleware</A>.
 *  Generated with <A HREF="http://jakarta.apache.org/velocity/">velocity</A> template engine.
 */
package com.sun.xml.ws.pept.ept;

import com.sun.xml.ws.pept.encoding.Decoder;
import com.sun.xml.ws.pept.encoding.Encoder;
import com.sun.xml.ws.pept.presentation.MessageStruct;
import com.sun.xml.ws.pept.protocol.MessageDispatcher;
import com.sun.xml.ws.pept.transport.Connection;
import java.util.*;

/**
 * <p>
 * 
 * @author Dr. Harold Carr
 * </p>
 */
public interface MessageInfo extends MessageStruct {

  ///////////////////////////////////////
  // operations

/**
 * <p>
 * Does ...
 * </p><p>
 * 
 * @return a EPTFactory with ...
 * </p>
 */
    public EPTFactory getEPTFactory();
/**
 * <p>
 * Does ...
 * </p><p>
 * 
 * @return a MessageDispatcher with ...
 * </p>
 */
    public MessageDispatcher getMessageDispatcher();
/**
 * <p>
 * Does ...
 * </p><p>
 * 
 * @return a Encoder with ...
 * </p>
 */
    public Encoder getEncoder();
/**
 * <p>
 * Does ...
 * </p><p>
 * 
 * @return a Decoder with ...
 * </p>
 */
    public Decoder getDecoder();
/**
 * <p>
 * Does ...
 * </p><p>
 * 
 * @return a Connection with ...
 * </p>
 */
    public Connection getConnection();
/**
 * <p>
 * Does ...
 * </p><p>
 * 
 * </p><p>
 * 
 * @param eptFactory ...
 * </p>
 */
    public void setEPTFactory(EPTFactory eptFactory);
/**
 * <p>
 * Does ...
 * </p><p>
 * 
 * </p><p>
 * 
 * @param messageDispatcher ...
 * </p>
 */
    public void setMessageDispatcher(MessageDispatcher messageDispatcher);
/**
 * <p>
 * Does ...
 * </p><p>
 * 
 * </p><p>
 * 
 * @param encoder ...
 * </p>
 */
    public void setEncoder(Encoder encoder);
/**
 * <p>
 * Does ...
 * </p><p>
 * 
 * </p><p>
 * 
 * @param decoder ...
 * </p>
 */
    public void setDecoder(Decoder decoder);
/**
 * <p>
 * Does ...
 * </p><p>
 * 
 * </p><p>
 * 
 * @param connection ...
 * </p>
 */
    public void setConnection(Connection connection);

} // end MessageInfo







