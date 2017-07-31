/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.sdo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceFeature;

import org.eclipse.persistence.sdo.helper.SDOHelperContext;
import org.eclipse.persistence.sdo.helper.SDOXSDHelper;

import junit.framework.TestCase;

import com.oracle.webservices.api.databinding.DatabindingModeFeature;
import com.oracle.webservices.api.databinding.ExternalMetadataFeature;
import com.oracle.webservices.api.databinding.JavaCallInfo;
import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.ComponentFeature;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.databinding.Databinding;
import com.sun.xml.ws.api.databinding.DatabindingConfig;
import com.sun.xml.ws.api.databinding.DatabindingFactory;
import com.sun.xml.ws.api.databinding.WSDLGenInfo;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.wsdl.WSDLModel;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.ClientTubeAssemblerContext;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.pipe.ContentType;
import com.sun.xml.ws.api.pipe.NextAction;
import com.sun.xml.ws.api.pipe.TransportTubeFactory;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractTubeImpl;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.api.server.SDDocumentSource;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.api.wsdl.parser.WSDLParserExtension;
import com.sun.xml.ws.api.wsdl.writer.WSDLGeneratorExtension;
import com.sun.xml.ws.binding.WebServiceFeatureList;
import com.sun.xml.ws.db.sdo.HelperContextResolver;
import com.sun.xml.ws.db.sdo.SchemaInfo;
import com.sun.xml.ws.util.ServiceFinder;
import com.sun.xml.ws.util.xml.XmlUtil;
import com.sun.xml.ws.wsdl.parser.RuntimeWSDLParser;

import commonj.sdo.helper.HelperContext;

/**
 * WsDatabindingTestBase
 * 
 * @author shih-chang.chen@oracle.com
 */
abstract public class SDODatabindingTestBase extends TestCase {
    static public DatabindingFactory factory  = DatabindingFactory.newInstance();
    
