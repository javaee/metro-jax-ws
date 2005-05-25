/**
 * $Id: ServerEncoderDecoder.java,v 1.2 2005-05-25 18:22:11 kohlert Exp $
 */
/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.encoding.soap;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

import javax.xml.ws.Holder;

import com.sun.pept.ept.MessageInfo;
import com.sun.pept.presentation.MessageStruct;
import com.sun.xml.ws.encoding.internal.InternalEncoder;
import com.sun.xml.ws.encoding.jaxb.JAXBBridgeInfo;
import com.sun.xml.ws.server.RuntimeContext;
import com.sun.xml.ws.util.StringUtils;
import com.sun.xml.ws.model.CheckedException;
import com.sun.xml.ws.model.ExceptionType;
import com.sun.xml.ws.model.JavaMethod;
import com.sun.xml.ws.model.Parameter;
import com.sun.xml.ws.model.RuntimeModel;
import com.sun.xml.ws.model.WrapperParameter;
import com.sun.xml.ws.model.soap.SOAPBinding;
import com.sun.xml.ws.model.soap.SOAPBlock;
import com.sun.xml.ws.model.soap.SOAPRuntimeModel;
import com.sun.xml.ws.encoding.soap.internal.BodyBlock;
import com.sun.xml.ws.encoding.soap.internal.HeaderBlock;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;
import com.sun.xml.ws.client.BindingProviderProperties;

/**
 * @author Vivek Pandey
 *
 * Server SOAP encoder decoder
 */
public class ServerEncoderDecoder extends EncoderDecoder implements InternalEncoder {
    public ServerEncoderDecoder() {
    }

