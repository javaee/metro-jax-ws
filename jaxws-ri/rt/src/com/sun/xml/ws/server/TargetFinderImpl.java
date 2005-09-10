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
