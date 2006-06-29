/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.tools.ws.wsdl.document;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import com.sun.tools.ws.wsdl.framework.Entity;
import com.sun.tools.ws.wsdl.framework.EntityAction;
import com.sun.tools.ws.wsdl.framework.ExtensibilityHelper;
import com.sun.tools.ws.wsdl.framework.Extensible;
import com.sun.tools.ws.wsdl.framework.Extension;

/**
 * Entity corresponding to the "operation" child element of a "portType" WSDL element.
 *
 * @author WS Development Team
 */
public class Operation extends Entity implements Extensible{

    public Operation() {
        _faults = new ArrayList();
        _helper = new ExtensibilityHelper();
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public String getUniqueKey() {
        if (_uniqueKey == null) {
            StringBuffer sb = new StringBuffer();
            sb.append(_name);
            sb.append(' ');
            if (_input != null) {
                sb.append(_input.getName());
            } else {
                sb.append(_name);
                if (_style == OperationStyle.REQUEST_RESPONSE) {
                    sb.append("Request");
                } else if (_style == OperationStyle.SOLICIT_RESPONSE) {
                    sb.append("Response");
                }
            }
            sb.append(' ');
            if (_output != null) {
                sb.append(_output.getName());
            } else {
                sb.append(_name);
                if (_style == OperationStyle.SOLICIT_RESPONSE) {
                    sb.append("Solicit");
                } else if (_style == OperationStyle.REQUEST_RESPONSE) {
                    sb.append("Response");
                }
            }
            _uniqueKey = sb.toString();
        }

        return _uniqueKey;
    }

    public OperationStyle getStyle() {
        return _style;
    }

    public void setStyle(OperationStyle s) {
        _style = s;
    }

    public Input getInput() {
        return _input;
    }

    public void setInput(Input i) {
        _input = i;
    }

    public Output getOutput() {
        return _output;
    }

    public void setOutput(Output o) {
        _output = o;
    }

    public void addFault(Fault f) {
        _faults.add(f);
    }

    public Iterator faults() {
        return _faults.iterator();
    }

    public String getParameterOrder() {
        return _parameterOrder;
    }

    public void setParameterOrder(String s) {
        _parameterOrder = s;
    }

    public QName getElementName() {
        return WSDLConstants.QNAME_OPERATION;
    }

    public Documentation getDocumentation() {
        return _documentation;
    }

    public void setDocumentation(Documentation d) {
        _documentation = d;
    }

    public void withAllSubEntitiesDo(EntityAction action) {
        super.withAllSubEntitiesDo(action);

        if (_input != null) {
            action.perform(_input);
        }
        if (_output != null) {
            action.perform(_output);
        }
        for (Iterator iter = _faults.iterator(); iter.hasNext();) {
            action.perform((Entity) iter.next());
        }
        _helper.withAllSubEntitiesDo(action);
    }

    public void accept(WSDLDocumentVisitor visitor) throws Exception {
        visitor.preVisit(this);
        if (_input != null) {
            _input.accept(visitor);
        }
        if (_output != null) {
            _output.accept(visitor);
        }
        for (Iterator iter = _faults.iterator(); iter.hasNext();) {
            ((Fault) iter.next()).accept(visitor);
        }
        visitor.postVisit(this);
    }

    public void validateThis() {
        if (_name == null) {
            failValidation("validation.missingRequiredAttribute", "name");
        }
        if (_style == null) {
            failValidation("validation.missingRequiredProperty", "style");
        }

        // verify operation style
        if (_style == OperationStyle.ONE_WAY) {
            if (_input == null) {
                failValidation("validation.missingRequiredSubEntity", "input");
            }
            if (_output != null) {
                failValidation("validation.invalidSubEntity", "output");
            }
            if (_faults != null && _faults.size() != 0) {
                failValidation("validation.invalidSubEntity", "fault");
            }
            if (_parameterOrder != null) {
                failValidation("validation.invalidAttribute", "parameterOrder");
            }
        } else if (_style == OperationStyle.NOTIFICATION) {
            if (_parameterOrder != null) {
                failValidation("validation.invalidAttribute", "parameterOrder");
            }
        }
    }

    /* (non-Javadoc)
     * @see Extensible#addExtension(Extension)
     */
    public void addExtension(Extension e) {
        _helper.addExtension(e);

    }

    /* (non-Javadoc)
     * @see Extensible#extensions()
     */
    public Iterator extensions() {
        return _helper.extensions();
    }


    private Documentation _documentation;
    private String _name;
    private Input _input;
    private Output _output;
    private List _faults;
    private OperationStyle _style;
    private String _parameterOrder;
    private String _uniqueKey;
    private ExtensibilityHelper _helper;
}
