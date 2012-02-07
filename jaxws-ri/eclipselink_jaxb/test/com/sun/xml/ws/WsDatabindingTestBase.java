/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.xml.ws;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceFeature;

import junit.framework.TestCase;

import org.jvnet.ws.databinding.DatabindingModeFeature;
import org.jvnet.ws.databinding.JavaCallInfo;
import org.jvnet.ws.message.ContentType;

import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.databinding.Databinding;
import com.sun.xml.ws.api.databinding.DatabindingConfig;
import com.sun.xml.ws.api.databinding.DatabindingFactory;
import com.sun.xml.ws.api.databinding.WSDLGenInfo;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
//import com.sun.xml.ws.api.pipe.ContentType;
import com.sun.xml.ws.api.wsdl.parser.WSDLParserExtension;
import com.sun.xml.ws.api.wsdl.writer.WSDLGeneratorExtension;
import com.sun.xml.ws.binding.WebServiceFeatureList;
import com.sun.xml.ws.model.wsdl.WSDLModelImpl;
import com.sun.xml.ws.util.ServiceFinder;
import com.sun.xml.ws.wsdl.parser.RuntimeWSDLParser;

/**
 * WsDatabindingTestBase
 * 
 * @author shih-chang.chen@oracle.com
 */
abstract public class WsDatabindingTestBase extends TestCase {
	static public DatabindingFactory factory  = DatabindingFactory.newInstance();
	
	static public <T> T createProxy(Class<T> proxySEI, Class<?> endpointClass, String db, boolean debug) throws Exception {
		DatabindingConfig srvConfig = new DatabindingConfig();
		srvConfig.setEndpointClass(endpointClass);
		DatabindingModeFeature dbf = new DatabindingModeFeature(db); 
        WebServiceFeatureList wsfeatures = new WebServiceFeatureList(endpointClass);
		WebServiceFeature[] f = { dbf };
//		config.setFeatures(wsfeatures.toArray());
		srvConfig.setFeatures(f);	

        DatabindingConfig cliConfig = new DatabindingConfig();
		cliConfig.setContractClass(proxySEI);
		cliConfig.setFeatures(f);		
		return createProxy(proxySEI, srvConfig, cliConfig, debug);
	}
	
    static public <T> T createProxy(Class<T> proxySEI, DatabindingConfig srvConfig, DatabindingConfig cliConfig, boolean debug) throws Exception {
    	Class<?> endpointClass = srvConfig.getEndpointClass();
    	//TODO default BindingID
    	if (srvConfig.getMappingInfo().getBindingID() == null)
		    srvConfig.getMappingInfo().setBindingID(BindingID.parse(endpointClass));
		Databinding srvDb = (Databinding) factory.createRuntime(srvConfig);		
		InVmWSDLResolver result = new InVmWSDLResolver();
        WSDLGenInfo wsdlGenInfo = new WSDLGenInfo();
        wsdlGenInfo.setWsdlResolver(result);
//        wsdlGenInfo.setContainer(container);
        wsdlGenInfo.setExtensions(ServiceFinder.find(WSDLGeneratorExtension.class).toArray());
        wsdlGenInfo.setInlineSchemas(true);
        srvDb.generateWSDL(wsdlGenInfo);
        if (debug) result.print();
        WSDLModelImpl wsdl = RuntimeWSDLParser.parse( 
        		result.getWsdlSource(), result.getEntityResolver(), false, null, new WSDLParserExtension[0]);
        QName serviceName = wsdl.getFirstServiceName();
        WSDLPort wsdlPort = wsdl.getService(serviceName).getFirstPort();
//        ((AbstractSEIModelImpl)((DatabindingImpl)srvDb).getModel()).freeze((WSDLPortImpl)wsdlPort);

		cliConfig.setWsdlPort(wsdlPort);
		cliConfig.getMappingInfo().setServiceName(serviceName);
		Databinding cliDb = (Databinding) factory.createRuntime(cliConfig);		        

		Class<?>[] intf = { proxySEI };
		WsDatabindingTestFacade h = new WsDatabindingTestFacade(cliDb, srvDb, endpointClass);
		h.wireLog = debug;
		Object proxy = Proxy.newProxyInstance(proxySEI.getClassLoader(), intf, h);
		return proxySEI.cast(proxy);
	}

	static public class WsDatabindingTestFacade implements InvocationHandler {
		Databinding cli;
		Databinding srv;
		Class<?> serviceBeanType;
		public Object serviceBeanInstance;
		public boolean wireLog = false;
		public boolean keepWireMessage = false;
		public boolean keepMessageObject = false;
		public String request;
		public String response;
		public Packet requestMessage;
		public Packet responseMessage;
		
