/**
 * $Id: PeptTie.java,v 1.1 2005-05-23 22:50:24 bbissett Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.sun.pept.ept.MessageInfo;
import com.sun.pept.presentation.MessageStruct;
import com.sun.pept.presentation.Tie;
import com.sun.xml.ws.util.MessageInfoUtil;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Creates a Stateless Tie object so that it is created only once and reused.
 */
public class PeptTie implements Tie {

    private static final Logger logger = Logger.getLogger(
            com.sun.xml.ws.util.Constants.LoggingDomain + ".server.PeptTie");

    public void _setServant(Object servant) {
        throw new UnsupportedOperationException();
    }

    public Object _getServant() {
        throw new UnsupportedOperationException();
    }

    /*
     * @see com.sun.pept.presentation.Tie#_invoke(com.sun.pept.ept.MessageInfo)
     */
    public void _invoke(MessageInfo messageInfo) {
        Object[] oa = messageInfo.getData();
        Method method = messageInfo.getMethod();
        RuntimeContext rtCtxt = MessageInfoUtil.getRuntimeContext(messageInfo);
        RuntimeEndpointInfo endpointInfo = rtCtxt.getRuntimeEndpointInfo();
        Object servant = endpointInfo.getImplementor();
        try {
            Object ret = method.invoke(servant, oa);
            messageInfo.setResponseType(MessageStruct.NORMAL_RESPONSE);
            messageInfo.setResponse(ret);
        } catch (IllegalArgumentException e) {
            setRuntimeException(messageInfo, e);
        } catch (IllegalAccessException e) {
            setRuntimeException(messageInfo, e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause != null) {
                if (!(cause instanceof RuntimeException) && cause instanceof Exception ) {
                    // Service specific exception
                    messageInfo.setResponseType(
                            MessageStruct.CHECKED_EXCEPTION_RESPONSE);
                    messageInfo.setResponse(cause);
                } else {
                    setRuntimeException(messageInfo, cause);
                }
            } else {
                setRuntimeException(messageInfo, e);
            }
        }
    }

    private void setRuntimeException(MessageInfo messageInfo, Throwable e) {
        logger.log(Level.SEVERE, e.getMessage(), e);
        messageInfo.setResponseType(MessageStruct.UNCHECKED_EXCEPTION_RESPONSE);
        messageInfo.setResponse(e);
    }

}
