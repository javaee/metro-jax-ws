/**
 * $Id: MessageInfo.java,v 1.1 2005-05-23 22:09:17 bbissett Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
/** Java interface "MessageInfo.java" generated from Poseidon for UML.
 *  Poseidon for UML is developed by <A HREF="http://www.gentleware.com">Gentleware</A>.
 *  Generated with <A HREF="http://jakarta.apache.org/velocity/">velocity</A> template engine.
 */
package com.sun.pept.ept;

import com.sun.pept.encoding.Decoder;
import com.sun.pept.encoding.Encoder;
import com.sun.pept.presentation.MessageStruct;
import com.sun.pept.protocol.MessageDispatcher;
import com.sun.pept.transport.Connection;
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







