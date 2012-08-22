/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.sdo.test;

import java.util.List;
import java.util.ArrayList;

import javax.jws.WebService;

import com.sun.xml.ws.sdo.test.helloSDO.MySDO;

@WebService(targetNamespace="http://oracle.j2ee.ws.jaxws.test/")
public class HelloSDO_DocLitWrapImpl {

    public MySDO echoSDO(MySDO coo) {
        coo.setIntPart(coo.getIntPart() + 1);
        coo.setStringPart("Gary");
        return coo;
    }   

    public MySDO partI(MySDO coo, int i) {
        coo.setIntPart(i);
        return coo;
    }
    public String returnS(MySDO coo) { return coo.getStringPart(); }
    
    public int returnI(MySDO coo) { return coo.getIntPart(); }    
    

    public List<String> testList1(List<MySDO> l) {
        ArrayList<String> res = new ArrayList<String>();
        for (MySDO m : l) res.add(m.getStringPart());
        return res;
    }

    public List<String> testList2(List<String> l1, List<MySDO> l2) {
        ArrayList<String> res = new ArrayList<String>();
        for (String s : l1) res.add(s);
        for (MySDO m : l2) res.add(m.getStringPart());
        return res;
    }

    public String[] arrayList01(MySDO[] l) {
        String[] s = new String[l.length];
        for (int i = 0; i < l.length; i++ ) s[i] = l[i].getStringPart();
        return s;
    }
}
