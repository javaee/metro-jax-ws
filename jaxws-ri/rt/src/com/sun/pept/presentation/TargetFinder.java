/**
 * $Id: TargetFinder.java,v 1.1 2005-05-23 22:09:17 bbissett Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
/** Java interface "TargetFinder.java" generated from Poseidon for UML.
 *  Poseidon for UML is developed by <A HREF="http://www.gentleware.com">Gentleware</A>.
 *  Generated with <A HREF="http://jakarta.apache.org/velocity/">velocity</A> template engine.
 */
package com.sun.pept.presentation;

import com.sun.pept.ept.MessageInfo;
import java.util.*;

/**
 * <p>
 * 
 * @author Dr. Harold Carr
 * </p>
 */
public interface TargetFinder {

  ///////////////////////////////////////
  // operations

/**
 * <p>
 * Does ...
 * </p><p>
 * 
 * @return a Tie with ...
 * </p><p>
 * @param x ...
 * </p>
 */
    public Tie findTarget(MessageInfo x);

} // end TargetFinder







