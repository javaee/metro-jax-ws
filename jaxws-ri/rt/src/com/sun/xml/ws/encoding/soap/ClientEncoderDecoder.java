/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */
package com.sun.xml.ws.encoding.soap;

import com.sun.xml.ws.pept.ept.MessageInfo;
import com.sun.xml.ws.pept.presentation.MessageStruct;
import com.sun.xml.ws.client.BindingProviderProperties;
import com.sun.xml.ws.client.RequestContext;
import com.sun.xml.ws.encoding.internal.InternalEncoder;
import com.sun.xml.ws.encoding.jaxb.JAXBBridgeInfo;
import com.sun.xml.ws.encoding.jaxb.RpcLitPayload;
import com.sun.xml.ws.encoding.soap.internal.AttachmentBlock;
import com.sun.xml.ws.encoding.soap.internal.BodyBlock;
import com.sun.xml.ws.encoding.soap.internal.HeaderBlock;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;
import com.sun.xml.ws.encoding.soap.message.FaultReasonText;
import com.sun.xml.ws.encoding.soap.message.SOAP12FaultInfo;
import com.sun.xml.ws.encoding.soap.message.SOAPFaultInfo;
import com.sun.xml.ws.model.CheckedException;
import com.sun.xml.ws.model.ExceptionType;
import com.sun.xml.ws.model.JavaMethod;
import com.sun.xml.ws.model.Parameter;
import com.sun.xml.ws.model.ParameterBinding;
import com.sun.xml.ws.model.RuntimeModel;
import com.sun.xml.ws.model.WrapperParameter;
import com.sun.xml.ws.model.soap.SOAPBinding;
import com.sun.xml.ws.server.RuntimeContext;
import com.sun.xml.ws.util.StringUtils;
import com.sun.xml.ws.handler.HandlerContext;

import javax.xml.namespace.QName;
import javax.xml.ws.soap.SOAPFaultException;
import javax.xml.ws.handler.MessageContext;
import javax.activation.DataHandler;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Vivek Pandey
 *
 * SOAP Client side encoder/decoder.
 */
public class ClientEncoderDecoder extends EncoderDecoder implements InternalEncoder {
    public ClientEncoderDecoder() {
    }

