/**
 * $Id: SOAPRuntimeModel.java,v 1.5 2005-07-12 23:32:52 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.model.soap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.ws.soap.SOAPFaultException;

import com.sun.xml.bind.api.TypeReference;
import com.sun.xml.messaging.saaj.soap.SOAPVersionMismatchException;
import com.sun.xml.ws.encoding.jaxb.JAXBBridgeInfo;
import com.sun.xml.ws.encoding.jaxb.RpcLitPayload;
import com.sun.xml.ws.encoding.soap.SOAPConstants;
import com.sun.xml.ws.encoding.soap.SOAPVersion;
import com.sun.xml.ws.encoding.soap.internal.BodyBlock;
import com.sun.xml.ws.encoding.soap.internal.HeaderBlock;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;
import com.sun.xml.ws.encoding.soap.message.*;
import com.sun.xml.ws.model.CheckedException;
import com.sun.xml.ws.model.JavaMethod;
import com.sun.xml.ws.model.Parameter;
import com.sun.xml.ws.model.RuntimeModel;
import com.sun.xml.ws.model.WrapperParameter;
import com.sun.xml.ws.server.ServerRtException;

/**
 * Creates SOAP specific RuntimeModel
 *
 * @author Vivek Pandey
 */
public class SOAPRuntimeModel extends RuntimeModel {

    protected void createDecoderInfo() {
        Collection<JavaMethod> methods = getJavaMethods();
        for (JavaMethod m : methods) {
            if(m.isAsync())
                continue;
            List<Parameter> params = new ArrayList<Parameter>();
            params.addAll(m.getRequestParameters());
            params.addAll(m.getResponseParameters());
            SOAPBinding binding = (SOAPBinding) m.getBinding();
            for (Parameter param : params) {
                SOAPBlock paramBinding = (SOAPBlock) param.getBinding();
                if (paramBinding.isBody() && binding.isRpcLit()) {
                    RpcLitPayload payload = new RpcLitPayload(param.getName());
                    WrapperParameter wp = (WrapperParameter) param;
                    List<Parameter> wc = wp.getWrapperChildren();
                    for (Parameter p : wc) {
                        JAXBBridgeInfo bi = new JAXBBridgeInfo(getBridge(p.getTypeReference()),
                            null);
                        payload.addParameter(bi);
                    }
                    addDecoderInfo(param.getName(), payload);
                } else {
                    JAXBBridgeInfo bi = new JAXBBridgeInfo(getBridge(param.getTypeReference()),
                        null);
                    addDecoderInfo(param.getName(), bi);
                }
            }
            for(CheckedException ce:m.getCheckedExceptions()){
                JAXBBridgeInfo bi = new JAXBBridgeInfo(getBridge(ce.getDetailType()));
                addDecoderInfo(ce.getDetailType().tagName, bi);
            }
        }

    }
    

    /* 
     * @see com.sun.xml.rpc.rt.RuntimeModel#populateMaps()
     */
    @Override
    protected void populateMaps() {
        int emptyBodyCount = 0;
        for(JavaMethod jm:getJavaMethods()){
            put(jm.getMethod(), jm);
            boolean bodyFound = false;
            for(Parameter p:jm.getRequestParameters()){
                SOAPBlock binding = (SOAPBlock)p.getBinding();
                if(binding.equals(SOAPBlock.BODY)){
                    put(p.getName(), jm);
                    bodyFound = true;
                }
            }            
            if(!bodyFound){
                put(emptyBodyName, jm);
//                System.out.println("added empty body for: "+jm.getMethod().getName());
                emptyBodyCount++;
            }
        }
        if(emptyBodyCount > 1){
            //TODO throw exception
//            System.out.println("Error: Unqiue signature violation - more than 1 empty body!");
        }
    }


    /* 
     * @see com.sun.xml.rpc.rt.RuntimeModel#fillTypes(com.sun.xml.rpc.rt.model.JavaMethod, java.util.List)
     */
    @Override
    protected void fillTypes(JavaMethod m, List<TypeReference> types) {
        if(!(m.getBinding() instanceof SOAPBinding)){
            //TODO throws exception
            System.out.println("Error: Wrong Binding!");
            return;
        }
        if(((SOAPBinding)m.getBinding()).isDocLit()){
            super.fillTypes(m, types);
            return;
        }
        
        //else is rpclit
        addTypes(m.getRequestParameters(), types);
        addTypes(m.getResponseParameters(), types);
    }
        
