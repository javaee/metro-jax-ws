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

import javax.xml.ws.spi.WebServiceFeatureAnnotation;
import javax.xml.bind.JAXBContext;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This feature instructs that the specified {@link JAXBContextFactory} be used for performing
 * data-binding for the SEI.
 *
 * <p>
 * For example,
 * <pre>
 * &#64;WebService
 * &#64;UsesJAXBContext(MyJAXBContextFactory.class)
 * public class HelloService {
 *   ...
 * }
 * </pre>
 *
 * <p>
 * If your {@link JAXBContextFactory} needs to carry some state from your calling application,
 * you can use {@link UsesJAXBContextFeature} to pass in an instance of {@link JAXBContextFactory},
 * instead of using this to specify the type.
 *
 * @author Kohsuke Kawaguchi
 * @since 2.1.5
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@WebServiceFeatureAnnotation(id=UsesJAXBContextFeature.ID,bean=UsesJAXBContextFeature.class)
public @interface UsesJAXBContext {
    /**
     * Designates the {@link JAXBContextFactory} to be used to create the {@link JAXBContext} object,
     * which in turn will be used by the JAX-WS runtime to marshal/unmarshal parameters and return
     * values to/from XML.
     */
    Class<? extends JAXBContextFactory> value();
}
