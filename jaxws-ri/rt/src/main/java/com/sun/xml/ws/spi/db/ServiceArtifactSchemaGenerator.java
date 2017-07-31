/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.spi.db;

import static com.sun.xml.ws.model.RuntimeModeler.DocWrappeeNamespapceQualified;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.sun.xml.ws.wsdl.writer.WSDLGenerator.XsdNs;

import javax.xml.bind.JAXBException;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.ws.WebServiceException;

import com.sun.xml.bind.v2.schemagen.xmlschema.ComplexType;
import com.sun.xml.bind.v2.schemagen.xmlschema.Element;
import com.sun.xml.bind.v2.schemagen.xmlschema.ExplicitGroup;
import com.sun.xml.bind.v2.schemagen.xmlschema.LocalElement;
import com.sun.xml.bind.v2.schemagen.xmlschema.Occurs;
import com.sun.xml.txw2.TXW;
import com.sun.xml.txw2.output.ResultFactory;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.model.AbstractSEIModelImpl;
import com.sun.xml.ws.model.JavaMethodImpl;
import com.sun.xml.ws.model.ParameterImpl;
import com.sun.xml.ws.model.WrapperParameter;
import com.sun.xml.ws.wsdl.writer.document.xsd.Schema;

/**
 * ServiceArtifactSchemaGenerator generates XML schema for service artifacts including the wrapper types of 
 * document-literal stype operation and exceptions.
 * 
 * @author shih-chang.chen@oracle.com
 */
public class ServiceArtifactSchemaGenerator {

    protected AbstractSEIModelImpl model;
    protected SchemaOutputResolver xsdResolver;
    
    public ServiceArtifactSchemaGenerator(SEIModel model) {
        this.model = (AbstractSEIModelImpl)model;
    }

    static final String FilePrefix = "jaxwsGen";
    protected int fileIndex = 0;
    protected Schema create(String tns) {
        try {
            Result res = xsdResolver.createOutput(tns, FilePrefix + (fileIndex++) + ".xsd"); 
            return TXW.create(Schema.class, ResultFactory.createSerializer(res));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            throw new WebServiceException(e);
        }
    }
    
    public void generate(SchemaOutputResolver resolver) {
        xsdResolver = resolver;
        List<WrapperParameter> wrappers = new ArrayList<WrapperParameter>();
        for (JavaMethodImpl method : model.getJavaMethods()) {
            if(method.getBinding().isRpcLit()) continue; 
            for (ParameterImpl p : method.getRequestParameters()) {
                if (p instanceof WrapperParameter) {
                    if (WrapperComposite.class.equals((((WrapperParameter)p).getTypeInfo().type))) {
                        wrappers.add((WrapperParameter)p);
                    }
                }
            }
            for (ParameterImpl p : method.getResponseParameters()) {
                if (p instanceof WrapperParameter) {
                    if (WrapperComposite.class.equals((((WrapperParameter)p).getTypeInfo().type))) {
                        wrappers.add((WrapperParameter)p);
                    }
                }
            }
        }        
        if (wrappers.isEmpty()) return;
        HashMap<String, Schema> xsds = initWrappersSchemaWithImports(wrappers); 
        postInit(xsds);
        for(WrapperParameter wp : wrappers) {
            String tns = wp.getName().getNamespaceURI();
            Schema xsd = xsds.get(tns);            
            Element e =  xsd._element(Element.class);
            e._attribute("name", wp.getName().getLocalPart());
            e.type(wp.getName());
            ComplexType ct =  xsd._element(ComplexType.class);
            ct._attribute("name", wp.getName().getLocalPart());
            ExplicitGroup sq = ct.sequence();            
            for (ParameterImpl p : wp.getWrapperChildren() ) if (p.getBinding().isBody()) addChild(sq, p); 
        }
        for(Schema xsd: xsds.values()) xsd.commit();
    }

    protected void postInit(HashMap<String, Schema> xsds) {        
    }

