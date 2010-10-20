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

package com.sun.tools.ws.processor.modeler.wsdl;

import com.sun.tools.ws.processor.model.ModelException;
import com.sun.tools.ws.processor.model.java.JavaSimpleType;
import com.sun.tools.ws.processor.model.java.JavaType;
import com.sun.tools.ws.processor.model.jaxb.JAXBMapping;
import com.sun.tools.ws.processor.model.jaxb.JAXBModel;
import com.sun.tools.ws.processor.model.jaxb.JAXBType;
import com.sun.tools.ws.processor.util.ClassNameCollector;
import com.sun.tools.ws.wscompile.AbortException;
import com.sun.tools.ws.wscompile.ErrorReceiver;
import com.sun.tools.ws.wscompile.WsimportOptions;
import com.sun.tools.ws.wsdl.parser.DOMForestScanner;
import com.sun.tools.ws.wsdl.parser.MetadataFinder;
import com.sun.tools.xjc.api.S2JJAXBModel;
import com.sun.tools.xjc.api.SchemaCompiler;
import com.sun.tools.xjc.api.TypeAndAnnotation;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.LocatorImpl;

import javax.xml.namespace.QName;

/**
 * @author  Vivek Pandey
 *
 * Uses JAXB XJC apis to build JAXBModel and resolves xml to java type mapping from JAXBModel
 */
public class JAXBModelBuilder {

    private final ErrorReceiver errReceiver;
    private final WsimportOptions options;
    private final MetadataFinder forest;

    public JAXBModelBuilder(WsimportOptions options, ClassNameCollector classNameCollector, MetadataFinder finder, ErrorReceiver errReceiver) {
        this._classNameAllocator = new ClassNameAllocatorImpl(classNameCollector);
        this.errReceiver = errReceiver;
        this.options = options;
        this.forest = finder;

        internalBuildJAXBModel();
    }

    /**
     * Builds model from WSDL document. Model contains abstraction which is used by the
     * generators to generate the stub/tie/serializers etc. code.
     *
     * @see com.sun.tools.ws.processor.modeler.Modeler#buildModel()
     */

    private void internalBuildJAXBModel(){
        try {
            schemaCompiler =  options.getSchemaCompiler();
            schemaCompiler.resetSchema();
            schemaCompiler.setEntityResolver(options.entityResolver);
            schemaCompiler.setClassNameAllocator(_classNameAllocator);
            schemaCompiler.setErrorListener(errReceiver);
            int schemaElementCount = 1;

            for (Element element : forest.getInlinedSchemaElement()) {
                String location = element.getOwnerDocument().getDocumentURI();
                String systemId = location + "#types?schema" + schemaElementCount++;
                if(forest.isMexMetadata)
                    schemaCompiler.parseSchema(systemId,element);
                else
                    new DOMForestScanner(forest).scan(element,schemaCompiler.getParserHandler(systemId));
            }

            //feed external jaxb:bindings file
            InputSource[] externalBindings = options.getSchemaBindings();
            if(externalBindings != null){
                for(InputSource jaxbBinding : externalBindings){
                    schemaCompiler.parseSchema(jaxbBinding);
                }
            }
        } catch (Exception e) {
            throw new ModelException(e);
        }
    }

    public JAXBType  getJAXBType(QName qname){
        JAXBMapping mapping = jaxbModel.get(qname);
        if (mapping == null){
            return null;
        }
        JavaType javaType = new JavaSimpleType(mapping.getType());
        return new JAXBType(qname, javaType, mapping, jaxbModel);
    }

    public TypeAndAnnotation getElementTypeAndAnn(QName qname){
        JAXBMapping mapping = jaxbModel.get(qname);
        if (mapping == null){
            return null;
        }
        return mapping.getType().getTypeAnn();
    }

    protected void bind(){
        S2JJAXBModel rawJaxbModel = schemaCompiler.bind();
        if(rawJaxbModel == null)
            throw new AbortException();
        options.setCodeModel(rawJaxbModel.generateCode(null, errReceiver));
        jaxbModel = new JAXBModel(rawJaxbModel);
        jaxbModel.setGeneratedClassNames(_classNameAllocator.getJaxbGeneratedClasses());
    }

    protected SchemaCompiler getJAXBSchemaCompiler(){
        return schemaCompiler;
    }

    public JAXBModel getJAXBModel(){
        return jaxbModel;
    }

    private JAXBModel jaxbModel;
    private SchemaCompiler schemaCompiler;
    private final ClassNameAllocatorImpl _classNameAllocator;
    protected static final LocatorImpl NULL_LOCATOR = new LocatorImpl();

}
