/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
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

package com.sun.tools.ws.processor.model;

import com.sun.tools.ws.processor.model.java.JavaMethod;
import com.sun.tools.ws.wsdl.document.soap.SOAPStyle;
import com.sun.tools.ws.wsdl.document.soap.SOAPUse;
import com.sun.tools.ws.wsdl.framework.Entity;
import com.sun.xml.ws.spi.db.BindingHelper;

import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author WS Development Team
 */
public class Operation extends ModelObject {

    public Operation(Entity entity) {
        super(entity);
    }

    public Operation(Operation operation, Entity entity){
        this(operation._name, entity);
        this._style = operation._style;
        this._use = operation._use;
        this.customizedName = operation.customizedName;
    }
    public Operation(QName name, Entity entity) {
        super(entity);
        _name = name;
        _uniqueName = name.getLocalPart();
        _faultNames = new HashSet<String>();
        _faults = new HashSet<Fault>();
    }

    public QName getName() {
        return _name;
    }

    public void setName(QName n) {
        _name = n;
    }

    public String getUniqueName() {
        return _uniqueName;
    }

    public void setUniqueName(String s) {
        _uniqueName = s;
    }

    public Request getRequest() {
        return _request;
    }

    public void setRequest(Request r) {
        _request = r;
    }

    public Response getResponse() {
        return _response;
    }

    public void setResponse(Response r) {
        _response = r;
    }

    public boolean isOverloaded() {
        return !_name.getLocalPart().equals(_uniqueName);
    }

    public void addFault(Fault f) {
        if (_faultNames.contains(f.getName())) {
            throw new ModelException("model.uniqueness");
        }
        _faultNames.add(f.getName());
        _faults.add(f);
    }

    public Iterator<Fault> getFaults() {
        return _faults.iterator();
    }

    public Set<Fault> getFaultsSet() {
        return _faults;
    }

    /* serialization */
    public void setFaultsSet(Set<Fault> s) {
        _faults = s;
        initializeFaultNames();
    }

    private void initializeFaultNames() {
        _faultNames = new HashSet<String>();
        if (_faults != null) {
            for (Iterator iter = _faults.iterator(); iter.hasNext();) {
                Fault f = (Fault) iter.next();
                if (f.getName() != null && _faultNames.contains(f.getName())) {
                    throw new ModelException("model.uniqueness");
                }
                _faultNames.add(f.getName());
            }
        }
    }

    public Iterator<Fault> getAllFaults() {
        Set<Fault> allFaults = getAllFaultsSet();
        return allFaults.iterator();
    }

    public Set<Fault> getAllFaultsSet() {
        Set transSet = new HashSet();
        transSet.addAll(_faults);
        Iterator iter = _faults.iterator();
        Fault fault;
        Set tmpSet;
        while (iter.hasNext()) {
            tmpSet = ((Fault)iter.next()).getAllFaultsSet();
            transSet.addAll(tmpSet);
        }
        return transSet;
    }

    public int getFaultCount() {
        return _faults.size();
    }

    public Set<Block> getAllFaultBlocks(){
        Set<Block> blocks = new HashSet<Block>();
        Iterator faults = _faults.iterator();
        while(faults.hasNext()){
            Fault f = (Fault)faults.next();
            blocks.add(f.getBlock());
        }
        return blocks;
    }

    public JavaMethod getJavaMethod() {
        return _javaMethod;
    }

    public void setJavaMethod(JavaMethod i) {
        _javaMethod = i;
    }

    public String getSOAPAction() {
        return _soapAction;
    }

    public void setSOAPAction(String s) {
        _soapAction = s;
    }

    public SOAPStyle getStyle() {
        return _style;
    }

    public void setStyle(SOAPStyle s) {
        _style = s;
    }

    public SOAPUse getUse() {
        return _use;
    }

    public void setUse(SOAPUse u) {
        _use = u;
    }

    public boolean isWrapped() {
        return _isWrapped;
    }

    public void setWrapped(boolean isWrapped) {
        _isWrapped = isWrapped;
    }


    public void accept(ModelVisitor visitor) throws Exception {
        visitor.visit(this);
    }

    public void setCustomizedName(String name){
        this.customizedName = name;
    }

    public String getCustomizedName(){
        return customizedName;
    }

    public String getJavaMethodName(){
        //if JavaMethod is created return the name
        if(_javaMethod != null){
            return _javaMethod.getName();
        }

        //return the customized operation name if any without mangling
        if(customizedName != null){
            return customizedName;
        }

        return BindingHelper.mangleNameToVariableName(_name.getLocalPart());
    }

    public com.sun.tools.ws.wsdl.document.Operation getWSDLPortTypeOperation(){
        return wsdlOperation;
    }

    public void setWSDLPortTypeOperation(com.sun.tools.ws.wsdl.document.Operation wsdlOperation){
        this.wsdlOperation = wsdlOperation;
    }



    private String customizedName;
    private boolean _isWrapped = true;
    private QName _name;
    private String _uniqueName;
    private Request _request;
    private Response _response;
    private JavaMethod _javaMethod;
    private String _soapAction;
    private SOAPStyle _style = SOAPStyle.DOCUMENT;
    private SOAPUse _use = SOAPUse.LITERAL;
    private Set<String> _faultNames;
    private Set<Fault> _faults;
    private com.sun.tools.ws.wsdl.document.Operation wsdlOperation;

}
