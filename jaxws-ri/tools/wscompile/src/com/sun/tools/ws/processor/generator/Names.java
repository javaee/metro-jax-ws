/*
 * $Id: Names.java,v 1.2 2005-07-18 18:13:58 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.processor.generator;

import com.sun.tools.ws.processor.model.*;
import com.sun.tools.ws.processor.model.java.JavaInterface;
import com.sun.tools.ws.processor.model.java.JavaStructureMember;
import com.sun.tools.ws.processor.model.java.JavaType;
import com.sun.tools.ws.processor.model.jaxb.JAXBProperty;
import com.sun.tools.ws.processor.util.DirectoryUtil;
import com.sun.tools.ws.processor.util.ProcessorEnvironment;
import com.sun.tools.ws.util.ClassNameInfo;
import com.sun.xml.ws.streaming.PrefixFactory;
import com.sun.xml.ws.streaming.PrefixFactoryImpl;
import com.sun.xml.ws.util.StringUtils;

import javax.xml.namespace.QName;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Names provides utility methods used by other wscompile classes
 * for dealing with identifiers.
 *
 * @author WS Development Team
 */
public class Names implements GeneratorConstants{

    public Names() {
    }

    /**
     * Return stub class name for impl class name.
     */
    public String stubFor(Port port) {
        return stubFor((Port) port, null);
    }

    public String stubFor(Port port, String infix) {
        String result =
            (String) port.getProperty(ModelProperties.PROPERTY_STUB_CLASS_NAME);
        if (result == null) {
            result =
                makeDerivedClassName(
                    port.getJavaInterface(),
                    STUB_SUFFIX,
                    infix);
        }
        return result;
    }

    public String clientContactInfoFor(Port port) {
        return clientContactInfoFor((Port) port, null);
    }

    public String clientContactInfoFor(Port port, String infix) {
        String result =
            (String) port.getProperty(
                ModelProperties.PROPERTY_CLIENT_CONTACTINFOLIST_CLASS_NAME);
        if (result == null) {
            result =
                makeDerivedClassName(
                    port.getJavaInterface(),
                    CLIENT_CONTACTINFOLIST_SUFFIX,
                    infix);
        }
        return result;
    }

    public String delegateFor(Port port) {
        return delegateFor((Port) port, null);
    }

    public String delegateFor(Port port, String infix) {
        String result =
            (String) port.getProperty(
                ModelProperties.PROPERTY_DELEGATE_CLASS_NAME);
        if (result == null) {
            result =
                makeDerivedClassName(
                    port.getJavaInterface(),
                    CLIENT_DELEGATE_SUFFIX,
                    infix);
        }
        return result;
    }


    /**
     * Return skeleton class name for impl class name.
     */
//    public String skeletonFor(JavaInterface javaInterface) {
//        String name =
//            ClassNameInfo.replaceInnerClassSym(javaInterface.getRealName());
//        return name + SKELETON_SUFFIX;
//    }

    /**
     * Return tie class name for impl class name.
     */
    public String tieFor(Port port) {
        return tieFor(port, serializerNameInfix);
    }

    public String tieFor(Port port, String infix) {
        String result =
            (String) port.getProperty(ModelProperties.PROPERTY_TIE_CLASS_NAME);
        if (result == null) {
            result =
                makeDerivedClassName(
                    port.getJavaInterface(),
                    TIE_SUFFIX,
                    infix);
        }
        return result;
    }

    public String makeDerivedClassName(
        JavaInterface javaInterface,
        String suffix,
        String infix) {

        String name =
            ClassNameInfo.replaceInnerClassSym(javaInterface.getRealName());
        return name + (infix == null ? "" : UNDERSCORE + infix) + suffix;
    }

    public static String getPortName(Port port) {
        String javaPortName =
            (String) port.getProperty(ModelProperties.PROPERTY_JAVA_PORT_NAME);
        if (javaPortName != null) {
            return javaPortName;
        } else {
            QName portName =
                (QName) port.getProperty(
                    ModelProperties.PROPERTY_WSDL_PORT_NAME);
            if (portName != null) {
                return portName.getLocalPart();
            } else {
                String name = stripQualifier(port.getJavaInterface().getName());
                return ClassNameInfo.replaceInnerClassSym(name);
            }
        }
    }

