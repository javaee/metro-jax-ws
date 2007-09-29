package com.sun.xml.ws.developer;

import com.sun.xml.ws.api.server.InstanceResolverAnnotation;
import com.sun.xml.ws.server.StatefulInstanceResolver;

import javax.xml.ws.spi.WebServiceFeatureAnnotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Designates a stateful {@link javax.jws.WebService}.
 *
 * <p>
 * A service class that has this feature on will behave as a stateful web service.
 * See {@link StatefulWebServiceManager} for more about stateful web service.
 *
 * @since 2.1
 * @author Jitendra Kotamraju
 * @ses StatefulWebServiceManager
 */
@Retention(RUNTIME)
@Target(TYPE)
@Documented
@WebServiceFeatureAnnotation(id = StatefulFeature.ID, bean = SchemaValidationFeature.class)
public @interface SchemaValidation {
}

