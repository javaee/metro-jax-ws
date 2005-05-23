/*
 * $Id: RemoteInterfaceImplGenerator.java,v 1.1 2005-05-23 23:14:49 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.processor.generator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import com.sun.tools.ws.processor.ProcessorAction;
import com.sun.tools.ws.processor.ProcessorOptions;
import com.sun.tools.ws.processor.config.Configuration;
import com.sun.tools.ws.processor.model.Model;
import com.sun.tools.ws.processor.model.Operation;
import com.sun.tools.ws.processor.model.Port;
import com.sun.tools.ws.processor.model.Service;
import com.sun.tools.ws.processor.model.java.JavaInterface;
import com.sun.tools.ws.processor.model.java.JavaMethod;
import com.sun.tools.ws.processor.model.java.JavaParameter;
import com.sun.tools.ws.processor.util.GeneratedFileInfo;
import com.sun.tools.ws.processor.util.IndentingWriter;
import com.sun.tools.ws.processor.util.ProcessorEnvironment;
import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;
import com.sun.xml.ws.encoding.soap.SOAPVersion;


/**
 *
 * @author JAX-RPC Development Team
 */
public class RemoteInterfaceImplGenerator extends GeneratorBase20 implements ProcessorAction {

    public RemoteInterfaceImplGenerator() {
        super();
    }

    protected RemoteInterfaceImplGenerator(
        Model model,
        Configuration config,
        Properties properties) {
        super(model, config, properties);
    }

    public GeneratorBase20 getGenerator(
        Model model,
        Configuration config,
        Properties properties) {
        return new RemoteInterfaceImplGenerator(model, config, properties);
    }


    public GeneratorBase20 getGenerator(
        Model model,
        Configuration config,
        Properties properties,
        SOAPVersion ver) {
        return new RemoteInterfaceImplGenerator(model, config, properties);
    }


    protected void visitPort(Port port) {
        JavaInterface intf = port.getJavaInterface();
        if (intf.getImpl() == null) {
            String className = env.getNames().interfaceImplClassName(intf);
            if ((donotOverride && GeneratorUtil.classExists(env, className))) {
                log("Class " + className + " exists. Not overriding.");
                return;
            }
            generateClassFor(className, port, service);
        }
    }

    private void generateClassFor(String className, Port port, Service service) {
        JavaInterface intf = port.getJavaInterface();
        try {
            File classFile =
                env.getNames().sourceFileForClass(
                    className,
                    className,
                    sourceDir,
                    env);
            /* here the file Generated is added to
               object along with its type */
            GeneratedFileInfo fi = new GeneratedFileInfo();
            fi.setFile(classFile);
            fi.setType(GeneratorConstants.FILE_TYPE_REMOTE_INTERFACE);
            env.addGeneratedFile(fi);

            IndentingWriter out =
                new IndentingWriter(
                    new OutputStreamWriter(new FileOutputStream(classFile)));
            writePackage(out, className);
            writeImports(out);
            writeWebService(out, port, service);
            out.plnI(
                "public class "
                    + Names.stripQualifier(className)
                    + " implements "
                    + env.getNames().customJavaTypeClassName(intf)
                    + ", java.rmi.Remote {");

            for (Operation operation: port.getOperationsList()) {
                JavaMethod method = operation.getJavaMethod();
                writeWebMethod(out, operation);
                out.p("public ");
                if (method.getReturnType() == null) {
                    out.p("void");
                } else {
                    out.p(method.getReturnType().getName());
                }
                out.p(" ");
                out.p(method.getName());
                out.p("(");
                boolean first = true;

                for (JavaParameter parameter: method.getParametersList()) {
                    if (!first) {
                        out.p(", ");
                    }
                    writeWebParam(out, parameter, operation);
                    if (parameter.isHolder()) {
                        out.p(
                            env.getNames().holderClassName(
                                port,
                                parameter.getType()));
                    } else {
                        out.p(
                            env.getNames().typeClassName(parameter.getType()));
                    }
                    out.p(" ");
                    out.p(parameter.getName());
                    first = false;
                    out.pO();
                }
                Iterator exceptions = method.getExceptions();
                if(exceptions.hasNext() || method.getThrowsRemoteException()){
                    out.plnI(") throws ");
                    out.pI();
                    //Iterator exceptions = method.getExceptions();
                    String exception;
                    boolean firstException = true;
                    while (exceptions.hasNext()) {
                        exception = (String) exceptions.next();
                        if(!firstException)
                            out.p(", ");
                        else
                            firstException = false;
                        out.p(exception);
                    }
                    if(method.getThrowsRemoteException()){
                        if(!firstException)
                            out.pln(", ");
                        out.pln("java.rmi.RemoteException {");
                    }else
                        out.pln("{");
                }else{
                    out.plnI(") {");
                    out.pI();
                }
                if (method.getReturnType() != null
                    && !method.getReturnType().getName().equals("void")) {
                    out.pln();
                    out.pln(
                        method.getReturnType().getName()
                            + " _retVal = "
                            + method.getReturnType().getInitString()
                            + ";");
                    out.pln("return _retVal;");
                }
                out.pOln("}");
                out.pln();
            }

            out.pOln("}");
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
            throw new GeneratorException(
                "generator.nestedGeneratorError",
                new LocalizableExceptionAdapter(e));
        }
    }

    protected void writeImports(IndentingWriter p) throws IOException {
        p.pln("import javax.jws.WebService;");
        p.pln();
    }

    protected void writeWebService(IndentingWriter p, Port port, Service service)
        throws IOException {
        String serviceName = Names.stripQualifier(env.getNames()
            .customJavaTypeClassName(service.getJavaInterface()));
        String name = Names.stripQualifier(env.getNames()
            .customJavaTypeClassName(port.getJavaInterface()));
        String targetNamespace = service.getName().getNamespaceURI();
        String seiName = env.getNames().customJavaTypeClassName(port.getJavaInterface());
//        String endpointInterface
        p.plnI("@WebService(");
//        p.pln("name=\""+name+"\",");
        p.pln("serviceName=\""+serviceName+"\",");
//        p.pln("targetNamespace=\""+targetNamespace+"\"");
        p.pln("endpointInterface=\""+seiName+"\"");
        p.pOln(")");
    }

    protected void writeWebMethod(IndentingWriter p, Operation operation)
        throws IOException {
    }

    protected void writeWebParam(IndentingWriter p,
                               JavaParameter javaParameter,
                               Operation operation)
        throws IOException {

    }
}
