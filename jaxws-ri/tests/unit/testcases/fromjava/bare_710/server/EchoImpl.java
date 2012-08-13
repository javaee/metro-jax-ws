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
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
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

package fromjava.bare_710.server;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.xml.ws.Holder;
import javax.xml.ws.*;

/**
 * fromjava doc/bare mapping
 *
 * @author Jitenddra Kotamraju
 */
@WebService(name="Echo", serviceName="EchoService", targetNamespace="http://echo.org/")
@SOAPBinding(parameterStyle=ParameterStyle.BARE)
public class EchoImpl {

    public int add(NumbersRequest numRequest) {
        if (numRequest.number1 != 10)
            throw new WebServiceException("numRequest.number1 expected=10"+" got="+numRequest.number1);
        if (numRequest.number2 != 20)
            throw new WebServiceException("numRequest.number2 expected=10"+" got="+numRequest.number2);
        if (numRequest.guess != 25)
            throw new WebServiceException("numRequest.guess expected=25"+" got="+numRequest.guess);
        return numRequest.number1+numRequest.number2;
    }

    public void addNumbers(NumbersRequest numRequest,
                        @WebParam(mode=WebParam.Mode.OUT)
                        Holder<Integer> res) {
        if (numRequest.number1 != 10)
            throw new WebServiceException("numRequest.number1 expected=10"+" got="+numRequest.number1);
        if (numRequest.number2 != 20)
            throw new WebServiceException("numRequest.number2 expected=10"+" got="+numRequest.number2);
        if (numRequest.guess != 25)
            throw new WebServiceException("numRequest.guess expected=25"+" got="+numRequest.guess);
        res.value = numRequest.number1+numRequest.number2;
    }

//    > 1 IN body part. Throws an exception
//    public void sumNumbers(NumbersRequest numRequest,
//                        @WebParam(mode=WebParam.Mode.INOUT)
//                        Holder<Integer> res) {
//        if (numRequest.number1 != 10)
//            throw new WebServiceException("numRequest.number1 expected=10"+" got"+numRequest.number1);
//        if (numRequest.number2 != 20)
//            throw new WebServiceException("numRequest.number2 expected=10"+" got"+numRequest.number2);
//        if (res.value != 25)
//            throw new WebServiceException("res.value expected=25"+" got"+res.value);
//
//        res.value = numRequest.number1+numRequest.number2;
//    }

    public void echoString(@WebParam(mode=WebParam.Mode.INOUT)
                           Holder<String> str) {        
        if (!str.value.equals("test"))
            throw new WebServiceException("str.value expected=test"+" got="+str.value);
    }

    @WebResult(header=true)
    public String echoHeaders(@WebParam(mode=WebParam.Mode.INOUT)
                           Holder<String> str,
                           @WebParam(header=true)
                           String inHeader,
                           @WebParam(header=true, mode=WebParam.Mode.OUT)
                           Holder<String> outHeader,
                           @WebParam(header=true, mode=WebParam.Mode.INOUT)
                           Holder<String> inoutHeader) {
        if (!str.value.equals("test"))
            throw new WebServiceException("str.value expected=test"+" got="+str.value);
        if (!inHeader.equals("inHeader"))
            throw new WebServiceException("inHeader expected=inHeader"+" got="+inHeader);
        if (!inoutHeader.value.equals("inoutHeader"))
            throw new WebServiceException("inoutHeader expected=inoutHeader"+" got="+inoutHeader);
        outHeader.value = "outHeader";
        return "returnHeader";
    }

}
