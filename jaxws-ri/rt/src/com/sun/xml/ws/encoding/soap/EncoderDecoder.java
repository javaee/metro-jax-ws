/**
 * $Id: EncoderDecoder.java,v 1.17 2005-09-07 19:40:09 vivekp Exp $
 */
/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.encoding.soap;

import com.sun.xml.ws.encoding.EncoderDecoderBase;
import com.sun.xml.ws.encoding.jaxb.JAXBBridgeInfo;
import com.sun.xml.ws.encoding.jaxb.RpcLitPayload;
import com.sun.xml.ws.encoding.jaxb.JAXBTypeSerializer;
import com.sun.xml.ws.encoding.soap.internal.AttachmentBlock;
import com.sun.xml.ws.encoding.soap.internal.HeaderBlock;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;
import com.sun.xml.ws.model.Parameter;
import com.sun.xml.ws.model.RuntimeModel;
import com.sun.xml.ws.model.WrapperParameter;
import com.sun.xml.ws.model.ParameterBinding;
import com.sun.xml.ws.model.soap.SOAPBinding;
import com.sun.xml.ws.server.RuntimeContext;
import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;
import com.sun.xml.ws.util.ASCIIUtility;
import com.sun.xml.bind.api.TypeReference;

import javax.xml.namespace.QName;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.WebServiceException;
import javax.activation.DataHandler;
import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.lang.reflect.Type;
import java.awt.*;

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
            SOAPBinding binding, ParameterBinding paramBinding) {
        if (param.isWrapperStyle()) {
            Object resp = null;
            for (Parameter p : ((WrapperParameter) param).getWrapperChildren()) {
                QName name = p.getName();
                Object value = null;
                if (binding.isDocLit()){
                    value = super.getWrapperChildValue(context, ((JAXBBridgeInfo)obj).getValue(), name.getNamespaceURI(), name
                            .getLocalPart());
                }else if (binding.isRpcLit()){
                    value = getWrapperChildValue(context, obj, name.getNamespaceURI(), name
                            .getLocalPart());
                    if(value == null)
                        value = setIfPrimitive(p.getTypeReference().type);
                }
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

        if(!paramBinding.isAttachment()){
            if(paramBinding.isUnbound())
                obj = setIfPrimitive(param.getTypeReference().type);
            else
                obj = (obj != null)?((JAXBBridgeInfo)obj).getValue():null;
        }
        if (param.isResponse()) {
            if(paramBinding.isUnbound())
                return setIfPrimitive(param.getTypeReference().type);
            return obj;
        } else if (data[param.getIndex()] != null) {
            Parameter.setHolderValue(data[param.getIndex()], obj);
        } else {
            data[param.getIndex()] = param.createHolderValue(obj);
        }
        return null;
    }

    /**
     * Returns the default values of primitive types. To be called when the object referene by this type is null.
     * @param type
     * @return default values of primitive types if type is primitive else null
     */
    private Object setIfPrimitive(Type type) {
        if(type instanceof Class){
            Class cls = (Class)type;
            if(cls.isPrimitive()){
                if(cls.getName().equals(boolean.class.getName())){
                    return false;
                }
                return 0;
            }
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
            Object result, SOAPBinding binding, ParameterBinding paramBinding) {
        if(paramBinding.isAttachment()){
            Object obj = null;
            if(param.isResponse())
                obj = result;
            else
                obj = param.getHolderValue(data[param.getIndex()]);
            return obj;
        }
        if (binding.isRpcLit() && paramBinding.isBody()) {
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
     * @see EncoderDecoderBase#getWrapperChildValue(RuntimeContext,
     *      Object, String, String)
     */
    @Override
    protected Object getWrapperChildValue(RuntimeContext context, Object obj, String nsURI,
            String localName) {
        RpcLitPayload payload = (RpcLitPayload) obj;
        JAXBBridgeInfo rpcParam = payload.getBridgeParameterByName(localName);
        if(rpcParam != null)
            return rpcParam.getValue();
        return null;
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
     * @return the <code>HeaderBlock</code> corresponding to the given 
     * <code>QName name</code>
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
        } catch(Exception e){
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
            if(p.getBinding().isUnbound())
                continue;
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

    protected Object getAttachment(RuntimeContext rtContext, Map<String, AttachmentBlock> attachments, 
                                   Parameter param, ParameterBinding paramBinding){
        Object obj = null;
        RuntimeModel model = rtContext.getModel();
        for(String id:attachments.keySet()){
            String part = getMimePart(id);
            if(part.equals(param.getPartName())){
                AttachmentBlock ab = attachments.get(id);
                if(ab == null)
                    return null;
                AttachmentPart ap = ab.getAttachmentPart();
                String mimeType = paramBinding.getMimeType();
                Class type = (Class)param.getTypeReference().type;
                try {
                    if (DataHandler.class.isAssignableFrom(type))
                        return ap.getDataHandler();
                    else if(isKnownAttachmentType(param.getTypeReference().type))
                        obj =  ap.getContent();
                    else
                        obj = ap.getRawContent();
                } catch (SOAPException e) {
                    throw new SerializationException(new LocalizableExceptionAdapter(e));
                }
                if((obj != null) && isXMLMimeType(mimeType) && !Source.class.isAssignableFrom(type)){
                    JAXBBridgeInfo bi = (JAXBBridgeInfo)rtContext.getDecoderInfo(param.getName());
                    if(Source.class.isAssignableFrom(obj.getClass())){
                        JAXBTypeSerializer.getInstance().deserialize((Source)obj, bi, rtContext.getBridgeContext());
                        return bi.getValue();
                    }else if(InputStream.class.isAssignableFrom(obj.getClass())){
//                        ByteArrayInputStream bais = new ByteArrayInputStream((byte[])obj);
//                        JAXBTypeSerializer.getInstance().deserialize(bais, bi, rtContext.getBridgeContext());
                        JAXBTypeSerializer.getInstance().deserialize((InputStream)obj, bi, rtContext.getBridgeContext());
                        return bi.getValue();
                    }
                }
                if(obj instanceof InputStream){
                    try {
                        return ASCIIUtility.getBytes((InputStream)obj);
                    } catch (IOException e) {
                        throw new WebServiceException(new LocalizableExceptionAdapter(e));
                    }
                }
                return obj;
            }
        }
        return null;
    }

    private boolean isKnownAttachmentType(Type type) {
        if(type instanceof Class){
            Class cls = (Class)type;
            if(DataHandler.class.isAssignableFrom(cls) ||
                    Source.class.isAssignableFrom(cls)||
                    Image.class.isAssignableFrom(cls))
                return true;            
        }
        return false;
    }

    protected void addAttachmentPart(RuntimeContext rtContext, InternalMessage im, Object obj, Parameter mimeParam){
        if(obj == null)
            return;
        RuntimeModel model = rtContext.getModel();
        String mimeType = mimeParam.getBinding().getMimeType();
        String contentId = null;
        try {
            contentId = java.net.URLEncoder.encode(mimeParam.getPartName(), "UTF-8")+"="+UUID.randomUUID()+"@jaxws.sun.com";
        } catch (UnsupportedEncodingException e) {
            throw new SerializationException(new LocalizableExceptionAdapter(e));
        }

        if(!DataHandler.class.isAssignableFrom(obj.getClass()) && isXMLMimeType(mimeType) && !Source.class.isAssignableFrom(obj.getClass())){
            JAXBBridgeInfo bi = new JAXBBridgeInfo(model.getBridge(mimeParam.getTypeReference()), obj);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            JAXBTypeSerializer.getInstance().serialize(bi, rtContext.getBridgeContext(), baos);
            obj = baos.toByteArray();
        }
        AttachmentBlock ab = new AttachmentBlock(contentId, obj, mimeType);
        im.addAttachment(contentId, ab);
    }

    private boolean isXMLMimeType(String mimeType){
        if(mimeType.equals("text/xml") || mimeType.equals("application/xml"))
            return true;
        return false;
    }

    /**
    * According to WSI AP 1.0
    * 3.8 Value-space of Content-Id Header
    *   Definition: content-id part encoding
    *   The "content-id part encoding" consists of the concatenation of:
     * The value of the name attribute of the wsdl:part element referenced by the mime:content, in which characters disallowed in content-id headers (non-ASCII characters as represented by code points above 0x7F) are escaped as follows:
     *     o Each disallowed character is converted to UTF-8 as one or more bytes.
     *     o Any bytes corresponding to a disallowed character are escaped with the URI escaping mechanism (that is, converted to %HH, where HH is the hexadecimal notation of the byte value).
     *     o The original character is replaced by the resulting character sequence.
     * The character '=' (0x3D).
     * A globally unique value such as a UUID.
     * The character '@' (0x40).
     * A valid domain name under the authority of the entity constructing the message.
     *
     * So a wsdl:part fooPart will be encoded as:
     *      <fooPart=somereallybignumberlikeauuid@example.com>
     *
     * @param cId
     * @return
     */
    protected String getMimePart(String cId){
        int index = cId.lastIndexOf('@', cId.length());
        if(index == -1){
            return null;
        }
        String localPart = cId.substring(0, index);
        index = localPart.lastIndexOf('=', localPart.length());
        if(index == -1){
            return null;
        }
        try {
            return java.net.URLDecoder.decode(localPart.substring(0, index), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new SerializationException(new LocalizableExceptionAdapter(e));
        }
    }
}