/*
 * $Id: Model.java,v 1.5 2005-08-04 22:08:16 kohlert Exp $
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
 * The model is used to represent the entire Web Service.  The JAX-WS ProcessorActions can process
 * this Model to generate Java artifacts such as the service interface.
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

    public Service getServiceByName(QName name) {
        if (servicesByName.size() != services.size()) {
            initializeServicesByName();
        }
        return (Service)servicesByName.get(name);
    }

    /* serialization */
    public List<Service> getServices() {
        return services;
    }

    /* serialization */
    public void setServices(List<Service> l) {
        services = l;
    }

    private void initializeServicesByName() {
        servicesByName = new HashMap();
        if (services != null) {
            for (Service service : services) {
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


    public void accept(ModelVisitor visitor) throws Exception {
        visitor.visit(this);
    }

    /**
     * @return the source version
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
