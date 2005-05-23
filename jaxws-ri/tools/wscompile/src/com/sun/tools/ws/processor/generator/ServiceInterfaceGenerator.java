/*
 * $Id: ServiceInterfaceGenerator.java,v 1.1 2005-05-23 23:14:49 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.processor.generator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.Properties;

import com.sun.tools.ws.processor.ProcessorAction;
import com.sun.tools.ws.processor.ProcessorConstants;
import com.sun.tools.ws.processor.ProcessorOptions;
import com.sun.tools.ws.processor.config.Configuration;
import com.sun.tools.ws.processor.model.Model;
import com.sun.tools.ws.processor.model.Port;
import com.sun.tools.ws.processor.model.Service;
import com.sun.tools.ws.processor.model.java.JavaInterface;
import com.sun.tools.ws.processor.util.GeneratedFileInfo;
import com.sun.tools.ws.processor.util.IndentingWriter;
import com.sun.tools.ws.processor.util.ProcessorEnvironment;
import com.sun.xml.ws.encoding.soap.SOAPVersion;
import com.sun.xml.ws.util.VersionUtil;
import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;

/**
 *
 * @author JAX-RPC Development Team
 */
public class ServiceInterfaceGenerator extends GeneratorBase20 implements ProcessorAction {

    public ServiceInterfaceGenerator() {
        super();
    }

    private ServiceInterfaceGenerator(
        Model model,
        Configuration config,
        Properties properties) {
        super(model, config, properties);
    }

    public GeneratorBase20 getGenerator(
        Model model,
        Configuration config,
        Properties properties) {
        return new ServiceInterfaceGenerator(model, config, properties);
    }

    public GeneratorBase20 getGenerator(
        Model model,
        Configuration config,
        Properties properties,
        SOAPVersion ver) {
        return new ServiceInterfaceGenerator(model, config, properties);
    }

    protected void visitService(Service service) {
        try {
            JavaInterface intf = (JavaInterface) service.getJavaInterface();
            String className = env.getNames().customJavaTypeClassName(intf);
            if (donotOverride && GeneratorUtil.classExists(env, className)) {
                log("Class " + className + " exists. Not overriding.");
                return;
            }
            log("creating service interface: " + className);
            File classFile =
                env.getNames().sourceFileForClass(
                    className,
                    className,
                    sourceDir,
                    env);

            /* the implementation of the Service Generated is
               added in */
            GeneratedFileInfo fi = new GeneratedFileInfo();
            fi.setFile(classFile);
            fi.setType(GeneratorConstants.FILE_TYPE_SERVICE);
            env.addGeneratedFile(fi);

            IndentingWriter out =
                new IndentingWriter(
                    new OutputStreamWriter(new FileOutputStream(classFile)));
            writePackage(out, className);
            out.pln("import javax.xml.ws.*;");
            out.pln();
            out.plnI(
                "public interface "
                    + Names.stripQualifier(className)
                    + " extends javax.xml.ws.Service {");
            Iterator ports = service.getPorts();
            Port port;
            String portClass;
            String portName;
            while (ports.hasNext()) {
                port = (Port) ports.next();
                if (port.isProvider()) {
                    continue;  // No getXYZPort() for porvider based endpoint
                }
                portClass = port.getJavaInterface().getName();
                //portName = Names.getPortName(port);
                /* here we change the first character of the PortName
                   to Capital Letter */
                //portName = env.getNames().validJavaClassName(portName);
                //String getPortMethodStr = "public "+portClass+" get"+portName+"()";
                String getPortMethodStr = "public "+portClass+ " " +port.getPortGetter()+"();";
                //getPortMethodStr += " throws WebServiceException;";
                out.pln(getPortMethodStr);
            }
            out.pOln("}");
            out.close();

        } catch (Exception e) {
            throw new GeneratorException(
                "generator.nestedGeneratorError",
                new LocalizableExceptionAdapter(e));
        }
    }
}