    public static String stripQualifier(Class classObj) {
        String name = classObj.getName();
        return stripQualifier(name);
    }

    public static String stripQualifier(String name) {
        return ClassNameInfo.getName(name);
    }

    public static String getPackageName(String className) {
        String packageName = ClassNameInfo.getQualifier(className);
        return packageName != null ? packageName : "";
    }

    public static String getUnqualifiedClassName(String className) {
        return ClassNameInfo.getName(className).replace('$', '.');
    }

    /**
     * Return the File object that should be used as the source file
     * for the given Java class, using the supplied destination
     * directory for the top of the package hierarchy.
     */
    public File sourceFileForClass(
        String className,
        String outputClassName,
        File destDir,
        ProcessorEnvironment env)
        throws GeneratorException {
        File packageDir =
            DirectoryUtil.getOutputDirectoryFor(className, destDir, env);
        String outputName = stripQualifier(outputClassName);

        String outputFileName = outputName + JAVA_SRC_SUFFIX;
        return new File(packageDir, outputFileName);
    }

    public String typeClassName(JavaType type) {
        String typeName = type.getName();
        return typeName;
    }

    public static String getPackageName(Service service) {
        String portPackage =
            getPackageName(service.getJavaInterface().getName());
        return portPackage;
    }

    public String customJavaTypeClassName(JavaInterface intf) {
        String intName = intf.getName();
        return intName;
    }

    public String customJavaTypeClassName(AbstractType type) {
        String typeName = type.getJavaType().getName();
        return typeName;
    }

    private String customJavaTypeClassName(String typeName) {
        return typeName;
    }

    public String customExceptionClassName(Fault fault) {
        String typeName = fault.getJavaException().getName();
        return typeName;
    }

    public String getExceptionClassMemberName(){
        return FAULT_CLASS_MEMBER_NAME;
    }

    public String interfaceImplClassName(
        JavaInterface intf) {
        String intName = intf.getName() + IMPL_SUFFIX;
        return intName;
    }

    /* (non-Javadoc)
     * @see com.sun.xml.rpc.processor.generator.Names#holderClassName(com.sun.xml.rpc.processor.model.Port, com.sun.xml.rpc.processor.model.AbstractType)
     */
    public String holderClassName(Port port, AbstractType type) {
        String typeName = SimpleToBoxedUtil.getBoxedClassName(type.getJavaType().getName());
        return "javax.xml.ws.Holder<"+typeName+">";
    }

    /* (non-Javadoc)
     * @see com.sun.xml.rpc.processor.generator.Names#holderClassName(com.sun.xml.rpc.processor.model.Port, com.sun.xml.rpc.processor.model.java.JavaType)
     */
    public String holderClassName(Port port, JavaType type) {
        String typeName = SimpleToBoxedUtil.getBoxedClassName(type.getName());
        return "javax.xml.ws.Holder<"+typeName+">";
    }
    /* (non-Javadoc)
     * @see com.sun.xml.rpc.processor.generator.Names#holderClassName(com.sun.xml.rpc.processor.model.Port, java.lang.String)
     */
    protected String holderClassName(Port port, String typeName) {
        typeName = SimpleToBoxedUtil.getBoxedClassName(typeName);
        return "javax.xml.ws.Holder<"+typeName+">";
    }
    public static boolean isInJavaOrJavaxPackage(String typeName) {
        return typeName.startsWith(JAVA_PACKAGE_PREFIX)
            || typeName.startsWith(JAVAX_PACKAGE_PREFIX);
    }

    public String memberName(String name) {
        return (MEMBER_PREFIX + name).replace('.', '$');
    }

    public String getClassMemberName(String className) {
        className = getUnqualifiedClassName(className);
        return memberName(className);
    }

