package com.sun.xml.ws.db.xmlbeans;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import com.sun.xml.bind.v2.schemagen.xmlschema.ComplexType;
import com.sun.xml.bind.v2.schemagen.xmlschema.Element;
import com.sun.xml.bind.v2.schemagen.xmlschema.ExplicitGroup;
import com.sun.xml.bind.v2.schemagen.xmlschema.Occurs;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.spi.db.ServiceArtifactSchemaGenerator;
import com.sun.xml.ws.wsdl.writer.document.xsd.Schema;
import com.sun.xml.ws.db.xmlbeans.FaultInfo.PropInfo;

/**
 * XmlBeansWrapperSchema
 * 
 * @author shih-chang.chen@oracle.com
 */
public class XmlBeansServiceArtifactSchemaGenerator extends ServiceArtifactSchemaGenerator {
    
    XMLBeansContext context;
    
    public XmlBeansServiceArtifactSchemaGenerator(SEIModel model, XMLBeansContext cxt) {
        super(model);
        context = cxt;
    }
    
    Schema initSchema(HashMap<String, Schema> xsds, String tns) {
        Schema xsd = xsds.get(tns);
        if (xsd == null) {
            xsd = create(tns);
            xsd.targetNamespace(tns);
            xsd._attribute("elementFormDefault", "qualified");
            xsds.put(tns, xsd);
        }   
        return xsd;
    }

    /**
     * Inserts the array types
     */
    protected void postInit(HashMap<String, Schema> xsds) {  
        HashMap<String, Set<String>> imports = new HashMap<String, Set<String>>(); 
        for(FaultInfo expInfo : context.faultInfos.values()) {
            if (XMLBeansContext.globalElementName(expInfo.implModel.getDetailBean()) != null) continue;
            String tns = expInfo.implModel.getDetailType().tagName.getNamespaceURI();
            Schema xsd = initSchema(xsds, tns);
            if (expInfo.detailInfo != null) {
                QName typeName = context.getTypeName(expInfo.detailInfo.typeInfo);
                if (typeName!= null)addImport(typeName.getNamespaceURI(), tns, imports);
            } else {
                for (PropInfo p : expInfo.propInfo ) {
                    QName typeName = context.getTypeName(p.typeInfo);
                    if (typeName!= null)addImport(typeName.getNamespaceURI(), tns, imports);
                }
            }
        }
        for(JavaBeanInfo jbi: context.javaBeanInfos.values()) {
            String tns = jbi.typeName.getNamespaceURI();
            Schema xsd = initSchema(xsds, tns);
            for(JavaBeanInfo.PropInfo prop: jbi.properties) {
                QName typeName = context.getTypeName(prop.typeInfo);
                if (typeName!= null)addImport(typeName.getNamespaceURI(), tns, imports);
            }
        }
        for(String tns : imports.keySet()) {
            Set<String> importSet = imports.get(tns);
            Schema xsd = xsds.get(tns);
            for(String nsToImport : importSet) xsd._namespace(nsToImport, true);
            for(String nsToImport : importSet) {
                com.sun.xml.ws.wsdl.writer.document.xsd.Import imp = xsd._import();
                imp.namespace(nsToImport);
            }            
        }
        
        for(ArrayInfo arrayInfo : context.arrayInfos.values()) {
            Schema xsd = xsds.get(arrayInfo.typeName.getNamespaceURI());  
            Element e =  xsd._element(Element.class);
            e._attribute("name", arrayInfo.typeName.getLocalPart());
            e.type(arrayInfo.typeName);
            ComplexType ct =  xsd._element(ComplexType.class);
            ct._attribute("name", arrayInfo.typeName.getLocalPart());
            ExplicitGroup sq = ct.sequence();     
            Occurs child = addChild(sq, arrayInfo.itemName, arrayInfo.itemTypeInfo);
            child.minOccurs(0);
            child.maxOccurs("unbounded");
        }
        HashSet<Class> done = new HashSet<Class>();
        for(FaultInfo expInfo : context.faultInfos.values()) {
            if (done.contains(expInfo.implModel.getExceptionClass())) continue;
            else done.add(expInfo.implModel.getExceptionClass());
            String tns = expInfo.implModel.getDetailType().tagName.getNamespaceURI();
            Schema xsd = xsds.get(tns); 
            if (expInfo.detailInfo != null) {
                QName typeName = context.getTypeName(expInfo.detailInfo.typeInfo);
                if (typeName!= null) {   
                    Element e =  xsd._element(Element.class);
                    e._attribute("name", expInfo.detailInfo.typeInfo.tagName.getLocalPart());    
                    e.type(typeName);
                }
            } else {  
                Element e =  xsd._element(Element.class);
                String name =  expInfo.implModel.getDetailType().tagName.getLocalPart();
                e._attribute("name", name);   
                e.type(new QName(tns, name));
                ComplexType ct =  xsd._element(ComplexType.class);
                ct._attribute("name", name);
                ExplicitGroup sq = ct.sequence();     
                for (PropInfo p : expInfo.propInfo ) addChild(sq, p.typeInfo.tagName, p.typeInfo);
            }
        }

        for(JavaBeanInfo jbi: context.javaBeanInfos.values()) {
            Schema xsd = xsds.get(jbi.typeName.getNamespaceURI()); 
            ComplexType ct =  xsd._element(ComplexType.class);
            ct._attribute("name", jbi.typeName.getLocalPart());
            ExplicitGroup sq = ct.sequence();     
            for(JavaBeanInfo.PropInfo prop: jbi.properties) {
                Occurs child = addChild(sq, prop.typeInfo.tagName, prop.typeInfo);
                child.minOccurs(1);
                child._attribute("nillable", !((Class)prop.typeInfo.type).isPrimitive());
            }
        }
    }
    
    private void addImport(String nsToImport, String tns, HashMap<String, Set<String>> imports) {
        if (nsToImport != null && !nsToImport.equals(tns) && !nsToImport.equals("http://www.w3.org/2001/XMLSchema")) {
            Set<String> importSet = imports.get(tns);
            if (importSet == null) {
                importSet = new HashSet<String>();
                imports.put(tns, importSet);
            }
            importSet.add(nsToImport);
        }
    }
}