    /**
     * @param params
     * @param types
     */
    private void addTypes(List<Parameter> params, List<TypeReference> types) {
        for(Parameter p:params){
            if(!p.isWrapperStyle()){
                types.add(p.getTypeReference());
            }else if(((SOAPBlock)p.getBinding()).isBody()){
                List<Parameter> wcParams = ((WrapperParameter)p).getWrapperChildren();
                for(Parameter wc:wcParams){
                    types.add(wc.getTypeReference());
                }
            }
        }
    }


    public Set<QName> getKnownHeaders() {
        Set<QName> headers = new HashSet<QName>();
        Iterator<JavaMethod> methods = getJavaMethods().iterator();
        while (methods.hasNext()) {
            JavaMethod method = methods.next();
            // fill in request headers
            Iterator<Parameter> params = method.getRequestParameters().iterator();
            fillHeaders(params, headers);

            // fill in response headers
            params = method.getResponseParameters().iterator();
            fillHeaders(params, headers);
        }
        return headers;
    }

    /**
     * @param params
     * @param headers
     */
    private void fillHeaders(Iterator<Parameter> params, Set<QName> headers) {
        while (params.hasNext()) {
            Parameter param = params.next();
            SOAPBlock binding = (SOAPBlock) param.getBinding();
            QName name = param.getName();
            if (binding.isHeader() && !headers.contains(name)) {
                headers.add(name);
            }
        }
    }
    
    /**
     * Called by server  
     * 
     * @param obj
     * @param actor
     * @param detail
     * @param internalMsg
     * @return
     */
    public static InternalMessage createFaultInBody(Object obj, String actor,
            Object detail, InternalMessage internalMsg) {
        SOAPFaultInfo faultInfo;
        if (obj instanceof SOAPFaultInfo) {
            faultInfo = (SOAPFaultInfo)obj;
        } else if (obj instanceof ServerRtException) {
            Throwable cause = ((ServerRtException)obj).getLinkedException();
            Throwable th = (cause == null) ? (ServerRtException)obj : cause;
            faultInfo = createSOAPFaultInfo(th, actor, detail);

        } else if (obj instanceof SOAPFaultException) {
            SOAPFaultException e = (SOAPFaultException)obj;
            faultInfo = new SOAPFaultInfo(e.getFaultCode(), e.getFaultString(),
                    e.getFaultActor(), e.getDetail());
        } else if (obj instanceof SOAPVersionMismatchException) {
            QName faultCode = SOAPConstants.FAULT_CODE_VERSION_MISMATCH;
            String faultString = "SOAP envelope version mismatch";
            faultInfo = new SOAPFaultInfo(faultCode, faultString, actor);
        } else if (obj instanceof Exception) {
            faultInfo = createSOAPFaultInfo((Exception)obj, actor, detail);
        } else {
            QName faultCode = SOAPConstants.FAULT_CODE_SERVER;
            String faultString = "Unknown fault type:"+obj.getClass();
            faultInfo = new SOAPFaultInfo(faultCode, faultString, actor, null);
        }
        if (internalMsg == null) {
            internalMsg = new InternalMessage();
        }
        BodyBlock bodyBlock = internalMsg.getBody();
        if (bodyBlock == null) {
            bodyBlock = new BodyBlock(faultInfo);
            internalMsg.setBody(bodyBlock);
        } else {
            bodyBlock.setFaultInfo(faultInfo);
        }

        return internalMsg;
    }

