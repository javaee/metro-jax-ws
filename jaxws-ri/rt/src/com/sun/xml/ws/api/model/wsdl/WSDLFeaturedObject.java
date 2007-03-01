package com.sun.xml.ws.api.model.wsdl;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.api.WSFeatureList;
import com.sun.xml.ws.api.wsdl.parser.WSDLParserExtension;

import javax.xml.ws.Dispatch;
import javax.xml.ws.WebServiceFeature;

/**
 * {@link WSDLObject} that can have features associated with it.
 *
 * <p>
 * {@link WSDLParserExtension}s can add features to this object,
 * which will then be incorporated when {@link Dispatch}s and
 * proxies are created for the port.
 *
 * @author Kohsuke Kawaguchi
 */
public interface WSDLFeaturedObject extends WSDLObject {

    @Nullable
    <F extends WebServiceFeature> F getFeature(@NotNull Class<F> featureType);

    /**
     * Gets the feature list associated with this object.
     */
    @NotNull WSFeatureList getFeatures();

    /**
     * Enables a {@link WebServiceFeature} based upon policy assertions on this port.
     * This method would be called during WSDL parsing by WS-Policy code.
     */
    void addFeature(@NotNull WebServiceFeature feature);
}
