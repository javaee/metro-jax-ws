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
/** Java interface "Delegate.java" generated from Poseidon for UML.
 *  Poseidon for UML is developed by <A HREF="http://www.gentleware.com">Gentleware</A>.
 *  Generated with <A HREF="http://jakarta.apache.org/velocity/">velocity</A> template engine.
 */
package com.sun.xml.ws.pept;

import com.sun.xml.ws.pept.presentation.MessageStruct;

/**
 * <p>
 * 
 * @author Dr. Harold Carr
 * </p>
 */
public interface Delegate {

  ///////////////////////////////////////
  // operations

/**
 * <p>
 * Does ...
 * </p><p>
 * 
 * @return a MessageStruct with ...
 * </p>
 */
    public MessageStruct getMessageStruct();
/**
 * <p>
 * Does ...
 * </p><p>
 * 
 * </p><p>
 * 
 * @param message ...
 * </p>
 */
    public void send(MessageStruct message);

} // end Delegate







