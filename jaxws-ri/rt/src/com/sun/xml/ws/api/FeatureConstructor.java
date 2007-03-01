package com.sun.xml.ws.api;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <p>
 * This annotation should be used on a constructor of classes extending {@link javax.xml.ws.WebServiceFeature} other than
 * Spec defined features, to help JAX-WS runtime recognize feature extensions.
 * </p>
 * <p>
 * For WebServiceFeature annotations to be recognizable by JAX-WS runtime, the feature annotation should point
 * to a corresponding bean (class extending WebServiceFeature). Only one of the constructors in the bean MUST be marked
 * with @FeatureConstructor whose value captures the annotaion attribute names for the corresponding parameters.
 * </p>
 * For example,
 * @see com.sun.xml.ws.developer.MemberSubmissionAddressingFeature
 * @see com.sun.xml.ws.developer.MemberSubmissionAddressing
 *
 * @see com.sun.xml.ws.developer.Stateful
 * @see com.sun.xml.ws.developer.StatefulFeature
 *
 * @author Rama Pulavarthi
 */
@Retention(RUNTIME)
@Target(ElementType.CONSTRUCTOR)

public @interface FeatureConstructor {
    /**
     * The name of the parameter.
     */
    String[] value() default {};
}
