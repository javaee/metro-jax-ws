/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.ws.Action;
import javax.xml.ws.Holder;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

@WebService(name = "DocServicePortType", targetNamespace = "http://performance.bea.com")
public interface DocServicePortType {

    /**
     * 
     * @param shortMessage
     * @param floatMessage
     */
    @WebMethod
    @RequestWrapper(localName = "echoBaseStruct", targetNamespace = "http://performance.bea.com", className = "com.bea.performance.BaseStruct")
    @ResponseWrapper(localName = "BaseStructOutput", targetNamespace = "http://weblogic/performance/benchmarks/jwswsee/doc", className = "com.bea.performance.BaseStruct")
    @Action(input = "http://performance.bea.com/DocServicePortType/echoBaseStructRequest", output = "http://performance.bea.com/DocServicePortType/echoBaseStructResponse")
    public void echoBaseStruct(
        @WebParam(name = "floatMessage", targetNamespace = "", mode = WebParam.Mode.INOUT)
        Holder<Float> floatMessage,
        @WebParam(name = "shortMessage", targetNamespace = "", mode = WebParam.Mode.INOUT)
        Holder<Short> shortMessage);

    /**
     * 
     * @param shortMessage
     * @param intMessage
     * @param anotherIntMessage
     * @param stringMessage
     * @param floatMessage
     */
    @WebMethod
    @RequestWrapper(localName = "echoExtendedStruct", targetNamespace = "http://performance.bea.com", className = "com.bea.performance.ExtendedStruct")
    @ResponseWrapper(localName = "ExtendedStructOutput", targetNamespace = "http://weblogic/performance/benchmarks/jwswsee/doc", className = "com.bea.performance.ExtendedStruct")
    @Action(input = "http://performance.bea.com/DocServicePortType/echoExtendedStructRequest", output = "http://performance.bea.com/DocServicePortType/echoExtendedStructResponse")
    public void echoExtendedStruct(
        @WebParam(name = "floatMessage", targetNamespace = "", mode = WebParam.Mode.INOUT)
        Holder<Float> floatMessage,
        @WebParam(name = "shortMessage", targetNamespace = "", mode = WebParam.Mode.INOUT)
        Holder<Short> shortMessage,
        @WebParam(name = "anotherIntMessage", targetNamespace = "", mode = WebParam.Mode.INOUT)
        Holder<Integer> anotherIntMessage,
        @WebParam(name = "intMessage", targetNamespace = "", mode = WebParam.Mode.INOUT)
        Holder<Integer> intMessage,
        @WebParam(name = "stringMessage", targetNamespace = "", mode = WebParam.Mode.INOUT)
        Holder<String> stringMessage);

  
    /**
     * 
     * @param shortMessage
     * @param booleanMessage
     * @param intMessage
     * @param anotherIntMessage
     * @param stringMessage
     * @param floatMessage
     */
    @WebMethod
    @RequestWrapper(localName = "modifyMoreExtendedStruct", targetNamespace = "http://performance.bea.com", className = "com.bea.performance.MoreExtendedStruct")
    @ResponseWrapper(localName = "MoreExtendedStructOutput", targetNamespace = "http://weblogic/performance/benchmarks/jwswsee/doc", className = "com.bea.performance.MoreExtendedStruct")
    @Action(input = "http://performance.bea.com/DocServicePortType/modifyMoreExtendedStructRequest", output = "http://performance.bea.com/DocServicePortType/modifyMoreExtendedStructResponse")
    public void modifyMoreExtendedStruct(
        @WebParam(name = "floatMessage", targetNamespace = "", mode = WebParam.Mode.INOUT)
        Holder<Float> floatMessage,
        @WebParam(name = "shortMessage", targetNamespace = "", mode = WebParam.Mode.INOUT)
        Holder<Short> shortMessage,
        @WebParam(name = "anotherIntMessage", targetNamespace = "", mode = WebParam.Mode.INOUT)
        Holder<Integer> anotherIntMessage,
        @WebParam(name = "intMessage", targetNamespace = "", mode = WebParam.Mode.INOUT)
        Holder<Integer> intMessage,
        @WebParam(name = "stringMessage", targetNamespace = "", mode = WebParam.Mode.INOUT)
        Holder<String> stringMessage,
        @WebParam(name = "booleanMessage", targetNamespace = "", mode = WebParam.Mode.INOUT)
        Holder<Boolean> booleanMessage);
}
