/*
 * $Id: CustomExceptionGenerator.java,v 1.1 2005-05-23 23:14:48 bbissett Exp $
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

import com.sun.tools.ws.processor.config.Configuration;
import com.sun.tools.ws.processor.model.Fault;
import com.sun.tools.ws.processor.model.Model;
import com.sun.tools.ws.processor.model.jaxb.JAXBElementMember;
import com.sun.tools.ws.processor.model.java.JavaStructureMember;
import com.sun.tools.ws.processor.model.java.JavaStructureType;
import com.sun.tools.ws.processor.model.java.JavaStructureMember;
import com.sun.tools.ws.processor.model.java.JavaStructureType;
import com.sun.tools.ws.processor.modeler.ModelerConstants;
import com.sun.tools.ws.processor.util.GeneratedFileInfo;
import com.sun.tools.ws.processor.util.IndentingWriter;
import com.sun.xml.ws.encoding.soap.SOAPVersion;

/**
 *
 * @author JAX-RPC Development Team
 */
public class CustomExceptionGenerator extends GeneratorBase20 {
    private Set faults;

    public CustomExceptionGenerator() {
    }

    public GeneratorBase20 getGenerator(
        Model model,
        Configuration config,
        Properties properties) {
        return new CustomExceptionGenerator(model, config, properties);
    }

    public GeneratorBase20 getGenerator(
        Model model,
        Configuration config,
        Properties properties,
        SOAPVersion ver) {
        return new CustomExceptionGenerator(model, config, properties);
    }

    protected CustomExceptionGenerator(
        Model model,
        Configuration config,
        Properties properties) {
        super(model, config, properties);
    }

    protected void preVisitModel(Model model) throws Exception {
        faults = new HashSet();
    }

    protected void postVisitModel(Model model) throws Exception {
        faults = null;
    }

    protected void preVisitFault(Fault fault) throws Exception {
        if (isRegistered(fault))
            return;
        registerFault(fault);
        if (fault.getParentFault() != null) {
            preVisitFault(fault.getParentFault());
        }
    }

    private boolean isRegistered(Fault fault) {
        return faults.contains(fault.getJavaException().getName());
    }

    private void registerFault(Fault fault) {
        faults.add(fault.getJavaException().getName());
        generateCustomException(fault);
    }

    private void generateCustomException(Fault fault) {
        if (fault.getJavaException().isPresent())
            return;
        log(
            "generating CustomException for: "
                + fault.getJavaException().getName());
        try {
            String className = env.getNames().customExceptionClassName(fault);
            if ((donotOverride && 
                 GeneratorUtil.classExists(env, className))) {
                log("Class " + className + " exists. Not overriding.");
                return;
            }            
            File classFile =
                env.getNames().sourceFileForClass(
                    className,
                    className,
                    sourceDir,
                    env);

            /* adding the file name and its type */
            GeneratedFileInfo fi = new GeneratedFileInfo();
            fi.setFile(classFile);
            fi.setType(GeneratorConstants.FILE_TYPE_EXCEPTION);
            env.addGeneratedFile(fi);

            IndentingWriter out =
                new IndentingWriter(
                    new OutputStreamWriter(new FileOutputStream(classFile)));
            writePackage(out, className);
            out.pln();
            JavaStructureType javaStructure =
                (JavaStructureType) fault.getJavaException();
            writeClassDecl(out, className, javaStructure);
            writeMembers(out, fault);
            out.pln();
            writeClassConstructor(out, className, fault);
            out.pln();
            writeGetter(out, fault);
            out.pOln("}"); // class
            out.close();
        } catch (Exception e) {
            fail(e);
        }
    }

    protected void writeClassDecl(
        IndentingWriter p,
        String className,
        JavaStructureType javaStruct)
        throws IOException {        

        JAXBElementMember jaxbMember = null;
        for (JavaStructureMember member : javaStruct.getMembersList())   {
            if (member.getReadMethod().equals("getFaultInfo") &&
                member.getOwner() instanceof JAXBElementMember) {
                jaxbMember = (JAXBElementMember)member.getOwner();
                break;
            }
        }
        if (jaxbMember == null) {
            throw new GeneratorException(
                "generator.internal.error.should.not.happen",
                "stub.generator.003");
        }
        p.plnI("@javax.xml.ws.WebFault(name=\""+jaxbMember.getName().getLocalPart()+"\", ");
        p.pln("targetNamespace=\""+jaxbMember.getName().getNamespaceURI()+"\")");
        p.pO();        
        JavaStructureType superclass = javaStruct.getSuperclass();
        if (superclass != null) {
            p.plnI(
                "public class "
                    + Names.stripQualifier(className)
                    + " extends "
                    + superclass.getName()
                    + " {");
        } else {
            p.plnI(
                "public class "
                    + Names.stripQualifier(className)
                    + " extends Exception {");
        }
    }

