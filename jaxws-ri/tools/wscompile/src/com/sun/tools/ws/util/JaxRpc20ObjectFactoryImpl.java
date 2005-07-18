/*
 * $Id: JaxRpc20ObjectFactoryImpl.java,v 1.2 2005-07-18 18:14:07 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.ws.util;

import java.io.OutputStream;

import com.sun.xml.ws.client.ClientTransportFactory;
import com.sun.xml.ws.transport.http.client.HttpClientTransportFactory;
import com.sun.xml.ws.spi.runtime.ClientTransportFactoryTypes;
import com.sun.xml.ws.wsdl.parser.WSDLParser;
import com.sun.tools.ws.wsdl.parser.WSDLUtil;
import com.sun.tools.ws.processor.util.ProcessorEnvironment;
import com.sun.tools.ws.processor.config.Configuration;

/**
 * Singleton factory class to instantiate concrete objects.
 *
 * @author WS Development Team
 */
public class JaxRpc20ObjectFactoryImpl {
//    extends com.sun.tools.ws.spi2.JaxRpcObjectFactory {
    
    public JaxRpc20ObjectFactoryImpl() {
    }
    
    
    public com.sun.xml.ws.spi.runtime.ClientTransportFactory
        createClientTransportFactory(int type, OutputStream outputStream) {
        
        ClientTransportFactory clientFactory = null;
        switch (type) {
            case ClientTransportFactoryTypes.HTTP :
                //return new HttpClientTransportFactory(outputStream);
                
            case ClientTransportFactoryTypes.LOCAL:         
                //return new LocalClientTransportFactory(null, outputStream);
        }
        return null;
    }
    
    public Configuration createConfiguration(
        ProcessorEnvironment processorEnvironment) {
        return null;
    }
    
    public com.sun.xml.ws.spi.runtime.SOAPMessageContext createSOAPMessageContext() {
        return null;
    }
    
    
    public WSDLUtil createWSDLUtil() {
        return null;
    }
    
    public WSDLParser createWSDLParser() {
        return null;
    }
    
}
