/**
 * $Id: EncoderDecoder.java,v 1.1 2005-05-23 22:30:14 bbissett Exp $
 */
/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.encoding.soap;

import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import com.sun.xml.ws.encoding.jaxb.JAXBBridgeInfo;
import com.sun.xml.ws.encoding.jaxb.RpcLitPayload;
import com.sun.xml.ws.server.RuntimeContext;
import com.sun.xml.ws.encoding.EncoderDecoderBase;
import com.sun.xml.ws.model.Parameter;
import com.sun.xml.ws.model.RuntimeModel;
import com.sun.xml.ws.model.WrapperParameter;
import com.sun.xml.ws.model.soap.SOAPBinding;
import com.sun.xml.ws.model.soap.SOAPBlock;
import com.sun.xml.ws.encoding.soap.internal.HeaderBlock;
import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;

/**
 * @author Vivek Pandey
 * 
 * Base SOAP encoder decoder class.
 */
public abstract class EncoderDecoder extends EncoderDecoderBase {

    /**
     * Fills the data[] which is ordered list of java method parameters.
     * 
     * @param context
     *            will be used if needed
     * @param obj
     * @param data
     * @return if the parameter is a return
     */
    protected Object fillData(RuntimeContext context, Parameter param, Object obj, Object[] data,
            SOAPBinding binding) {
        if (param.isWrapperStyle()) {
            Object resp = null;
            for (Parameter p : ((WrapperParameter) param).getWrapperChildren()) {
                QName name = p.getName();
                Object value = null;
                if (binding.isDocLit())
                    value = super.getWrapperChildValue(context, ((JAXBBridgeInfo)obj).getValue(), name.getNamespaceURI(), name
                            .getLocalPart());
                else if (binding.isRpcLit())
                    value = getWrapperChildValue(context, obj, name.getNamespaceURI(), name
                            .getLocalPart());
                if (p.isResponse())
                    resp = value;
                else {
                    if (data[p.getIndex()] != null) {
                        Parameter.setHolderValue(data[p.getIndex()], value);
                    } else {
                        data[p.getIndex()] = p.createHolderValue(value);
                    }
                }
            }
            return resp;
        }
        obj = (obj != null)?((JAXBBridgeInfo)obj).getValue():null;
        if (param.isResponse()) {
            return obj;
        } else if (data[param.getIndex()] != null) {
            Parameter.setHolderValue(data[param.getIndex()], obj);
        } else {
            data[param.getIndex()] = param.createHolderValue(obj);
        }
        return null;
    }

    /**
     * creates the payload to be serilized over the wire. It can be constructed
     * from the parameters in data[] or from the result.
     * 
     * @param context
     *            runtime context. It can be used to get access to the
     *            JAXBContext.
     * @param data
     *            parameters
     * @param result
     *            it could be null if there are no response object,for example
     *            incase of outgoing client message.
     * @return Payload - decided by the binding used
     */
    protected Object createPayload(RuntimeContext context, Parameter param, Object[] data,
            Object result, SOAPBinding binding) {
        if (binding.isRpcLit() && ((SOAPBlock)param.getBinding()).isBody()) {
            return createRpcLitPayload(context, (WrapperParameter) param, data, result);
        }
        Object obj = createDocLitPayloadValue(context, param, data, result, binding);
        RuntimeModel model = context.getModel();
        return new JAXBBridgeInfo(model.getBridge(param.getTypeReference()), obj);
    }

    /*
     * Returns the value corresponding to the localName or part accessor from
     * rpclit structure.
     * 
     * @see com.sun.xml.rpc.rt.encoding.EncoderDecoderBase#getWrapperChildValue(com.sun.xml.rpc.rt.RuntimeContext,
     *      java.lang.Object, java.lang.String, java.lang.String)
     */
    @Override
    protected Object getWrapperChildValue(RuntimeContext context, Object obj, String nsURI,
            String localName) {
        RpcLitPayload payload = (RpcLitPayload) obj;
        JAXBBridgeInfo rpcParam = payload.getBridgeParameterByName(localName);
        return rpcParam.getValue();
    }

    /**
     * Gives the binding specific object to be serialized.
     * 
     * @param context
     * @param data
     * @param result
     * @return
     */
    private Object createDocLitPayloadValue(RuntimeContext context, Parameter param, Object[] data,
            Object result, SOAPBinding binding) {        
        if (param.isWrapperStyle()) {
            return createJAXBBeanPayload(context, (WrapperParameter) param, data, result);
        }
        return getBarePayload(context, param, data, result);
    }

    /**
     * Gets the HeaderBlock corresponding to the given QName.
     * 
     * @param name
     * @param headers
     * @return
     */
    protected HeaderBlock getHeaderBlock(QName name, List<HeaderBlock> headers) {
        for (HeaderBlock header : headers) {
            if (name.equals(header.getName()))
                return header;
        }
        return null;
    }

    /**
     * Returns either the value corresponding to the parameter or result.
     * 
     * @param context
     * @param param
     * @param data
     * @param result
     * @return Either the value of response of the parameter corresponding to
     *         the parameter index, takes care of Holder.value.
     * 
     */
    private Object getBarePayload(RuntimeContext context, Parameter param, Object[] data,
            Object result) {
        Object obj = null;
        if (param.isResponse()) {
            obj = result;
        } else {
            obj = param.getHolderValue(data[param.getIndex()]);
        }
        return obj;
    }

    /**
     * Creates JAXB style wrapper bean from the parameters or result.
     * 
     * @param context
     * @param param
     *            WrapperParameter
     * @param data
     * @param result
     * @return non-null JAXB style bean.
     */
    private Object createJAXBBeanPayload(RuntimeContext context, WrapperParameter param,
            Object[] data, Object result) {
        Class bean = (Class) param.getTypeReference().type;
        Iterator<Parameter> wc = param.getWrapperChildren().iterator();
        try {
            Object obj = bean.newInstance();
            while (wc.hasNext()) {
                Parameter p = wc.next();
                Object value = null;
                if (p.isResponse())
                    value = result;
                else
                    value = p.getHolderValue(data[p.getIndex()]);
                QName name = p.getName();
                setWrapperChildValue(context, obj, value, name.getNamespaceURI(), name
                        .getLocalPart());
            }
            return obj;
        } catch (java.lang.InstantiationException e) {
            throw new SerializationException(new LocalizableExceptionAdapter(e));
        } catch (java.lang.IllegalAccessException e) {
            throw new SerializationException(new LocalizableExceptionAdapter(e));
        }
    }

    /**
     * Creates RpcLitPayload from the parameters or response.
     * 
     * @param context
     * @param param
     * @param data
     * @param result
     * @return non-null RpcLitPayload
     */
    private Object createRpcLitPayload(RuntimeContext context, WrapperParameter param,
            Object[] data, Object result) {
        Iterator<Parameter> wc = param.getWrapperChildren().iterator();
        RpcLitPayload payload = new RpcLitPayload(param.getName());

        while (wc.hasNext()) {
            Parameter p = wc.next();
            Object value = null;
            if (p.isResponse())
                value = result;
            else
                value = p.getHolderValue(data[p.getIndex()]);
            RuntimeModel model = context.getModel();
            JAXBBridgeInfo bi = new JAXBBridgeInfo(model.getBridge(p.getTypeReference()), value);
            payload.addParameter(bi);
        }
        return payload;
    }
}