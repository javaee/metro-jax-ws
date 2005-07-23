/**
 * $Id: ProviderPeptTie.java,v 1.3 2005-07-23 04:10:12 kohlert Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.server.provider;

import java.lang.reflect.Method;
import javax.xml.ws.Provider;
import java.util.Map;
import javax.xml.ws.WebServiceException;

import com.sun.pept.ept.MessageInfo;
import com.sun.pept.presentation.MessageStruct;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.xml.ws.server.PeptTie;
import com.sun.xml.ws.server.RuntimeContext;
import com.sun.xml.ws.server.RuntimeEndpointInfo;
import com.sun.xml.ws.util.MessageInfoUtil;


/**
 * Creates a Stateless Tie object so that it is created only once and reused.
 */
public class ProviderPeptTie extends PeptTie {

    private static final Logger logger = Logger.getLogger(
        com.sun.xml.ws.util.Constants.LoggingDomain + ".server.ProviderPeptTie");

    public static final Method invoke_Method;
    static {
        try {
            Class[] methodParams = { Object.class, Map.class };
            invoke_Method = (Provider.class).getMethod("invoke", methodParams);
        } catch (NoSuchMethodException e) {
            throw new WebServiceException(e.getMessage(), e);
        }
    };

    /*
     * @see Tie#_invoke(MessageInfo)
     */
    public void _invoke(MessageInfo messageInfo) {
        Object[] oa = messageInfo.getData();
        Map<String, Object> context = (Map<String, Object>) oa[1];
        RuntimeContext rtCtxt = MessageInfoUtil.getRuntimeContext(messageInfo);
        RuntimeEndpointInfo endpointInfo = rtCtxt.getRuntimeEndpointInfo();
        Provider servant = (Provider)endpointInfo.getImplementorProxy();
        if (servant == null) {     // If no proxy object, use actual implementor
            servant = (Provider)endpointInfo.getImplementor();
        }
        try {
            Object response = servant.invoke(oa[0], context);
            messageInfo.setResponse(response);
            messageInfo.setResponseType(MessageStruct.NORMAL_RESPONSE);
            if (response == null) {
                messageInfo.setMEP(MessageStruct.ONE_WAY_MEP);
            } else {
                messageInfo.setMEP(MessageStruct.REQUEST_RESPONSE_MEP);
            }
        } catch (RuntimeException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            messageInfo.setResponseType(MessageStruct.UNCHECKED_EXCEPTION_RESPONSE);
            messageInfo.setResponse(e);
        } catch (Exception e) {
            messageInfo.setResponseType(MessageStruct.CHECKED_EXCEPTION_RESPONSE);
            messageInfo.setResponse(e);
        }
    }

}