    /* (non-Javadoc)
     * @see com.sun.xml.ws.rt.encoding.EncoderDecoderBase#toMessageInfo(java.lang.Object, com.sun.pept.ept.MessageInfo)
     */
    public void toMessageInfo(Object intMessage, MessageInfo mi) {
        InternalMessage im = (InternalMessage) intMessage;
        BodyBlock bodyBlock = im.getBody();
        RuntimeContext rtContext = (RuntimeContext) mi.getMetaData(BindingProviderProperties.JAXWS_RUNTIME_CONTEXT);
        JavaMethod jm = rtContext.getModel().getJavaMethod(mi.getMethod());
        mi.setMEP(jm.getMEP());

        Object bodyValue  = (bodyBlock == null) ? null : bodyBlock.getValue();

        if(bodyValue instanceof SOAPFaultInfo){
            SOAPFaultInfo sfi = (SOAPFaultInfo)bodyValue;
            Object detail = sfi.getDetail();
            if(detail == null || detail instanceof javax.xml.soap.Detail) {
                SOAPFaultException sfe = new SOAPFaultException(sfi.getSOAPFault());
                mi.setResponseType(MessageStruct.CHECKED_EXCEPTION_RESPONSE);
                mi.setResponse(sfe);
                return;
            }
            JAXBBridgeInfo bi = (JAXBBridgeInfo)detail;
            CheckedException ce = jm.getCheckedException(bi.getType());
            Exception ex = createCheckedException(sfi.getString(), ce, bi.getValue());
            mi.setResponseType(MessageStruct.CHECKED_EXCEPTION_RESPONSE);
            mi.setResponse(ex);
            return;
        }

        if(bodyValue instanceof SOAP12FaultInfo){
            SOAP12FaultInfo sfi = (SOAP12FaultInfo)bodyValue;
            Object detail = sfi.getDetail();
            if(detail == null || detail instanceof javax.xml.soap.Detail) {
//                javax.xml.soap.Detail sfeDetail = null;
//                if(detail != null)
//                    sfeDetail = (javax.xml.soap.Detail)detail;
//                String reason = null;
//                List<FaultReasonText> frt = sfi.getReasons().getFaultReasonTexts();
//
//                //for now we pickup onkly the first Reason Text
//                if(frt != null && !frt.isEmpty())
//                    reason = frt.get(0).getValue();

                SOAPFaultException sfe = new SOAPFaultException(sfi.getSOAPFault());
                mi.setResponseType(MessageStruct.CHECKED_EXCEPTION_RESPONSE);
                mi.setResponse(sfe);
                return;
            }
            JAXBBridgeInfo bi = (JAXBBridgeInfo)detail;
            CheckedException ce = jm.getCheckedException(bi.getType());
            String reason = null;
            List<FaultReasonText> frt = sfi.getReasons().getFaultReasonTexts();
            //for now we pickup onkly the first Reason Text
            if(frt != null && !frt.isEmpty())
                reason = frt.get(0).getValue();
            Exception ex = createCheckedException(reason, ce, bi.getValue());
            mi.setResponseType(MessageStruct.CHECKED_EXCEPTION_RESPONSE);
            mi.setResponse(ex);
            return;
        }

        // process body/headers/attachments
        List<HeaderBlock> headers = im.getHeaders();
        Map<String, AttachmentBlock> attachments = im.getAttachments();
        Object[] data = mi.getData();
        SOAPBinding soapBinding = (SOAPBinding)jm.getBinding();

        // TODO: why is the binding determined by the instance?
        //what happens when client receives unsolicited headers?
        int bBlocks = (bodyValue != null)?1:0;
        int hBlocks = (im.getHeaders() != null)?im.getHeaders().size():0;
        int mBlocks = (im.getAttachments() != null)?im.getAttachments().size():0;

        boolean isResponseAsynWrapper = (bBlocks + hBlocks + mBlocks) > 1;

        //for rpclit there could be more than one parts but only one bodyblock
        // so we use different rule for rpclit
        if((bodyValue instanceof RpcLitPayload) && !isResponseAsynWrapper){
            isResponseAsynWrapper = (((RpcLitPayload)bodyValue).getBridgeParameters().size() > 1);
        }

        if(jm.isAsync() && isResponseAsynWrapper){
            Object asyncWrapper = createAsyncResponseClass(jm.getResponseParameters().get(0));
            if(bodyValue instanceof RpcLitPayload){
                RpcLitPayload payload = (RpcLitPayload)bodyValue;
                for(JAXBBridgeInfo bi : payload.getBridgeParameters()){
                    setAsyncResponseWrapperValue(rtContext, asyncWrapper, bi.getValue(), bi.getType().tagName);
                }
            }else {
                JAXBBridgeInfo value = (JAXBBridgeInfo)bodyValue;
                setAsyncResponseWrapperValue(rtContext, asyncWrapper, value.getValue(), value.getType().tagName);
            }

            if(im.getHeaders() != null) {
                for(HeaderBlock hb : im.getHeaders()){
                    JAXBBridgeInfo value = (JAXBBridgeInfo)hb.getValue();
                    setAsyncResponseWrapperValue(rtContext, asyncWrapper, value.getValue(), value.getType().tagName);
                }
            }else if(attachments != null){
                for(String id : attachments.keySet()){
                    AttachmentBlock ab = attachments.get(id);
                    if(ab == null)
                        return;
                    String part = ab.getWSDLPartName();

                    // TODO: this isn't correct
                    setAsyncResponseWrapperValue(rtContext, asyncWrapper, ab.asDataHandler(), new QName("", part));
                }
            }
            mi.setResponse(asyncWrapper);
            return;
        }else if(jm.isAsync() && ((bBlocks+hBlocks+mBlocks) == 1)){
            //there is only 1 response part
            Object value = null;
            if(bodyValue instanceof RpcLitPayload){
                RpcLitPayload payload = (RpcLitPayload)bodyValue;
                for(JAXBBridgeInfo bi:payload.getBridgeParameters()){
                    value = bi.getValue();
                    break;
                }
            }else{
                value = ((JAXBBridgeInfo)bodyValue).getValue();
            }

            if(value != null){
                mi.setResponse(value);
                return;
            }

            if(headers != null){
                for(HeaderBlock hb : headers){
                    value = ((JAXBBridgeInfo)hb.getValue()).getValue();
                    if(value != null){
                        mi.setResponse(value);
                        return;
                    }
                }
            }

            if(attachments != null){
                for(String id:attachments.keySet()){
                    AttachmentBlock ab = attachments.get(id);
                    if(ab == null)      continue;

                    // TODO: this isn't correct
                    mi.setResponse(ab.asDataHandler());
                    return;
                }
            }

            // bBlocks+hBlocks+mBlocks==1, so we shall never get here
            assert false;
        }


        for (Parameter param : jm.getResponseParameters()) {
            Object obj = null;
            ParameterBinding paramBinding = param.getOutBinding();
            if (paramBinding.isBody()) {
                //TODO: check if the bodyValue qname is the one we expect!
                obj = bodyValue;
            } else if (headers != null && paramBinding.isHeader()) {
                HeaderBlock header = getHeaderBlock(param.getName(), headers);
                obj = (header != null) ? header.getValue() : null;
            } else if (paramBinding.isAttachment()) {
                obj = getAttachment(rtContext, attachments, param, paramBinding);
            }
            Object resp = fillData(rtContext, param, obj, data, soapBinding, paramBinding);
            if (param.isResponse()) {
                mi.setResponse(resp);
            }
        }
    }

