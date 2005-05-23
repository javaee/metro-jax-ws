/**
 * $Id: TargetFinderImpl.java,v 1.1 2005-05-23 22:50:26 bbissett Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
//
// Created       : 2004 Apr 09 (Fri) 06:16:58 by Harold Carr.
// Last Modified : 2004 May 03 (Mon) 17:28:38 by Harold Carr.
//
// @(#)FakeTargetFinder.java    1.2 04/05/03
//

package com.sun.xml.ws.server;

import com.sun.pept.ept.MessageInfo;
import com.sun.pept.presentation.TargetFinder;
import com.sun.pept.presentation.Tie;

public class TargetFinderImpl implements TargetFinder {

    private Tie tie;

    public TargetFinderImpl(Tie tie) {
        this.tie = tie;
    }

    public Tie findTarget(MessageInfo messageInfo) {
        return tie;
    }
}
