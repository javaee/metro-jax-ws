package com.sun.xml.ws.api.server;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;

/**
 * Interface that allows components around {@link WSEndpoint} to hook up
 * with each other.
 *
 * @author Kohsuke Kawaguchi
 * @since 2.1.2
 * @see WSEndpoint#getComponentRegistry()
 */
public interface EndpointComponent {
    /**
     * Gets the specified SPI.
     *
     * <p>
     * This method works as a kind of directory service
     * for SPIs, allowing various components to define private contract
     * and talk to each other.
     *
     * @return
     *      null if such an SPI is not provided by this object.
     */
    @Nullable <T> T getSPI(@NotNull Class<T> spiType);
}
