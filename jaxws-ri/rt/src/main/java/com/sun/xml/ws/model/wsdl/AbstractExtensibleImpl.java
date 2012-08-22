/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package com.sun.xml.ws.model.wsdl;

import com.sun.xml.ws.api.model.wsdl.WSDLExtensible;
import com.sun.xml.ws.api.model.wsdl.WSDLExtension;
import com.sun.xml.ws.api.model.wsdl.WSDLObject;
import com.sun.xml.ws.resources.UtilMessages;
import com.sun.xml.ws.wsdl.parser.WSDLConstants;
import com.sun.istack.NotNull;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.Location;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.xml.sax.Locator;
import org.xml.sax.helpers.LocatorImpl;

/**
 * All the WSDL 1.1 elements that are extensible should subclass from this abstract implementation of
 * {@link WSDLExtensible} interface.
 *
 * @author Vivek Pandey
 * @author Kohsuke Kawaguchi
 */
abstract class AbstractExtensibleImpl extends AbstractObjectImpl implements WSDLExtensible {
    protected final Set<WSDLExtension> extensions = new HashSet<WSDLExtension>();
    // this captures any wsdl extensions that are not understood by WSDLExtensionParsers
    // and have wsdl:required=true
    protected List<UnknownWSDLExtension> notUnderstoodExtensions =
            new ArrayList<UnknownWSDLExtension>();

    protected AbstractExtensibleImpl(XMLStreamReader xsr) {
        super(xsr);
    }

    protected AbstractExtensibleImpl(String systemId, int lineNumber) {
        super(systemId, lineNumber);
    }

    public final Iterable<WSDLExtension> getExtensions() {
        return extensions;
    }

    public final <T extends WSDLExtension> Iterable<T> getExtensions(Class<T> type) {
        // TODO: this is a rather stupid implementation
        List<T> r = new ArrayList<T>(extensions.size());
        for (WSDLExtension e : extensions) {
            if(type.isInstance(e))
                r.add(type.cast(e));
        }
        return r;
    }

    public <T extends WSDLExtension> T getExtension(Class<T> type) {
        for (WSDLExtension e : extensions) {
            if(type.isInstance(e))
                return type.cast(e);
        }
        return null;
    }

    public void addExtension(WSDLExtension ex) {
        if(ex==null)
            // I don't trust plugins. So let's always check it, instead of making this an assertion
            throw new IllegalArgumentException();
        extensions.add(ex);
    }

    /**
     * This can be used if a WSDL extension element that has wsdl:required=true
     * is not understood
     * @param extnEl
     * @param locator
     */
    public void addNotUnderstoodExtension(QName extnEl, Locator locator) {
        notUnderstoodExtensions.add(new UnknownWSDLExtension(extnEl, locator));
    }

    protected static class UnknownWSDLExtension implements WSDLExtension, WSDLObject {
        private final QName extnEl;
        private final Locator locator;
        public UnknownWSDLExtension(QName extnEl, Locator locator) {
            this.extnEl = extnEl;
            this.locator = locator;
        }
        public QName getName() {
            return extnEl;
        }
        @NotNull public Locator getLocation() {
            return locator;
        }
        public String toString(){
           return extnEl + " "+ UtilMessages.UTIL_LOCATION( locator.getLineNumber(), locator.getSystemId());
       }
    }

    /**
     * This method should be called after freezing the WSDLModel
     * @return true if all wsdl required extensions on Port and Binding are understood
     */
    public boolean areRequiredExtensionsUnderstood() {
        if (notUnderstoodExtensions.size() != 0) {
            StringBuilder buf = new StringBuilder("Unknown WSDL extensibility elements:");
            for (UnknownWSDLExtension extn : notUnderstoodExtensions)
                buf.append('\n').append(extn.toString());
            throw new WebServiceException(buf.toString());
        }
        return true;
    }    
}
