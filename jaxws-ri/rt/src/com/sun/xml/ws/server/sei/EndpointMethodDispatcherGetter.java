/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

package com.sun.xml.ws.server.sei;

import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.model.AbstractSEIModelImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * Gets the list of {@link EndpointMethodDispatcher}s for {@link SEIInvokerTube}.
 * a request {@link Packet}. If WS-Addressing is enabled on the endpoint, then
 * only {@link ActionBasedDispatcher} is added to the list. Otherwise,
 * {@link PayloadQNameBasedDispatcher} is added to the list.
 *
 * <p>
 * {@link Message} payload's QName to obtain the handler. If no handler is
 * registered corresponding to that QName, then uses Action Message
 * Addressing Property value to get the handler. 
 *
 * @author Arun Gupta
 */
final class EndpointMethodDispatcherGetter {
    private final List<EndpointMethodDispatcher> dispatcherList;

    EndpointMethodDispatcherGetter(AbstractSEIModelImpl model, WSBinding binding, SEIInvokerTube invokerTube) {
        dispatcherList = new ArrayList<EndpointMethodDispatcher>();

        if (binding.getAddressingVersion() != null) {
            dispatcherList.add(new ActionBasedDispatcher(model, binding, invokerTube));
        }

        // even when action based dispatching is in place,
        // we still need this because clients are alowed not to use addressing headers
        dispatcherList.add(new PayloadQNameBasedDispatcher(model, binding, invokerTube));
        dispatcherList.add(new SOAPActionBasedDispatcher(model, binding, invokerTube));
    }

    List<EndpointMethodDispatcher> getDispatcherList() {
        return dispatcherList;
    }
}
