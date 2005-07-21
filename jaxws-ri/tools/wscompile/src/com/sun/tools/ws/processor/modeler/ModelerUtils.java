/*
 * $Id: ModelerUtils.java,v 1.3 2005-07-21 19:53:23 vivekp Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.ws.processor.modeler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import com.sun.tools.xjc.api.S2JJAXBModel;
import com.sun.tools.xjc.api.TypeAndAnnotation;
import com.sun.tools.ws.processor.model.AbstractType;
import com.sun.tools.ws.processor.model.Block;
import com.sun.tools.ws.processor.model.ModelProperties;
import com.sun.tools.ws.processor.model.Parameter;
import com.sun.tools.ws.processor.model.java.JavaSimpleType;
import com.sun.tools.ws.processor.model.java.JavaStructureMember;
import com.sun.tools.ws.processor.model.java.JavaStructureType;
import com.sun.tools.ws.processor.model.java.JavaType;
import com.sun.tools.ws.processor.model.jaxb.*;
import com.sun.tools.ws.util.ClassNameInfo;
import com.sun.tools.ws.wsdl.document.Message;
import com.sun.tools.ws.wsdl.document.MessagePart;

/**
 * Utilities to be used by modelers such as WSDLModeler and Rmimodeler
 *
 * @author Vivek Pandey
 *
 */
public class ModelerUtils {

    /**
     *
     */
    public ModelerUtils() {
        super();
    }

    /**
     * This method should be called incase of wrapper style operations. This is
     * equivalent to wrapper style schema component or JAXB Mapping object.
     *
     * @param jaxbType JAXBType from which a JAXBStructured type will be created.
     * @return returns JAXBStructured type
     */
    public static JAXBStructuredType createJAXBStructureType(JAXBType jaxbType) {
        JAXBStructuredType type = new JAXBStructuredType(jaxbType);
        type.setName(jaxbType.getName());
        type.setJavaType(jaxbType.getJavaType());
        return type;
    }

    /**
     * This method uses JAXBStructured type (wrapper style operations) and
     * unwraps it to create list of parameters.
     *
     *
     * @param jaxbType instance of JAXBType, could be JAXBStructured type.
     * @param block The Block (body/Header/Attachment) to which the created Parameter belong.
     * @return list of Parameters
     */
    public static List<Parameter> createUnwrappedParameters(JAXBType jaxbType,
            Block block) {
        List<Parameter> paramList = new ArrayList<Parameter>();
        JAXBStructuredType type = null;
        if (!(jaxbType instanceof JAXBStructuredType))
            type = createJAXBStructureType(jaxbType);
        else
            type = (JAXBStructuredType) jaxbType;

        JavaStructureType jst = new JavaStructureType(jaxbType.getJavaType()
                .getRealName(), true, type);
        type.setJavaType(jst);
        block.setType(type);
        List memberList = jaxbType.getWrapperChildren();
        Iterator props = memberList.iterator();
        java.util.Set<String> propNames = new java.util.HashSet<String>();
        while (props.hasNext()) {
            JAXBProperty prop = (JAXBProperty) props.next();
            paramList.add(createUnwrappedParameter(prop, jaxbType, block, type,
                    jst));
        }

        return paramList;
    }

    /**
     * @param prop
     * @param jaxbType
     * @param block
     * @return
     */
    private static Parameter createUnwrappedParameter(JAXBProperty prop,
            JAXBType jaxbType, Block block, JAXBStructuredType type,
            JavaStructureType jst) {
        QName elementName = prop.getElementName();
        JavaType javaType = new JavaSimpleType(prop.getType());
        JAXBElementMember eType = new JAXBElementMember(elementName, jaxbType);
        JavaStructureMember jsm = new JavaStructureMember(elementName
                .getLocalPart(), javaType, eType);
        eType.setJavaStructureMember(jsm);
        jst.add(jsm);
        eType.setProperty(prop);
        type.add(eType);
        JAXBType t = new JAXBType(elementName, javaType, jaxbType
                .getJaxbMapping(), jaxbType.getJaxbModel());
        t.setUnwrapped(true);
        Parameter parameter = createParameter(elementName.getLocalPart(), t, block);
        parameter.setEmbedded(true);
        return parameter;
    }

