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

package fromjava.soap12.nosei.server;

import javax.jws.*;

import javax.xml.ws.Holder;
import javax.xml.ws.*;
import javax.xml.soap.*;
import javax.xml.ws.soap.SOAPFaultException;
import javax.xml.namespace.QName;

@WebService(name="Echo", serviceName="EchoService", targetNamespace="http://echo.org/")
public class EchoImpl {
/*    @WebMethod(operationName="echoBar2", action="urn:echoBar")
    public stub_tie_gen.annotation.nosei.server.types.Bar echoBar2(
		@WebParam(name="bar", mode=WebParam.Mode.IN)stub_tie_gen.annotation.nosei.server.types.Bar param){
        return param;
    }
*/
  
    @WebMethod(operationName="echoBar", action="urn:echoBar")
    public Bar echoBar(@WebParam(name="bar", mode=WebParam.Mode.IN)Bar param) throws Exception1 {
        return param;
    }

    @WebMethod
    public String echoString(@WebParam String str) throws Exception1, Fault1, WSDLBarException, Fault2 {
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

    @WebMethod
    @Oneway
    public void oneway(String str, @WebParam(name="num2", header=true)float num) {
    }

    @WebMethod
    @Oneway
    public void oneway2(String str) {
    }
    
    
    @WebMethod
    public void voidTest() {
    }

    // overload tests
    @WebMethod
    public String overloadedOperation(String param) throws java.rmi.RemoteException {
        return param;
    }

// TODO we need the wrapperbean annotation 
//    @WebMethod(operationName="overloadedOperation2")
//    public String overloadedOperation(String param, String param2) throws java.rmi.RemoteException {
//        return param + param2;
//    }

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
    public Long echoInHeader(int age, @WebParam(name="num", header=true, targetNamespace="foo/bar")Long num, String str) {
        return num;
    }

    @WebMethod
    public Long echoIn2Header(int age, @WebParam(name="num", header=true, targetNamespace="foo/bar")Long num, 
					@WebParam(name="name", header=true)String name, String str) {
System.out.println("name: "+name);
        return num;
    }


//    @WebMethod
//    public String echoInOutHeader(int age, @WebParam(name="num", mode=WebParam.Mode.INOUT, header=true, targetNamespace="foo/bar")LongHolder num, String str) {
//        num.value = num.value*2;
//        return str+num.value;
//    }
    
    @WebMethod
    public String echoInOutHeader(int age, @WebParam(name="num", mode=WebParam.Mode.INOUT, header=true, targetNamespace="foo/bar")Holder<Long> num, String str) {
        num.value = num.value*2;
        return str+num.value;
    }

//    @WebMethod
//    public String echoOutHeader(int age, @WebParam(name="num", mode=WebParam.Mode.OUT, header=true, targetNamespace="foo/bar")LongHolder num, String str) {
//        num.value = new Long(age);;
//        return str+num.value;
//    }
    
    @WebMethod
    public String echoOutHeader(int age, @WebParam(name="num", mode=WebParam.Mode.OUT, header=true, targetNamespace="foo/bar")Holder<Long> num, String str) {
        num.value = new Long(age);;
        return str+num.value;
    }

//    @WebMethod
//    public String echoOut2Header(int age, @WebParam(name="num", mode=WebParam.Mode.OUT, header=true, targetNamespace="foo/bar")LongHolder num, 
//					   @WebParam(name="name", header=true, mode=WebParam.Mode.OUT)Holder<String> name, String str) {
//        num.value = new Long(age);;
//        name.value = "Fred";
//        return str+num.value;
//    }

    @WebMethod
    public String echoOut2Header(int age, @WebParam(name="num", mode=WebParam.Mode.OUT, header=true, targetNamespace="foo/bar")Holder<Long> num,
                       @WebParam(name="name", header=true, mode=WebParam.Mode.OUT)Holder<String> name, String str) {
        num.value = new Long(age);;
        name.value = "Fred";
        return str+num.value;
    }

    @WebMethod
    public String throwException(String throwWhat) {
        if(throwWhat.equals("SFE")){
            throw createSOAPFaultException();
        }
        return "Can't do.";
    }
    
    private SOAPFaultException createSOAPFaultException(){
        try {
            String namespace = "http://example.com/auctiontraq/schemas/doclit";
            SOAPFactory soapFactory = SOAPFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
            Name name = soapFactory.createName("MySOAPFault", "ns0",
                    namespace);
            Detail detail = soapFactory.createDetail();
            DetailEntry entry = detail.addDetailEntry(name);
            entry.addNamespaceDeclaration("data",namespace );
            Name attrName1 = soapFactory.createName("myAttr", "data",namespace);
            entry.addAttribute(attrName1, "myvalue");
            SOAPElement child = entry.addChildElement("message");
            child.addTextNode("Server Exception");
            
            Name name2 = soapFactory.createName("ExtraInformation", "ns0",
                    namespace);
            DetailEntry entry2 = detail.addDetailEntry(name2);
            
            SOAPElement child2 = entry2.addChildElement("Reason");
            child2.addTextNode("Address Not Found");
            
	    QName qname = new QName("http://www.w3.org/2003/05/soap-envelope", "Receiver");
            SOAPFault sf = soapFactory.createFault("SOAP Fault Exception:Address Not Found", qname);
            org.w3c.dom.Node n = sf.getOwnerDocument().importNode(detail, true);
            sf.appendChild(n);
            return new SOAPFaultException(sf); 
            //printDetail(detail);
           // return new SOAPFaultException(qname,
             //       "SOAP Fault Exception:Address Not Found", null, detail);
           
        } catch (SOAPException e) {
            e.printStackTrace();
            //QName qname = new QName("http://schemas.xmlsoap.org/soap/envelope/", "client");
            throw new WebServiceException("Exception While Creating SOAP Fault Exception",e);
        }
    }
}
