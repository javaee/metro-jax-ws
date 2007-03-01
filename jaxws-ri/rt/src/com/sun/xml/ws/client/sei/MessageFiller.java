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

package com.sun.xml.ws.client.sei;

import com.sun.xml.bind.api.Bridge;
import com.sun.xml.ws.api.message.Attachment;
import com.sun.xml.ws.api.message.Headers;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.message.ByteArrayAttachment;
import com.sun.xml.ws.message.DataHandlerAttachment;
import com.sun.xml.ws.message.JAXBAttachment;
import com.sun.xml.ws.model.ParameterImpl;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.UUID;
import javax.activation.DataHandler;
import javax.xml.transform.Source;
import javax.xml.ws.WebServiceException;

/**
 * Puts a non-payload message parameter to {@link Message}.
 *
 * <p>
 * Instance of this class is used to handle header parameters and attachment parameters.
 * They add things to {@link Message}.
 *
 * @see BodyBuilder
 * @author Kohsuke Kawaguchi
 * @author Jitendra Kotamraju
 */
abstract class MessageFiller {

    /**
     * The index of the method invocation parameters that this object looks for.
     */
    protected final int methodPos;

    protected MessageFiller( int methodPos) {
        this.methodPos = methodPos;
    }

    /**
     * Moves an argument of a method invocation into a {@link Message}.
     */
    abstract void fillIn(Object[] methodArgs, Message msg);
    
    /**
     * Adds a parameter as an MIME attachment to {@link Message}.
     */
    static abstract class AttachmentFiller extends MessageFiller {
        protected final ParameterImpl param;
        protected final ValueGetter getter;
        protected final String mimeType;
        private final String contentIdPart;
        
        protected AttachmentFiller(ParameterImpl param, ValueGetter getter) {
            super(param.getIndex());
            this.param = param;
            this.getter = getter;
            mimeType = param.getBinding().getMimeType();
            try {
                contentIdPart = URLEncoder.encode(param.getPartName(), "UTF-8")+'=';
            } catch (UnsupportedEncodingException e) {
                throw new WebServiceException(e);
            }
        }
        
        /**
         * Creates an MessageFiller based on the parameter type
         *
         * @param param
         *      runtime Parameter that abstracts the annotated java parameter
         * @param getter
         *      Gets a value from an object that represents a parameter passed
         *      as a method argument.
         */
        public static MessageFiller createAttachmentFiller(ParameterImpl param, ValueGetter getter) {
            Class type = (Class)param.getTypeReference().type;
            if (DataHandler.class.isAssignableFrom(type) || Source.class.isAssignableFrom(type)) {
                return new DataHandlerFiller(param, getter);
            } else if (byte[].class==type) {
                return new ByteArrayFiller(param, getter);
            } else if(isXMLMimeType(param.getBinding().getMimeType())) {
                return new JAXBFiller(param, getter);
            } else {
                return new DataHandlerFiller(param, getter);
            }
        }
        
        String getContentId() {
            return contentIdPart+UUID.randomUUID()+"@jaxws.sun.com";
        }
    }
    
    private static class ByteArrayFiller extends AttachmentFiller {
        protected ByteArrayFiller(ParameterImpl param, ValueGetter getter) {
            super(param, getter);
        }
        void fillIn(Object[] methodArgs, Message msg) {
            String contentId = getContentId();
            Object obj = getter.get(methodArgs[methodPos]);
            Attachment att = new ByteArrayAttachment(contentId,(byte[])obj,mimeType);
            msg.getAttachments().add(att);
        }
    }
    
    private static class DataHandlerFiller extends AttachmentFiller {
        protected DataHandlerFiller(ParameterImpl param, ValueGetter getter) {
            super(param, getter);
        }
        void fillIn(Object[] methodArgs, Message msg) {
            String contentId = getContentId();
            Object obj = getter.get(methodArgs[methodPos]);
            DataHandler dh = (obj instanceof DataHandler) ? (DataHandler)obj : new DataHandler(obj,mimeType);
            Attachment att = new DataHandlerAttachment(contentId, dh);
            msg.getAttachments().add(att);
        }
    }
    
    private static class JAXBFiller extends AttachmentFiller {
        protected JAXBFiller(ParameterImpl param, ValueGetter getter) {
            super(param, getter);
        }
        void fillIn(Object[] methodArgs, Message msg) {
            String contentId = getContentId();
            Object obj = getter.get(methodArgs[methodPos]);
            Attachment att = new JAXBAttachment(contentId, obj, param.getBridge(), mimeType);
            msg.getAttachments().add(att);
        }
    }

    /**
     * Adds a parameter as an header.
     */
    static final class Header extends MessageFiller {
        private final Bridge bridge;
        private final ValueGetter getter;

        protected Header(int methodPos, Bridge bridge, ValueGetter getter) {
            super(methodPos);
            this.bridge = bridge;
            this.getter = getter;
        }

        void fillIn(Object[] methodArgs, Message msg) {
            Object value = getter.get(methodArgs[methodPos]);
            msg.getHeaders().add(Headers.create(bridge,value));
        }
    }
    
    private static boolean isXMLMimeType(String mimeType){
        return (mimeType.equals("text/xml") || mimeType.equals("application/xml")) ? true : false;
    }

}