    public static List<Parameter> createRpcLitParameters(Message message, Block block, S2JJAXBModel jaxbModel){
        RpcLitStructure rpcStruct = (RpcLitStructure)block.getType();

        List<Parameter> parameters = new ArrayList<Parameter>();
        for(MessagePart part : message.getParts()){
            if(part.getBindingExtensibilityElementKind() == MessagePart.PART_NOT_BOUNDED)
                continue;
            QName name = part.getDescriptor();
            TypeAndAnnotation typeAndAnn = jaxbModel.getJavaType(name);
            if(typeAndAnn == null)
                continue;
            String type = typeAndAnn.getTypeClass().fullName();
            type = ClassNameInfo.getGenericClass(type);
            RpcLitMember param = new RpcLitMember(new QName("", part.getName()), type);
            JavaType javaType = new JavaSimpleType(new JAXBTypeAndAnnotation(typeAndAnn));
            param.setJavaType(javaType);
            rpcStruct.addRpcLitMember(param);
            Parameter parameter = ModelerUtils.createParameter(part.getName(), param, block);
            parameter.setEmbedded(true);
            parameters.add(parameter);
        }
        return parameters;
    }

    /**
     * Called for non-wrapper style operations. It returns a Parameter constructed
     * using the JAXBType and the Block.
     *
     * @param partName typically wsdl:part or any name to be given to the parameter
     * @param jaxbType type of Parameter
     * @param block Block to which the parameter belongs to
     * @return Parameter created.
     */
    public static Parameter createParameter(String partName, AbstractType jaxbType,
            Block block) {
        Parameter parameter = new Parameter(partName);
        parameter.setProperty(ModelProperties.PROPERTY_PARAM_MESSAGE_PART_NAME,
                partName);
        parameter.setEmbedded(false);
        parameter.setType(jaxbType);        
        parameter.setTypeName(jaxbType.getJavaType().getType().getName());
        parameter.setBlock(block);
        //add jaxb type annotations
        if(jaxbType instanceof JAXBType){
            JAXBMapping jaxbMapping = ((JAXBType)jaxbType).getJaxbMapping();
        }
        return parameter;
    }

    /**
     * Get Parameter from the list of parameters.
     *
     * @param paramName
     * @param parameters
     * @return
     */
    public static Parameter getParameter(String paramName, List<Parameter> parameters){
        if(parameters == null)
            return null;
        for(Parameter param: parameters){
            //if(param.getName().equals("_return") && paramName.equals("return") || param.getName().equals(paramName)) {
            if(param.getName().equals(paramName)){
                return param;
            }
        }
        return null;
    }

    /**
     * Compares two JAXBStructures.
     *
     * @param struct1
     * @param struct2
     * @return true if struct1 and struct2 are equivalent.
     */
    public static boolean isEquivalentLiteralStructures(
        JAXBStructuredType struct1,
        JAXBStructuredType struct2) {
        if (struct1.getElementMembersCount() != struct2.getElementMembersCount())
            return false;
        Iterator members = struct1.getElementMembers();
        JAXBElementMember member1;
        JavaStructureMember javaMember1, javaMember2;
        for (int i = 0; members.hasNext(); i++) {
            member1 = (JAXBElementMember)members.next();
            javaMember1 = member1.getJavaStructureMember();
            javaMember2 =
                ((JavaStructureType)struct2.getJavaType()).getMemberByName(
                    member1.getJavaStructureMember().getName());
            if (javaMember2.getConstructorPos() != i
                || !javaMember1.getType().equals(javaMember2.getType())) {
                return false;
            }
        }
        return false;
    }
}