    private void setAsyncResponseWrapperValue(RuntimeContext rtContext, Object bean, Object value, QName tag){
        if(value != null){
            setWrapperChildValue(rtContext, bean, value, tag.getNamespaceURI(), tag.getLocalPart());
        }
    }

    private Object createAsyncResponseClass(Parameter parameter) {
        Class asyncWrapper = (Class)parameter.getTypeReference().type;
        if(RpcLitPayload.class.isAssignableFrom(asyncWrapper)){
            WrapperParameter wp = (WrapperParameter)parameter;
            if(wp.getWrapperChildren().size() > 0){
                Parameter p = wp.getWrapperChildren().get(0);
                asyncWrapper = (Class) p.getTypeReference().type;
            }
        }

        try {
            return asyncWrapper.newInstance();
        } catch(Exception e){
            throw new SerializationException(e);
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
        } catch(Exception e){
            throw new SerializationException(e);
        }
    }

    /**
     * @param message
     * @param ce
     * @param detail
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
        } catch(Exception e){
            throw new SerializationException(e);
        }
    }

    /**
     * @param f
     */
    private String getWriteMethod(Field f){
        return "set" + StringUtils.capitalize(f.getName());
    }

    /* (non-Javadoc)
     * @see com.sun.xml.ws.rt.encoding.EncoderDecoderBase#toInternalMessage(com.sun.pept.ept.MessageInfo)
     */
    public InternalMessage toInternalMessage(MessageInfo mi) {
        RuntimeContext rtContext = (RuntimeContext) mi.getMetaData(BindingProviderProperties.JAXWS_RUNTIME_CONTEXT);
        RuntimeModel model = rtContext.getModel();

        JavaMethod jm = model.getJavaMethod(mi.getMethod());
        Object[] data = mi.getData();
        InternalMessage im = new InternalMessage();
        //copy the attachments from the outbound attachments to InternalMessage
        RequestContext ctxt = (RequestContext)mi.getMetaData(BindingProviderProperties.JAXWS_CONTEXT_PROPERTY);
        copyAttachmentProperty(ctxt, im);            
        Iterator<Parameter> iter = jm.getRequestParameters().iterator();
        SOAPBinding soapBinding = (SOAPBinding)jm.getBinding();
        while (iter.hasNext()) {
            Parameter param = iter.next();
            ParameterBinding paramBinding = param.getInBinding();
            Object obj = createPayload(rtContext, param, data, null, soapBinding, paramBinding);
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

}
