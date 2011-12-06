package com.sun.xml.ws.db.sdo;

import commonj.sdo.Property;
import commonj.sdo.Type;
import commonj.sdo.helper.HelperContext;
import commonj.sdo.helper.XSDHelper;
import org.eclipse.persistence.sdo.SDOType;
import org.eclipse.persistence.sdo.helper.CodeWriter;
import org.eclipse.persistence.sdo.helper.SDOClassGenerator;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: giglee
 * Date: May 14, 2009
 * Time: 11:11:25 AM
 * To change this template use File | Settings | File Templates.
 */

/**
 * This class contains the result of java type mapping on a set of schemas.
 * It is only aware of the global elements for now.
 */
public class Xsd2JavaSDOModel {


    private List<SDOType> types = null;
    private HelperContext context = null;

    public Xsd2JavaSDOModel(HelperContext context, List<SDOType> types) {
        this.context = context;
        this.types = types;        
    }

    /**
     * write the java class to the code writer, see toplink CodeWriter interface
     * @param cw
     */
    public void generateCode(CodeWriter cw) {
        if (types == null) {
            return;
        }
        SDOClassGenerator generator = new SDOClassGenerator(context);
        generator.generate(cw, types);
    }

    /**
     * Receive a list of java classes modeled by this xsd2java model
     * @return
     */
    public List<String> getClassList() {
        // the model only needs to know the interface class???
        List<String> list = new ArrayList<String>();
        for (SDOType type : types) {
            list.add(type.getInstanceClassName());
        }
        return list;
    }

    /**
     * Return the type qname used to define this java class
     *
     * @param javaClass
     * @return
     */
    public QName getXsdTypeName(String javaClass) {
        for (SDOType type : types) {
            if (type.getInstanceClassName().equals(javaClass)) {
                return ((SDOType) type).getQName();
            }
        }
        return null;
    }

    /**
     * return the java type used for the element, only Global elements can be located.
     * Containing types are not searched
     *
     * @param qname
     * @return
     */
    public String getJavaTypeForElementName(QName qname) {
        XSDHelper xsdHelper = context.getXSDHelper();
        Property globalProperty = xsdHelper.getGlobalProperty(qname.getNamespaceURI(), qname.getLocalPart(), true);
        if (globalProperty == null) {
           throw new RuntimeException("Given element with name: " + qname + "is not found."); 
        }
        Type elementType = globalProperty.getType();
        if (elementType == null) {
            throw new RuntimeException("Given element with name: " + qname + "is not found.");
        }
        return ((SDOType) elementType).getInstanceClassName();
    }

    /**
     * return the java type for a given xsd type
     * @param name
     * @return
     */
    public String getJavaTypeForElementType(QName name) {
        if (types != null) {
            for (SDOType type : types) {
                QName qname = type.getXsdType();
                if (qname != null && qname.equals(name)) {
                    return type.getInstanceClassName();
                }
            }
        }
        return null;
    }
    
}
