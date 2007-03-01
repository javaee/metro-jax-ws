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
package com.sun.xml.ws.api.model;

/**
 * Denotes the binding of a parameter.
 *
 * <p>
 * This is somewhat like an enumeration (but it is <b>NOT</b> an enumeration.)
 *
 * <p>
 * The possible values are
 * BODY, HEADER, UNBOUND, and ATTACHMENT. BODY, HEADER, and UNBOUND
 * has a singleton semantics, but there are multiple ATTACHMENT instances
 * as it carries additional MIME type parameter.
 *
 * <p>
 * So don't use '==' for testing the equality.
 */
public final class ParameterBinding {
    /**
     * Singleton instance that represents 'BODY'
     */
    public static final ParameterBinding BODY = new ParameterBinding(Kind.BODY,null);
    /**
     * Singleton instance that represents 'HEADER'
     */
    public static final ParameterBinding HEADER = new ParameterBinding(Kind.HEADER,null);
    /**
     * Singleton instance that represents 'UNBOUND',
     * meaning the parameter doesn't have a representation in a SOAP message.
     */
    public static final ParameterBinding UNBOUND = new ParameterBinding(Kind.UNBOUND,null);
    /**
     * Creates an instance that represents the attachment
     * with a given MIME type.
     *
     * <p>
     * TODO: shall we consider givint the singleton semantics by using
     * a cache? It's more elegant to do so, but
     * no where in JAX-WS RI two {@link ParameterBinding}s are compared today,
     */
    public static ParameterBinding createAttachment(String mimeType) {
        return new ParameterBinding(Kind.ATTACHMENT,mimeType);
    }

    /**
     * Represents 4 kinds of binding.
     */
    public static enum Kind {
        BODY, HEADER, UNBOUND, ATTACHMENT;
    }


    /**
     * Represents the kind of {@link ParameterBinding}.
     * Always non-null.
     */
    public final Kind kind;

    /**
     * Only used with attachment binding.
     */
    private String mimeType;

    private ParameterBinding(Kind kind,String mimeType) {
        this.kind = kind;
        this.mimeType = mimeType;
    }



    public String toString() {
        return kind.toString();
    }

    /**
     * Returns the MIME type associated with this binding.
     *
     * @throws IllegalStateException
     *      if this binding doesn't represent an attachment.
     *      IOW, if {@link #isAttachment()} returns false.
     * @return
     *      Can be null, if the MIME type is not known.
     */
    public String getMimeType() {
        if(!isAttachment())
            throw new IllegalStateException();
        return mimeType;
    }

    public boolean isBody(){
        return this==BODY;
    }

    public boolean isHeader(){
        return this==HEADER;
    }

    public boolean isUnbound(){
        return this==UNBOUND;
    }

    public boolean isAttachment(){
        return kind==Kind.ATTACHMENT;
    }
}
