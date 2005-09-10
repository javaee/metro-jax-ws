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
/** Java interface "EPTFactory.java" generated from Poseidon for UML.
 *  Poseidon for UML is developed by <A HREF="http://www.gentleware.com">Gentleware</A>.
 *  Generated with <A HREF="http://jakarta.apache.org/velocity/">velocity</A> template engine.
 */
package com.sun.pept.ept;

import com.sun.pept.encoding.Decoder;
import com.sun.pept.encoding.Encoder;
import com.sun.pept.presentation.TargetFinder;
import com.sun.pept.protocol.Interceptors;
import com.sun.pept.protocol.MessageDispatcher;
import java.util.*;

/**
 * <p>
 * 
 * @author Dr. Harold Carr
 * </p>
 */
public interface EPTFactory {

  ///////////////////////////////////////
  // operations

/**
 * <p>
 * Does ...
 * </p><p>
 * 
 * @return a MessageDispatcher with ...
 * </p><p>
 * @param messageInfo ...
 * </p>
 */
    public MessageDispatcher getMessageDispatcher(MessageInfo messageInfo);
/**
 * <p>
 * Does ...
 * </p><p>
 * 
 * @return a Encoder with ...
 * </p><p>
 * @param messageInfo ...
 * </p>
 */
    public Encoder getEncoder(MessageInfo messageInfo);
/**
 * <p>
 * Does ...
 * </p><p>
 * 
 * @return a Decoder with ...
 * </p><p>
 * @param messageInfo ...
 * </p>
 */
    public Decoder getDecoder(MessageInfo messageInfo);
/**
 * <p>
 * Does ...
 * </p><p>
 * 
 * @return a Interceptors with ...
 * </p><p>
 * @param x ...
 * </p>
 */
    public Interceptors getInterceptors(MessageInfo x);
/**
 * <p>
 * Does ...
 * </p><p>
 * 
 * @return a TargetFinder with ...
 * </p><p>
 * @param x ...
 * </p>
 */
    public TargetFinder getTargetFinder(MessageInfo x);

} // end EPTFactory