    /*
     * (non-Javadoc)
     *
     * @see com.sun.xml.rpc.encoding.util.EncoderDecoderBase#toMessageInfo(java.lang.Object,
     *      com.sun.pept.ept.MessageInfo)
     */
    public void toMessageInfo(Object intMessage, MessageInfo mi) {
        InternalMessage im = (InternalMessage) intMessage;
        RuntimeContext rtContext = (RuntimeContext) mi.getMetaData(BindingProviderProperties.JAXWS_RUNTIME_CONTEXT);

        BodyBlock bodyBlock = im.getBody();
        JavaMethod jm = rtContext.getModel().getJavaMethod(mi.getMethod());
        mi.setMEP(jm.getMEP());
        List<HeaderBlock> headers = im.getHeaders();

        Iterator<Parameter> iter = jm.getRequestParameters().iterator();
        Object bodyValue = (bodyBlock == null) ? null :  bodyBlock.getValue();

        // TODO process exceptions

        int numInputParams = jm.getInputParametersCount();
        Object data[] = new Object[numInputParams];
        SOAPBinding soapBinding = (SOAPBinding)jm.getBinding();
        while (iter.hasNext()) {
            Parameter param = iter.next();
            SOAPBlock paramBinding = (SOAPBlock)param.getBinding();
            Object obj = null;
            if (paramBinding.equals(SOAPBlock.BODY)) {
                obj = bodyValue;
            } else if (paramBinding.equals(SOAPBlock.HEADER)) {
                HeaderBlock header = getHeaderBlock(param.getName(), headers);
                obj = (header != null)?header.getValue():null;
            } else if (paramBinding.equals(SOAPBlock.ATTACHMENT)) {
                // TODO Attachment
            }
            fillData(rtContext, param, obj, data, soapBinding);
        }
        Iterator<Parameter> resIter = jm.getResponseParameters().iterator();
        while(resIter.hasNext()){
            Parameter p = resIter.next();
            createOUTHolders(p, data);
        }
        mi.setData(data);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.sun.xml.rpc.encoding.util.EncoderDecoderBase#toInternalMessage(com.sun.pept.ept.MessageInfo)
     */
    public Object toInternalMessage(MessageInfo mi) {
        RuntimeContext rtContext = (RuntimeContext) mi.getMetaData(BindingProviderProperties.JAXWS_RUNTIME_CONTEXT);
        // TODO remove, we dont want enc/dec to have any state
        RuntimeModel model = rtContext.getModel();
        JavaMethod jm = model.getJavaMethod(mi.getMethod());
        Object[] data = mi.getData();
        Object result = mi.getResponse();
        InternalMessage im = new InternalMessage();

        switch (mi.getResponseType()) {
            case MessageStruct.CHECKED_EXCEPTION_RESPONSE:
                if (result instanceof java.rmi.RemoteException) {
                    SOAPRuntimeModel.createFaultInBody(result, getActor(), null, im);
                    return im;
                }
                if(!(result instanceof java.lang.Exception)){
                    //TODO its error, throw excetion?
                    break;
                }
                CheckedException ce = jm.getCheckedException(result.getClass());
                if(ce == null){
                    //TODO: throw exception
                    System.out.println("Error: Couldnt find model for: " + result.getClass());
                    break;
                }
                Object detail = getDetail(jm.getCheckedException(result.getClass()), result);
                JAXBBridgeInfo di = new JAXBBridgeInfo(model.getBridge(ce.getDetailType()), detail);
                if(ce.isHeaderFault())
                    SOAPRuntimeModel.createHeaderFault(result, null, di, im);
                else
                    SOAPRuntimeModel.createFaultInBody(result, null, di, im);
                return im;
            case MessageStruct.UNCHECKED_EXCEPTION_RESPONSE:
                SOAPRuntimeModel.createFaultInBody(result, getActor(), null, im);
                return im;
        }

        SOAPBinding soapBinding = (SOAPBinding)jm.getBinding();
        Iterator<Parameter> iter = jm.getResponseParameters().iterator();
        while (iter.hasNext()) {
            Parameter param = iter.next();
            SOAPBlock paramBinding = (SOAPBlock) param.getBinding();
            Object obj = createPayload(rtContext, param, data, result, soapBinding);
            if (paramBinding.equals(SOAPBlock.BODY)) {
                im.setBody(new BodyBlock(obj));
            } else if (paramBinding.equals(SOAPBlock.HEADER)) {
                im.addHeader(new HeaderBlock(param.getName(), obj));
            } else if (paramBinding.equals(SOAPBlock.ATTACHMENT)) {
                // TODO Attachment
            }
        }
        return im;
    }

    private Object getDetail(CheckedException ce, Object exception) {
        if(ce.getExceptionType().equals(ExceptionType.UserDefined)){
            return createDetailFromUserDefinedException(ce, exception);
        }
        try {
            Method m = exception.getClass().getMethod("getFaultInfo");
            return m.invoke(exception);
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // TODO Auto-generated method stub
        return null;
    }

    private Object createDetailFromUserDefinedException(CheckedException ce, Object exception) {
        Class detailBean = ce.getDetailBean();
        Field[] fields = detailBean.getFields();
        try {
            Object detail = detailBean.newInstance();
            for(Field f : fields){
                Method em = exception.getClass().getMethod(getReadMethod(f));
                f.set(detail, em.invoke(exception));
            }
            return detail;
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

    private String getReadMethod(Field f){
        if(f.getType().isAssignableFrom(boolean.class))
            return "is" + StringUtils.capitalize(f.getName());
        return "get" + StringUtils.capitalize(f.getName());
    }

    /**
     * @return
     */
    public String getActor() {
        return null;
    }

    /**
     * To be used by the incoming message on the server side to set the OUT
     * holders with Holder instance.
     *
     * @param data
     */
    private void createOUTHolders(Parameter param, Object[] data) {
        if(param.isWrapperStyle()){
            for(Parameter p : ((WrapperParameter)param).getWrapperChildren()){
                if(!p.isResponse() && p.isOUT())
                    data[p.getIndex()] = new Holder();
            }
            return;
        }
        //its BARE
        if (!param.isResponse() && param.isOUT())
            data[param.getIndex()] = new Holder();
    }
}
