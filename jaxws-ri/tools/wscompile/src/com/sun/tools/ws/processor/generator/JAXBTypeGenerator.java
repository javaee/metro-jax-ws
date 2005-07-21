/*
 * $Id: JAXBTypeGenerator.java,v 1.2 2005-07-21 02:06:20 vivekp Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.ws.processor.generator;

import java.util.Properties;

import org.xml.sax.SAXParseException;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.writer.ProgressCodeWriter;
//import com.sun.tools.xjc.addon.Augmenter;
import com.sun.tools.xjc.api.ErrorListener;
import com.sun.tools.xjc.api.JAXBModel;
import com.sun.tools.xjc.api.S2JJAXBModel;
import com.sun.tools.ws.processor.config.Configuration;
import com.sun.tools.ws.processor.model.Model;
import com.sun.tools.ws.processor.model.jaxb.JAXBType;
import com.sun.tools.ws.processor.model.jaxb.RpcLitStructure;
import com.sun.xml.ws.encoding.soap.SOAPVersion;
import com.sun.tools.ws.wscompile.JAXRPCCodeWriter;

/**
 * @author Vivek Pandey
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class JAXBTypeGenerator extends GeneratorBase20 {

    /**
     * @author Vivek Pandey
     *
     * To change the template for this generated type comment go to
     * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
     */
    public static class JAXBErrorListener implements ErrorListener {

        /**
         *
         */
        public JAXBErrorListener() {
            super();
        }

        /* (non-Javadoc)
         * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
         */
        public void error(SAXParseException arg0) {
            // TODO Auto-generated method stub

        }

        /* (non-Javadoc)
         * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
         */
        public void fatalError(SAXParseException arg0) {
            // TODO Auto-generated method stub

        }

        /* (non-Javadoc)
         * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
         */
        public void warning(SAXParseException arg0) {
            // TODO Auto-generated method stub

        }

        /* (non-Javadoc)
         * @see com.sun.tools.xjc.api.ErrorListener#info(org.xml.sax.SAXParseException)
         */
        public void info(SAXParseException arg0) {
            // TODO Auto-generated method stub

        }

    }
    /**
     *
     */
    public JAXBTypeGenerator() {
        super();
        // TODO Auto-generated constructor stub
    }
    /**
     * @param model
     * @param config
     * @param properties
     */
    public JAXBTypeGenerator(Model model, Configuration config,
            Properties properties) {
        super(model, config, properties);
    }
    /* (non-Javadoc)
     * @see com.sun.xml.rpc.processor.generator.GeneratorBase#getGenerator(com.sun.xml.rpc.processor.model.Model, com.sun.xml.rpc.processor.config.Configuration, java.util.Properties)
     */
    public GeneratorBase20 getGenerator(Model model, Configuration config,
            Properties properties) {
        return new JAXBTypeGenerator(model, config, properties);
    }
    /* (non-Javadoc)
     * @see com.sun.xml.rpc.processor.generator.GeneratorBase#getGenerator(com.sun.xml.rpc.processor.model.Model, com.sun.xml.rpc.processor.config.Configuration, java.util.Properties, com.sun.xml.rpc.soap.SOAPVersion)
     */
    public GeneratorBase20 getGenerator(Model model, Configuration config,
            Properties properties, SOAPVersion ver) {
        return new JAXBTypeGenerator(model, config, properties);
    }

    /* (non-Javadoc)
     * @see com.sun.xml.rpc.processor.model.jaxb.JAXBTypeVisitor#visit(com.sun.xml.rpc.processor.model.jaxb.JAXBType)
     */
    public void visit(JAXBType type) throws Exception {
        S2JJAXBModel model = type.getJaxbModel().getS2JJAXBModel();
        if (model != null)
            generateJAXBClasses(model);
    }


    /* (non-Javadoc)
     * @see com.sun.xml.rpc.processor.model.jaxb.JAXBTypeVisitor#visit(com.sun.xml.rpc.processor.model.jaxb.RpcLitStructure)
     */
    public void visit(RpcLitStructure type) throws Exception {
        S2JJAXBModel model = type.getJaxbModel().getS2JJAXBModel();
        generateJAXBClasses(model);
    }

    private static boolean doneGeneration = true;
    private void generateJAXBClasses(S2JJAXBModel model) throws Exception{
        if(doneGeneration)
            return;
        JCodeModel cm = null;

        // get the list of jaxb source files
        CodeWriter cw = new JAXRPCCodeWriter(sourceDir,env);

        if(env.verbose())
            cw = new ProgressCodeWriter(cw, System.out); // TODO this should not be System.out, should be
                                                         // something from ProcessorEnvironment
        /*
        //Fast ASN.1 JAXB extension
        Class jaxbASNAddOn = null;
        try{
            jaxbASNAddOn = Class.forName("com.sun.tools.xjc.asn.JAXBASN1AddOn");
        }catch(ClassNotFoundException e){
            log("Can't generate ASN.1 artifacts! com.sun.tools.xjc.asn.JAXBASN1AddOn is not found!");
        }
        if(jaxbASNAddOn != null) {
            log("Can't generate ASN.1 artifacts!");
            //log("Generating both ASN.1 and XML artifacts.");
            //cm = model.generateCode(new Augmenter[]{(Augmenter) jaxbASNAddOn.newInstance()}, new JAXBErrorListener());
            cm = model.generateCode(null, new JAXBErrorListener());
        }else{
            cm = model.generateCode(null, new JAXBErrorListener());
        }
         */
        cm = model.generateCode(null, new JAXBErrorListener());
        cm.build(cw);
        doneGeneration = true;
    }


}
