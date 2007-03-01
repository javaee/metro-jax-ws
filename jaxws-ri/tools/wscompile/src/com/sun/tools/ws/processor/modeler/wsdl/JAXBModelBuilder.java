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