    public static InternalMessage createSOAP12FaultInBody(Object obj, String role, String node, List detail, InternalMessage im) {
        SOAP12FaultInfo faultInfo;
        if (obj instanceof SOAP12FaultInfo) {
            faultInfo = (SOAP12FaultInfo)obj;
        } else if (obj instanceof ServerRtException) {
            Throwable cause = ((ServerRtException)obj).getLinkedException();
            Throwable th = (cause == null) ? (ServerRtException)obj : cause;
            faultInfo = createSOAP12FaultInfo(th, role, node, detail);

        } else if (obj instanceof SOAP12FaultException) {
            SOAP12FaultException e = (SOAP12FaultException)obj;
            faultInfo = new SOAP12FaultInfo(e.getCode(), e.getReason(),
                    e.getNode(), e.getRole(), e.getDetail());
        } else if (obj instanceof SOAPVersionMismatchException) {
            String faultString = "SOAP envelope version mismatch";
            FaultCode code = new FaultCode(FaultCodeEnum.VersionMismatch, null);
            FaultReason reason = new FaultReason(new FaultReasonText("en", faultString));
            faultInfo = new SOAP12FaultInfo(code, reason, null, null, null);
        } else if (obj instanceof Exception) {
            faultInfo = createSOAP12FaultInfo((Exception)obj, role, node, detail);
        } else {
            String faultString = "Unknown fault type:"+obj.getClass();
            FaultCode code = new FaultCode(FaultCodeEnum.Receiver, null);
            FaultReason reason = new FaultReason(new FaultReasonText("en", faultString));
            faultInfo = new SOAP12FaultInfo(code, reason, null, null, null);
        }
        if (im == null) {
            im = new InternalMessage();
        }
        BodyBlock bodyBlock = im.getBody();
        if (bodyBlock == null) {
            bodyBlock = new BodyBlock(faultInfo);
            im.setBody(bodyBlock);
        } else {
            bodyBlock.setValue(faultInfo);
        }
        return im;
    }

    private static SOAP12FaultInfo createSOAP12FaultInfo(Throwable e, String role, String node, List detail) {
        SOAP12FaultException soapFaultException = null;
        FaultCode code = null;
        FaultReason reason = null;
        String faultRole = null;
        String faultNode = null;
        Throwable cause = e.getCause();
        if (e instanceof SOAP12FaultException) {
            soapFaultException = (SOAP12FaultException)e;
        } else if (cause != null && cause instanceof SOAP12FaultException) {
            soapFaultException = (SOAP12FaultException)e.getCause();
        }
        if (soapFaultException != null) {
            code = soapFaultException.getCode();
            reason = soapFaultException.getReason();
            faultRole = soapFaultException.getRole();
            if(faultRole == null)
                faultRole = role;
            faultNode = soapFaultException.getNode();
            if(faultNode == null)
                faultNode = node;
        }

        if (code == null || ((code != null) && code.getValue() == null)) {
            code = new FaultCode(FaultCodeEnum.Receiver, null);
        }

        if(reason == null){
            String faultString = e.getMessage();
            if (faultString == null) {
                faultString = e.toString();
            }

            reason = new FaultReason(new FaultReasonText("en", faultString));
        }

        if ((detail == null || ((detail != null) && detail.isEmpty())) && soapFaultException != null) {
            detail = soapFaultException.getDetail();
        }

        return new SOAP12FaultInfo(code, reason, faultRole, faultNode, detail);
    }

    //TODO: createSOAP12HeaderFault()


    /**
     * @param obj
     * @param actor
     * @param detailBlock
     * @param internalMsg
     * @return
     */
    public static InternalMessage createHeaderFault(Object obj, String actor, JAXBBridgeInfo detailBlock, InternalMessage internalMsg){
        //its headerfault so, create body fault with no detail. detail object goes as a header block
        internalMsg = createFaultInBody(obj, actor, null, internalMsg);
        HeaderBlock hdrBlock = new HeaderBlock(detailBlock);
        internalMsg.addHeader(hdrBlock);
        return internalMsg;
    }
    
    /**
     * @param e
     * @param actor
     * @param detail
     * @return
     */
    private static SOAPFaultInfo createSOAPFaultInfo(Throwable e, String actor,
            Object detail) {
//        e.printStackTrace();
        SOAPFaultException soapFaultException = null;
        QName faultCode = null;
        String faultString = null;
        String faultActor = null;
        Throwable cause = e.getCause();
        if (e instanceof SOAPFaultException) {
            soapFaultException = (SOAPFaultException)e;
        } else if (cause != null && cause instanceof SOAPFaultException) {
            soapFaultException = (SOAPFaultException)e.getCause();
        }
        if (soapFaultException != null) {
            faultCode = soapFaultException.getFaultCode();
            faultString = soapFaultException.getFaultString();
            faultActor = soapFaultException.getFaultActor();
        }
        
        if (faultCode == null) {
            faultCode = SOAPConstants.FAULT_CODE_SERVER;
        }
        
        if (faultString == null) {
            faultString = e.getMessage();
            if (faultString == null) {
                faultString = e.toString(); 
            }
        }

        if (faultActor == null) {
            faultActor = actor;   
        }
        
        if (detail == null && soapFaultException != null) {
            detail = soapFaultException.getDetail();   
        }

        return new SOAPFaultInfo(faultCode, faultString, faultActor, detail);
    }

}
