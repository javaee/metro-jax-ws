package com.sun.xml.ws.api;import com.sun.istack.NotNull;
import com.sun.istack.Nullable;

import javax.xml.ws.WebServiceFeature;

/**
 * Read-only list of {@link WebServiceFeature}s.
 *
 * @author Kohsuke Kawaguchi
 */
public interface WSFeatureList extends Iterable<WebServiceFeature> {
    /**
     * Checks if a particular {@link WebServiceFeature} is enabled.
     *
     * @return
     *      true if enabled.
     */
    boolean isEnabled(@NotNull Class<? extends WebServiceFeature> feature);

    /**
     * Gets a {@link WebServiceFeature} of the specific type.
     *
     * @param featureType
     *      The type of the feature to retrieve.
     * @return
     *      If the feature is present and enabled, return a non-null instance.
     *      Otherwise null.
     */
    @Nullable <F extends WebServiceFeature> F get(@NotNull Class<F> featureType);

    /**
     * Obtains all the features in the array.
      */
    @NotNull WebServiceFeature[] toArray();
}
