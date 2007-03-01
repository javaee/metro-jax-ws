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

package com.sun.xml.ws.api;

import com.sun.istack.Nullable;
import com.sun.istack.NotNull;

import javax.xml.ws.WebServiceException;

/**
 * Extension point to plug in additional {@link BindingID} parsing logic.
 *
 * <p>
 * When the JAX-WS RI is asked to parse a binding ID string into a {@link BindingID}
 * object, it uses service idiom to look for the implementations of this class
 * in the <tt>META-INF/services/...</tt>.
 *
 * @since JAX-WS 2.0.next
 * @author Kohsuke Kawaguchi
 * @see BindingID#parse(String) 
 */
public abstract class BindingIDFactory {
    /**
     * Parses a binding ID string into {@link BindingID} if possible.
     *
     * @return
     *      a non-null return value would cause the JAX-WS RI to consider
     *      the parsing to be successful. No furhter {@link BindingIDFactory}
     *      will be consulted.
     *
     *      <p>
     *      Retruning a null value indicates that this factory doesn't understand
     *      this string, in which case the JAX-WS RI will keep asking next
     *      {@link BindingIDFactory}.
     *
     * @throws WebServiceException
     *      if the implementation understood the lexical value but it is not correct,
     *      this exception can be thrown to abort the parsing with error.
     *      No further {@link BindingIDFactory} will be consulted, and
     *      {@link BindingID#parse(String)} will throw the exception.
     */
    public abstract @Nullable BindingID parse(@NotNull String lexical) throws WebServiceException;
}
