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

package com.sun.xml.ws.cts.jws_webparam1;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebParam.Mode;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;

import javax.xml.ws.Action;
import javax.xml.ws.FaultAction;
import javax.xml.ws.Holder;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

import com.sun.xml.ws.cts.jws_webparam1.Address;
import com.sun.xml.ws.cts.jws_webparam1.Employee;
import com.sun.xml.ws.cts.jws_webparam1.Name;
import com.sun.xml.ws.cts.jws_webparam1.NameException;
import com.sun.xml.ws.cts.jws_webparam1.NameException_Exception;


@WebService(targetNamespace="http://server.webparam1.webparam.jws.tests.ts.sun.com/", 
  name="webParamWebService")
public interface WebParamWebService
{
  @WebMethod(action="urn:HelloString")
  @Action(input="urn:HelloString", output="http://server.webparam1.webparam.jws.tests.ts.sun.com/webParamWebService/helloString/Response")
  @SOAPBinding(parameterStyle=ParameterStyle.BARE)
  @WebResult(targetNamespace="http://server.webparam1.webparam.jws.tests.ts.sun.com/", 
    partName="helloStringResponse", name="helloStringResponse")
  public String helloString(@WebParam(targetNamespace="http://server.webparam1.webparam.jws.tests.ts.sun.com/", 
      partName="string1", name="string1")
    String string1);

  @WebMethod(action="urn:HelloString2")
  @Action(input="urn:HelloString2", output="http://server.webparam1.webparam.jws.tests.ts.sun.com/webParamWebService/helloString2/Response")
  @SOAPBinding(parameterStyle=ParameterStyle.BARE)
  @WebResult(targetNamespace="http://server.webparam1.webparam.jws.tests.ts.sun.com/", 
    partName="helloString2Response", name="helloString2Response")
  public String helloString2(@WebParam(targetNamespace="helloString2/name", 
      partName="string2", name="name2")
    String string2);

  @WebMethod(action="urn:HelloString3")
  @Action(input="urn:HelloString3", output="http://server.webparam1.webparam.jws.tests.ts.sun.com/webParamWebService/helloString3/Response")
  @SOAPBinding(parameterStyle=ParameterStyle.BARE)
  public void helloString3(@WebParam(targetNamespace="helloString3/name", 
      header=true, partName="string3", name="name3")
    String string3, @WebParam(targetNamespace="helloString3/Name", mode=Mode.INOUT, 
      partName="Name", name="Name")
    Holder<Name> Name);

  @WebMethod(action="urn:HelloString4")
  @Action(input="urn:HelloString4", fault =
      { @FaultAction(value="http://server.webparam1.webparam.jws.tests.ts.sun.com/webParamWebService/helloString4/Fault/NameException", 
          className = NameException_Exception.class)
        } , output="http://server.webparam1.webparam.jws.tests.ts.sun.com/webParamWebService/helloString4/Response")
  @SOAPBinding(parameterStyle=ParameterStyle.BARE)
  public void helloString4(@WebParam(targetNamespace="http://server.webparam1.webparam.jws.tests.ts.sun.com/", 
      partName="string4", name="name4")
    String string4, @WebParam(targetNamespace="http://server.webparam1.webparam.jws.tests.ts.sun.com/", 
      mode=Mode.OUT, partName="Employee", name="Employee")
    Holder<Employee> Employee)
    throws NameException_Exception;

  @WebMethod(action="urn:HelloString5")
  @Action(input="urn:HelloString5", output="http://server.webparam1.webparam.jws.tests.ts.sun.com/webParamWebService/helloString5/Response")
  @SOAPBinding(parameterStyle=ParameterStyle.BARE)
  @WebResult(targetNamespace="http://server.webparam1.webparam.jws.tests.ts.sun.com/", 
    partName="helloString5Response", name="helloString5Response")
  public String helloString5(@WebParam(targetNamespace="http://server.webparam1.webparam.jws.tests.ts.sun.com/", 
      partName="helloString5", name="helloString5")
    String helloString5);

  @WebMethod(action="urn:HelloString6")
  @Action(input="urn:HelloString6", output="http://server.webparam1.webparam.jws.tests.ts.sun.com/webParamWebService/helloString6/Response")
  @SOAPBinding(parameterStyle=ParameterStyle.BARE)
  @WebResult(targetNamespace="http://server.webparam1.webparam.jws.tests.ts.sun.com/", 
    partName="helloString6Response", name="helloString6Response")
  public String helloString6(@WebParam(targetNamespace="http://server.webparam1.webparam.jws.tests.ts.sun.com/", 
      partName="helloString6", name="helloString6")
    int helloString6, @WebParam(targetNamespace="http://server.webparam1.webparam.jws.tests.ts.sun.com/", 
      header=true, partName="name6", name="name6")
    String name6);

  @WebMethod(action="urn:HelloString7")
  @Action(input="urn:HelloString7", fault =
      { @FaultAction(value="http://server.webparam1.webparam.jws.tests.ts.sun.com/webParamWebService/helloString7/Fault/NameException", 
          className = NameException_Exception.class)
        } , output="http://server.webparam1.webparam.jws.tests.ts.sun.com/webParamWebService/helloString7/Response")
  @SOAPBinding(parameterStyle=ParameterStyle.BARE)
  public void helloString7(@WebParam(targetNamespace="http://server.webparam1.webparam.jws.tests.ts.sun.com/", 
      header=true, partName="string7", name="name7")
    String string7, @WebParam(targetNamespace="http://server.webparam1.webparam.jws.tests.ts.sun.com/", 
      partName="string8", name="name8")
    Name string8, @WebParam(targetNamespace="http://server.webparam1.webparam.jws.tests.ts.sun.com/", 
      mode=Mode.OUT, partName="MyEmployee", name="MyEmployee")
    Holder<Employee> MyEmployee)
    throws NameException_Exception;

  @WebMethod(action="urn:HelloString8")
  @ResponseWrapper(targetNamespace="http://server.webparam1.webparam.jws.tests.ts.sun.com/", 
    className="oracle.j2ee.ws.jaxws.cts.jws_webparam1.HelloString8Response", 
    localName="helloString8Response")
  @RequestWrapper(targetNamespace="http://server.webparam1.webparam.jws.tests.ts.sun.com/", 
    className="oracle.j2ee.ws.jaxws.cts.jws_webparam1.HelloString8", 
    localName="helloString8")
  @Action(input="urn:HelloString8", output="http://server.webparam1.webparam.jws.tests.ts.sun.com/webParamWebService/helloString8/Response")
  @WebResult(targetNamespace="")
  public String helloString8(@WebParam(targetNamespace="", name="string8")
    String string8, @WebParam(targetNamespace="", name="arg1")
    Address arg1);
}
