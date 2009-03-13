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
import java.lang.annotation.*;
import java.io.File;

/**
 * This feature represents the use of StreamingAttachment attachments with a
 * web service.
 *
 * <p>
 * for e.g.: To keep all MIME attachments in memory, do the following
 *
 * <pre>
 * &#64;WebService
 * &#64;MIME(memoryThreshold=-1L)
 * public class HelloService {
 * }
 * </pre>
 *
 * @see StreamingAttachmentFeature
 *
 * @author Jitendra Kotamraju
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@WebServiceFeatureAnnotation(id = StreamingAttachmentFeature.ID, bean = StreamingAttachmentFeature.class)
public @interface StreamingAttachment {

    /**
     * Directory in which large attachments are stored. {@link File#createTempFile}
     * methods are used to create temp files for storing attachments. This
     * value is used in {@link File#createTempFile}, if specified. If a file
     * cannot be created in this dir, then all the content is kept in memory.
     */
    String dir() default "";

    /**
     * MIME message is parsed eagerly.
     */
    boolean parseEagerly() default false;

    /**
     * After this threshold(no of bytes per attachment), large attachment is
     * written to file system.
     *
     * If the value is -1, then all the attachment content is kept in memory.
     */
    long memoryThreshold() default 1048576L;

}
