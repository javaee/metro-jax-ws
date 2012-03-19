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

package com.sun.xml.ws.cts.jws_webparam1;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.Holder;

@WebService(targetNamespace="http://server.webparam1.webparam.jws.tests.ts.sun.com/")
@SOAPBinding(style=SOAPBinding.Style.DOCUMENT, use=SOAPBinding.Use.LITERAL, parameterStyle=SOAPBinding.ParameterStyle.BARE)
public class WebParamWebServiceImpl {
    
    @WebMethod(operationName="helloString", action="urn:HelloString")
    public String hello(@WebParam (name="string1") String name) {
      return "hello : Hello " + name + " to Web Service";
    }


    @WebMethod(operationName="helloString2", action="urn:HelloString2")
    public String hello2(@WebParam (name="name2", partName="string2", targetNamespace="helloString2/name") String name) {
      return "hello2 : Hello " + name + " to Web Service";
    }

    @WebMethod(operationName="helloString3", action="urn:HelloString3")
    public void hello3(@WebParam (name="name3", partName="string3", targetNamespace="helloString3/name", header=true) String name, @WebParam (name="Name", targetNamespace="helloString3/Name", mode=WebParam.Mode.INOUT) Holder<Name> name2) {
   
//      System.out.println(" Invoking hello3 ");
      
      Name newName = new Name();
     
      newName.setFirstName("jsr181"); 
      newName.setLastName("jsr109"); 

      name2.value = newName;

    }

    @WebMethod(operationName="helloString4", action="urn:HelloString4")
    public void hello4(@WebParam (name="name4", partName="string4") String name, @WebParam (name="Employee", mode=WebParam.Mode.OUT) Holder<com.sun.xml.ws.cts.jws_common.Employee> employee)       throws com.sun.xml.ws.cts.jws_common.NameException {
   
//      System.out.println(" Invoking hello4 ");
     
      com.sun.xml.ws.cts.jws_common.Name newName = new com.sun.xml.ws.cts.jws_common.Name();
      com.sun.xml.ws.cts.jws_common.Employee oldEmployee = new com.sun.xml.ws.cts.jws_common.Employee();
     
      newName.setFirstName("jsr181"); 
      newName.setLastName("jaxws"); 

      oldEmployee.setName(newName);

      employee.value = oldEmployee;

    }

   @WebMethod(operationName="helloString5", action="urn:HelloString5")
    public String hello5(String name) {
      return "hello5 : Hello " + name + " to Web Service";
    }

    
    @WebMethod(operationName="helloString6", action="urn:HelloString6")
    public String hello6(@WebParam() int age, @WebParam(name="name6", header=true) String name) {
      return "hello6 : Hello " + name + " "+  age + " to Web Service";
    }

    @WebMethod(operationName="helloString7", action="urn:HelloString7")
    public void hello7(@WebParam (name="name7", partName="string7", header=true) String name, @WebParam (name="name8", partName="string8") com.sun.xml.ws.cts.jws_common.Name name2, @WebParam (name="MyEmployee", mode=WebParam.Mode.OUT) Holder<com.sun.xml.ws.cts.jws_common.Employee> employee) throws com.sun.xml.ws.cts.jws_common.NameException {

//      System.out.println(" Invoking hello7 ");

      com.sun.xml.ws.cts.jws_common.Employee oldEmployee = new com.sun.xml.ws.cts.jws_common.Employee();
      oldEmployee.setName(name2);

      employee.value = oldEmployee;

    }

    @WebMethod(operationName="helloString8", action="urn:HelloString8")
    @SOAPBinding(style=SOAPBinding.Style.DOCUMENT, use=SOAPBinding.Use.LITERAL, parameterStyle=SOAPBinding
  .ParameterStyle.WRAPPED)
    public String hello8(@WebParam (name="string8") String name, Address address) {
      return "hello8 : " + address.getCity() + " to Web Service";
    }

  }