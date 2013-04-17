/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

/*
 $Id: AddNumbersClient.java,v 1.1.2.5 2006/08/29 19:59:00 arung Exp $

 Copyright (c) 2006 Sun Microsystems, Inc.
 All rights reserved.
*/

package wsa.fromwsdl.requiredfalse.client;

import junit.framework.TestCase;

/**
 * @author Arun Gupta
 */
public class AddNumbersClient extends TestCase {
    public AddNumbersClient(String name) {
        super(name);
    }

    private AddNumbersPortType createStub() throws Exception {
        return new AddNumbersService().getAddNumbersPort();
    }

    public void testDefaultActions() throws Exception {
        int result = createStub().addNumbers(10, 10);
        assertTrue(result == 20);
    }

    public void testActionWithExplicitNames() throws Exception {
        int result = createStub().addNumbers2(10, 10);
        assertEquals(20, result);
    }

    public void testActionWithInputNameOnly() throws Exception {
        int result = createStub().addNumbers3(10, 10);
        assertEquals(20, result);
    }

    public void testActionWithOutputNameOnly() throws Exception {
        int result = createStub().addNumbers4(10, 10);
        assertEquals(20, result);
    }

    public void testExplicitActionsBoth() throws Exception {
        int result = createStub().addNumbers5(10, 10);
        assertEquals(20, result);
    }

    public void testExplicitActionsInputOnly() throws Exception {
        int result = createStub().addNumbers6(10, 10);
        assertEquals(20, result);
    }

    public void testExplicitActionsOutputOnly() throws Exception {
        int result = createStub().addNumbers7(10, 10);
        assertEquals(20, result);
    }

    public void testEmptyActions() throws Exception {
        int result = createStub().addNumbers8(10, 10);
        assertEquals(20, result);
    }
}
