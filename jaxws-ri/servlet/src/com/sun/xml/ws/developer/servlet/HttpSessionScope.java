package com.sun.xml.ws.developer.servlet;

import com.sun.xml.ws.api.server.InstanceResolverAnnotation;
import com.sun.xml.ws.server.servlet.HttpSessionInstanceResolver;

import javax.jws.WebService;
import javax.servlet.http.HttpSession;
import javax.xml.ws.spi.WebServiceFeatureAnnotation;
import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Designates a service class that should be tied to {@link HttpSession} scope.
 *
 * <p>
 * When a service class is annotated with this annotation like the following,
 * the JAX-WS RI runtime will instanciate a new instance of the service class for
 * each {@link HttpSession}.
 *
 * <pre>
 * &#64;{@link WebService}
 * &#64;{@link HttpSessionScope}
 * class CounterService {
 *     protected int count = 0;
 *
 *     public CounterService() {}
 *
 *     public int inc() {
 *         return count++;
 *     }
 * }
 * </pre>
 *
 * <p>
 * This allows you to use instance fields for storing per-session state
 * (in the above example, it will create a separate counter for each client.)
 *
 * <p>
 * The service instance will be GCed when the corresponding {@link HttpSession}
 * is GCed. Refer to servlet documentation for how to configure the timeout behavior.
 *
 * @author Kohsuke Kawaguchi
 * @since JAX-WS 2.1
 */
@Retention(RUNTIME)
@Target(TYPE)
@Documented
@WebServiceFeatureAnnotation(id=HttpSessionScopeFeature.ID, bean=HttpSessionScopeFeature.class)
@InstanceResolverAnnotation(HttpSessionInstanceResolver.class)
public @interface HttpSessionScope {
}
