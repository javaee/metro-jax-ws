package com.sun.xml.ws.api;

import com.sun.xml.ws.binding.WebServiceFeatureList;

import javax.xml.ws.WebServiceFeature;
import java.lang.annotation.Annotation;

/**
 * Factory methods to get web service features from the corresponding
 * feature annotations
 *
 * @author Jitendra Kotamraju
 */
public class WebServiceFeatureFactory {

    /**
     * Returns a feature list for feature annotations(i.e which have
     * {@link javax.xml.ws.spi.WebServiceFeatureAnnotation} meta annotation)
     *
     * @param ann list of annotations(that can also have non-feature annotations)
     * @return non-null feature list object
     */
    public static WSFeatureList getWSFeatureList(Iterable<Annotation> ann) {
        WebServiceFeatureList list = new WebServiceFeatureList();
        list.parseAnnotations(ann);
        return list;
    }

    /**
     * Returns a corresponding feature for a feature annotation(i.e which has
     * {@link javax.xml.ws.spi.WebServiceFeatureAnnotation} meta annotation)
     *
     * @param ann any annotation, not required to be a feature annotation
     * @return corresponding feature for the annotation
     *         null, if the annotation is not a feature annotation
     */
    public static WebServiceFeature getWebServiceFeature(Annotation ann) {
        return WebServiceFeatureList.getFeature(ann);
    }

}
