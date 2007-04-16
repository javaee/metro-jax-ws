package com.sun.xml.ws.transport.http.servlet;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.server.BoundEndpoint;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.api.server.Module;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides access to {@link ServletContext} via {@link Container}. Pipes
 * can get ServletContext from Container and use it to load some resources.
 */
class ServletContainer extends Container {
    private final ServletContext servletContext;

    private final Module module = new Module() {
        private final List<BoundEndpoint> endpoints = new ArrayList<BoundEndpoint>();

        public @NotNull
        List<BoundEndpoint> getBoundEndpoints() {
            return endpoints;
        }
    };

    ServletContainer(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public <T> T getSPI(Class<T> spiType) {
        if (spiType == ServletContext.class) {
            return (T)servletContext;
        }
        if (spiType == Module.class) {
            return spiType.cast(module);
        }
        return null;
    }
}
