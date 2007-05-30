/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

import com.sun.tools.ws.wsdl.parser.MetadataFinder;
import com.sun.tools.ws.wsdl.parser.DOMForest;
import com.sun.tools.ws.wscompile.ErrorReceiver;
import com.sun.tools.ws.wscompile.AbortException;
import com.sun.tools.ws.resources.WsdlMessages;

import javax.xml.namespace.QName;
import java.util.*;

import org.xml.sax.helpers.LocatorImpl;

/**
 * An abstract class for documents containing entities.
 *
 * @author WS Development Team
 */
public abstract class AbstractDocument {

    protected final DOMForest forest;
    protected final ErrorReceiver errReceiver;

    protected AbstractDocument(MetadataFinder forest, ErrorReceiver errReceiver) {
        this.forest = forest;
        this.errReceiver = errReceiver;
        kinds = new HashMap();
        importedEntities = new ArrayList();
        importedDocuments = new HashSet();
        includedEntities = new ArrayList();
        includedDocuments = new HashSet();
    }

    public String getSystemId() {
        return _systemId;
    }

    public void setSystemId(String s) {
        if (_systemId != null && !_systemId.equals(s)) {
            // avoid redefinition of a system identifier
            throw new IllegalArgumentException();
        }

        _systemId = s;
        if (s != null) {
            importedDocuments.add(s);
        }
    }

    public void addIncludedDocument(String systemId) {
        includedDocuments.add(systemId);
    }

    public boolean isIncludedDocument(String systemId) {
        return includedDocuments.contains(systemId);
    }

    public void addIncludedEntity(Entity entity) {
        includedEntities.add(entity);
    }

    public void addImportedDocument(String systemId) {
        importedDocuments.add(systemId);
    }

    public boolean isImportedDocument(String systemId) {
        return importedDocuments.contains(systemId);
    }

    public void addImportedEntity(Entity entity) {
        importedEntities.add(entity);
    }

    public void withAllSubEntitiesDo(EntityAction action) {
        if (getRoot() != null) {
            action.perform(getRoot());
        }

        for (Iterator iter = importedEntities.iterator(); iter.hasNext();) {
            action.perform((Entity) iter.next());
        }

        for (Iterator iter = includedEntities.iterator(); iter.hasNext();) {
            action.perform((Entity) iter.next());
        }
    }

    public Map getMap(Kind k) {
        Map m = (Map) kinds.get(k.getName());
        if (m == null) {
            m = new HashMap();
            kinds.put(k.getName(), m);
        }
        return m;
    }

    public void define(GloballyKnown e) {
        Map map = getMap(e.getKind());
        if (e.getName() == null)
            return;
        QName name =
            new QName(e.getDefining().getTargetNamespaceURI(), e.getName());

        if (map.containsKey(name)){
            errReceiver.error(e.getLocator(), WsdlMessages.ENTITY_DUPLICATE_WITH_TYPE(e.getElementName().getLocalPart(), e.getName()));
            throw new AbortException();
        }else{
            map.put(name, e);
        }
    }

    public GloballyKnown find(Kind k, QName name) {
        Map map = getMap(k);
        Object result = map.get(name);
        if (result == null){
            errReceiver.error(null, WsdlMessages.ENTITY_NOT_FOUND_BY_Q_NAME(k.getName(), name, _systemId));
            throw new AbortException();
        }
        return (GloballyKnown) result;
    }

    public void validateLocally() {
        LocallyValidatingAction action = new LocallyValidatingAction();
        withAllSubEntitiesDo(action);
        if (action.getException() != null) {
            throw action.getException();
        }
    }

    public abstract void validate(EntityReferenceValidator validator);

    protected abstract Entity getRoot();

    private final Map kinds;
    private String _systemId;
    private final Set importedDocuments;
    private final List importedEntities;
    private final Set includedDocuments;
    private final List includedEntities;

    private class LocallyValidatingAction implements EntityAction {
        public LocallyValidatingAction() {
        }

        public void perform(Entity entity) {
            try {
                entity.validateThis();
                entity.withAllSubEntitiesDo(this);
            } catch (ValidationException e) {
                if (_exception == null) {
                    _exception = e;
                }
            }
        }

        public ValidationException getException() {
            return _exception;
        }

        private ValidationException _exception;
    }
}
