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
package com.sun.xml.ws.server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.sun.xml.ws.pept.ept.MessageInfo;
import com.sun.xml.ws.pept.presentation.MessageStruct;
import com.sun.xml.ws.pept.presentation.Tie;
import com.sun.xml.ws.util.MessageInfoUtil;
import java.rmi.RemoteException;
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
     * @see Tie#_invoke(MessageInfo)
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
                // Consider RemoteException as RuntimeException
                if (!(cause instanceof RuntimeException) && cause instanceof Exception
                        && !(cause instanceof RemoteException)) {
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
