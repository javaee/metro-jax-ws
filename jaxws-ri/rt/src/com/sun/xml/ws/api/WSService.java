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

package com.sun.xml.ws.api;

import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.client.WSServiceDelegate;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.ws.Dispatch;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.spi.ServiceDelegate;
import java.lang.reflect.Field;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * JAX-WS implementation of {@link ServiceDelegate}.
 *
 * <p>
 * This abstract class is used only to improve the static type safety
 * of the JAX-WS internal API.
 *
 * <p>
 * The class name intentionally doesn't include "Delegate",
 * because the fact that it's a delegate is a detail of
 * the JSR-224 API, and for the layers above us this object
 * nevertheless represents {@link Service}. We want them
 * to think of this as an internal representation of a service.
 *
 * <p>
 * Only JAX-WS internal code may downcast this to {@link WSServiceDelegate}.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class WSService extends ServiceDelegate {
    protected WSService() {
    }

    /**
     * Works like {@link #getPort(EndpointReference, Class, WebServiceFeature...)}
     * but takes {@link WSEndpointReference}. 
     */
    public abstract <T> T getPort(WSEndpointReference epr, Class<T> portInterface, WebServiceFeature... features);

    /**
     * Works like {@link #createDispatch(EndpointReference, Class, Mode, WebServiceFeature[])}
     * but it takes the port name separately, so that EPR without embedded metadata can be used.
     */
    public abstract <T> Dispatch<T> createDispatch(QName portName, WSEndpointReference wsepr, Class<T> aClass, Service.Mode mode, WebServiceFeature... features);

    /**
     * Works like {@link #createDispatch(EndpointReference, JAXBContext, Mode, WebServiceFeature[])}
     * but it takes the port name separately, so that EPR without embedded metadata can be used.
     */
    public abstract Dispatch<Object> createDispatch(QName portName, WSEndpointReference wsepr, JAXBContext jaxbContext, Service.Mode mode, WebServiceFeature... features);

    /**
     * Create a <code>Service</code> instance.
     *
     * The specified WSDL document location and service qualified name MUST
     * uniquely identify a <code>wsdl:service</code> element.
     *
     * @param wsdlDocumentLocation URL for the WSDL document location
     *                             for the service
     * @param serviceName QName for the service
     * @throws WebServiceException If any error in creation of the
     *                    specified service.
     **/
    public static WSService create( URL wsdlDocumentLocation, QName serviceName) {
        return new WSServiceDelegate(wsdlDocumentLocation,serviceName,Service.class);
    }

    /**
     * Create a <code>Service</code> instance.
     *
     * @param serviceName QName for the service
     * @throws WebServiceException If any error in creation of the
     *                    specified service
     */
    public static WSService create(QName serviceName) {
        return create(null,serviceName);
    }

    /**
     * Creates a service with a dummy service name.
     */
    public static WSService create() {
        return create(null,new QName(WSService.class.getName(),"dummy"));
    }

    /**
     * Obtains the {@link WSService} that's encapsulated inside a {@link Service}.
     *
     * @throws IllegalArgumentException
     *      if the given service object is not from the JAX-WS RI.
     */
    public static WSService unwrap(final Service svc) {
        return AccessController.doPrivileged(new PrivilegedAction<WSService>() {
            public WSService run() {
                try {
                    Field f = svc.getClass().getField("delegate");
                    f.setAccessible(true);
                    Object delegate = f.get(svc);
                    if(!(delegate instanceof WSService))
                        throw new IllegalArgumentException();
                    return (WSService) delegate;
                } catch (NoSuchFieldException e) {
                    AssertionError x = new AssertionError("Unexpected service API implementation");
                    x.initCause(e);
                    throw x;
                } catch (IllegalAccessException e) {
                    IllegalAccessError x = new IllegalAccessError(e.getMessage());
                    x.initCause(e);
                    throw x;
                }
            }
        });
    }
}
