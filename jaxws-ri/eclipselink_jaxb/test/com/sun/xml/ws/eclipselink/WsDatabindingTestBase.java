package com.sun.xml.ws.eclipselink;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceFeature;

import junit.framework.TestCase;

import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.databinding.ClientCallBridge;
import com.sun.xml.ws.api.databinding.Databinding;
import com.sun.xml.ws.api.databinding.DatabindingConfig;
import com.sun.xml.ws.api.databinding.DatabindingFactory;
import com.sun.xml.ws.api.databinding.DatabindingModeFeature;
import com.sun.xml.ws.api.databinding.EndpointCallBridge;
import com.sun.xml.ws.api.databinding.JavaCallInfo;
import com.sun.xml.ws.api.databinding.WSDLGenInfo;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.ContentType;
import com.sun.xml.ws.api.wsdl.parser.WSDLParserExtension;
import com.sun.xml.ws.api.wsdl.writer.WSDLGeneratorExtension;
import com.sun.xml.ws.binding.WebServiceFeatureList;
import com.sun.xml.ws.db.DatabindingImpl;
import com.sun.xml.ws.model.AbstractSEIModelImpl;
import com.sun.xml.ws.model.wsdl.WSDLModelImpl;
import com.sun.xml.ws.model.wsdl.WSDLPortImpl;
import com.sun.xml.ws.util.ServiceFinder;
import com.sun.xml.ws.wsdl.parser.RuntimeWSDLParser;

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
		Databinding srvDb = factory.createRuntime(srvConfig);		
		InVmWSDLResolver result = new InVmWSDLResolver();
        WSDLGenInfo wsdlGenInfo = new WSDLGenInfo();
        wsdlGenInfo.setWsdlResolver(result);
//        wsdlGenInfo.setContainer(container);
        wsdlGenInfo.setExtensions(ServiceFinder.find(WSDLGeneratorExtension.class).toArray());
        wsdlGenInfo.setInlineSchemas(false);
        srvDb.generateWSDL(wsdlGenInfo);
        if (debug) result.print();
        WSDLModelImpl wsdl = RuntimeWSDLParser.parse( 
        		result.getWsdlSource(), result.getEntityResolver(), false, null, new WSDLParserExtension[0]);
        QName serviceName = wsdl.getFirstServiceName();
        WSDLPort wsdlPort = wsdl.getService(serviceName).getFirstPort();
        ((AbstractSEIModelImpl)((DatabindingImpl)srvDb).getModel()).freeze((WSDLPortImpl)wsdlPort);

		cliConfig.setWsdlPort(wsdlPort);
		cliConfig.getMappingInfo().setServiceName(serviceName);
		Databinding cliDb = factory.createRuntime(cliConfig);		        

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
			JavaCallInfo cliCall = new JavaCallInfo();
			cliCall.setMethod(method);
			cliCall.setParameters(args);
			ClientCallBridge clientBridge = cli.getClientBridge(method);
			Packet cliSoapReq = clientBridge.createRequestPacket(cliCall);
			//Transmit to Server
			ByteArrayOutputStream cliBo = new ByteArrayOutputStream();
			ContentType cliCt = cli.encode(cliSoapReq, cliBo);
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
			Packet srvSoapReq = new Packet();
//	        packet.soapAction = fixQuotesAroundSoapAction(con.getRequestHeader("SOAPAction"));
			srv.decode(srvBi, cliCt.getContentType(), srvSoapReq);
//			Message srvSoapReq = tie.getMessageFactory().createMessage(srcReq, cliSoapReq.transportHeaders(), null);
			EndpointCallBridge endpointBridge = srv.getEndpointBridge(srvSoapReq);
			JavaCallInfo srcCall = endpointBridge.deserializeRequest(srvSoapReq);			
			Method intfMethod = srcCall.getMethod();
			Method implMethod = serviceBeanType.getMethod(intfMethod.getName(), intfMethod.getParameterTypes());
			try {
				Object ret = implMethod.invoke(serviceBeanInstance, srcCall.getParameters());
				srcCall.setReturnValue(ret);
			} catch (Exception e) {
				srcCall.setException(e);
			}
			Message srvSoapResMsg = endpointBridge.serializeResponse(srcCall);
			Packet srvSoapRes = srvSoapReq.createResponse(srvSoapResMsg);
			//Transmit to Client
			ByteArrayOutputStream srvBo = new ByteArrayOutputStream();
			ContentType srvCt = srv.encode(srvSoapRes, srvBo);
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
			Packet cliSoapRes = new Packet();
			cli.decode(cliBi, srvCt.getContentType(), cliSoapRes);
			cliCall = clientBridge.readResponse(cliSoapRes, cliCall);
			if (cliCall.getException() != null) {
				throw cliCall.getException(); 
			}
			return cliCall.getReturnValue();
		}		
	}
}
