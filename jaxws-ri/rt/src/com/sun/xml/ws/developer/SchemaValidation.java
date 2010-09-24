/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.xml.ws.developer;

import com.sun.xml.ws.server.DraconianValidationErrorHandler;

import javax.jws.WebService;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import javax.xml.ws.spi.WebServiceFeatureAnnotation;
import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * Validates all request and response messages payload(SOAP:Body) for a {@link WebService}
 * against the XML schema. To use this feature, annotate the endpoint class with
 * this annotation.
 *
 * <pre>
 * for e.g.:
 *
 * &#64;WebService
 * &#64;SchemaValidation
 * public class HelloImpl {
 *   ...
 * }
 * </pre>
 *
 * At present, schema validation works for doc/lit web services only.
 *
 * @since JAX-WS 2.1.3
 * @author Jitendra Kotamraju
 * @see SchemaValidationFeature
 */
@Retention(RUNTIME)
@Target({TYPE, ElementType.METHOD, ElementType.FIELD})
@Documented
@WebServiceFeatureAnnotation(id = SchemaValidationFeature.ID, bean = SchemaValidationFeature.class)
public @interface SchemaValidation {

    /**
     * Configure the validation behaviour w.r.t error handling. The default handler
     * just rejects any invalid schema intances. If the application want to change
     * this default behaviour(say just log the errors), it can do so by providing
     * a custom implementation of {@link ValidationErrorHandler}.
     */
    Class<? extends ValidationErrorHandler> handler() default DraconianValidationErrorHandler.class;

    /**
     * Turns validation on/off for inbound messages
     *
     * @since JAX-WS RI 2.2.2
     */
    boolean inbound() default true;


    /**
     * Turns validation on/off for outbound messages
     * 
     * @since JAX-WS RI 2.2.2
     */
    boolean outbound() default true;

    /**
     * Does validation for bound headers in a SOAP message.
     *
    boolean headers() default false;
     */

    /**
     * Additional schema documents that are used to create {@link Schema} object. Useful
     * when the application adds additional SOAP headers to the message. This is a list
     * of system-ids, that are used to create {@link Source} objects and used in creation
     * of {@link Schema} object
     *
     * for e.g.:
     * @SchemaValidation(schemaLocations={"http://bar.foo/b.xsd", "http://foo.bar/a.xsd"}
     *
    String[] schemaLocations() default {};
     */

}
