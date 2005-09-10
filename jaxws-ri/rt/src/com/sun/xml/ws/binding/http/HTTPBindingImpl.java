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
package com.sun.xml.ws.binding.http;

import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.http.HTTPBinding;

import java.util.List;

import com.sun.xml.ws.binding.BindingImpl;
import com.sun.xml.ws.util.localization.Localizable;
import com.sun.xml.ws.util.localization.LocalizableMessageFactory;
import com.sun.xml.ws.util.localization.Localizer;

/**
 * @author WS Development Team
 */
public class HTTPBindingImpl extends BindingImpl implements HTTPBinding {

    public HTTPBindingImpl() {
        super(HTTPBinding.HTTP_BINDING);
    }

    public HTTPBindingImpl(List<Handler> handlerChain) {
        super(handlerChain, HTTPBinding.HTTP_BINDING);
    }

    /*
     * Sets the handler chain. Only logical handlers are
     * allowed with HTTPBinding.
     */
    @Override
    public void setHandlerChain(List<Handler> chain) {
        for (Handler handler : chain) {
            if (!(handler instanceof LogicalHandler)) {
                LocalizableMessageFactory messageFactory =
                    new LocalizableMessageFactory(
                    "com.sun.xml.ws.resources.client");
                Localizer localizer = new Localizer();
                Localizable locMessage =
                    messageFactory.getMessage("non.logical.handler.set",
                    handler.getClass().toString());
                throw new WebServiceException(localizer.localize(locMessage));
            }
        }
        super.setHandlerChain(chain);
    }
    
}