    private void writeMembers(IndentingWriter p, Fault fault)
        throws IOException {
        Iterator members =
            ((JavaStructureType) fault.getJavaException()).getMembers();
        JavaStructureMember member;
        while (members.hasNext()) {
            member = (JavaStructureMember) members.next();
            if (!member.isInherited()) {
                p.pln(
                    "private "
                        + member.getType().getName()
                        + " "
                        + member.getName()
                        + ";");
            }
        }
        p.pln();
    }

    protected void writeClassConstructor(
        IndentingWriter p,
        String className,
        Fault fault)
        throws IOException {
        JavaStructureType javaStructure =
            (JavaStructureType) fault.getJavaException();

        //WrapperException(String message, FaultBean faultInfo)
        p.p("public " + Names.stripQualifier(className) + "(String message, ");
        Iterator members = javaStructure.getMembers();
        JavaStructureMember member;
        for (int i = 0; members.hasNext(); i++) {
            member = (JavaStructureMember) members.next();
            if (i > 0)
                p.p(", ");
            p.p(member.getType().getName() + " " + member.getName());
        }
        p.plnI(") {");
        if (fault.getParentFault() != null) {
            members = javaStructure.getMembers();
            int i = 0;
            while (members.hasNext()) {
                member = (JavaStructureMember) members.next();
                if (member.isInherited()) {
                    if (i++ == 0)
                        p.p("super(message, ");
                    else
                        p.p(", ");
                    p.p(member.getName());
                }
            }
            if (i > 0)
                p.pln(");");
        } else{
            p.pln("super(message);");
        }
        members = fault.getJavaException().getMembers();
        for (int i = 0; members.hasNext(); i++) {
            member = (JavaStructureMember) members.next();
            if (!member.isInherited()) {
                p.pln("this." + member.getName() + " = " + member.getName() + ";");
            }
        }
        p.pOln("}");
        p.pln();

        //WrapperException(String message, FaultBean faultInfo, Throwable cause)
        p.p("public " + Names.stripQualifier(className) + "(String message, ");
        members = javaStructure.getMembers();
        for (int i = 0; members.hasNext(); i++) {
            member = (JavaStructureMember) members.next();
            if (i > 0)
                p.p(", ");
            p.p(member.getType().getName() + " " + member.getName());
        }
        p.plnI(", Throwable cause) {");
        if (fault.getParentFault() != null) {
            members = javaStructure.getMembers();
            int i = 0;
            while (members.hasNext()) {
                member = (JavaStructureMember) members.next();
                if (member.isInherited()) {
                    if (i++ == 0)
                        p.p("super(message, ");
                    else
                        p.p(", ");
                    p.p(member.getName());
                }
            }
            if (i > 0)
                p.pln(", cause);");
        } else{
            p.pln("super(message, cause);");
        }
        members = fault.getJavaException().getMembers();
        for (int i = 0; members.hasNext(); i++) {
            member = (JavaStructureMember) members.next();
            if (!member.isInherited()) {
                p.pln("this." + member.getName() + " = " + member.getName() + ";");
            }
        }
        p.pOln("}");
    }

    private void writeGetter(IndentingWriter p, Fault fault)
        throws IOException {
        Iterator members =
            ((JavaStructureType) fault.getJavaException()).getMembers();
        JavaStructureMember member;
        int i = 0;
        while (members.hasNext()) {
            if (i > 0)
                p.pln();
            member = (JavaStructureMember) members.next();
            if (!member.isInherited()) {
                p.plnI(
                    "public "
                        + member.getType().getName()
                        + " "
                        + member.getReadMethod()
                        + "() {");
                p.pln("return " + member.getName() + ";");
                p.pOln("}");
                i++;
            }
        }
    }

}
