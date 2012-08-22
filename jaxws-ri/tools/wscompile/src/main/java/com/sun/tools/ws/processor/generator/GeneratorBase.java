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

package com.sun.tools.ws.processor.generator;

import com.sun.codemodel.ClassType;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.tools.ws.ToolVersion;
import com.sun.tools.ws.processor.model.Block;
import com.sun.tools.ws.processor.model.Fault;
import com.sun.tools.ws.processor.model.Model;
import com.sun.tools.ws.processor.model.ModelVisitor;
import com.sun.tools.ws.processor.model.Operation;
import com.sun.tools.ws.processor.model.Parameter;
import com.sun.tools.ws.processor.model.Port;
import com.sun.tools.ws.processor.model.Request;
import com.sun.tools.ws.processor.model.Response;
import com.sun.tools.ws.processor.model.Service;
import com.sun.tools.ws.processor.util.DirectoryUtil;
import com.sun.tools.ws.processor.util.IndentingWriter;
import com.sun.tools.ws.wscompile.ErrorReceiver;
import com.sun.tools.ws.wscompile.WsimportOptions;
import com.sun.xml.ws.util.xml.XmlUtil;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.jws.HandlerChain;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.annotation.processing.Filer;
import javax.tools.FileObject;

import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

public abstract class GeneratorBase implements ModelVisitor {
    private File destDir;
    private String targetVersion;
    protected boolean donotOverride;
    protected JCodeModel cm;
    protected Model model;
    protected String wsdlLocation;
    protected ErrorReceiver receiver;
    protected WsimportOptions options;

    protected GeneratorBase() {    	
    }
    
    public void init(Model model, WsimportOptions options, ErrorReceiver receiver){
        this.model = model;
        this.options = options;
        this.destDir = options.destDir;
        this.receiver = receiver;
        this.wsdlLocation = options.wsdlLocation;
        this.targetVersion = options.target.getVersion();
        this.cm = options.getCodeModel();
    }

    public void doGeneration() {
        try {
            model.accept(this);
        } catch (Exception e) {
            receiver.error(e);
        }
    }

    public void visit(Model model) throws Exception {
        for (Service service : model.getServices()) {
            service.accept(this);
        }
    }

    public void visit(Service service) throws Exception {
        for (Port port : service.getPorts()) {
            port.accept(this);
        }
    }

    public void visit(Port port) throws Exception {
        for (Operation operation : port.getOperations()) {
            operation.accept(this);
        }
    }

    public void visit(Operation operation) throws Exception {
        operation.getRequest().accept(this);
        if (operation.getResponse() != null)
            operation.getResponse().accept(this);
        Iterator faults = operation.getFaultsSet().iterator();
        if (faults != null) {
            Fault fault;
            while (faults.hasNext()) {
                fault = (Fault) faults.next();
                fault.accept(this);
            }
        }
    }

    public void visit(Parameter param) throws Exception {
    }

    public void visit(Block block) throws Exception {
    }

    public void visit(Response response) throws Exception {
    }


    public void visit(Request request) throws Exception {
    }

    public void visit(Fault fault) throws Exception {
    }

    public List<String> getJAXWSClassComment(){
        return getJAXWSClassComment(targetVersion);
    }

    public static List<String> getJAXWSClassComment(String targetVersion) {
        List<String> comments = new ArrayList<String>();
        comments.add("This class was generated by the JAX-WS RI.\n");
        comments.add(ToolVersion.VERSION.BUILD_VERSION+"\n");
        comments.add("Generated source version: " + targetVersion);
        return comments;
    }

    protected JDefinedClass getClass(String className, ClassType type) throws JClassAlreadyExistsException {
        JDefinedClass cls;
        try {
            cls = cm._class(className, type);
        } catch (JClassAlreadyExistsException e){
            cls = cm._getClass(className);
            if(cls == null)
                throw e;
        }
        return cls;
    }

    protected void log(String msg) {
        if (options.verbose) {
            System.out.println(
                "["
                    + Names.stripQualifier(this.getClass().getName())
                    + ": "
                    + msg
                    + "]");
        }
    }

    protected void writeHandlerConfig(String className, JDefinedClass cls, WsimportOptions options) {
        Element e = options.getHandlerChainConfiguration();
        if(e == null)
            return;
        JAnnotationUse handlerChainAnn = cls.annotate(cm.ref(HandlerChain.class));
        NodeList nl = e.getElementsByTagNameNS(
            "http://java.sun.com/xml/ns/javaee", "handler-chain");
        if(nl.getLength() > 0){
            String fName = getHandlerConfigFileName(className);
            handlerChainAnn.param("file", fName);
            generateHandlerChainFile(e, className);
        }
    }

     private String getHandlerConfigFileName(String fullName){
        String name = Names.stripQualifier(fullName);
        return name+"_handler.xml";
    }

    private void generateHandlerChainFile(Element hChains, String name) {
        
        Filer filer = options.filer;

        try {
            IndentingWriter p;
            FileObject jfo;
            if (filer != null) { 
                jfo = filer.createResource(StandardLocation.SOURCE_OUTPUT, 
                        Names.getPackageName(name), getHandlerConfigFileName(name));
                options.addGeneratedFile(new File(jfo.toUri()));
                p = new IndentingWriter(new OutputStreamWriter(jfo.openOutputStream()));
            } else { // leave for backw. compatibility now
                String hcName = getHandlerConfigFileName(name);
                File packageDir = DirectoryUtil.getOutputDirectoryFor(name, destDir);
                File hcFile = new File(packageDir, hcName);
                options.addGeneratedFile(hcFile);
                p = new IndentingWriter(new OutputStreamWriter(new FileOutputStream(hcFile)));
            }
        
            Transformer it = XmlUtil.newTransformer();

            it.setOutputProperty(OutputKeys.METHOD, "xml");
            it.setOutputProperty(OutputKeys.INDENT, "yes");
            it.setOutputProperty(
                "{http://xml.apache.org/xslt}indent-amount",
                "2");
            it.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            it.transform( new DOMSource(hChains), new StreamResult(p) );
            p.close();
        } catch (Exception e) {
            throw new GeneratorException(
                    "generator.nestedGeneratorError",
                    e);
        }
    }

}
