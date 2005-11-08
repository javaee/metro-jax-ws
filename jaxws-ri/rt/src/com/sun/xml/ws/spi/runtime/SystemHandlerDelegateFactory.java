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
package com.sun.xml.ws.spi.runtime;

import javax.xml.ws.WebServiceException;
import javax.xml.namespace.QName;
import static java.lang.Class.forName;
import static java.lang.Thread.currentThread;
import java.util.Map;
import java.util.HashMap;

public abstract class SystemHandlerDelegateFactory {
  
    private final static String DEFAULT_FACTORY_NAME =
        "com.sun.xml.xwss.SystemHandlerDelegateFactory";

    private static String factoryName;

    private static HashMap factoryMap;

    static {
        init();
    }

    private static synchronized void init() {
        factoryName = DEFAULT_FACTORY_NAME;
        factoryMap = new HashMap();
    };

    // foctory implementations that maintain a map of serviceName to
    // would override this method
    public SystemHandlerDelegate getDelegate(QName serviceName) {
        return create();
    }

    public abstract SystemHandlerDelegate create();

    public abstract boolean isEnabled(MessageContext messageContext);

    // factory name can be set to null, in which case,
    // the default factory will be disabled.
    public static synchronized void setFactoryName(String name) {
        factoryName = name;
    }

    public static synchronized String getFactoryName() {
        return factoryName;
    }

    public static synchronized SystemHandlerDelegateFactory getFactory() {

        SystemHandlerDelegateFactory factory =
            (SystemHandlerDelegateFactory) factoryMap.get(factoryName);

        if (factory != null || factoryMap.containsKey(factoryName)) {
            return factory;
        } else {

            Class clazz = null;
            try {
                ClassLoader loader = currentThread().getContextClassLoader();

                if (loader == null) {
                    clazz = forName(factoryName);
                } else {
                    clazz = loader.loadClass(factoryName);
                }

                if (clazz != null) {
                    factory = (SystemHandlerDelegateFactory) clazz.newInstance();
                }
            } catch (ClassNotFoundException e) {
                factory = null;
                // e.printStackTrace(); //todo:need to add log
            } catch (Exception x) {
                throw new WebServiceException(x);
            } finally {
                // stores null factory values in map to prevent
                // repeated class loading and instantiation errors.
                factoryMap.put(factoryName, factory);
            }
        }
        return factory;
    }
}