		WsDatabindingTestFacade(Databinding client, Databinding server, Class endpoint) {
			cli = client;
			srv = server;
			serviceBeanType = endpoint;
			try {
				serviceBeanInstance = serviceBeanType.newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//			JavaCallInfo cliCall = new JavaCallInfo();
//			cliCall.setMethod(method);
//			cliCall.setParameters(args);
		    JavaCallInfo cliCall = cli.createJavaCallInfo(method, args);
//			ClientCallBridge clientBridge = cli.getClientBridge(method);
			Packet cliSoapReq = (Packet)cli.serializeRequest(cliCall);
			//Transmit to Server
			ByteArrayOutputStream cliBo = new ByteArrayOutputStream();
//			ContentType cliCt = cli.encode(cliSoapReq, cliBo);
			ContentType cliCt = cliSoapReq.writeTo(cliBo);
			if (wireLog) {
				System.out.println("Request Message: " + cliCt.getContentType() + " " + cliCt.getSOAPActionHeader() );
				System.out.println(new String(cliBo.toByteArray()));
			}
			if (keepMessageObject) {
			  requestMessage = cliSoapReq;
			}
			if (keepWireMessage) {
				request = new String(cliBo.toByteArray());
			}
			
			ByteArrayInputStream srvBi = new ByteArrayInputStream(cliBo.toByteArray());
			Packet srvSoapReq = (Packet)srv.getMessageContextFactory().createContext(srvBi, cliCt.getContentType());
//			Packet srvSoapReq = new Packet();
//	        packet.soapAction = fixQuotesAroundSoapAction(con.getRequestHeader("SOAPAction"));
//			srv.decode(srvBi, cliCt.getContentType(), srvSoapReq);
//			Message srvSoapReq = tie.getMessageFactory().createMessage(srcReq, cliSoapReq.transportHeaders(), null);
//			EndpointCallBridge endpointBridge = srv.getEndpointBridge(srvSoapReq);
			JavaCallInfo srcCall = srv.deserializeRequest(srvSoapReq);			
			Method intfMethod = srcCall.getMethod();
			Method implMethod = serviceBeanType.getMethod(intfMethod.getName(), intfMethod.getParameterTypes());
			try {
				Object ret = implMethod.invoke(serviceBeanInstance, srcCall.getParameters());
				srcCall.setReturnValue(ret);
			} catch (Exception e) {
				srcCall.setException(e);
			}
			Packet srvSoapRes = (Packet)srv.serializeResponse(srcCall);
//			Packet srvSoapRes = srvSoapReq.createResponse(srvSoapResMsg);
			//Transmit to Client
			ByteArrayOutputStream srvBo = new ByteArrayOutputStream();
			ContentType srvCt = srvSoapRes.writeTo(srvBo);
			if (wireLog) {
				System.out.println("Response Message: " + srvCt.getContentType());
				System.out.println(new String(srvBo.toByteArray()));
			}
			if (keepMessageObject) {
                responseMessage = srvSoapRes;
             }
			if (keepWireMessage) {
				response = new String(srvBo.toByteArray());
			}
			ByteArrayInputStream cliBi = new ByteArrayInputStream(srvBo.toByteArray());
//			Packet cliSoapRes = new Packet();
//			cli.decode(cliBi, srvCt.getContentType(), cliSoapRes);
			Packet cliSoapRes = (Packet)cli.getMessageContextFactory().createContext(cliBi, srvCt.getContentType());
			cliCall = cli.deserializeResponse(cliSoapRes, cliCall);
			if (cliCall.getException() != null) {
				throw cliCall.getException(); 
			}
			return cliCall.getReturnValue();
		}		
	}

    
    static public void assertEqualList(List<?> list1, List<?> list2) {
        assertTrue(list1.size() == list2.size());
        for (int i = 0; i < list1.size(); i++) {
            assertEquals(list1.get(i), list2.get(i));
        }        
    }

    static public void assertEqualCollection(Collection<?> c1, Collection<?> c2) {
        assertTrue(c1.size() == c2.size());
        for (Iterator i = c1.iterator(); i.hasNext();) {
            assertTrue(c2.contains(i.next()));
        }        
    }
    
    static public void assertEqualArray(Object a1, Object a2) {
        assertTrue(Array.getLength(a1) == Array.getLength(a2));
        for (int i = 0; i < Array.getLength(a1); i++) {
            assertEquals(Array.get(a1, i), Array.get(a2, i));
        }        
    }   

    static public boolean equalsMap(Map<?,?> req, Map<?,?>  res) {
      if (req.size() != res.size()) return false;
      for(Object k : req.keySet()) if(!req.get(k).equals(res.get(k))) return false;
      return true;
    }
}
