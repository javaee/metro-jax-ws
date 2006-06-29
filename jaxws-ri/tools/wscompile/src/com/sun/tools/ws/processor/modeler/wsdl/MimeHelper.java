/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */
package com.sun.tools.ws.processor.modeler.wsdl;

import java.util.HashMap;
import java.util.Map;

import com.sun.tools.ws.processor.modeler.JavaSimpleTypeCreator;

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
