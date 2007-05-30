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
package com.sun.tools.ws.processor.modeler.wsdl;

import com.sun.tools.ws.processor.modeler.JavaSimpleTypeCreator;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Vivek Pandey
 *
 */
public class MimeHelper {
    /**
     * @param mimePart
     * @return unique attachment ID
     */
    protected static String getAttachmentUniqueID(String mimePart) {
        //return "uuid@" + mimePart;
        return mimePart;
    }

    /**
     * @param mimeType
     * @return true if mimeType is a binary type
     */
    protected static boolean isMimeTypeBinary(String mimeType) {
        if (mimeType.equals(JPEG_IMAGE_MIME_TYPE)
            || mimeType.equals(GIF_IMAGE_MIME_TYPE)
        ) {
            return true;
        } else if (
            mimeType.equals(TEXT_XML_MIME_TYPE)
                || mimeType.equals(TEXT_HTML_MIME_TYPE)
                || mimeType.equals(TEXT_PLAIN_MIME_TYPE)
                || mimeType.equals(APPLICATION_XML_MIME_TYPE)
                || mimeType.equals(MULTIPART_MIME_TYPE)) {
            return false;
        }
        //some unknown mime type, will be mapped to DataHandler java type so
        // return true
        return true;
    }

    protected static void initMimeTypeToJavaType() {
        mimeTypeToJavaType.put(JPEG_IMAGE_MIME_TYPE, javaType.IMAGE_JAVATYPE);
        //mimeTypeToJavaType.put(PNG_IMAGE_MIME_TYPE, javaType.IMAGE_JAVATYPE);
        mimeTypeToJavaType.put(GIF_IMAGE_MIME_TYPE,
         javaType.IMAGE_JAVATYPE);
        mimeTypeToJavaType.put(TEXT_XML_MIME_TYPE, javaType.SOURCE_JAVATYPE);
        //mimeTypeToJavaType.put(TEXT_HTML_MIME_TYPE, javaType.SOURCE_JAVATYPE);
        mimeTypeToJavaType.put(
            APPLICATION_XML_MIME_TYPE,
            javaType.SOURCE_JAVATYPE);
        mimeTypeToJavaType.put(TEXT_PLAIN_MIME_TYPE, javaType.STRING_JAVATYPE);
        mimeTypeToJavaType.put(
            MULTIPART_MIME_TYPE,
            javaType.MIME_MULTIPART_JAVATYPE);

    }

    protected static Map mimeTypeToJavaType;
    protected static JavaSimpleTypeCreator javaType;

    public static final String JPEG_IMAGE_MIME_TYPE = "image/jpeg";
    //public static final String PNG_IMAGE_MIME_TYPE = "image/png";
    public static final String GIF_IMAGE_MIME_TYPE = "image/gif";
    public static final String TEXT_XML_MIME_TYPE = "text/xml";
    public static final String TEXT_HTML_MIME_TYPE = "text/html";
    public static final String TEXT_PLAIN_MIME_TYPE = "text/plain";
    public static final String APPLICATION_XML_MIME_TYPE = "application/xml";
    public static final String MULTIPART_MIME_TYPE = "multipart/*";

    /**
     *
     */
    public MimeHelper() {
        mimeTypeToJavaType = new HashMap();
        javaType = new JavaSimpleTypeCreator();
        initMimeTypeToJavaType();
    }

}
