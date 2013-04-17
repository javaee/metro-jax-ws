/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2006-2013 Oracle and/or its affiliates. All rights reserved.
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

package testutil;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.XMLConstants;

import com.sun.xml.ws.addressing.W3CAddressingConstants;
import com.sun.xml.ws.addressing.v200408.MemberSubmissionAddressingConstants;

/**
 * @author Arun Gupta
 */
public class NamespaceContextImpl implements NamespaceContext {
    public NamespaceContextImpl() {
        bindPrefixToNS("S11", WsaUtils.S11_NS);
        bindPrefixToNS("S12", WsaUtils.S12_NS);
        bindPrefixToNS("wsa", W3CAddressingConstants.WSA_NAMESPACE_NAME);
        bindPrefixToNS("wsa04", MemberSubmissionAddressingConstants.WSA_NAMESPACE_NAME);
        bindPrefixToNS("ck", "http://example.org/customer");
    }

    private static final Map<String, String> prefixToNSMap = new HashMap<String, String>();
    private static final Map<String, String> nsToPrefixMap = new HashMap<String, String>();

    public void bindPrefixToNS(String prefix, String namespaceURI) {
        prefixToNSMap.put(prefix, namespaceURI);
        nsToPrefixMap.put(namespaceURI, prefix);
    }

    public String getNamespaceURI(String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException(
                    "NamespaceContextImpl#getNamespaceURI(String prefix) with prefix == null");
        }

        // constants
        if (prefix.equals(XMLConstants.XML_NS_PREFIX)) {
            return XMLConstants.XML_NS_URI;
        }
        if (prefix.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
            return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
        }

        // default
        if (prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
            if (prefixToNSMap.containsKey(prefix)) {
                return prefixToNSMap.get(prefix);
            } else {
                return XMLConstants.NULL_NS_URI;
            }
        }

        // bound
        if (prefixToNSMap.containsKey(prefix)) {
            return prefixToNSMap.get(prefix);
        }

        // unbound
        return XMLConstants.NULL_NS_URI;
    }

    public String getPrefix(String namespaceURI) {
        if (namespaceURI == null) {
            throw new IllegalArgumentException(
                    "NamespaceContextImpl#getPrefix(String namespaceURI) with namespaceURI == null");
        }

        // constants
        if (namespaceURI.equals(XMLConstants.XML_NS_URI)) {
            return XMLConstants.XML_NS_PREFIX;
        }
        if (namespaceURI.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI)) {
            return XMLConstants.XMLNS_ATTRIBUTE;
        }

        // bound
        if (nsToPrefixMap.containsKey(namespaceURI)) {
            return nsToPrefixMap.get(namespaceURI);
        }

        // mimic "default Namespace URI"
        if (namespaceURI.equals(XMLConstants.NULL_NS_URI)) {
            return XMLConstants.DEFAULT_NS_PREFIX;
        }

        // unbound
        return null;
    }

    public Iterator getPrefixes(String namespaceURI) {
        return prefixToNSMap.keySet().iterator();
    }
}
