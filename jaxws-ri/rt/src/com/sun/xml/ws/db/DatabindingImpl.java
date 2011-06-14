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

package com.sun.xml.ws.db;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceFeature;

import org.jvnet.ws.message.MessageContext;

import com.sun.xml.ws.api.databinding.EndpointCallBridge;
import com.sun.xml.ws.api.databinding.JavaCallInfo;
import com.sun.xml.ws.api.databinding.WSDLGenInfo;
import com.sun.xml.ws.api.databinding.Databinding;
import com.sun.xml.ws.api.databinding.DatabindingConfig;
import com.sun.xml.ws.api.databinding.ClientCallBridge;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.MEP;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.pipe.ContentType;
import com.sun.xml.ws.binding.BindingImpl;
import com.sun.xml.ws.client.sei.StubAsyncHandler;
import com.sun.xml.ws.client.sei.StubHandler;
import com.sun.xml.ws.model.AbstractSEIModelImpl;
import com.sun.xml.ws.model.JavaMethodImpl;
import com.sun.xml.ws.model.RuntimeModeler;
import com.sun.xml.ws.server.sei.TieHandler;
import com.sun.xml.ws.util.QNameMap;
import com.sun.xml.ws.wsdl.ActionBasedOperationSignature;
import com.sun.xml.ws.wsdl.DispatchException;
import com.sun.xml.ws.wsdl.OperationDispatcher;

/**
 * WsRuntimeImpl is the databinding processor built on SEIModel
 *
 * @author shih-chang.chen@oracle.com
 */
public class DatabindingImpl implements Databinding, org.jvnet.ws.databinding.Databinding {
	
    AbstractSEIModelImpl seiModel;
	Map<Method, StubHandler> stubHandlers;
    QNameMap<TieHandler> wsdlOpMap = new QNameMap<TieHandler>();
	Map<Method, TieHandler> tieHandlers = new HashMap<Method, TieHandler>();
    OperationDispatcher operationDispatcher;
    OperationDispatcher operationDispatcherNoWsdl;
    boolean clientConfig = false;
    Codec codec;
    
	public DatabindingImpl(DatabindingProviderImpl p, DatabindingConfig config) {
		RuntimeModeler modeler = new RuntimeModeler(config);
		modeler.setClassLoader(config.getClassLoader());
		seiModel = modeler.buildRuntimeModel();
		WSDLPort wsdlport = config.getWsdlPort();
		clientConfig = isClientConfig(config);
		if ( clientConfig ) initStubHandlers();
		seiModel.setDatabinding(this);
		if (wsdlport != null) freeze(wsdlport);
		if (operationDispatcher == null) operationDispatcherNoWsdl = new OperationDispatcher(null, seiModel.getWSBinding(), seiModel);
//    if(!clientConfig) {
		for(JavaMethodImpl jm: seiModel.getJavaMethods()) if (!jm.isAsync()) {
            TieHandler th = new TieHandler(jm, seiModel.getWSBinding());
            wsdlOpMap.put(jm.getOperationQName(), th);
            tieHandlers.put(th.getMethod(), th);
        }
//    }
	}
	
	//TODO isClientConfig
	private boolean isClientConfig(DatabindingConfig config) {
		if (config.getContractClass() == null) return false;
		if (!config.getContractClass().isInterface()) return false;
		return (config.getEndpointClass() == null || config.getEndpointClass().isInterface());
	}
	//TODO fix freeze
	public synchronized void freeze(WSDLPort port) {
		if (clientConfig) return;
		if (operationDispatcher != null) return;
		operationDispatcher = (port == null) ? null : new OperationDispatcher(port, seiModel.getWSBinding(), seiModel);
	}
	
	public SEIModel getModel() {
		return seiModel;
	}
//Refactored from SEIStub
    private void initStubHandlers() {
		stubHandlers = new HashMap<Method, StubHandler>();
        Map<ActionBasedOperationSignature, JavaMethodImpl> syncs = new HashMap<ActionBasedOperationSignature, JavaMethodImpl>();
        // fill in methodHandlers.
        // first fill in sychronized versions
        for (JavaMethodImpl m : seiModel.getJavaMethods()) {
            if (!m.getMEP().isAsync) {
            	StubHandler handler = new StubHandler(m);
                syncs.put(m.getOperationSignature(), m);
                stubHandlers.put(m.getMethod(), handler);
            }
        }
        for (JavaMethodImpl jm : seiModel.getJavaMethods()) {
            JavaMethodImpl sync = syncs.get(jm.getOperationSignature());
            if (jm.getMEP() == MEP.ASYNC_CALLBACK || jm.getMEP() == MEP.ASYNC_POLL) {
                Method m = jm.getMethod();
                StubAsyncHandler handler = new StubAsyncHandler(jm, sync);
                stubHandlers.put(m, handler);
            }
        }
    }

