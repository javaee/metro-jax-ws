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

package com.sun.tools.ws.wsdl.parser;

import com.sun.tools.ws.wsdl.framework.ParseException;
import com.sun.xml.ws.util.xml.XmlUtil;
import org.w3c.dom.Comment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import javax.xml.namespace.QName;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

/**2
 * Defines various utility methods.
 *
 * @author WS Development Team
 */
public class Util {

    public static String getRequiredAttribute(Element element, String name) {
        String result = XmlUtil.getAttributeOrNull(element, name);
        if (result == null)
            fail(
                "parsing.missingRequiredAttribute",
                element.getTagName(),
                name);
        return result;
    }

    public static void verifyTag(Element element, String tag) {
        if (!element.getLocalName().equals(tag))
            fail("parsing.invalidTag", element.getTagName(), tag);
    }

    public static void verifyTagNS(Element element, String tag, String nsURI) {
        if (!element.getLocalName().equals(tag)
            || (element.getNamespaceURI() != null
                && !element.getNamespaceURI().equals(nsURI)))
            fail(
                "parsing.invalidTagNS",
                new Object[] {
                    element.getTagName(),
                    element.getNamespaceURI(),
                    tag,
                    nsURI });
    }

    public static void verifyTagNS(Element element, QName name) {
        if (!isTagName(element, name))
            fail(
                "parsing.invalidTagNS",
                new Object[] {
                    element.getTagName(),
                    element.getNamespaceURI(),
                    name.getLocalPart(),
                    name.getNamespaceURI()});
    }

    public static boolean isTagName(Element element, QName name){
        return (element.getLocalName().equals(name.getLocalPart())
            && (element.getNamespaceURI() != null
                && element.getNamespaceURI().equals(name.getNamespaceURI())));

    }

    public static void verifyTagNSRootElement(Element element, QName name) {
        if (!element.getLocalName().equals(name.getLocalPart())
            || (element.getNamespaceURI() != null
                && !element.getNamespaceURI().equals(name.getNamespaceURI())))
            fail(
                "parsing.incorrectRootElement",
                new Object[] {
                    element.getTagName(),
                    element.getNamespaceURI(),
                    name.getLocalPart(),
                    name.getNamespaceURI()});
    }

    public static Element nextElementIgnoringCharacterContent(Iterator iter) {
        while (iter.hasNext()) {
            Node n = (Node) iter.next();
            if (n instanceof Text)
                continue;
            if (n instanceof Comment)
                continue;
            if (!(n instanceof Element))
                fail("parsing.elementExpected");
            return (Element) n;
        }

        return null;
    }

    public static Element nextElement(Iterator iter) {
        while (iter.hasNext()) {
            Node n = (Node) iter.next();
            if (n instanceof Text) {
                Text t = (Text) n;
                if (t.getData().trim().length() == 0)
                    continue;
                fail("parsing.nonWhitespaceTextFound", t.getData().trim());
            }
            if (n instanceof Comment)
                continue;
            if (!(n instanceof Element))
                fail("parsing.elementExpected");
            return (Element) n;
        }

        return null;
    }

    public static String processSystemIdWithBase(
        String baseSystemId,
        String systemId) {
        try {
            URL base = null;
            try {
                base = new URL(baseSystemId);
            } catch (MalformedURLException e) {
                base = new File(baseSystemId).toURL();
            }

            try {
                URL url = new URL(base, systemId);
                return url.toString();
            } catch (MalformedURLException e) {
                fail("parsing.invalidURI", systemId);
            }

        } catch (MalformedURLException e) {
            fail("parsing.invalidURI", baseSystemId);
        }

        return null; // keep compiler happy
    }

    public static void fail(String key) {
        throw new ParseException(key);
    }

    public static void fail(String key, String arg) {
        throw new ParseException(key, arg);
    }

    public static void fail(String key, String arg1, String arg2) {
        throw new ParseException(key, new Object[] { arg1, arg2 });
    }

    public static void fail(String key, Object[] args) {
        throw new ParseException(key, args);
    }
}
