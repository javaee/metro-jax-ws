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

package com.sun.tools.ws.wsdl.framework;

import com.sun.tools.ws.api.wsdl.TWSDLParserContext;
import com.sun.tools.ws.wsdl.parser.DOMForest;
import com.sun.tools.ws.wscompile.ErrorReceiver;
import com.sun.tools.ws.resources.WsdlMessages;
import com.sun.xml.ws.util.NamespaceSupport;
import com.sun.xml.ws.util.xml.XmlUtil;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.xml.sax.Locator;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The context used by parser classes.
 *
 * @author WS Development Team
 */
public class TWSDLParserContextImpl implements TWSDLParserContext {

    private final static String PREFIX_XMLNS = "xmlns";
    private boolean _followImports;
    private final AbstractDocument _document;
    private final NamespaceSupport _nsSupport;
    private final ArrayList<ParserListener> _listeners;
    private final WSDLLocation _wsdlLocation;
    private final DOMForest forest;
    private final ErrorReceiver errorReceiver;

    public TWSDLParserContextImpl(DOMForest forest, AbstractDocument doc, ArrayList<ParserListener> listeners, ErrorReceiver errReceiver) {
        this._document = doc;
        this._listeners = listeners;
        this._nsSupport = new NamespaceSupport();
        this._wsdlLocation = new WSDLLocation();
        this.forest = forest;
        this.errorReceiver = errReceiver;
    }

    public AbstractDocument getDocument() {
        return _document;
    }

    public boolean getFollowImports() {
        return _followImports;
    }

    public void setFollowImports(boolean b) {
        _followImports = b;
    }

    public void push() {
        _nsSupport.pushContext();
    }

    public void pop() {
        _nsSupport.popContext();
    }

    public String getNamespaceURI(String prefix) {
        return _nsSupport.getURI(prefix);
    }

    public Iterable<String> getPrefixes() {
        return _nsSupport.getPrefixes();
    }

    public String getDefaultNamespaceURI() {
        return getNamespaceURI("");
    }

    public void registerNamespaces(Element e) {
        for (Iterator iter = XmlUtil.getAllAttributes(e); iter.hasNext();) {
            Attr a = (Attr) iter.next();
            if (a.getName().equals(PREFIX_XMLNS)) {
                // default namespace declaration
                _nsSupport.declarePrefix("", a.getValue());
            } else {
                String prefix = XmlUtil.getPrefix(a.getName());
                if (prefix != null && prefix.equals(PREFIX_XMLNS)) {
                    String nsPrefix = XmlUtil.getLocalPart(a.getName());
                    String uri = a.getValue();
                    _nsSupport.declarePrefix(nsPrefix, uri);
                }
            }
        }
    }

    public Locator getLocation(Element e) {
        return forest.locatorTable.getStartLocation(e);
    }

    public QName translateQualifiedName(Locator locator, String s) {
        if (s == null)
            return null;

        String prefix = XmlUtil.getPrefix(s);
        String uri = null;

        if (prefix == null) {
            uri = getDefaultNamespaceURI();
        } else {
            uri = getNamespaceURI(prefix);
            if (uri == null) {
                errorReceiver.error(locator, WsdlMessages.PARSING_UNKNOWN_NAMESPACE_PREFIX(prefix));
            }
        }

        return new QName(uri, XmlUtil.getLocalPart(s));
    }

    public void fireIgnoringExtension(Element e, Entity entity) {
        QName name = new QName(e.getNamespaceURI(), e.getLocalName());
        QName parent = entity.getElementName();
        List _targets = null;

        synchronized (this) {
            if (_listeners != null) {
                _targets = (List) _listeners.clone();
            }
        }

        if (_targets != null) {
            for (Iterator iter = _targets.iterator(); iter.hasNext();) {
                ParserListener l = (ParserListener) iter.next();
                l.ignoringExtension(entity, name, parent);
            }
        }
    }

    public void fireDoneParsingEntity(QName element, Entity entity) {
        List _targets = null;

        synchronized (this) {
            if (_listeners != null) {
                _targets = (List) _listeners.clone();
            }
        }

        if (_targets != null) {
            for (Iterator iter = _targets.iterator(); iter.hasNext();) {
                ParserListener l = (ParserListener) iter.next();
                l.doneParsingEntity(element, entity);
            }
        }
    }

    //bug fix: 4856674, WSDLLocation context maintainence
    //and utility funcitons
    public void pushWSDLLocation() {
        _wsdlLocation.push();
    }

    public void popWSDLLocation() {
        _wsdlLocation.pop();
    }

    public void setWSDLLocation(String loc) {
        _wsdlLocation.setLocation(loc);
    }

    public String getWSDLLocation() {
        return _wsdlLocation.getLocation();
    }
}
