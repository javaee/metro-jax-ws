/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.xml.ws.client.sei;

import java.util.List;

import javax.jws.soap.SOAPBinding.Style;

import com.sun.xml.ws.api.message.MessageContextFactory;
import com.sun.xml.ws.model.JavaMethodImpl;
import com.sun.xml.ws.model.ParameterImpl;
import com.sun.xml.ws.model.WrapperParameter;

public class StubAsyncHandler extends StubHandler {

    private final Class asyncBeanClass;

	public StubAsyncHandler(JavaMethodImpl jm, JavaMethodImpl sync, MessageContextFactory mcf) {
        super(sync, mcf);

        List<ParameterImpl> rp = sync.getResponseParameters();
        int size = 0;
        for( ParameterImpl param : rp ) {
            if (param.isWrapperStyle()) {
                WrapperParameter wrapParam = (WrapperParameter)param;
                size += wrapParam.getWrapperChildren().size();
                if (sync.getBinding().getStyle() == Style.DOCUMENT) {
                    // doc/asyncBeanClass - asyncBeanClass bean is in async signature
                    // Add 2 or more so that it is considered as async bean case
                    size += 2;
                }
            } else {
                ++size;
            }
        }

        Class tempWrap = null;
        if (size > 1) {
            rp = jm.getResponseParameters();
            for(ParameterImpl param : rp) {
                if (param.isWrapperStyle()) {
                    WrapperParameter wrapParam = (WrapperParameter)param;
                    if (sync.getBinding().getStyle() == Style.DOCUMENT) {
                        // doc/asyncBeanClass style
                        tempWrap = (Class)wrapParam.getTypeInfo().type;
                        break;
                    }
                    for(ParameterImpl p : wrapParam.getWrapperChildren()) {
                        if (p.getIndex() == -1) {
                            tempWrap = (Class)p.getTypeInfo().type;
                            break;
                        }
                    }
                    if (tempWrap != null) {
                        break;
                    }
                } else {
                    if (param.getIndex() == -1) {
                        tempWrap = (Class)param.getTypeInfo().type;
                        break;
                    }
                }
            }
        }
        asyncBeanClass = tempWrap;
        
        switch(size) {
            case 0 :
                responseBuilder = buildResponseBuilder(sync, ValueSetterFactory.NONE);
                break;
            case 1 :
                responseBuilder = buildResponseBuilder(sync, ValueSetterFactory.SINGLE);
                break;
            default :
                responseBuilder = buildResponseBuilder(sync, new ValueSetterFactory.AsyncBeanValueSetterFactory(asyncBeanClass));
        }
       
    }
	
	protected void initArgs(Object[] args) throws Exception {
        if (asyncBeanClass != null) {
            args[0] = asyncBeanClass.newInstance();
        }
	}

    ValueGetterFactory getValueGetterFactory() {
        return ValueGetterFactory.ASYNC;
    }
}
