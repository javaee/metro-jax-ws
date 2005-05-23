/**
 * $Id: JAXBModel.java,v 1.1 2005-05-23 23:18:52 bbissett Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.ws.processor.model.jaxb;

import com.sun.tools.xjc.api.*;

import javax.xml.namespace.QName;
import java.util.*;

import com.sun.tools.ws.processor.model.Persistent;

/**
 * Root of the JAXB Model.
 *
 * <p>
 * This is just a wrapper around a list of {@link JAXBMapping}s.
 *
 * @author Kohsuke Kawaguchi, Vivek Pandey
 */
public class JAXBModel {

    /**
     * All the mappings known to this model.
     */
    private List<JAXBMapping> mappings;

    /**
     * @see com.sun.tools.xjc.api.JAXBModel#getClassList()
     */
    @Persistent
    private List<String> classList;

    // index for faster access.
    private final Map<QName,JAXBMapping> byQName = new HashMap<QName,JAXBMapping>();
    private final Map<String,JAXBMapping> byClassName = new HashMap<String,JAXBMapping>();

    private com.sun.tools.xjc.api.JAXBModel rawJAXBModel;

    public com.sun.tools.xjc.api.JAXBModel getRawJAXBModel() {
        return rawJAXBModel;
    }

    /**
     * @return Schema to Java model
     */
    public S2JJAXBModel getS2JJAXBModel(){
        if(rawJAXBModel instanceof S2JJAXBModel)
            return (S2JJAXBModel)rawJAXBModel;
        return null;
    }

    /**
     * @return Java to Schema JAXBModel
     */
    public J2SJAXBModel getJ2SJAXBModel(){
        if(rawJAXBModel instanceof J2SJAXBModel)
            return (J2SJAXBModel)rawJAXBModel;
        return null;
    }


    /**
     * Default constructor for the persistence.
     */
    public JAXBModel() {}

    /**
     * Constructor that fills in the values from the given raw model
     */
    public JAXBModel( com.sun.tools.xjc.api.JAXBModel rawModel ) {
        this.rawJAXBModel = rawModel;
        if(rawModel instanceof S2JJAXBModel){
            S2JJAXBModel model = (S2JJAXBModel)rawModel;
            List<JAXBMapping> ms = new ArrayList<JAXBMapping>(model.getMappings().size());
            for( Mapping m : model.getMappings())
                ms.add(new JAXBMapping(m));
            setMappings(ms);
        }
        setClassList(new ArrayList<String>(rawModel.getClassList()));
    }

    /**
     * @see com.sun.tools.xjc.api.JAXBModel#getClassList()
     */
    public List<String> getClassList() {
        return classList;
    }

    public void setClassList(List<String> classList) {
        this.classList = classList;
    }

    /**
     * @see com.sun.tools.xjc.api.JAXBModel#getMappings()
     */
    public List<JAXBMapping> getMappings() {
        return mappings;
    }

    //public void setMappings(List<JAXBMapping> mappings) {
    public void setMappings(List<JAXBMapping> mappings) {
        this.mappings = mappings;
        byQName.clear();
        byClassName.clear();
        Iterator iter = mappings.iterator();
        for( JAXBMapping m : mappings ) {
            byQName.put(m.getElementName(),m);
            byClassName.put(m.getTypeClass(),m);
        }
    }

    /**
     * @see com.sun.tools.xjc.api.JAXBModel#get(QName)
     */
    public JAXBMapping get( QName elementName ) {
        return byQName.get(elementName);
    }

    /**
     * @see com.sun.tools.xjc.api.JAXBModel#get(String)
     */
    public JAXBMapping get( String className ) {
        return byClassName.get(className);
    }


    /**
     *
     * @return set of full qualified class names that jaxb will generate
     */
    public Set<String> getGeneratedClassNames() {
        return generatedClassNames;
    }

    public void setGeneratedClassNames(Set<String> generatedClassNames) {
        this.generatedClassNames = generatedClassNames;
    }

    private Set<String> generatedClassNames;
}
