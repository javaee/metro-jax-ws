/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package fromwsdl.jaxws102.client;

import junit.framework.TestCase;

import javax.xml.ws.Holder;

/**
 * @author Vivek Pandey
 */
public class Client extends TestCase {

    private BenchMarkPortType port1;
    private BenchMarkSubBPPortType port2;
    public Client(String name) {
        super(name);
        port1 = new BenchMarkService().getBenchMarkPort();
        port2 = new BenchMarkSubBPService().getBenchMarkSubPort();
    }

    public void testBenchMarkPortTypeOp1(){
        BenchMarkType bm = new BenchMarkType();
        bm.setString("Type1");
        Holder<BenchMarkType> type = new Holder<BenchMarkType>(bm);
        port1.benchMarkOperation1(type);
        assertTrue(type.value.getString().equals("Type2"));
    }

    public void testBenchMarkPortTypeOp2(){
        BenchMarkType type = new BenchMarkType();
        type.setString("Type2");
        port1.benchMarkOperation2(type);
    }

    public void testBenchMarkPortSubBPTypeOp(){
        BenchMarkType bm = new BenchMarkType();
        bm.setString("BPType1");
        Holder<BenchMarkType> type = new Holder<BenchMarkType>(bm);
        port2.benchMarkSubBPOperation1(type);
        System.out.println("REceived: "+type.value.getString());
        assertTrue(type.value.getString().equals("BPType2"));
    }
}
