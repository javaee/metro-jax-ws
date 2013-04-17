/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004-2013 Oracle and/or its affiliates. All rights reserved.
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

package fromjava.mixed_bindings.server;


import javax.jws.*;
import javax.jws.soap.*;

import javax.xml.ws.Holder;
import javax.xml.ws.*;


@WebService(name="Echo", serviceName="EchoService", targetNamespace="http://echo.org/")
public class EchoImpl {

    /*
     * ParameterStyle=BARE Methods 
     */

    @SOAPBinding(parameterStyle=SOAPBinding.ParameterStyle.BARE)
    @WebMethod
    public String echoBar(String bar) {
        return bar;
    }

    /*
     * ParameterStyle=WRAPPED Methods 
     */
    @WebMethod
    public String echoFoo(String foo, String random) {
        return foo + random;
    }
    
    @WebMethod(operationName="echoFoo1", action="echoFoo1")   
    @RequestWrapper(className="misc.java_overload.Foo1Req")
    @ResponseWrapper(targetNamespace="urn:foo", className="misc.java_overload.Foo1Resp")
    public int echoFoo(int foo) {
        return foo;
    }
    
}
