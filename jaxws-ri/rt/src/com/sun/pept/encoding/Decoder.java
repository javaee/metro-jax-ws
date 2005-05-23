/**
 * $Id: Decoder.java,v 1.1 2005-05-23 22:09:15 bbissett Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
/** Java interface "Decoder.java" generated from Poseidon for UML.
 *  Poseidon for UML is developed by <A HREF="http://www.gentleware.com">Gentleware</A>.
 *  Generated with <A HREF="http://jakarta.apache.org/velocity/">velocity</A> template engine.
 */
package com.sun.pept.encoding;

import com.sun.pept.ept.MessageInfo;
import java.util.*;

/**
 * <p>
 * 
 * @author Dr. Harold Carr
 * </p>
 */
public interface Decoder {

  ///////////////////////////////////////
  // operations

/**
 * <p>
 * Does ...
 * </p><p>
 * 
 * </p><p>
 * 
 * @param messageInfo ...
 * </p>
 */
    public void decode(MessageInfo messageInfo);
/**
 * <p>
 * Does ...
 * </p><p>
 * 
 * </p><p>
 * 
 * @param messageInfo ...
 * </p>
 */
    public void receiveAndDecode(MessageInfo messageInfo);

} // end Decoder







