/*
 * $Id: CustomExceptionGenerator.java,v 1.3 2005-07-21 02:00:28 vivekp Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.processor.generator;

import com.sun.codemodel.*;
import com.sun.tools.ws.processor.config.Configuration;
import com.sun.tools.ws.processor.model.Fault;
import com.sun.tools.ws.processor.model.Model;
import com.sun.xml.ws.encoding.soap.SOAPVersion;
import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;

import javax.xml.ws.WebFault;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author WS Development Team
 */
public class CustomExceptionGenerator extends GeneratorBase20 {
    private Map<String, JClass> faults = new HashMap<String, JClass>();

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
    }

    protected void postVisitModel(Model model) throws Exception {
        faults = null;
    }

    protected void preVisitFault(Fault fault) throws Exception {
        if (isRegistered(fault))
            return;
        registerFault(fault);
    }

    private boolean isRegistered(Fault fault) {
        if(faults.keySet().contains(fault.getJavaException().getName())){
            fault.setExceptionClass(faults.get(fault.getJavaException().getName()));
            return true;
        }
        return false;
    }

    private void registerFault(Fault fault) {
         try {
            write(fault);
            faults.put(fault.getJavaException().getName(), fault.getExceptionClass()); 
        } catch (Exception e) {
            throw new GeneratorException(
                "generator.nestedGeneratorError",
                new LocalizableExceptionAdapter(e));
        }
    }

    private void write(Fault fault) throws Exception{
        String className = env.getNames().customExceptionClassName(fault);

        JDefinedClass cls = cm._class(className, ClassType.CLASS);
        JDocComment comment = cls.javadoc();
        for(String doc:getJAXWSClassComment()){
            comment.add(doc);
        }
        cls._extends(java.lang.Exception.class);

        //@WebFault
        JAnnotationUse faultAnn = cls.annotate(WebFault.class);
        faultAnn.param("name", fault.getBlock().getName().getLocalPart());
        faultAnn.param("targetNamespace", fault.getBlock().getName().getNamespaceURI());

        JType faultBean = fault.getBlock().getType().getJavaType().getType().getType();

        //faultInfo filed
        JFieldVar fi = cls.field(JMod.PRIVATE, faultBean, "faultInfo");

        //add jaxb annotations
        fault.getBlock().getType().getJavaType().getType().annotate(fi);

        fi.javadoc().add("Java type that goes as soapenv:Fault detail element.");
        JFieldRef fr = JExpr.ref(JExpr._this(), fi);

        //Constructor
        JMethod constrc1 = cls.constructor(JMod.PUBLIC);
        JVar var1 = constrc1.param(String.class, "message");
        JVar var2 = constrc1.param(faultBean, "faultInfo");
        constrc1.javadoc().addParam(var1);
        constrc1.javadoc().addParam(var2);
        JBlock cb1 = constrc1.body();
        cb1.invoke("super").arg(var1);

        cb1.assign(fr, var2);

        //constructor with Throwable
        JMethod constrc2 = cls.constructor(JMod.PUBLIC);
        var1 = constrc2.param(String.class, "message");
        var2 = constrc2.param(faultBean, "faultInfo");
        JVar var3 = constrc2.param(Throwable.class, "cause");
        constrc2.javadoc().addParam(var1);
        constrc2.javadoc().addParam(var2);
        constrc2.javadoc().addParam(var3);
        JBlock cb2 = constrc2.body();
        cb2.invoke("super").arg(var1).arg(var3);
        cb2.assign(fr, var2);


        //getFaultInfo() method
        JMethod fim = cls.method(JMod.PUBLIC, faultBean, "getFaultInfo");
        fim.javadoc().addReturn().add("returns fault bean: "+faultBean.fullName());
        JBlock fib = fim.body();
        fib._return(fi);
        fault.setExceptionClass(cls);

    }
}
