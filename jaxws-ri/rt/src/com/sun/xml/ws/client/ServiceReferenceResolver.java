/*
 * $Id: ServiceReferenceResolver.java,v 1.5 2005-09-10 19:47:27 kohsuke Exp $
 */

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

/**
 *
 * @author WS Development Team
 */
package com.sun.xml.ws.client;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.naming.spi.ObjectFactory;
import javax.xml.ws.Service;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class ServiceReferenceResolver implements ObjectFactory {
    protected static final Map registeredServices =
        Collections.synchronizedMap(new HashMap());

    public Object getObjectInstance(Object obj, Name name,
                                    Context nameCtx, Hashtable<?, ?> environment) throws Exception {

        if (obj instanceof StringRefAddr) {
            StringRefAddr ref = (StringRefAddr) obj;
            if (ref.getType() == "ServiceName") {
                return registeredServices.get(ref.getContent());
            } else if (ref.getType() == "ServiceClassName") {
                Object serviceKey = ref.getContent();
                Object service = registeredServices.get(serviceKey);
                if (service == null) {
                    ClassLoader ctxLoader =
                        Thread.currentThread().getContextClassLoader();
                    service = Class.forName((String) ref.getContent(),
                        true, ctxLoader).newInstance();
                    registeredServices.put(serviceKey, service);
                }
                return service;
            }
        }
        return null;
    }

    public static String registerService(Service service) {
        String serviceName = getQualifiedServiceNameString(service);
        registeredServices.put(serviceName, service);
        return serviceName;
    }

    protected static String getQualifiedServiceNameString(Service service) {
        String serviceName = "";
        URL wsdlLocation = service.getWSDLDocumentLocation();
        if (wsdlLocation != null) {
            serviceName += wsdlLocation.toExternalForm() + ":";
        }
        serviceName += service.getServiceName().toString();
        return serviceName;
    }

    public Reference getServiceClassReference(Class serviceClass) {
        return getServiceClassReference(serviceClass.getName());
    }

    public Reference getServiceClassReference(String serviceClassName) {
        Reference reference = new Reference(serviceClassName,
            "com.sun.xml.ws.naming.ServiceReferenceResolver", null);
        reference.add(new StringRefAddr("ServiceClassName", serviceClassName));
        return reference;
    }
}
