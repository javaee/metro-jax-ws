/*
 * $Id: JAXBModelBuilder.java,v 1.6 2005-08-18 15:27:53 vivekp Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.ws.processor.modeler.wsdl;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

import com.sun.tools.xjc.api.ErrorListener;
import com.sun.tools.xjc.api.SchemaCompiler;
import com.sun.tools.xjc.api.XJC;
import com.sun.tools.ws.processor.ProcessorOptions;
import com.sun.tools.ws.processor.config.ModelInfo;
import com.sun.tools.ws.processor.config.WSDLModelInfo;
import com.sun.tools.ws.processor.model.ModelException;
import com.sun.tools.ws.processor.model.java.JavaSimpleType;
import com.sun.tools.ws.processor.model.java.JavaType;
import com.sun.tools.ws.processor.model.jaxb.JAXBMapping;
import com.sun.tools.ws.processor.model.jaxb.JAXBModel;
import com.sun.tools.ws.processor.model.jaxb.JAXBType;
import com.sun.tools.ws.processor.modeler.JavaSimpleTypeCreator;
import com.sun.tools.ws.processor.util.ClassNameCollector;
import com.sun.tools.ws.processor.util.ProcessorEnvironment;
import com.sun.tools.ws.util.JAXWSUtils;
import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;
import com.sun.xml.ws.util.localization.LocalizableMessageFactory;

/**
 * @author Kathy Walsh, Vivek Pandey
 *
 * Uses JAXB XJC apis to build JAXBModel and resolves xml to java type mapping from JAXBModel
 */
public class JAXBModelBuilder {
    public JAXBModelBuilder(ModelInfo modelInfo,
                            Properties options, ClassNameCollector classNameCollector, List elements) {
        _messageFactory =
            new LocalizableMessageFactory("com.sun.tools.ws.resources.model");
        _modelInfo = modelInfo;
        _env = (ProcessorEnvironment) modelInfo.getParent().getEnvironment();
        _classNameAllocator = new ClassNameAllocatorImpl(classNameCollector);

        internalBuildJAXBModel(elements);
        printstacktrace = Boolean.valueOf(options.getProperty(ProcessorOptions.PRINT_STACK_TRACE_PROPERTY));
    }

    /**
     * Builds model from WSDL document. Model contains abstraction which is used by the
     * generators to generate the stub/tie/serializers etc. code.
     *
     * @see com.sun.tools.ws.processor.modeler.Modeler#buildModel()
     */

    private void internalBuildJAXBModel(List elements){
        try {
            schemaCompiler = XJC.createSchemaCompiler();
            schemaCompiler.setClassNameAllocator(_classNameAllocator);
            schemaCompiler.setErrorListener(new ConsoleErrorReporter(_env, printstacktrace));
            int schemaElementCount = 1;
            String wsdlLocation =((WSDLModelInfo)_modelInfo).getLocation();
            wsdlLocation = JAXWSUtils.absolutize(JAXWSUtils.getFileOrURLName(wsdlLocation));
            for(Iterator iter = elements.iterator(); iter.hasNext();){
                Element schemaElement = (Element)iter.next();
                String systemId = new String(wsdlLocation + "#types?schema"+schemaElementCount++);
                schemaCompiler.parseSchema(systemId,schemaElement);
            }

            //feed external jaxb:bindings file
            Set<InputSource> externalBindings = ((WSDLModelInfo)_modelInfo).getJAXBBindings();
            if(externalBindings != null){
                for(InputSource jaxbBinding : externalBindings){
                    schemaCompiler.parseSchema(jaxbBinding);
                }
            }
        } catch (Exception e) {
            throw new ModelException(new LocalizableExceptionAdapter(e));
        }
    }

    public JAXBType  getJAXBType(QName qname){
        JAXBMapping mapping = jaxbModel.get(qname);
        if (mapping == null){
            fail("model.schema.elementNotFound", new Object[]{qname});
        }

        JavaType javaType = new JavaSimpleType(mapping.getType());
        JAXBType type =  new JAXBType(qname, javaType, mapping, jaxbModel);
        return type;
    }

    protected void bind(){
        com.sun.tools.xjc.api.JAXBModel rawJaxbModel = schemaCompiler.bind();
        if(rawJaxbModel == null)
            fail("model.schema.jaxbModelIsNULL", null);
        jaxbModel = new JAXBModel(rawJaxbModel);
        jaxbModel.setGeneratedClassNames(_classNameAllocator.getJaxbGeneratedClasses());
    }

    protected SchemaCompiler getJAXBSchemaCompiler(){
        return schemaCompiler;
    }

    protected void fail(String key, Object[] arg) {
        throw new ModelException(key, arg);
    }

    protected void error(String key, Object[] args){
        _env.error(_messageFactory.getMessage(key, args));
    }

    protected void warn(String key, Object[] args) {
        _env.warn(_messageFactory.getMessage(key, args));
    }

    protected void inform(String key, Object[] args) {
        _env.info(_messageFactory.getMessage(key, args));
    }

    public JAXBModel getJAXBModel(){
        return jaxbModel;
    }

    private JAXBModel jaxbModel;
    private SchemaCompiler schemaCompiler;
    private LocalizableMessageFactory _messageFactory;
    private ModelInfo _modelInfo;
    private ProcessorEnvironment _env;
    private boolean printstacktrace;
    private ClassNameAllocatorImpl _classNameAllocator;
}
