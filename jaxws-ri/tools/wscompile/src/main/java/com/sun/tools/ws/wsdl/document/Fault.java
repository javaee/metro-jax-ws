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

package com.sun.tools.ws.wsdl.document;

import com.sun.tools.ws.api.wsdl.TWSDLExtensible;
import com.sun.tools.ws.api.wsdl.TWSDLExtension;
import com.sun.tools.ws.wsdl.framework.*;
import org.xml.sax.Locator;

import javax.xml.namespace.QName;

/**
 * Entity corresponding to the "fault" child element of a port type operation.
 *
 * @author WS Development Team
 */
public class Fault extends Entity implements TWSDLExtensible {

    public Fault(Locator locator) {
        super(locator);
        _helper = new ExtensibilityHelper();
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public QName getMessage() {
        return _message;
    }

    public void setMessage(QName n) {
        _message = n;
    }

    public Message resolveMessage(AbstractDocument document) {
        return (Message) document.find(Kinds.MESSAGE, _message);
    }

    @Override
    public QName getElementName() {
        return WSDLConstants.QNAME_FAULT;
    }

    public Documentation getDocumentation() {
        return _documentation;
    }

    public void setDocumentation(Documentation d) {
        _documentation = d;
    }

    @Override
    public void withAllQNamesDo(QNameAction action) {
        if (_message != null) {
            action.perform(_message);
        }
    }

    @Override
    public void withAllEntityReferencesDo(EntityReferenceAction action) {
        super.withAllEntityReferencesDo(action);
        if (_message != null) {
            action.perform(Kinds.MESSAGE, _message);
        }
    }

    public void accept(WSDLDocumentVisitor visitor) throws Exception {
        visitor.preVisit(this);
        visitor.postVisit(this);
    }

    @Override
    public void validateThis() {
        if (_name == null) {
            failValidation("validation.missingRequiredAttribute", "name");
        }
        if (_message == null) {
            failValidation("validation.missingRequiredAttribute", "message");
        }
    }

    private Documentation _documentation;
    private String _name;
    private QName _message;
    private String _action;
    private ExtensibilityHelper _helper;

    @Override
    public String getNameValue() {
        return getName();
    }

    @Override
    public String getNamespaceURI() {
        return (parent == null) ? null : parent.getNamespaceURI();
    }

    @Override
    public QName getWSDLElementName() {
        return getElementName();
    }

    /* (non-Javadoc)
    * @see TWSDLExtensible#addExtension(ExtensionImpl)
    */
    @Override
    public void addExtension(TWSDLExtension e) {
        _helper.addExtension(e);

    }

    /* (non-Javadoc)
     * @see TWSDLExtensible#extensions()
     */
    @Override
    public Iterable<TWSDLExtension> extensions() {
        return _helper.extensions();
    }

    @Override
    public TWSDLExtensible getParent() {
        return parent;
    }


    public void setParent(TWSDLExtensible parent) {
        this.parent = parent;
    }

    private TWSDLExtensible parent;

    public String getAction() {
        return _action;
    }

    public void setAction(String _action) {
        this._action = _action;
    }
}
