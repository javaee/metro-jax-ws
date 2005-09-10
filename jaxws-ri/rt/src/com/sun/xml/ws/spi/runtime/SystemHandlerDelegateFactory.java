/*
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All Rights Reserved.
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
