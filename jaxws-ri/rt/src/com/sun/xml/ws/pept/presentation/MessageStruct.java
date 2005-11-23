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
/** Java interface "MessageStruct.java" generated from Poseidon for UML.
 *  Poseidon for UML is developed by <A HREF="http://www.gentleware.com">Gentleware</A>.
 *  Generated with <A HREF="http://jakarta.apache.org/velocity/">velocity</A> template engine.
 */
package com.sun.xml.ws.pept.presentation;

import java.lang.reflect.Method;

/**
 * <p>
 * 
 * @author Dr. Harold Carr
 * </p>
 */
public interface MessageStruct {

  ///////////////////////////////////////
  //attributes


/**
 * <p>
 * Represents ...
 * </p>
 */
    public static final int NORMAL_RESPONSE = 0; 

/**
 * <p>
 * Represents ...
 * </p>
 */
    public static final int CHECKED_EXCEPTION_RESPONSE = 1; 

/**
 * <p>
 * Represents ...
 * </p>
 */
    public static final int UNCHECKED_EXCEPTION_RESPONSE = 2; 

/**
 * <p>
 * Represents ...
 * </p>
 */
    public static final int REQUEST_RESPONSE_MEP = 1; 

/**
 * <p>
 * Represents ...
 * </p>
 */
    public static final int ONE_WAY_MEP = 2; 

/**
 * <p>
 * Represents ...
 * </p>
 */
    public static final int ASYNC_POLL_MEP = 3; 

/**
 * <p>
 * Represents ...
 * </p>
 */
    public static final int ASYNC_CALLBACK_MEP = 4; 

  ///////////////////////////////////////
  // operations

/**
 * <p>
 * Does ...
 * </p><p>
 * 
 * @param data ...
 * </p><p>
 * 
 * </p>
 */
    public void setData(Object[] data);
/**
 * <p>
 * Does ...
 * </p><p>
 * 
 * @return a Object[] with ...
 * </p>
 */
    public Object[] getData();
/**
 * <p>
 * Does ...
 * </p><p>
 * 
 * </p><p>
 * 
 * @param name ...
 * </p><p>
 * @param value ...
 * </p>
 */
    public void setMetaData(Object name, Object value);
/**
 * <p>
 * Does ...
 * </p><p>
 * 
 * @return a Object with ...
 * </p><p>
 * @param name ...
 * </p>
 */
    public Object getMetaData(Object name);
/**
 * <p>
 * Does ...
 * </p><p>
 * 
 * </p><p>
 * 
 * @param messageExchangePattern ...
 * </p>
 */
    public void setMEP(int messageExchangePattern);
/**
 * <p>
 * Does ...
 * </p><p>
 * 
 * @return a int with ...
 * </p>
 */
    public int getMEP();
/**
 * <p>
 * Does ...
 * </p><p>
 * 
 * @return a int with ...
 * </p>
 */
    public int getResponseType();
/**
 * <p>
 * Does ...
 * </p><p>
 * 
 * </p><p>
 * 
 * @param responseType ...
 * </p>
 */
    public void setResponseType(int responseType);
/**
 * <p>
 * Does ...
 * </p><p>
 * 
 * @return a Object with ...
 * </p>
 */
    public Object getResponse();
/**
 * <p>
 * Does ...
 * </p><p>
 * 
 * </p><p>
 * 
 * @param response ...
 * </p>
 */
    public void setResponse(Object response);
/**
 * <p>
 * Does ...
 * </p><p>
 * 
 * </p><p>
 * 
 * @param method ...
 * </p>
 */
    public void setMethod(Method method);
/**
 * <p>
 * Does ...
 * </p><p>
 * 
 * @return a Method with ...
 * </p>
 */
    public Method getMethod();

} // end MessageStruct