    public String getClassMemberName(
        String className,
        AbstractType type,
        String suffix) {
        className = getUnqualifiedClassName(className);
        String additionalClassName =
            type.getJavaType().getName().replace('.', '_');
        int idx = additionalClassName.indexOf('[');
        if (idx > 0)
            additionalClassName = additionalClassName.substring(0, idx);
        return memberName(
            getPrefix(type.getName())
                + UNDERSCORE
                + validJavaName(type.getName().getLocalPart())
                + "__"
                + additionalClassName
                + UNDERSCORE
                + className
                + suffix);
    }

    public String getClassMemberName(String className, AbstractType type) {
        className = getUnqualifiedClassName(className);
        return getClassMemberName(
            getPrefix(type.getName())
                + UNDERSCORE
                + validJavaName(type.getName().getLocalPart())
                + "__"
                + className);
    }

    public String getTypeMemberName(AbstractType type) {
        return getTypeMemberName(type.getJavaType());
    }

    public String getTypeMemberName(JavaType javaType) {
        String typeName = javaType.getRealName();
        return getTypeMemberName(typeName);
    }

    /* this fix was implemented with respect to the fact that
       for StringArray ... when wsdl -> javamapping happens and
       the generated code would create variables[]name : which
       which were wrong */
    public String getTypeMemberName(String typeName) {
        typeName = getUnqualifiedClassName(typeName);
        int i = 0;
        while (typeName.endsWith(BRACKETS)) {
            typeName = typeName.substring(0, typeName.length() - 2);
            i++;
        }
        for (; i < 0; i--)
            typeName += ARRAY;

        return memberName(typeName);
    }

    public String getOPCodeName(String name) {
        String qname = name + OPCODE_SUFFIX;
        return validInternalJavaIdentifier(qname);
    }

    public String getQNameName(QName name) {
        String qname =
            getPrefix(name) + UNDERSCORE + name.getLocalPart() + QNAME_SUFFIX;
        return validInternalJavaIdentifier(qname);
    }

    public String getBlockQNameName(Operation operation, Block block) {
        QName blockName = block.getName();
        String qname = getPrefix(blockName);
        if (operation != null)
            qname += UNDERSCORE + operation.getUniqueName();
        qname += UNDERSCORE + blockName.getLocalPart() + QNAME_SUFFIX;
        return validInternalJavaIdentifier(qname);
    }

    public void setJavaStructureMemberMethodNames(JavaStructureMember javaMember) {
        javaMember.setReadMethod(getJavaMemberReadMethod(javaMember));
        javaMember.setWriteMethod(getJavaMemberWriteMethod(javaMember));
    }

    public String getBlockUniqueName(Operation operation, Block block) {
        QName blockName = block.getName();
        String qname = getPrefix(blockName);
        if (operation != null)
            qname += UNDERSCORE + operation.getUniqueName();
        qname += UNDERSCORE + blockName.getLocalPart();
        return validInternalJavaIdentifier(qname);
    }

    public String getTypeQName(QName name) {
        String qname =
            getPrefix(name)
                + UNDERSCORE
                + name.getLocalPart()
                + TYPE_QNAME_SUFFIX;
        return validInternalJavaIdentifier(qname);
    }

    /* (non-Javadoc)
     * @see com.sun.xml.rpc.processor.generator.Names#validJavaClassName(java.lang.String)
     */
    public String validJavaClassName(String name) {
        return com.sun.tools.xjc.api.XJC.mangleNameToClassName(name);
    }

    /* (non-Javadoc)
     * @see com.sun.xml.rpc.processor.generator.Names#validJavaMemberName(java.lang.String)
     */
    public String validJavaMemberName(String name) {
        return com.sun.tools.xjc.api.XJC.mangleNameToVariableName(name);    
    }

    public String validJavaPackageName(String name) {
        return validJavaName(StringUtils.decapitalize(name));
    }

