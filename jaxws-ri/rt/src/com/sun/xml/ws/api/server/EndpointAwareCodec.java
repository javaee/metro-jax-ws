package com.sun.xml.ws.api.server;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.pipe.Codec;

/**
 * Implemented by {@link Codec}s that want to have access to
 * {@link WSEndpoint} object.
 *
 * @author Kohsuke Kawaguchi
 * @since 2.1.1
 */
public interface EndpointAwareCodec extends Codec {
    /**
     * Called by the {@linK WSEndpoint} implementation
     * when the codec is associated with an endpoint.
     */
    void setEndpoint(@NotNull WSEndpoint endpoint);
}
