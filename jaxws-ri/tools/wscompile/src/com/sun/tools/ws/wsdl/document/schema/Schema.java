/*
 * $Id: Schema.java,v 1.1 2005-05-24 13:58:13 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.wsdl.document.schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import com.sun.tools.ws.wsdl.framework.AbstractDocument;
import com.sun.tools.ws.wsdl.framework.Defining;
import com.sun.tools.ws.wsdl.framework.Extension;
import com.sun.tools.ws.wsdl.framework.Kind;
import com.sun.tools.ws.wsdl.framework.ValidationException;
import com.sun.tools.ws.wsdl.parser.Constants;

/**
 *
 * @author JAX-RPC Development Team
 */
public class Schema extends Extension implements Defining {

    public Schema(AbstractDocument document) {
        _document = document;
        _nsPrefixes = new HashMap();
        _definedEntities = new ArrayList();
    }

    public QName getElementName() {
        return SchemaConstants.QNAME_SCHEMA;
    }

    public SchemaElement getContent() {
        return _content;
    }

    public void setContent(SchemaElement entity) {
        _content = entity;
        _content.setSchema(this);
    }

    public void setTargetNamespaceURI(String uri) {
        _targetNamespaceURI = uri;
    }

    public String getTargetNamespaceURI() {
        return _targetNamespaceURI;
    }

    public void addPrefix(String prefix, String uri) {
        _nsPrefixes.put(prefix, uri);
    }

    public String getURIForPrefix(String prefix) {
        return (String) _nsPrefixes.get(prefix);
    }

    public Iterator prefixes() {
        return _nsPrefixes.keySet().iterator();
    }

    public void defineAllEntities() {
        if (_content == null) {
            throw new ValidationException(
                "validation.shouldNotHappen",
                "missing schema content");
        }

        for (Iterator iter = _content.children(); iter.hasNext();) {
            SchemaElement child = (SchemaElement) iter.next();
            if (child.getQName().equals(SchemaConstants.QNAME_ATTRIBUTE)) {
                QName name =
                    new QName(
                        _targetNamespaceURI,
                        child.getValueOfMandatoryAttribute(
                            Constants.ATTR_NAME));
                defineEntity(child, SchemaKinds.XSD_ATTRIBUTE, name);
            } else if (
                child.getQName().equals(
                    SchemaConstants.QNAME_ATTRIBUTE_GROUP)) {
                QName name =
                    new QName(
                        _targetNamespaceURI,
                        child.getValueOfMandatoryAttribute(
                            Constants.ATTR_NAME));
                defineEntity(child, SchemaKinds.XSD_ATTRIBUTE_GROUP, name);
            } else if (
                child.getQName().equals(SchemaConstants.QNAME_ELEMENT)) {
                QName name =
                    new QName(
                        _targetNamespaceURI,
                        child.getValueOfMandatoryAttribute(
                            Constants.ATTR_NAME));
                defineEntity(child, SchemaKinds.XSD_ELEMENT, name);
            } else if (child.getQName().equals(SchemaConstants.QNAME_GROUP)) {
                QName name =
                    new QName(
                        _targetNamespaceURI,
                        child.getValueOfMandatoryAttribute(
                            Constants.ATTR_NAME));
                defineEntity(child, SchemaKinds.XSD_GROUP, name);
            } else if (
                child.getQName().equals(SchemaConstants.QNAME_COMPLEX_TYPE)) {
                QName name =
                    new QName(
                        _targetNamespaceURI,
                        child.getValueOfMandatoryAttribute(
                            Constants.ATTR_NAME));
                defineEntity(child, SchemaKinds.XSD_TYPE, name);
            } else if (
                child.getQName().equals(SchemaConstants.QNAME_SIMPLE_TYPE)) {
                QName name =
                    new QName(
                        _targetNamespaceURI,
                        child.getValueOfMandatoryAttribute(
                            Constants.ATTR_NAME));
                defineEntity(child, SchemaKinds.XSD_TYPE, name);
            }
        }
    }

    public void defineEntity(SchemaElement element, Kind kind, QName name) {
        SchemaEntity entity = new SchemaEntity(this, element, kind, name);
        _document.define(entity);
        _definedEntities.add(entity);
    }

    public Iterator definedEntities() {
        return _definedEntities.iterator();
    }

    public void validateThis() {
        if (_content == null) {
            throw new ValidationException(
                "validation.shouldNotHappen",
                "missing schema content");
        }
    }

    public String asString(QName name) {
        if (name.getNamespaceURI().equals("")) {
            return name.getLocalPart();
        } else {
            // look for a prefix
            for (Iterator iter = prefixes(); iter.hasNext();) {
                String prefix = (String) iter.next();
                if (prefix.equals(name.getNamespaceURI())) {
                    return prefix + ":" + name.getLocalPart();
                }
            }

            // not found
            return null;
        }
    }

    private AbstractDocument _document;
    private String _targetNamespaceURI;
    private SchemaElement _content;
    private List _definedEntities;
    private Map _nsPrefixes;
}
