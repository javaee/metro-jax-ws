/*
 * $Id: XMLWriterFactory.java,v 1.1 2005-05-23 22:59:38 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.streaming;

import java.io.OutputStream;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.xml.parsers.FactoryConfigurationError;

/**
 * <p> Define a factory API to enable pluggable XMLWriter implementations. </p>
 *
 * @see XMLWriter
 *
 * @author JAX-RPC Development Team
 */
public abstract class XMLWriterFactory {

    protected XMLWriterFactory() {
    }

    /**
     * Obtain an instance of a factory.
     * Since factories are stateless, only one copy of a factory exists and is
     * returned to the application each time this method is called.
     *
     * The implementation class to be used can be overridden by setting the
     * com.sun.xml.ws.streaming.XMLWriterFactory system property.
     *
     */
    public static XMLWriterFactory newInstance() {
        if (_instance == null) {
            String factoryImplName = getFactoryImplName();
            XMLWriterFactory factoryImpl;
            try {
                Class clazz = Class.forName(factoryImplName);
                _instance = (XMLWriterFactory) clazz.newInstance();
            } catch (ClassNotFoundException e) {
                throw new FactoryConfigurationError(e);
            } catch (IllegalAccessException e) {
                throw new FactoryConfigurationError(e);
            } catch (InstantiationException e) {
                throw new FactoryConfigurationError(e);
            }
        }
        return _instance;
    }

    private static String getFactoryImplName() {
        String factoryImplName;
        try {
            factoryImplName =
                (String) AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    // AccessControll problem
                    return System.getProperty(
                        "com.sun.xml.ws.streaming.XMLWriterFactory",
                        "com.sun.xml.ws.streaming.XMLWriterFactoryImpl");
                }
            });
        } catch (AccessControlException e) {
            factoryImplName = "com.sun.xml.ws.streaming.XMLWriterFactoryImpl";
        }
        return factoryImplName;
    }

    /**
     * Obtain an XMLWriter on the given OutputStream using the default encoding
     * and XML declaration settings.
     *
     */
    public abstract XMLWriter createXMLWriter(OutputStream stream);

    /**
     * Obtain an XMLWriter on the given OutputStream using the given encoding
     * and the default XML declaration settings.
     *
     */
    public abstract XMLWriter createXMLWriter(
        OutputStream stream,
        String encoding);

    /**
     * Obtain an XMLWriter on the given OutputStream using the given encoding
     * and the given XML declaration settings.
     *
     */
    public abstract XMLWriter createXMLWriter(
        OutputStream stream,
        String encoding,
        boolean declare);

    private static XMLWriterFactory _instance;
}
