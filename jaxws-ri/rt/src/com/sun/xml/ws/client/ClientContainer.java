package com.sun.xml.ws.client;

import com.sun.xml.ws.api.ResourceLoader;
import com.sun.xml.ws.api.server.Container;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Jitendra Kotamraju
 */
final class ClientContainer extends Container {

    private final ResourceLoader loader = new ResourceLoader() {
        public URL getResource(String resource) throws MalformedURLException {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (cl == null) {
                cl = this.getClass().getClassLoader();
            }
            return cl.getResource("META-INF/"+resource);
        }
    };

    public <T> T getSPI(Class<T> spiType) {
        if (spiType == ResourceLoader.class) {
            return spiType.cast(loader);
        }
        return null;
    }

}

