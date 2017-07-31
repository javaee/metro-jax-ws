/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