    public String getIDObjectResolverName(String name) {
        return validJavaClassName(name) + "IDObjectResolver";
    }
    public String validInternalJavaIdentifier(String name) {
        // return a valid java identifier without dropping characters (i.e. do not apply
        // the mapping of XML names to Java identifiers in the spec); it's only meant
        // to be used to generate internal identifiers (e.g. variable names in generated code)

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < name.length(); ++i) {
            char ch = name.charAt(i);
            if (i == 0) {
                if (Character.isJavaIdentifierStart(ch)) {
                    sb.append(ch);
                } else {
                    sb.append("_$");
                    sb.append(Integer.toHexString((int) ch));
                    sb.append("$");
                }
            } else {
                if (Character.isJavaIdentifierPart(ch)) {
                    sb.append(ch);
                } else {
                    sb.append("$");
                    sb.append(Integer.toHexString((int) ch));
                    sb.append("$");
                }
            }
        }

        String id = sb.toString();

        String tmp = (String) reservedWords.get(id);
        if (tmp != null)
            id = tmp;
        return id;
    }

    public String validExternalJavaIdentifier(String name) {
        return validInternalJavaIdentifier(name).replace('$', '_');
    }

    public String validJavaName(String name) {
        name = wordBreakString(name);
        name = removeWhiteSpace(name);

        String tmp = (String) reservedWords.get(name);
        if (tmp != null)
            name = tmp;
        return name;
    }

    public boolean isJavaReservedWord(String name) {
        return reservedWords.get(name) != null;
    }

    /* here we check on wether return values datatype is
       boolean. If its boolean, instead of a get method
       its set a is<MethodName> to comply with JavaBeans
       Pattern spec */
    public String getJavaMemberReadMethod(JavaStructureMember member) {
        String return_value = null;
        if ((member.getType().getRealName()) == "boolean") {
            return_value = IS + StringUtils.capitalize(member.getName());
        } else {
            return_value = GET + StringUtils.capitalize(member.getName());
        }
        return (return_value);
    }

    public String getJavaMemberWriteMethod(JavaStructureMember member) {
        return SET + StringUtils.capitalize(member.getName());
    }

    public static String getResponseName(String messageName) {
        return messageName + RESPONSE;
    }


    public String getJavaReadMethod(JAXBProperty prop){
        if(prop.getType().equals("boolean"))
            return IS + StringUtils.capitalize(prop.getName());
        return getJavaReadMethod(prop.getName());
    }

    public String getJavaWriteMethod(JAXBProperty prop){
        return getJavaWriteMethod(prop.getName());
    }

    public String getJavaReadMethod(String prop){
        return GET + StringUtils.capitalize(prop);
    }

    public String getJavaWriteMethod(String prop){
        return SET + StringUtils.capitalize(prop);
    }

    public String removeWhiteSpace(String str) {
        String tmp = removeCharacter(' ', str);
        return tmp;
    }

    public String wordBreakString(String str) {
        StringBuffer buf = new StringBuffer(str);
        char ch;
        for (int i = 0; i < buf.length(); i++) {
            ch = buf.charAt(i);
            if (Character.isDigit(ch)) {
                if (i + 1 < buf.length()
                    && !Character.isDigit(buf.charAt(i + 1))) {
                    buf.insert(1 + i++, ' ');
                }
            } else if (Character.isSpaceChar(ch) || ch == '_') {
                continue;
            } else if (!Character.isJavaIdentifierPart(ch)) {
                buf.setCharAt(i, ' ');
            } else if (!Character.isLetter(ch)) {
                buf.setCharAt(i, ' ');
            }
        }
        return buf.toString();
    }

    public String removeCharacter(int ch, String str) {
        String tmp;
        int idx = str.indexOf(ch);
        while (idx >= 0) {
            str =
                str.substring(0, idx)
                    + StringUtils.capitalize(str.substring(idx + 1).trim());
            idx = str.indexOf(' ');
        }

        return str;
    }

    public String getPrefix(QName name) {
        return getPrefix(name.getNamespaceURI());
    }

    public String getPrefix(String uri) {
        return prefixFactory.getPrefix(uri);
    }

    public void resetPrefixFactory() {
        prefixFactory = new PrefixFactoryImpl(NS_PREFIX);
    }

    public void setSerializerNameInfix(String serNameInfix) {
        if (serNameInfix != null && serNameInfix.length() > 0)
            serializerNameInfix = UNDERSCORE + serNameInfix;
    }

    public String getSerializerNameInfix() {
        // Fix for bug 4811625 and 4778136, undoing what setter does (remove beginning underscore)
        String str = serializerNameInfix;
        if ((serializerNameInfix != null)
            && (serializerNameInfix.charAt(0) == '_'))
            str = serializerNameInfix.substring(1);
        return str;
    }

    //bug fix: 4865124
    public static String getAdjustedURI(String namespaceURI, String pkgName) {
        if (pkgName == null)
            return namespaceURI;
        else if (namespaceURI == null)
            return pkgName;

        int length = namespaceURI.length();
        int i = namespaceURI.lastIndexOf('/');
        if (i == -1) {
            //check if its URN
            i = namespaceURI.lastIndexOf(':');
            if (i == -1) {
                //not a URI or URN pattern, return pkgName
                return pkgName;
            }

        }

        if ((i != -1) && (i + 1 == length)) {
            return namespaceURI + pkgName;
        } else {
            int j = namespaceURI.indexOf('.', i);
            if (j != -1)
                return namespaceURI.substring(0, i + 1) + pkgName;
            else
                return namespaceURI + "/" + pkgName;
        }
    }

    protected String serializerNameInfix = null;
    protected PrefixFactory prefixFactory = new PrefixFactoryImpl(NS_PREFIX);
    protected static Map reservedWords;

    static {
        reservedWords = new HashMap();
        reservedWords.put("abstract", "_abstract");
        reservedWords.put("assert", "_assert");
        reservedWords.put("boolean", "_boolean");
        reservedWords.put("break", "_break");
        reservedWords.put("byte", "_byte");
        reservedWords.put("case", "_case");
        reservedWords.put("catch", "_catch");
        reservedWords.put("char", "_char");
        reservedWords.put("class", "_class");
        reservedWords.put("const", "_const");
        reservedWords.put("continue", "_continue");
        reservedWords.put("default", "_default");
        reservedWords.put("do", "_do");
        reservedWords.put("double", "_double");
        reservedWords.put("else", "_else");
        reservedWords.put("extends", "_extends");
        reservedWords.put("false", "_false");
        reservedWords.put("final", "_final");
        reservedWords.put("finally", "_finally");
        reservedWords.put("float", "_float");
        reservedWords.put("for", "_for");
        reservedWords.put("goto", "_goto");
        reservedWords.put("if", "_if");
        reservedWords.put("implements", "_implements");
        reservedWords.put("import", "_import");
        reservedWords.put("instanceof", "_instanceof");
        reservedWords.put("int", "_int");
        reservedWords.put("interface", "_interface");
        reservedWords.put("long", "_long");
        reservedWords.put("native", "_native");
        reservedWords.put("new", "_new");
        reservedWords.put("null", "_null");
        reservedWords.put("package", "_package");
        reservedWords.put("private", "_private");
        reservedWords.put("protected", "_protected");
        reservedWords.put("public", "_public");
        reservedWords.put("return", "_return");
        reservedWords.put("short", "_short");
        reservedWords.put("static", "_static");
        reservedWords.put("strictfp", "_strictfp");
        reservedWords.put("super", "_super");
        reservedWords.put("switch", "_switch");
        reservedWords.put("synchronized", "_synchronized");
        reservedWords.put("this", "_this");
        reservedWords.put("throw", "_throw");
        reservedWords.put("throws", "_throws");
        reservedWords.put("transient", "_transient");
        reservedWords.put("true", "_true");
        reservedWords.put("try", "_try");
        reservedWords.put("void", "_void");
        reservedWords.put("volatile", "_volatile");
        reservedWords.put("while", "_while");
    }
}
