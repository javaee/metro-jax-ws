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

package fromjava.nosei_apt.server;


import fromjava.nosei_apt.server.Bar.InnerBar;
import javax.jws.*;

import javax.xml.ws.Holder;
import javax.xml.ws.*;


@WebService(name="Echo", serviceName="EchoService", targetNamespace="http://echo.org/")
public class EchoImpl {

    // Result headers
    @WebMethod
    @WebResult(name="intHeaderResult", header=true)
    public int echoIntHeaderResult(int in) {
       return in*2;
    }


    //standard tests   
    @WebMethod(operationName="echoBar", action="urn:echoBar")
    public Bar echoBar(@WebParam(name="bar", mode=WebParam.Mode.IN)Bar param) throws Exception1 {
        return param;
    }
    
    @WebMethod
    public Bar.InnerBar echoInnerBar(Bar.InnerBar param) throws Exception1 {
        Bar.InnerBar innerBar = new Bar.InnerBar();
        innerBar.setName(param.getName()+param.getName());
        return innerBar;
    }    

    @WebMethod
    public String echoString(@WebParam String str) throws Exception1, Fault1, WSDLBarException, Fault2,
	  InterruptedException {
        if (str.equals("Exception1")) 
            throw new Exception1("my exception1");
        if (str.equals("Fault1")) {
            FooException fooException = new FooException();
            fooException.setVarString("foo");
            fooException.setVarInt(33);
            fooException.setVarFloat(44F);
            throw new Fault1("fault1", fooException);
        }
        if (str.equals("WSDLBarException")) 
            throw new WSDLBarException("my barException", new Bar(33));
        if (str.equals("Fault2"))
            throw new Fault2("my fault2", 33);
        if (str.equals("Interrupted"))
            throw new InterruptedException("bummer");
        return str;
    }

    @WebMethod
    public String[] echoStringArray(String[] str) {
        return str;
    }

    @WebMethod
    public long[] echoLongArray(long[] array) {
        return array;
    }

    @WebMethod
    public long echoLong(long lng) {
        return lng;
    }
    
    @WebMethod
    @WebResult(name="echoResult")
    public Bar[] echoBarArray(Bar[] bar) {
        return bar;
    }

    @WebMethod(operationName="echoBarAndBar", action="urn:echoBarAndBar")
    public Bar[] echoTwoBar(Bar bar, Bar bar2) {
        return new Bar[] { bar, bar2 };
    }

    volatile boolean onewayCalled = false;
    @WebMethod
    @Oneway
    public void oneway() {
        onewayCalled = true;
    }

    @WebMethod
    public boolean verifyOneway() {
        return onewayCalled;
    }

    @WebMethod
    public void voidTest() {
    }

    // Holders and modes
    @WebMethod
    public String outString(String tmp, @WebParam(mode=WebParam.Mode.OUT)Holder<String> str, int age) {
        str.value = tmp+age;
	  return tmp;
    }


    @WebMethod
    public String inOutString(String tmp, @WebParam(mode=WebParam.Mode.INOUT)Holder<String> str, int age) {
        str.value += str.value;
	  return tmp;
    }
 

    @WebMethod
    public int outLong(int age, @WebParam(mode=WebParam.Mode.OUT)Holder<Long> lng, String bogus) {
        lng.value = 345L;
	  return age;
    }

    @WebMethod
    public int inOutLong(int age, @WebParam(mode=WebParam.Mode.INOUT)Holder<Long> lng, String bogus) {
        lng.value = 2*lng.value;
	  return age;
    }


    // Headers, modes and holders
    @WebMethod
    public Long echoInHeader(int age, @WebParam(name="num", header=true)Long num, String str) {
        System.out.println("echoInHeader returnning: "+num);
        return num;
    }

    @WebMethod
    public String echoInOutHeader(int age, @WebParam(name="num", mode=WebParam.Mode.INOUT, header=true)Holder<Long> num, String str) {
        num.value = num.value*2;
        return str+num.value;
    }

    @WebMethod
    public String echoOutHeader(int age, @WebParam(name="num", mode=WebParam.Mode.OUT, header=true)Holder<Long> num, String str) {
        num.value = new Long(age);;
        return str+num.value;
    }

    // overload tests
    @WebMethod
    public String overloadedOperation(String param) throws java.rmi.RemoteException {
        return param;
    }

    @WebMethod(operationName="overloadedOperation2")
    @RequestWrapper(localName="req", targetNamespace="foo_bar", className="fromjava.nosei_apt.server.jaxws.OverloadedOperation2")
    @ResponseWrapper(localName="res", targetNamespace="foo_bar", className="fromjava.nosei_apt.server.jaxws.OverloadedOperation2Response")
    public String overloadedOperation(String param, String param2) throws java.rmi.RemoteException {
        return param + param2;
    }

    @WebMethod(operationName="overloadedOperation3")
    @RequestWrapper(targetNamespace="foo_bar", className="fromjava.nosei_apt.server.jaxws.OverloadedOperation3")
    @ResponseWrapper(targetNamespace="foo_bar", className="fromjava.nosei_apt.server.jaxws.OverloadedOperation3Response")
    public String overloadedOperation(String param, String param2, String param3) throws java.rmi.RemoteException {
        return param + param2 + param3;
    }

    @WebMethod(operationName="overloadedOperation4")
    @RequestWrapper(className="fromjava.nosei_apt.server.jaxws.OverloadedOperation4")
    @ResponseWrapper(className="fromjava.nosei_apt.server.jaxws.OverloadedOperation4Response")
    public String overloadedOperation(String param, String param2, String param3, String param4) throws java.rmi.RemoteException {
        return param + " "+ param2 + " "+ param3 + " "+ param4;
    }
}
