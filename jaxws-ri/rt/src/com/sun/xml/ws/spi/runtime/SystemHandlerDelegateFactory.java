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
import static java.lang.Class.forName;
import static java.lang.Thread.currentThread;
import java.util.Map;

public abstract class SystemHandlerDelegateFactory {
    //this is currently hardcoded to xws SystemHandlerDelegate
    //package is com.sun.xml.xwss
    private static SystemHandlerDelegateFactory factory = null;
    private final static String SHD_FACTORY_NAME =
        "com.sun.xml.xwss.SystemHandlerDelegateFactory";

    public abstract SystemHandlerDelegate create();

    public abstract boolean isEnabled(MessageContext messageContext);

    public static SystemHandlerDelegateFactory getFactory() {
        if (factory == null) {
            ClassLoader classLoader = null;
            Class shdFactoryClass = null;
            try {
                classLoader = currentThread().getContextClassLoader();
            } catch (Exception x) {
                throw new WebServiceException(x);
            }

            try {
                if (classLoader == null) {
                    shdFactoryClass = forName(SHD_FACTORY_NAME);
                } else {
                    shdFactoryClass = classLoader.loadClass(SHD_FACTORY_NAME);
                }
                return (SystemHandlerDelegateFactory) shdFactoryClass.newInstance();
            } catch (ClassNotFoundException e) {
                //e.printStackTrace(); //todo:need to add log
            } catch (IllegalAccessException e) {
                throw new WebServiceException(e);
            } catch (InstantiationException e) {
                throw new WebServiceException(e);
            }
        }
        return factory;
    }

}