    static public <T> T createProxy(Class<T> proxySEI, Class<?> endpointClass, String db, boolean debug) throws Exception {
        DatabindingConfig srvConfig = new DatabindingConfig();
        srvConfig.setEndpointClass(endpointClass);
        DatabindingModeFeature dbf = new DatabindingModeFeature(db); 
        WebServiceFeatureList wsfeatures = new WebServiceFeatureList(endpointClass);
        WebServiceFeature[] f = { dbf };
//      config.setFeatures(wsfeatures.toArray());
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
        Databinding srvDb = (Databinding)factory.createRuntime(srvConfig);       
        InVmWSDLResolver result = new InVmWSDLResolver();
        WSDLGenInfo wsdlGenInfo = new WSDLGenInfo();
        wsdlGenInfo.setWsdlResolver(result);
//        wsdlGenInfo.setContainer(container);
        wsdlGenInfo.setExtensions(ServiceFinder.find(WSDLGeneratorExtension.class).toArray());
        wsdlGenInfo.setInlineSchemas(true);
        srvDb.generateWSDL(wsdlGenInfo);
        if (debug) result.print();
        WSDLModel wsdl = RuntimeWSDLParser.parse( 
                result.getWsdlSource(), result.getEntityResolver(), false, null, new WSDLParserExtension[0]);
        QName serviceName = wsdl.getFirstServiceName();
        WSDLPort wsdlPort = wsdl.getService(serviceName).getFirstPort();
//        ((AbstractSEIModelImpl)((DatabindingImpl)srvDb).getModel()).freeze((WSDLPortImpl)wsdlPort);

        cliConfig.setWsdlPort(wsdlPort);
        cliConfig.getMappingInfo().setServiceName(serviceName);
        Databinding cliDb = (Databinding)factory.createRuntime(cliConfig);               

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
//          JavaCallInfo cliCall = new JavaCallInfo();
//          cliCall.setMethod(method);
//          cliCall.setParameters(args);
            JavaCallInfo cliCall = cli.createJavaCallInfo(method, args);
//            Packet cliSoapReq = clientBridge.createRequestPacket(cliCall);
            Packet cliSoapReq = (Packet)cli.serializeRequest(cliCall);
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
//          packet.soapAction = fixQuotesAroundSoapAction(con.getRequestHeader("SOAPAction"));
            srv.decode(srvBi, cliCt.getContentType(), srvSoapReq);
//          Message srvSoapReq = tie.getMessageFactory().createMessage(srcReq, cliSoapReq.transportHeaders(), null);
//            EndpointCallBridge endpointBridge = srv.getEndpointBridge(srvSoapReq);
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
//          Packet srvSoapRes = srvSoapReq.createResponse(srvSoapResMsg);
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
//            cliCall = clientBridge.readResponse(cliSoapRes, cliCall);
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

    static public URL getResource(String str) throws Exception {
        return Thread.currentThread().getContextClassLoader().getResource("etc/"+str);
    } 
    
    static protected class SDOConfig {
        HelperContext context;
        HelperContextResolver resolver;
    }
    
    protected SDOConfig sdoConfig(Set<SchemaInfo> schemas, boolean server) {
        final SDOConfig config = new SDOConfig();
        config.context = server ?
            SDOHelperContext.getHelperContext("server"):
            SDOHelperContext.getHelperContext();//SDODatabindingContext.getLocalHelperContext();
            config.resolver = new HelperContextResolver() {
            public HelperContext getHelperContext(boolean isClient, QName serviceName, Map<String, Object> properties) {
                return config.context;
            }            
        };
        for (SchemaInfo xi : schemas) ((SDOXSDHelper) config.context.getXSDHelper()).define(xi.getSchemaSource(), null);        
        return config;
    }
    
    protected WebServiceFeature[] invmSetup(final URL wsdlURL, final Class sei, final Class seb, final QName serviceName, final QName portName) {
        DatabindingModeFeature dbmf = new DatabindingModeFeature("eclipselink.sdo");
        Class implementorClass = seb;
        boolean handlersSetInDD = false;
        Container container = Container.NONE; 
        Map<String, SDDocumentSource> docs = new HashMap<String, SDDocumentSource>();
        SDDocumentSource primaryWSDL = SDDocumentSource.create(wsdlURL);
        docs.put(wsdlURL.toString(), primaryWSDL);
        ExternalMetadataFeature exm = ExternalMetadataFeature.builder().setReader( new com.sun.xml.ws.model.ReflectAnnotationReader() {
            public <A extends Annotation> A getAnnotation(final Class<A> annType, final Class<?> cls) {
                if (WebService.class.equals(annType)) {
                    final WebService ws = cls.getAnnotation(WebService.class);
                    return (A)new javax.jws.WebService() {
                        public Class<? extends Annotation> annotationType() {
                            return WebService.class;
                        }
                        @Override
                        public String endpointInterface() {
                            return sei.getName();
                        }
                        @Override
                        public String name() {
                            return (ws != null)? ws.name() : null;
                        }
                        @Override
                        public String portName() {
                            return (ws != null)? ws.portName() : null;
                        }
                        @Override
                        public String serviceName() {
                            return (ws != null)? ws.serviceName() : null;
                        }
                        @Override
                        public String targetNamespace() {
                            return (ws != null)? ws.targetNamespace() : null;
                        }
                        @Override
                        public String wsdlLocation() {
                            return (ws != null)? ws.wsdlLocation() : null;
                        }                                
                    };
                }
                return cls.getAnnotation(annType);
            }
        }).build();
        BindingID bindingID = BindingID.parse(implementorClass);
        WSBinding binding = bindingID.createBinding(dbmf, exm);
        final WSEndpoint<?> endpoint = WSEndpoint.create(
                implementorClass, !handlersSetInDD,
                null,
                serviceName, portName, container, binding,
                primaryWSDL, docs.values(), XmlUtil.createEntityResolver(null), false
        );   
        ComponentFeature cf = new ComponentFeature( new com.sun.xml.ws.api.Component() {
            public <S> S getSPI(Class<S> spiType) {
                if (TransportTubeFactory.class.equals(spiType)) return (S) new TransportTubeFactory() {
                    public Tube doCreate( ClientTubeAssemblerContext context) {
                        return new InVmTransportTube(context.getCodec(), context.getBinding(), wsdlURL, endpoint);
                    }
                };
                return null;
            }
        });
        WebServiceFeature[] f = {dbmf, cf};
        return f;
    } 

    static public class InVmTransportTube extends AbstractTubeImpl {
        Codec     clientCodec;
        WSBinding clientBinding;
        WSEndpoint<?> endpoint;
        Codec     endpointCodec;
        
        InVmTransportTube(Codec codec, WSBinding b, URL wsdlURL, WSEndpoint<?> e) {
            clientCodec = codec;
            clientBinding = b;
            endpoint = e;
            endpointCodec = endpoint.createCodec();;
        }
        
        @Override
        public NextAction processRequest(Packet request) {
            ByteArrayOutputStream os1 = new ByteArrayOutputStream(); 
            ContentType ctReq = clientCodec.getStaticContentType(request);
            Packet response = null;    
            try {
                clientCodec.encode(request, os1);
                Packet requestOnServer = new Packet();
                endpointCodec.decode(new ByteArrayInputStream(os1.toByteArray()), ctReq.getContentType(), requestOnServer);
                Packet responseOnServer = endpoint.createPipeHead().process(requestOnServer, null, null);
                ByteArrayOutputStream os2 = new ByteArrayOutputStream(); 
                ContentType ctRes = endpointCodec.encode(responseOnServer, os2);
                response = new Packet();
                clientCodec.decode(new ByteArrayInputStream(os2.toByteArray()), ctRes.getContentType(), response);

            } catch (IOException e) {
                e.printStackTrace();
            }          
            NextAction na = new NextAction();
            na.returnWith(response);        
            return na;
        }
        @Override
        public NextAction processResponse(Packet response) { 
            System.out.println("InVmTransportTube.processResponse " + response);
            return null; 
        }
        @Override
        public NextAction processException(Throwable t) { return null; }
        @Override
        public void preDestroy() {}
        @Override
        public AbstractTubeImpl copy(TubeCloner cloner) { return null; }
    }
}

