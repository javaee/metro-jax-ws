/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Oracle and/or its affiliates. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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
package fromwsdl.soap12.fault.server;

import javax.xml.ws.ProtocolException;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.BindingType;
import javax.xml.ws.soap.SOAPFaultException;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.dom.DOMSource;
import java.io.ByteArrayOutputStream;
import java.util.Properties;

/**
 * @author Vivek Pandey
 */
@javax.jws.WebService(endpointInterface="fromwsdl.soap12.fault.server.Fault")
@BindingType(SOAPBinding.SOAP12HTTP_BINDING)
public class FaultImpl
        implements Fault {
    public java.lang.String echo(java.lang.String type)
            throws
            Fault1Exception,
            Fault2Exception,
            Fault3Exception,
            Fault4Exception {
        if (type.equals("Fault1")) {
            FooException fault = new FooException();
            fault.setVarInt(1);
            fault.setVarString("1");
            fault.setVarFloat(1);
            System.out.println("Throwing Fault1Exception");
            throw new Fault1Exception("Fault1 message", fault);
        } else if (type.equals("Fault1-SOAPFaultException")) {
            FooException fault = new FooException();
            fault.setVarInt(1);
            fault.setVarString("1");
            fault.setVarFloat(1);
            System.out.println("Throwing Fault1Exception with Cause");
            throw new Fault1Exception("Fault1 message", fault, createSOAPFaultException(null, "Fault1 message"));
        }else if (type.equals("Fault2")) {
            String fault = "fault2";
            System.out.println("Throwing Fault2Exception");
            throw new Fault2Exception("Fault2 message", fault);
        } else if (type.equals("Fault3")) {
            Integer fault = new Integer("1");
            System.out.println("Throwing Fault3Exception");
            throw new Fault3Exception("Fault3 message", fault);
        } else if (type.equals("Fault4")) {
            Fault4 fault = new Fault4();
            fault.setMessage("fault4");
            fault.setCount(1);
            System.out.println("Throwing Fault4Exception");
            throw new Fault4Exception("Fault4 message", fault);
        } else if (type.equals("SOAPFaultException")) {
                throw createSOAPFaultException(null, "SOAPFaultException");
        } else if (type.equals("NullPointerException")) {
            Object o = null;
            o.hashCode();
        } else if (type.equals("ProtocolException")) {
            throw new ProtocolException();
        } else if (type.equals("ProtocolException2")) {
            throw new ProtocolException("FaultImpl");
//        } else if (type.equals("RemoteExceptionWithSFECause")) {
//            throw new RemoteException("RemoteExceptionWithSFECause", createSOAP12FaultException(null,
//                    "SOAP12FaultException Test"));
//        } else if (type.equals("RemoteExceptionWithSFECause2")) {
//            SOAP12FaultException soapFault = createSOAP12FaultException(new QName("SoapFault"),
//                    "Endpoint encountered an error");
//            throw new RemoteException("RemoteExceptionWithSFECause2", soapFault);
        } else if (type.equals("echo")) {
            return "echo"; // used in MU test
        }
        return "Unknown fault: " + type;
    }

//    private SOAP12FaultException createSOAP12FaultException(QName subcode, String msg) {
//        FaultSubcode fsc = null;
//        if(subcode != null)
//            fsc = new FaultSubcode(subcode, null);
//        // passing null as the exception message
//        return new SOAP12FaultException(new FaultCode(FaultCodeEnum.Sender, fsc),
//                new FaultReason(new FaultReasonText("en", msg)), null, null, null);
//    }


       private SOAPFaultException createSOAPFaultException(QName subcode, String msg){
        try {
            String namespace = "http://faultservice.org/wsdl";
            SOAPFactory soapFactory = SOAPFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
            Name name = soapFactory.createName("BasicFault", "ns0",
                    namespace);
            Detail detail = soapFactory.createDetail();
            DetailEntry entry = detail.addDetailEntry(name);
            entry.addNamespaceDeclaration("ns0", namespace);
            entry.setEncodingStyle("http://schemas.xmlsoap.org/soap/encoding/");
            entry.addNamespaceDeclaration("myenv",
                    "http://schemas.xmlsoap.org/soap/envelope/");
            entry.addNamespaceDeclaration("myns", "http://myurri/tmp");
            Name attrName = soapFactory.createName("encodingStyle", "myenv",
                    "http://schemas.xmlsoap.org/soap/envelope/");
            entry.addAttribute(attrName,
                    "http://schemas.xmlsoap.org/soap/encoding/");
            Name attrName2 = soapFactory.createName("myAttr", "myns",
                    "http://myurri/tmp");
            entry.addAttribute(attrName2, "myvalue");
            SOAPElement child = entry.addChildElement("message");
            child.addTextNode("basic fault");

            Name name2 = soapFactory.createName("AdditionalElement", "ns0",
                    namespace);
            DetailEntry entry2 = detail.addDetailEntry(name2);
            entry2.addNamespaceDeclaration("ns0", namespace);

            SOAPElement child2 = entry2.addChildElement("BOGUS");
            child2.addTextNode("2 text");

            QName qname = new QName("http://www.w3.org/2003/05/soap-envelope", "Sender");
            //printDetail(detail);
            SOAPFault sf = soapFactory.createFault(msg, qname);
            if(subcode != null)
                sf.appendFaultSubcode(subcode);
            //Node n = sf.addDetail().getOwnerDocument().importNode(detail, true);
            org.w3c.dom.Node n = sf.getOwnerDocument().importNode(detail, true);
//            printDetail((Detail)n);
            sf.appendChild(n);
            return new SOAPFaultException(sf);

//            return new SOAPFaultException(qname,
//                    "soap fault exception fault", null, detail);
        } catch (SOAPException e) {
            e.printStackTrace();
            QName qname = new QName("http://schemas.xmlsoap.org/soap/envelope/", "client");
            throw new WebServiceException("soap fault exception fault", e);
//            throw new SOAPFaultException(qname,
//                    "soap fault exception fault", null, null);
        }
    }

    public void printDetail(Detail detail) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            StreamResult sr = new StreamResult(bos );
            Transformer trans = TransformerFactory.newInstance().newTransformer();
            Properties oprops = new Properties();
            oprops.put(OutputKeys.OMIT_XML_DECLARATION, "yes");
            trans.setOutputProperties(oprops);
            trans.transform(new DOMSource(detail), sr);
            System.out.println("**** bos ******"+bos.toString());
            bos.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}