    public QName resolveOperationQName(Packet req) throws DispatchException {
        return (operationDispatcher != null)?
                operationDispatcher.getWSDLOperationQName(req):
                operationDispatcherNoWsdl.getWSDLOperationQName(req);
    }

	public JavaCallInfo deserializeRequest(Packet req) {
		JavaCallInfo call = new JavaCallInfo();
		try {
			QName wsdlOp = resolveOperationQName(req);
			TieHandler tie = wsdlOpMap.get(wsdlOp);
			call.setMethod(tie.getMethod());
			Object[] args = tie.readRequest(req.getMessage());
			call.setParameters(args);
		} catch (DispatchException e) {
			call.setException(e);
		}
		return call;
	}

	public JavaCallInfo deserializeResponse(Packet res, JavaCallInfo call) {
        StubHandler stubHandler = stubHandlers.get(call.getMethod());
        try {
            return stubHandler.readResponse(res, call);
        } catch (Throwable e) {
            call.setException(e);
            return call;
        }
	}

	public WebServiceFeature[] getFeatures() {
		// TODO Auto-generated method stub
		return null;
	}

	public Packet serializeRequest(JavaCallInfo call) {
        StubHandler stubHandler = stubHandlers.get(call.getMethod());
        return stubHandler.createRequestPacket(call);	    
	}

	public Packet serializeResponse(JavaCallInfo call) {
		Method method = call.getMethod();
		Message message = null;
		if (method != null) {
			TieHandler th = tieHandlers.get(method);
			if (th != null) {
			    return th.serializeResponse(call);
			}
		} 
		if (call.getException() instanceof DispatchException) {
		    message = ((DispatchException)call.getException()).fault;
		}
        Packet response = new Packet();
        response.setMessage(message);
        return response;
	}

	public ClientCallBridge getClientBridge(Method method) {
		return stubHandlers.get(method);
	}
	
	
	public void generateWSDL(WSDLGenInfo info) {        
	    com.sun.xml.ws.wsdl.writer.WSDLGenerator wsdlGen = new com.sun.xml.ws.wsdl.writer.WSDLGenerator(
		    seiModel, 
		    info.getWsdlResolver(), 
		    seiModel.getWSBinding(), 
		    info.getContainer(), seiModel.getEndpointClass(), 
		    info.isInlineSchemas(),
		    info.getExtensions());
        wsdlGen.doGeneration();		
	}
	
	public EndpointCallBridge getEndpointBridge(Packet req) throws DispatchException {
		QName wsdlOp = resolveOperationQName(req);
		return wsdlOpMap.get(wsdlOp);
	}

	
	Codec getCodec() {
		if (codec == null) codec = ((BindingImpl)seiModel.getWSBinding()).createCodec();
		return codec;
	}

	public ContentType encode( Packet packet, OutputStream out ) throws IOException {
    	return getCodec().encode(packet, out);    	
    }

	public void decode( InputStream in, String ct, Packet p ) throws IOException{
    	getCodec().decode(in, ct, p);    	
    }

    public org.jvnet.ws.databinding.JavaCallInfo createJavaCallInfo(Method method, Object[] args) {
        return new JavaCallInfo(method, args);
    }

    public MessageContext serializeRequest(org.jvnet.ws.databinding.JavaCallInfo call) {
        return serializeRequest((JavaCallInfo)call);
    }

    public org.jvnet.ws.databinding.JavaCallInfo deserializeResponse(
            MessageContext message, org.jvnet.ws.databinding.JavaCallInfo call) {
        return deserializeResponse((Packet)message, (JavaCallInfo)call);
    }

    public org.jvnet.ws.databinding.JavaCallInfo deserializeRequest(MessageContext message) {
        return deserializeRequest((Packet)message);
    }

    public MessageContext serializeResponse(org.jvnet.ws.databinding.JavaCallInfo call) {
        return serializeResponse((JavaCallInfo)call);
    }
}
