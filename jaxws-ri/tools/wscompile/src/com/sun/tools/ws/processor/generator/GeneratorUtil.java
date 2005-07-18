/*
 * $Id: GeneratorUtil.java,v 1.3 2005-07-18 18:13:57 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.processor.generator;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.xml.namespace.QName;

import com.sun.tools.ws.processor.model.Block;
import com.sun.tools.ws.processor.model.Fault;
import com.sun.tools.ws.processor.model.Operation;
import com.sun.tools.ws.processor.model.java.JavaStructureType;
import com.sun.tools.ws.processor.util.IndentingWriter;
import com.sun.tools.ws.processor.util.ProcessorEnvironment;
import com.sun.tools.ws.wsdl.document.schema.SchemaConstants;
import com.sun.xml.ws.encoding.soap.InternalEncodingConstants;
import com.sun.xml.ws.encoding.soap.SOAP12Constants;
import com.sun.xml.ws.encoding.soap.SOAPConstants;
import com.sun.xml.ws.encoding.soap.SOAPVersion;

/**
 *
 * @author WS Development Team
 */
public class GeneratorUtil implements GeneratorConstants {

    private static QName QNAME_SOAP_FAULT = null;
    private final static String MUST_UNDERSTAND_FAULT_MESSAGE_STRING =
        "SOAP must understand error";

    protected GeneratorUtil() {
    }

    protected GeneratorUtil(SOAPVersion ver) {
    }
/*
    public static String getQNameConstant(QName name) {
        return (String) typeMap.get(name);
    }*/
    public static void writeNewQName(IndentingWriter p, QName name)
        throws IOException {
/*        String qnameConstant = getQNameConstant(name);
        if (qnameConstant != null) {
            p.p(qnameConstant);
        } else {*/
            p.p(
                "new QName(\""
                    + name.getNamespaceURI()
                    + "\", \""
                    + name.getLocalPart()
                    + "\")");
//        }
    }


    public static void writeBlockQNameDeclaration(
        IndentingWriter p,
        Operation operation,
        Block block,
        Names names)
        throws IOException {
        String qname = names.getBlockQNameName(operation, block);
        p.p("private static final QName ");
        p.p(qname + " = ");
        writeNewQName(p, block.getName());
        p.pln(";");
    }

    public static void writeQNameDeclaration(
        IndentingWriter p,
        QName name,
        Names names)
        throws IOException {
        String qname = names.getQNameName(name);
        p.p("private static final QName ");
        p.p(qname + " = ");
        writeNewQName(p, name);
        p.pln(";");
    }

    public static void writeQNameTypeDeclaration(
        IndentingWriter p,
        QName name,
        Names names)
        throws IOException {
        String qname = names.getTypeQName(name);
        p.p("private static final QName ");
        p.p(qname + " = ");
        writeNewQName(p, name);
        p.pln(";");
    }

    public static boolean classExists(
        ProcessorEnvironment env,
        String className) {
        try {
            // Takes care of inner classes.
            String name = getLoadableClassName(className,
                env.getClassLoader());
            return true;
        } catch(ClassNotFoundException ce) {
        }
        return false;
    }

    public static String getLoadableClassName(
        String className,
        ClassLoader classLoader)
        throws ClassNotFoundException {

        try {
            Class seiClass = Class.forName(className, true, classLoader);
        } catch (ClassNotFoundException e) {
            int idx = className.lastIndexOf(DOTC);
            if (idx > -1) {
                String tmp = className.substring(0, idx) + SIG_INNERCLASS;
                tmp += className.substring(idx + 1);
                return getLoadableClassName(tmp, classLoader);
            }
            throw e;
        }
        return className;
    }    
    
    public static Hashtable ht = null;
    static {
        /* the primitive types have been preloaded */
        ht = new Hashtable();
        ht.put("int", "Integer.TYPE");
        ht.put("boolean", "Boolean.TYPE");
        ht.put("char", "Character.TYPE");
        ht.put("byte", "Byte.TYPE");
        ht.put("short", "Short.TYPE");
        ht.put("long", "Long.TYPE");
        ht.put("float", "Float.TYPE");
        ht.put("double", "Double.TYPE");
        ht.put("void", "Void.TYPE");

        /* here we load the condition for the primitive Array type */
        ht.put("int[]", "I");
        ht.put("boolean[]", "Z");
        ht.put("char[]", "C");
        ht.put("byte[]", "B");
        ht.put("short[]", "S");
        ht.put("long[]", "J");
        ht.put("float[]", "F");
        ht.put("double[]", "D");

    }

    public static class FaultComparator implements Comparator {
        private boolean sortName = false;
        public FaultComparator() {
        }
        public FaultComparator(boolean sortName) {
            this.sortName = sortName;
        }

        public int compare(Object o1, Object o2) {
            if (sortName) {
                QName name1 = ((Fault) o1).getBlock().getName();
                QName name2 = ((Fault) o2).getBlock().getName();
                // Faults that are processed by name first, then type 
                if (!name1.equals(name2)) {
                    return name1.toString().compareTo(name2.toString());
                }
            }
            JavaStructureType type1 = ((Fault) o1).getJavaException();
            JavaStructureType type2 = ((Fault) o2).getJavaException();
            int result = sort(type1, type2);
            return result;
        }

        protected int sort(JavaStructureType type1, JavaStructureType type2) {
            if (type1.getName().equals(type2.getName())) {
                return 0;
            }
            JavaStructureType superType;
            superType = type1.getSuperclass();
            while (superType != null) {
                if (superType.equals(type2)) {
                    return -1;
                }
                superType = superType.getSuperclass();
            }
            superType = type2.getSuperclass();
            while (superType != null) {
                if (superType.equals(type1)) {
                    return 1;
                }
                superType = superType.getSuperclass();
            }
            if (type1.getSubclasses() == null && type2.getSubclasses() != null)
                return -1;
            if (type1.getSubclasses() != null && type2.getSubclasses() == null)
                return 1;
            if (type1.getSuperclass() != null
                && type2.getSuperclass() == null) {
                return 1;
            }
            if (type1.getSuperclass() == null
                && type2.getSuperclass() != null) {
                return -1;
            }
            return type1.getName().compareTo(type2.getName());
        }
    }
/*
    public static class SubclassComparator implements Comparator {
        public SubclassComparator() {
        }

        public int compare(Object o1, Object o2) {
            JavaStructureType type1 = (JavaStructureType) o1;
            JavaStructureType type2 = (JavaStructureType) o2;
            return sort(type1, type2);
        }

        protected int sort(JavaStructureType type1, JavaStructureType type2) {
            JavaStructureType parent = type1;
            while (parent.getSuperclass() != null) {
                parent = parent.getSuperclass();
                if (parent.equals(type2))
                    return -1;
            }
            parent = type2;
            while (parent.getSuperclass() != null) {
                parent = parent.getSuperclass();
                if (parent.equals(type1))
                    return 1;
            }
            return type1.getName().compareTo(type2.getName());
        }
    }
*/
}
