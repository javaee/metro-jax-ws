package com.sun.xml.ws.client;

import com.sun.xml.ws.util.exception.JAXWSExceptionBase;

/**
 * @author Vivek Pandey
 */
public class ClientConfigurationException extends JAXWSExceptionBase {
    public ClientConfigurationException(String key, Object... args) {
        super(key, args);
    }

    public ClientConfigurationException(Throwable throwable) {
        super(throwable);
    }

    public String getResourceBundleName() {
        return "com.sun.xml.ws.resources.client";
    }
}