    protected void addChild(ExplicitGroup sq, ParameterImpl param) {
        TypeInfo typeInfo = param.getItemType();
        boolean repeatedElement = false;
        if (typeInfo == null) {
            typeInfo = param.getTypeInfo();
        } else {
            if (typeInfo.getWrapperType() != null) typeInfo = param.getTypeInfo();
            else repeatedElement = true;
        }
        Occurs child = addChild(sq, param.getName(), typeInfo);
        if (repeatedElement && child != null) {
            child.maxOccurs("unbounded");
        }
    }
    
    protected Occurs addChild(ExplicitGroup sq, QName name, TypeInfo typeInfo) {
        LocalElement le = null;;
        QName type = model.getBindingContext().getTypeName(typeInfo); 
        if (type != null) {
            le = sq.element();
            le._attribute("name", name.getLocalPart());
            le.type(type);
        } else {
            if (typeInfo.type instanceof Class) {
                try {
                    QName elemName = model.getBindingContext().getElementName((Class)typeInfo.type);
                    if (elemName.getLocalPart().equals("any") && elemName.getNamespaceURI().equals(XsdNs)) {
                        return sq.any();
                    } else {
                        le = sq.element();
                        le.ref(elemName);
                    }
                } catch (JAXBException je) {
                    throw new WebServiceException(je.getMessage(), je);
                }
            }
        }
        return le;
    }
    
    //All the imports have to go first ...
    private HashMap<String, Schema> initWrappersSchemaWithImports(List<WrapperParameter> wrappers) {
        Object o = model.databindingInfo().properties().get(DocWrappeeNamespapceQualified);
        boolean wrappeeQualified = (o!= null && o instanceof Boolean) ? ((Boolean) o) : false;
        HashMap<String, Schema> xsds = new HashMap<String, Schema>(); 
        HashMap<String, Set<String>> imports = new HashMap<String, Set<String>>(); 
        for(WrapperParameter wp : wrappers) {
            String tns = wp.getName().getNamespaceURI();
            Schema xsd = xsds.get(tns);
            if (xsd == null) {
                xsd = create(tns);
                xsd.targetNamespace(tns);
                if (wrappeeQualified) xsd._attribute("elementFormDefault", "qualified");
                xsds.put(tns, xsd);
            }          
            for (ParameterImpl p : wp.getWrapperChildren() ) {
                String nsToImport = (p.getBinding().isBody())? bodyParamNS(p): null;
                if (nsToImport != null && !nsToImport.equals(tns) && !nsToImport.equals("http://www.w3.org/2001/XMLSchema")) {
                    Set<String> importSet = imports.get(tns);
                    if (importSet == null) {
                        importSet = new HashSet<String>();
                        imports.put(tns, importSet);
                    }
                    importSet.add(nsToImport);
                }
            }
        }
        for(Entry<String, Set<String>> entry: imports.entrySet()) {
            String tns = entry.getKey();
            Set<String> importSet = entry.getValue();
            Schema xsd = xsds.get(tns);
            for(String nsToImport : importSet) xsd._namespace(nsToImport, true);
            for(String nsToImport : importSet) {
                com.sun.xml.ws.wsdl.writer.document.xsd.Import imp = xsd._import();
                imp.namespace(nsToImport);
            }                       
        }
        return xsds;
    }

    protected String bodyParamNS(ParameterImpl p) {
        String nsToImport = null;
        TypeInfo typeInfo = p.getItemType();
        if (typeInfo == null) typeInfo = p.getTypeInfo();
        QName type = model.getBindingContext().getTypeName(typeInfo); 
        if (type != null) {
            nsToImport = type.getNamespaceURI();
        } else {
            if (typeInfo.type instanceof Class) {
                try {
                    QName elemRef = model.getBindingContext().getElementName((Class)typeInfo.type);
                    if (elemRef != null) nsToImport = elemRef.getNamespaceURI();
                } catch (JAXBException je) {
                    throw new WebServiceException(je.getMessage(), je);
                }
            }
        }
        return nsToImport;
    }
}
