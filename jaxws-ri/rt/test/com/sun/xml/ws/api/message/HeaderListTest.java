/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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
package com.sun.xml.ws.api.message;

import javax.xml.namespace.QName;
import junit.framework.TestCase;

public class HeaderListTest extends TestCase {

    public static final String TEST_NS = "http://jaxws.dev.java.net/";
    private HeaderList testInstance;

    public HeaderListTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        testInstance = new HeaderList();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        testInstance = null;
    }

    public void testRemoveHeader() throws Exception {

        for (int i = 0; i < 40; i++) {
            testInstance.add(Headers.create(new QName(TEST_NS, "" + i), "" + i));
        }

        testInstance.understood(1);
        testInstance.understood(testInstance.size() - 2);

        int expectedSize = testInstance.size();
        for (int i = 2; i < testInstance.size() - 2; i++) {
            testInstance.remove(new QName(TEST_NS, "" + i));


            assertEquals(--expectedSize, testInstance.size());
            assertFalse(testInstance.isUnderstood(0));
            assertTrue(testInstance.isUnderstood(1));
            for (int j = 2; j < testInstance.size() - 2; j++) {
                assertFalse(testInstance.isUnderstood(j));
            }
            assertTrue(testInstance.isUnderstood(testInstance.size() - 2));
            assertFalse(testInstance.isUnderstood(testInstance.size() - 1));
        }
    }
}