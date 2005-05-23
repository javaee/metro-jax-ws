/**
 * $Id: Delegate.java,v 1.1 2005-05-23 22:09:19 bbissett Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
/** Java interface "Delegate.java" generated from Poseidon for UML.
 *  Poseidon for UML is developed by <A HREF="http://www.gentleware.com">Gentleware</A>.
 *  Generated with <A HREF="http://jakarta.apache.org/velocity/">velocity</A> template engine.
 */
package com.sun.pept;

import com.sun.pept.presentation.MessageStruct;
import java.util.*;

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







