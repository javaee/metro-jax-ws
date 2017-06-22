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

package com.sun.xml.ws.api.policy.subject;

import javax.xml.namespace.QName;

import com.sun.istack.logging.Logger;
import com.sun.xml.ws.resources.BindingApiMessages;

/**
 * Experimental: This class identifies WSDL scopes. That allows to attach and
 * retrieve elements to and from WSDL scopes.
 *
 * @author Fabian Ritzmann
 */
public class BindingSubject {

    /**
     * For message subjects, this needs to be set to one of the values INPUT, OUTPUT
     * or FAULT. Any other subject has the message type NO_MESSAGE.
     */
    private enum WsdlMessageType {
        NO_MESSAGE,
        INPUT,
        OUTPUT,
        FAULT
    }

    /**
     * Identifies the scope to which this subject belongs. See WS-PolicyAttachment
     * for an explanation on WSDL scopes.
     *
     * The SERVICE scope is not actually used and only listed here for completeness
     * sake.
     */
    private enum WsdlNameScope {
        SERVICE,
        ENDPOINT,
        OPERATION,
        MESSAGE
    }

    private static final Logger LOGGER = Logger.getLogger(BindingSubject.class);

    private final QName name;
    private final WsdlMessageType messageType;
    private final WsdlNameScope nameScope;
    private final BindingSubject parent;

    BindingSubject(final QName name, final WsdlNameScope scope, final BindingSubject parent) {
        this(name, WsdlMessageType.NO_MESSAGE, scope, parent);
    }

    BindingSubject(final QName name, final WsdlMessageType messageType, final WsdlNameScope scope, final BindingSubject parent) {
        this.name = name;
        this.messageType = messageType;
        this.nameScope = scope;
        this.parent = parent;
    }

    public static BindingSubject createBindingSubject(QName bindingName) {
        return new BindingSubject(bindingName, WsdlNameScope.ENDPOINT, null);
    }

    public static BindingSubject createOperationSubject(QName bindingName, QName operationName) {
        final BindingSubject bindingSubject = createBindingSubject(bindingName);
        return new BindingSubject(operationName, WsdlNameScope.OPERATION, bindingSubject);
    }

    public static BindingSubject createInputMessageSubject(QName bindingName, QName operationName, QName messageName) {
        final BindingSubject operationSubject = createOperationSubject(bindingName, operationName);
        return new BindingSubject(messageName, WsdlMessageType.INPUT, WsdlNameScope.MESSAGE, operationSubject);
    }

    public static BindingSubject createOutputMessageSubject(QName bindingName, QName operationName, QName messageName) {
        final BindingSubject operationSubject = createOperationSubject(bindingName, operationName);
        return new BindingSubject(messageName, WsdlMessageType.OUTPUT, WsdlNameScope.MESSAGE, operationSubject);
    }

    public static BindingSubject createFaultMessageSubject(QName bindingName, QName operationName, QName messageName) {
        if (messageName == null) {
            throw LOGGER.logSevereException(new IllegalArgumentException(BindingApiMessages.BINDING_API_NO_FAULT_MESSAGE_NAME()));
        }
        final BindingSubject operationSubject = createOperationSubject(bindingName, operationName);
        return new BindingSubject(messageName, WsdlMessageType.FAULT, WsdlNameScope.MESSAGE, operationSubject);
    }

    public QName getName() {
        return this.name;
    }

    public BindingSubject getParent() {
        return this.parent;
    }

    public boolean isBindingSubject() {
        if (this.nameScope == WsdlNameScope.ENDPOINT) {
            return this.parent == null;
        }
        else {
            return false;
        }
    }

    public boolean isOperationSubject() {
        if (this.nameScope == WsdlNameScope.OPERATION) {
            if (this.parent != null) {
                return this.parent.isBindingSubject();
            }
        }
        return false;
    }

    public boolean isMessageSubject() {
        if (this.nameScope == WsdlNameScope.MESSAGE) {
            if (this.parent != null) {
                return this.parent.isOperationSubject();
            }
        }
        return false;
    }

    public boolean isInputMessageSubject() {
        return isMessageSubject() && (this.messageType == WsdlMessageType.INPUT);
    }

    public boolean isOutputMessageSubject() {
        return isMessageSubject() && (this.messageType == WsdlMessageType.OUTPUT);
    }

    public boolean isFaultMessageSubject() {
        return isMessageSubject() && (this.messageType == WsdlMessageType.FAULT);
    }

    @Override
    public boolean equals(final Object that) {
        if (this == that) {
            return true;
        }

        if (that == null || !(that instanceof BindingSubject)) {
            return false;
        }

        final BindingSubject thatSubject = (BindingSubject) that;
        boolean isEqual = true;

        isEqual = isEqual && ((this.name == null) ? thatSubject.name == null : this.name.equals(thatSubject.name));
        isEqual = isEqual && this.messageType.equals(thatSubject.messageType);
        isEqual = isEqual && this.nameScope.equals(thatSubject.nameScope);
        isEqual = isEqual && ((this.parent == null) ? thatSubject.parent == null : this.parent.equals(thatSubject.parent));

        return isEqual;
    }

    @Override
    public int hashCode() {
        int result = 23;

        result = 29 * result + ((this.name == null) ? 0 : this.name.hashCode());
        result = 29 * result + this.messageType.hashCode();
        result = 29 * result + this.nameScope.hashCode();
        result = 29 * result + ((this.parent == null) ? 0 : this.parent.hashCode());

        return result;
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder("BindingSubject[");
        result.append(this.name).append(", ").append(this.messageType);
        result.append(", ").append(this.nameScope).append(", ").append(this.parent);
        return result.append("]").toString();
    }

}
