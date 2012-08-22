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

package com.sun.xml.ws.policy.jaxws;

import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyMapExtender;
import com.sun.xml.ws.policy.PolicyMapKey;
import com.sun.xml.ws.policy.PolicySubject;
import com.sun.xml.ws.policy.sourcemodel.PolicySourceModel;

import java.util.Collection;
import java.util.Map;
import javax.xml.namespace.QName;

/**
 *
 * @author Jakub Podlesak (jakub.podlesak at sun.com)
 */
final class BuilderHandlerMessageScope extends BuilderHandler{
    private final QName service;
    private final QName port;
    private final QName operation;
    private final QName message;    
    private final Scope scope;
    
    enum Scope{
        InputMessageScope,
        OutputMessageScope,
        FaultMessageScope,
    };
    
    
    /** Creates a new instance of WSDLServiceScopeBuilderHandler */
    BuilderHandlerMessageScope(
            Collection<String> policyURIs
            , Map<String,PolicySourceModel> policyStore
            , Object policySubject
            , Scope scope
            , QName service, QName port, QName operation, QName message) {
        
        super(policyURIs, policyStore, policySubject);
        this.service = service;
        this.port = port;
        this.operation = operation;
        this.scope = scope;
        this.message = message;
    }
    
    /**
     * Multiple bound operations may refer to the same fault messages. This would result
     * in multiple builder handlers referring to the same policies. This method allows
     * to sort out these duplicate handlers.
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (!(obj instanceof BuilderHandlerMessageScope)) {
            return false;
        }
        
        final BuilderHandlerMessageScope that = (BuilderHandlerMessageScope) obj;
        boolean result = true;
        
        result = result && ((this.policySubject == null) ? ((that.policySubject == null) ? true : false) :this.policySubject.equals(that.policySubject));
        result = result && ((this.scope == null) ? ((that.scope == null) ? true : false) :this.scope.equals(that.scope));
        result = result && ((this.message == null) ? ((that.message == null) ? true : false) :this.message.equals(that.message));
        if (this.scope != Scope.FaultMessageScope) {
            result = result && ((this.service == null) ? ((that.service == null) ? true : false) :this.service.equals(that.service));
            result = result && ((this.port == null) ? ((that.port == null) ? true : false) :this.port.equals(that.port));
            result = result && ((this.operation == null) ? ((that.operation == null) ? true : false) :this.operation.equals(that.operation));
        }
        
        return result;
    }

    @Override
    public int hashCode() {
        int hashCode = 19;
        hashCode = 31 * hashCode + (policySubject == null ? 0 : policySubject.hashCode());
        hashCode = 31 * hashCode + (message == null ? 0 : message.hashCode());
        hashCode = 31 * hashCode + (scope == null ? 0 : scope.hashCode());
        if (scope != Scope.FaultMessageScope) {
            hashCode = 31 * hashCode + (service == null ? 0 : service.hashCode());
            hashCode = 31 * hashCode + (port == null ? 0 : port.hashCode());
            hashCode = 31 * hashCode + (operation == null ? 0 : operation.hashCode());
        }
        return hashCode;
    }
    
    protected void doPopulate(final PolicyMapExtender policyMapExtender) throws PolicyException{
        PolicyMapKey mapKey;
        
        if (Scope.FaultMessageScope == scope) {
            mapKey = PolicyMap.createWsdlFaultMessageScopeKey(service, port, operation, message);
        } else { // in|out msg scope
            mapKey = PolicyMap.createWsdlMessageScopeKey(service, port, operation);
        }
        
        if (Scope.InputMessageScope == scope) {
            for (PolicySubject subject:getPolicySubjects()) {
                policyMapExtender.putInputMessageSubject(mapKey, subject);
            }
        } else if (Scope.OutputMessageScope == scope) {
            for (PolicySubject subject:getPolicySubjects()) {
                policyMapExtender.putOutputMessageSubject(mapKey, subject);
            }
        } else if (Scope.FaultMessageScope == scope) {
            for (PolicySubject subject : getPolicySubjects()) {
                policyMapExtender.putFaultMessageSubject(mapKey, subject);
            }
        }
    }
}
