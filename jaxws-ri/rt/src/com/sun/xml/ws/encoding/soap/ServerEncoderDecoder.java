/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */
package com.sun.xml.ws.encoding.soap;

import com.sun.xml.ws.pept.ept.MessageInfo;
import com.sun.xml.ws.pept.presentation.MessageStruct;
import com.sun.xml.ws.binding.BindingImpl;
import com.sun.xml.ws.client.BindingProviderProperties;
import com.sun.xml.ws.encoding.internal.InternalEncoder;
import com.sun.xml.ws.encoding.jaxb.JAXBBridgeInfo;
import com.sun.xml.ws.encoding.soap.internal.AttachmentBlock;
import com.sun.xml.ws.encoding.soap.internal.BodyBlock;
import com.sun.xml.ws.encoding.soap.internal.HeaderBlock;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;
import com.sun.xml.ws.model.CheckedException;
import com.sun.xml.ws.model.ExceptionType;
import com.sun.xml.ws.model.JavaMethod;
import com.sun.xml.ws.model.Parameter;
import com.sun.xml.ws.model.ParameterBinding;
import com.sun.xml.ws.model.RuntimeModel;
import com.sun.xml.ws.model.WrapperParameter;
import com.sun.xml.ws.model.soap.SOAPBinding;
import com.sun.xml.ws.model.soap.SOAPRuntimeModel;
import com.sun.xml.ws.server.RuntimeContext;
import com.sun.xml.ws.util.MessageInfoUtil;
import com.sun.xml.ws.util.StringUtils;

import javax.xml.ws.Holder;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
     * @see com.sun.xml.ws.encoding.util.EncoderDecoderBase#toMessageInfo(java.lang.Object,
     *      com.sun.pept.ept.MessageInfo)
     */
    public void toMessageInfo(Object intMessage, MessageInfo mi) {
        InternalMessage im = (InternalMessage) intMessage;
        RuntimeContext rtContext = (RuntimeContext) mi.getMetaData(BindingProviderProperties.JAXWS_RUNTIME_CONTEXT);

        BodyBlock bodyBlock = im.getBody();
        JavaMethod jm = rtContext.getModel().getJavaMethod(mi.getMethod());
        mi.setMEP(jm.getMEP());
        List<HeaderBlock> headers = im.getHeaders();
        Map<String, AttachmentBlock> attachments = im.getAttachments();

        Iterator<Parameter> iter = jm.getRequestParameters().iterator();
        Object bodyValue = (bodyBlock == null) ? null :  bodyBlock.getValue();

        int numInputParams = jm.getInputParametersCount();
        Object data[] = new Object[numInputParams];
        SOAPBinding soapBinding = (SOAPBinding)jm.getBinding();
        while (iter.hasNext()) {
            Parameter param = iter.next();
            ParameterBinding paramBinding = param.getInBinding();
            Object obj = null;
            if (paramBinding.isBody()) {
                obj = bodyValue;
            } else if (headers != null && paramBinding.isHeader()) {
                HeaderBlock header = getHeaderBlock(param.getName(), headers);
                obj = (header != null)?header.getValue():null;
            } else if (paramBinding.isAttachment()) {
              obj = getAttachment(rtContext, attachments, param, paramBinding);
            }
            fillData(rtContext, param, obj, data, soapBinding, paramBinding);
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
     * @see com.sun.xml.ws.encoding.util.EncoderDecoderBase#toInternalMessage(com.sun.pept.ept.MessageInfo)
     */
    public Object toInternalMessage(MessageInfo mi) {
        RuntimeContext rtContext = MessageInfoUtil.getRuntimeContext(mi);
        RuntimeModel model = rtContext.getModel();
        JavaMethod jm = model.getJavaMethod(mi.getMethod());
        Object[] data = mi.getData();
        Object result = mi.getResponse();
        InternalMessage im = new InternalMessage();
        if(rtContext.getHandlerContext() != null){
            copyAttachmentProperty(rtContext.getHandlerContext().getMessageContext(), im);
        }
        BindingImpl bindingImpl = 
            (BindingImpl)rtContext.getRuntimeEndpointInfo().getBinding();
        String bindingId = bindingImpl.getBindingId();

        switch (mi.getResponseType()) {
            case MessageStruct.CHECKED_EXCEPTION_RESPONSE:
                if(!(result instanceof java.lang.Exception)){
                    throw new SerializationException("exception.incorrectType", result.getClass().toString());
                }
                CheckedException ce = jm.getCheckedException(result.getClass());
                if(ce == null){
                    throw new SerializationException("exception.notfound", result.getClass().toString());
                }
                Object detail = getDetail(jm.getCheckedException(result.getClass()), result);
                JAXBBridgeInfo di = new JAXBBridgeInfo(model.getBridge(ce.getDetailType()), detail);

                if (bindingId.equals(javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_BINDING)) {
                    SOAPRuntimeModel.createFaultInBody(result, null, di, im);
                } else if (bindingId.equals(javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING)){
                    SOAPRuntimeModel.createSOAP12FaultInBody(result, null, null, di, im);
                }

                return im;
            case MessageStruct.UNCHECKED_EXCEPTION_RESPONSE:
                if (bindingId.equals(javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_BINDING))
                    SOAPRuntimeModel.createFaultInBody(result, getActor(), null, im);
                else if (bindingId.equals(javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING))
                    SOAPRuntimeModel.createSOAP12FaultInBody(result, null, null, null, im);
                return im;
        }
        
        SOAPBinding soapBinding = (SOAPBinding)jm.getBinding();
        Iterator<Parameter> iter = jm.getResponseParameters().iterator();
        while (iter.hasNext()) {
            Parameter param = iter.next();
            ParameterBinding paramBinding = param.getOutBinding();
            Object obj = createPayload(rtContext, param, data, result, soapBinding, paramBinding);
            if (paramBinding.isBody()) {
                im.setBody(new BodyBlock(obj));
            } else if (paramBinding.isHeader()) {
                im.addHeader(new HeaderBlock((JAXBBridgeInfo)obj));
            } else if (paramBinding.isAttachment()) {
                addAttachmentPart(rtContext, im, obj, param);
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
        } catch(Exception e){
            throw new SerializationException(e);
        }
    }

    private Object createDetailFromUserDefinedException(CheckedException ce, Object exception) {
        Class detailBean = ce.getDetailBean();
        Field[] fields = detailBean.getDeclaredFields();
        try {
            Object detail = detailBean.newInstance();
            for(Field f : fields){
                Method em = exception.getClass().getMethod(getReadMethod(f));
                Method sm = detailBean.getMethod(getWriteMethod(f), em.getReturnType());
                sm.invoke(detail, em.invoke(exception));
            }
            return detail;
        } catch(Exception e){
            throw new SerializationException(e);
        }
    }

    private String getReadMethod(Field f){
        if(f.getType().isAssignableFrom(boolean.class))
            return "is" + StringUtils.capitalize(f.getName());
        return "get" + StringUtils.capitalize(f.getName());
    }
    
    private String getWriteMethod(Field f){
        return "set" + StringUtils.capitalize(f.getName());
    }    

    /**
     * @return the actor
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
