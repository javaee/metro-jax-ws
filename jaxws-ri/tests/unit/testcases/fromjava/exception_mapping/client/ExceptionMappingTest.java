/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
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

package fromjava.exception_mapping.client;

import junit.framework.TestCase;

import javax.xml.ws.Holder;

import org.omg.CORBA.StringHolder;

import java.util.List;
import java.util.ArrayList;

/**
 * This tests the mapping of Exceptions from Java to WSDL covering the rules defined in JAX-WS for request/response
 * and oneway operations.  
 *
 * @author Rama Pulavarthi
 */
public class ExceptionMappingTest extends TestCase {

    public void test1() {
        EchoImpl port =new EchoImplService().getEchoImplPort();
        Bar bar = new Bar();
        bar.setName("JavaOne");
        bar.setAge(13);
        try {
            port.echoBar(bar);
        } catch (EchoException_Exception e) {
            //do nothing this is the only checked exception that this method should throw
        }
    }

    public void test2() {
        EchoImpl port =new EchoImplService().getEchoImplPort();
        port.echoString("Duke");
    }

    public void test3() {
        EchoImpl port =new EchoImplService().getEchoImplPort();
        Holder<String> holder = new Holder<String>();
        holder.value = "Duke";
        port.echoStringHolder(holder);
    }

    public void test4() {
        EchoImpl port =new EchoImplService().getEchoImplPort();
        Bar bar = new Bar();
                bar.setName("JavaOne");
                bar.setAge(13);

        List<Bar> list = new ArrayList<Bar>();
        list.add(bar);
        port.echoBarList(list);
    }

    public void test5() {
        EchoImpl port =new EchoImplService().getEchoImplPort();
        port.notify("Duke");        
    }

}
