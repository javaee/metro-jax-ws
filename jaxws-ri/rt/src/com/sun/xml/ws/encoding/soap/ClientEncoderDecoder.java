/**
 * $Id: ClientEncoderDecoder.java,v 1.3 2005-06-01 00:51:33 jitu Exp $
 */
/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.encoding.soap;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;

import javax.xml.ws.soap.SOAPFaultException;

import com.sun.pept.ept.MessageInfo;
import com.sun.pept.presentation.MessageStruct;
import com.sun.xml.ws.encoding.internal.InternalEncoder;
import com.sun.xml.ws.encoding.jaxb.JAXBBridgeInfo;
import com.sun.xml.ws.encoding.soap.internal.BodyBlock;
import com.sun.xml.ws.encoding.soap.internal.HeaderBlock;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;
import com.sun.xml.ws.encoding.soap.message.SOAPFaultInfo;
import com.sun.xml.ws.model.CheckedException;
import com.sun.xml.ws.model.ExceptionType;
import com.sun.xml.ws.model.JavaMethod;
import com.sun.xml.ws.model.Parameter;
import com.sun.xml.ws.model.RuntimeModel;
import com.sun.xml.ws.model.soap.SOAPBinding;
import com.sun.xml.ws.model.soap.SOAPBlock;
import com.sun.xml.ws.server.RuntimeContext;
import com.sun.xml.ws.util.StringUtils;
import com.sun.xml.ws.client.BindingProviderProperties;

/**
 * @author Vivek Pandey
 *
 * SOAP Client side encoder/decoder.
 */
public class ClientEncoderDecoder extends EncoderDecoder implements InternalEncoder {
    public ClientEncoderDecoder() {
    }

    /* (non-Javadoc)
     * @see com.sun.xml.rpc.rt.encoding.EncoderDecoderBase#toMessageInfo(java.lang.Object, com.sun.pept.ept.MessageInfo)
     */
    public void toMessageInfo(Object intMessage, MessageInfo mi) {
        InternalMessage im = (InternalMessage) intMessage;
        BodyBlock bodyBlock = im.getBody();
        // TODO what if bodyBlock is null, may be use NULL_BODY constant QNAME
        RuntimeContext rtContext = (RuntimeContext) mi.getMetaData(BindingProviderProperties.JAXWS_RUNTIME_CONTEXT);
        JavaMethod jm = rtContext.getModel().getJavaMethod(mi.getMethod());
        mi.setMEP(jm.getMEP());

        Object bodyValue  = (bodyBlock == null) ? null : bodyBlock.getValue();

        // TODO process exceptions
        if(bodyValue instanceof SOAPFaultInfo){
            SOAPFaultInfo sfi = (SOAPFaultInfo)bodyValue;
            Object detail = sfi.getDetail();
            if(detail == null || detail instanceof javax.xml.soap.Detail) {
                javax.xml.soap.Detail sfeDetail = null;
                if(detail != null)
                    sfeDetail = (javax.xml.soap.Detail)detail;
                RemoteException rex = new RemoteException(sfi.getString(), new SOAPFaultException(sfi.getCode(), sfi.getString(), sfi.getActor(), sfeDetail));
                mi.setResponseType(MessageStruct.CHECKED_EXCEPTION_RESPONSE);
                mi.setResponse(rex);
                return;
            }
            JAXBBridgeInfo bi = (JAXBBridgeInfo)detail;
            CheckedException ce = jm.getCheckedException(bi.getType());
            Exception ex = createCheckedException(sfi.getString(), ce, bi.getValue());
            mi.setResponseType(MessageStruct.CHECKED_EXCEPTION_RESPONSE);
            mi.setResponse(ex);
            return;
        }
        // process body/headers/attachments
        List<HeaderBlock> headers = im.getHeaders();
        Iterator<Parameter> iter = jm.getResponseParameters().iterator();
        Object[] data = mi.getData();
        SOAPBinding soapBinding = (SOAPBinding)jm.getBinding();
        while (iter.hasNext()) {
            Parameter param = iter.next();
            Object obj = null;
            SOAPBlock paramBinding = (SOAPBlock)param.getBinding();
            if (paramBinding.equals(SOAPBlock.BODY)) {
                //TODO: check if the bodyValue qname is the one we expect!
                obj = bodyValue;
            } else if (paramBinding.equals(SOAPBlock.HEADER)) {
                HeaderBlock header = getHeaderBlock(param.getName(), headers);
                obj = (header != null)?header.getValue():null;
            }
            Object resp = fillData(rtContext, param, obj, data, soapBinding);
            if(param.isResponse())
                mi.setResponse(resp);
        }
    }

    private Exception createCheckedException(String message, CheckedException ce, Object detail) {
        if(ce.getExceptionType().equals(ExceptionType.UserDefined)){
            return createUserDefinedException(message, ce, detail);

        }
        Class exceptionClass = ce.getExcpetionClass();
        try {
            Constructor constructor = exceptionClass.getConstructor(new Class[]{String.class, (Class) ce.getDetailType().type});
            Object exception = constructor.newInstance(new Object[]{message, detail});
            return (Exception)exception;
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param message
     * @param ce
     * @param detail
     * @return
     */
    private Exception createUserDefinedException(String message, CheckedException ce, Object detail) {
        Class exceptionClass = ce.getExcpetionClass();
        try {
            Constructor constructor = exceptionClass.getConstructor(new Class[]{String.class});
            Object exception = constructor.newInstance(new Object[]{message});
            Field[] fields = detail.getClass().getFields();
            for(Field f : fields){
                Method m = exceptionClass.getMethod(getWriteMethod(f));
                m.invoke(exception, new Object[]{f.get(detail)});
            }
            return (Exception)exception;
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param f
     * @return
     */
    private String getWriteMethod(Field f){
        return "set" + StringUtils.capitalize(f.getName());
    }

    /* (non-Javadoc)
     * @see com.sun.xml.rpc.rt.encoding.EncoderDecoderBase#toInternalMessage(com.sun.pept.ept.MessageInfo)
     */
    public Object toInternalMessage(MessageInfo mi) {
        RuntimeContext rtContext = (RuntimeContext) mi.getMetaData(BindingProviderProperties.JAXWS_RUNTIME_CONTEXT);
        RuntimeModel model = rtContext.getModel();

        JavaMethod jm = model.getJavaMethod(mi.getMethod());
        Object[] data = mi.getData();
        InternalMessage im = new InternalMessage();
        Iterator<Parameter> iter = jm.getRequestParameters().iterator();
        SOAPBinding soapBinding = (SOAPBinding)jm.getBinding();
        while (iter.hasNext()) {
            Parameter param = iter.next();
            SOAPBlock paramBinding = (SOAPBlock)param.getBinding();
            Object obj = createPayload(rtContext, param, data, null, soapBinding);
            if (paramBinding.equals(SOAPBlock.BODY)) {
                im.setBody(new BodyBlock(obj));
            } else if (paramBinding.equals(SOAPBlock.HEADER)) {
                im.addHeader(new HeaderBlock((JAXBBridgeInfo)obj));
            } else if (paramBinding.equals(SOAPBlock.ATTACHMENT)) {
                // TODO Attachment
            }
        }
        return im;
    }
}
