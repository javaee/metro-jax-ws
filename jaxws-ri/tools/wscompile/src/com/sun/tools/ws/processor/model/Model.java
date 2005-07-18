/*
 * $Id: Model.java,v 1.2 2005-07-18 18:13:59 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.processor.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.EnumSet;
import javax.xml.namespace.QName;

import com.sun.tools.ws.processor.model.jaxb.JAXBModel;
import com.sun.tools.ws.processor.ProcessorActionVersion;
//import com.sun.xml.rpc.processor.config.ImportedDocumentInfo;

/**
 *
 * @author WS Development Team
 */
public class Model extends ModelObject {

    public Model() {
    }

    public Model(QName name) {
        this.name = name;
    }

    public QName getName() {
        return name;
    }

    public void setName(QName n) {
        name = n;
    }

    public String getTargetNamespaceURI() {
        return targetNamespace;
    }

    public void setTargetNamespaceURI(String s) {
        targetNamespace = s;
    }

    public void addService(Service service) {
        if (servicesByName.containsKey(service.getName())) {
            throw new ModelException("model.uniqueness");
        }
        services.add(service);
        servicesByName.put(service.getName(), service);
    }

    public Iterator getServices() {
        return services.iterator();
    }

    public Service getServiceByName(QName name) {
        if (servicesByName.size() != services.size()) {
            initializeServicesByName();
        }
        return (Service)servicesByName.get(name);
    }

    /* serialization */
    public List<Service> getServicesList() {
        return services;
    }

    /* serialization */
    public void setServicesList(List<Service> l) {
        services = l;
    }

    private void initializeServicesByName() {
        servicesByName = new HashMap();
        if (services != null) {
            for (Iterator iter = services.iterator(); iter.hasNext();) {
                Service service = (Service)iter.next();
                if (service.getName() != null &&
                    servicesByName.containsKey(service.getName())) {

                    throw new ModelException("model.uniqueness");
                }
                servicesByName.put(service.getName(), service);
            }
        }
    }

    public void addExtraType(AbstractType type) {
        extraTypes.add(type);
    }

    public Iterator getExtraTypes() {
        return extraTypes.iterator();
    }

    /* serialization */
    public Set<AbstractType> getExtraTypesSet() {
        return extraTypes;
    }

    /* serialization */
    public void setExtraTypesSet(Set<AbstractType> s) {
        extraTypes = s;
    }

//    public Iterator getImportedDocuments() {
//        return importedDocuments.values().iterator();
//    }
//
//    public ImportedDocumentInfo getImportedDocument(String namespace) {
//        return (ImportedDocumentInfo) importedDocuments.get(namespace);
//    }
//
//    public void addImportedDocument(ImportedDocumentInfo i) {
//        importedDocuments.put(i.getNamespace(), i);
//    }
//
//    /* serialization */
//    public Map getImportedDocumentsMap() {
//        return importedDocuments;
//    }
//
//    /* serialization */
//    public void setImportedDocumentsMap(Map m) {
//        importedDocuments = m;
//    }

    public void accept(ModelVisitor visitor) throws Exception {
        visitor.visit(this);
    }

    /**
     * @return
     */
    public String getSource() {
        return source;
    }

    /**
     * @param string
     */
    public void setSource(String string) {
        source = string;
    }

    public ProcessorActionVersion getProcessorActionVersion(){
        return processorActionVersion;
    }

    public void setProcessorActionVersion(ProcessorActionVersion version){
        this.processorActionVersion = version;
    }

    public void setProcessorActionVersion(String version){
        for(ProcessorActionVersion paVersion : EnumSet.allOf(ProcessorActionVersion.class)){
            switch(paVersion){
                case PRE_20:
                    if(version.equals(ProcessorActionVersion.PRE_20.toString()))
                        processorActionVersion = ProcessorActionVersion.PRE_20;
                    break;
                case VERSION_20:
                    if(version.equals(ProcessorActionVersion.VERSION_20.toString()))
                        processorActionVersion = ProcessorActionVersion.VERSION_20;
                    break;
                default:
                    throw new ModelException("model.invalid.processorActionVersion", new Object[]{version});
            }
        }
    }

    public void setJAXBModel(JAXBModel jaxBModel) {
        this.jaxBModel = jaxBModel;
    }

    public JAXBModel getJAXBModel() {
        return jaxBModel;
    }

    private QName name;
    private String targetNamespace;
    private List<Service> services = new ArrayList<Service>();
    private Map<QName, Service> servicesByName = new HashMap<QName, Service>();
    private Set<AbstractType> extraTypes = new HashSet<AbstractType>();
    private Map importedDocuments = new HashMap();
    private String source;
    private JAXBModel jaxBModel = null;
    private ProcessorActionVersion processorActionVersion = ProcessorActionVersion.VERSION_20;
}
