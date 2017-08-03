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

package fromjava.wsa.action_mapping.client;

import testutil.WsaUtils;

import javax.xml.namespace.NamespaceContext;
import javax.xml.XMLConstants;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.NoSuchElementException;

import com.sun.xml.ws.addressing.W3CAddressingConstants;
import com.sun.xml.ws.addressing.v200408.MemberSubmissionAddressingConstants;

/**
 * @author Rama Pulavarthi
 */

public class NSContextImpl implements NamespaceContext {
    public NSContextImpl() {
        addToContext("wsdl", "http://schemas.xmlsoap.org/wsdl/");
        addToContext("tns", "http://foobar.org/");
        addToContext("wsaw", "http://www.w3.org/2006/05/addressing/wsdl");
        addToContext("wsam", "http://www.w3.org/2007/05/addressing/metadata");
        addToContext("wsu", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");
        addToContext("wsp","http://www.w3.org/ns/ws-policy");
    }

    private static final Map<String, String> prefixToNSMap = new HashMap<String, String>();
    private static final Map<String, String> nsToPrefixMap = new HashMap<String, String>();

    public void addToContext(String prefix, String namespaceURI) {
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

    public Iterator getPrefixes(final String namespaceURI) {
       return new Iterator<String>() {
           String next = nsToPrefixMap.get(namespaceURI);
           public boolean hasNext() {
               return next != null;
           }

           public String next() {
               if(next != null) {
                   String tmp = next;
                   next = null;
                   return tmp;
               }
               throw new NoSuchElementException();
           }

           public void remove() {
               throw new UnsupportedOperationException();
           }
       };
    }
}
