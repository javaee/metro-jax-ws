/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.xml.ws.db.sdo;

import commonj.sdo.DataObject;
import commonj.sdo.Property;
import commonj.sdo.Type;
import commonj.sdo.impl.HelperProvider;
import commonj.sdo.helper.HelperContext;
import commonj.sdo.helper.TypeHelper;
import commonj.sdo.helper.XMLDocument;
import commonj.sdo.helper.XSDHelper;

import org.eclipse.persistence.sdo.SDOType;
import org.eclipse.persistence.sdo.helper.SDOXMLHelper;
import org.eclipse.persistence.sdo.helper.SDOXSDHelper;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.helpers.NamespaceSupport;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

/**
 * A set of core utility methods that shapes the sdo databinding
 * Created by IntelliJ IDEA.
 * User: giglee
 * Date: May 13, 2009
 * Time: 10:21:12 AM
 * To change this template use File | Settings | File Templates.
 */
public class SDOUtils {

    public static final String NS_XMLNS = "http://www.w3.org/2001/XMLSchema";
    public static final String NS_WSDL = "http://schemas.xmlsoap.org/wsdl/";
    private static final String NS_XSD = "http://www.w3.org/2001/XMLSchema";
    private static final QName SCHEMA_INCLUDE_QNAME = new QName(NS_XSD, "include");
    private static final QName SCHEMA_IMPORT_QNAME = new QName(NS_XSD, "import");
    public static final QName QNAME_SCHEMA = new QName(NS_XMLNS, "schema");


    static TransformerFactory transformerFactory = null;
    static DocumentBuilderFactory dbf = null;

    public static Transformer newTransformer() {
        if (transformerFactory == null)
            transformerFactory = TransformerFactory.newInstance();
        try {
            return transformerFactory.newTransformer();
        }
        catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public static DocumentBuilder newDocumentBuilder() {
        if (dbf == null) {
            dbf = DocumentBuilderFactory.newInstance();
            dbf.setValidating(false);
            dbf.setNamespaceAware(true);
            dbf.setIgnoringElementContentWhitespace(true);
            dbf.setIgnoringComments(true);
        }
        try {
            DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
            return documentBuilder;
        }
        catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Serialize a DataObject to the specified element
     * Per bug  6120620,, we use only GMT timezone
     */
    public static Element sdoToDom(HelperContext hc, DataObject obj, String targetNamespace, String localName)
            throws ParserConfigurationException, IOException {
        SDOXMLHelper sdoXMLHelper = (SDOXMLHelper) hc.getXMLHelper();

        // Removed this from JRF for ADF use case.
        //sdoXMLHelper.setTimeZone(TimeZone.getTimeZone("GMT"));

        sdoXMLHelper.setTimeZoneQualified(true);

        XMLDocument xmlDoc = sdoXMLHelper.createDocument(obj, targetNamespace, localName);
        if (xmlDoc == null) {
            return null;
        }

        Document doc = newDocumentBuilder().newDocument();
        DOMResult result = new DOMResult(doc);
        sdoXMLHelper.save(xmlDoc, result, null);
        return ((Document) result.getNode()).getDocumentElement();
    }

    /**
     * Serialize a DataObject to the specified xml element in text xml
     *
     * @param hc
     * @param obj
     * @param targetNamespace
     * @param localName
     * @return
     * @throws ParserConfigurationException
     * @throws IOException
     */
    public static Source sdoToXML(HelperContext hc, DataObject obj, String targetNamespace, String localName)
            throws ParserConfigurationException, IOException {
        SDOXMLHelper sdoXMLHelper = (SDOXMLHelper) hc.getXMLHelper();
        sdoXMLHelper.setTimeZone(TimeZone.getTimeZone("GMT"));
        sdoXMLHelper.setTimeZoneQualified(true);

        XMLDocument xmlDoc = sdoXMLHelper.createDocument(obj, targetNamespace, localName);
        if (xmlDoc == null) {
            return null;
        }

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        StreamResult result = new StreamResult(bout);

        sdoXMLHelper.save(xmlDoc, result, null);
        byte[] bytes = bout.toByteArray();
        System.out.println("data obj converted to xml: " + new String(bytes));
        return new StreamSource(new ByteArrayInputStream(bytes));
    }

    /**
     * Register the types defined in the given schema with the given sdo helper context
     *
     * @param context
     * @param schemas
     */
    public static void registerSDOContext(HelperContext context, List<Source> schemas) {
        SDOXSDHelper xsdHelper = (SDOXSDHelper) context.getXSDHelper();
        SDODatabindingSchemaResolver schemaResolver = new SDODatabindingSchemaResolver(schemas);
        for (Source source : schemas) {
            //SDOUtils.printDOM(source);
            List<Type> lt = xsdHelper.define(source, schemaResolver);
        }
    }


    public static List<Source> getSchemaClosureFromWSDL(Source wsdlSource) {
        String systemId = wsdlSource.getSystemId();
        Document wsdl = createDOM(wsdlSource);
        List<Source> list = new ArrayList<Source>();
        addSchemaFragmentSource(wsdl, systemId, list);
        return list;
    }

    public static Document createDOM(Source wsdlSource) {
        Transformer trans = newTransformer();
        DocumentBuilder builder = newDocumentBuilder();
        Document doc = builder.newDocument();
        DOMResult result = new DOMResult(doc);
        try {
            trans.transform(wsdlSource, result);
        } catch (TransformerException te) {
            throw new RuntimeException(te);
        }
        return (Document) result.getNode();
    }

    public static Map<String, Source> getMetadataClosure(List<Source> schemas) {
        Map<String, Source> closureDocs = new HashMap<String, Source>();
        Map<String, Source> currentDocs = new HashMap<String, Source>();
        Set<String> remaining = new HashSet<String>();
        for (Source src : schemas) {
            currentDocs.put(src.getSystemId(), src);
        }

        remaining.addAll(currentDocs.keySet());

        while (!remaining.isEmpty()) {
            Iterator<String> it = remaining.iterator();
            String current = it.next();
            remaining.remove(current);
            Source currentDoc = currentDocs.get(current);
            if (currentDoc == null) {
                currentDoc = loadSourceFromURL(current);
            }

            Set<String> imports = new HashSet<String>();
            currentDoc = getImports(currentDoc, imports);
            closureDocs.put(current, currentDoc);
            for (String importedDoc : imports) {
                if (closureDocs.get(importedDoc) == null) {
                    remaining.add(importedDoc);
                }
            }
        }
        return closureDocs;
    }

    @SuppressWarnings("CallToThreadDumpStack")
    private static Source loadSourceFromURL(String systemID) {
        Source targetXSD;
        try {
            URL targetURL = new URL(systemID);
            InputStream is = targetURL.openStream();
            targetXSD = new StreamSource(is);
            targetXSD.setSystemId(targetURL.toExternalForm());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return targetXSD;
    }

    private static Source getImports(Source currentDoc, Set<String> importedDocs) {
        Document doc = createDOM(currentDoc);
        Element root = doc.getDocumentElement();

        try {
            NodeList imports = root.getElementsByTagNameNS(SCHEMA_INCLUDE_QNAME.getNamespaceURI(), SCHEMA_INCLUDE_QNAME.getLocalPart());
            NodeList includes = root.getElementsByTagNameNS(SCHEMA_IMPORT_QNAME.getNamespaceURI(), SCHEMA_INCLUDE_QNAME.getLocalPart());

            if (imports != null) {
                for (int i = 0; i < imports.getLength(); i++) {
                    Element e = (Element) imports.item(i);
                    String importedDoc = e.getAttributeNS(NS_XMLNS, "schemaLocation");
                    if (importedDoc != null) {
                        URL u = new URL(currentDoc.getSystemId());
                        importedDocs.add(new URL(u, importedDoc).toString());
                    }
                }
            }
            if (includes != null) {
                for (int i = 0; i < imports.getLength(); i++) {
                    Element e = (Element) imports.item(i);
                    String importedDoc = e.getAttributeNS(NS_XMLNS, "schemaLocation");
                    if (importedDoc != null) {
                        URL u = new URL(currentDoc.getSystemId());
                        importedDocs.add(new URL(u, importedDoc).toString());
                    }
                }
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new DOMSource(doc, currentDoc.getSystemId());
    }


    private static void addSchemaFragmentSource(Document doc, String systemId, List<Source> list) {

        Element e = doc.getDocumentElement();
        assert e.getNamespaceURI().equals(NS_WSDL);
        assert e.getLocalName().equals("definitions");

        NodeList typesList = e.getElementsByTagNameNS(NS_WSDL, "types");
        for (int i = 0; i < typesList.getLength(); i++) {
            NodeList schemaList =
                    ((Element) typesList.item(i)).getElementsByTagNameNS(NS_XMLNS, "schema");
            for (int j = 0; j < schemaList.getLength(); j++) {
                Element elem = (Element) schemaList.item(j);
                NamespaceSupport nss = new NamespaceSupport();
                buildNamespaceSupport(nss, elem);
                patchDOMFragment(nss, elem);
                list.add(new DOMSource(elem, systemId + "#schema" + j));
            }
        }
    }

    private static void buildNamespaceSupport(NamespaceSupport nss, Node node) {
        if (node == null || node.getNodeType() != Node.ELEMENT_NODE) {
            return;
        }

        buildNamespaceSupport(nss, node.getParentNode());

        nss.pushContext();
        NamedNodeMap atts = node.getAttributes();
        for (int i = 0; i < atts.getLength(); i++) {
            Attr a = (Attr) atts.item(i);
            if ("xmlns".equals(a.getPrefix())) {
                nss.declarePrefix(a.getLocalName(), a.getValue());
                continue;
            }
            if ("xmlns".equals(a.getName())) {
                nss.declarePrefix("", a.getValue());
                continue;
            }
        }
    }

    private static void patchDOMFragment(NamespaceSupport nss, Element elem) {
        NamedNodeMap atts = elem.getAttributes();
        for (Enumeration en = nss.getPrefixes(); en.hasMoreElements();) {
            String prefix = (String) en.nextElement();

            for (int i = 0; i < atts.getLength(); i++) {
                Attr a = (Attr) atts.item(i);
                if (!"xmlns".equals(a.getPrefix()) || !a.getLocalName().equals("prefix")) {
                    elem.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:" + prefix, nss.getURI(prefix));
                }
            }
        }
    }


    public static Object unwrapPrimitives(Object obj) {
        if (obj == null) {
            return obj;
        }
        if (!(obj instanceof DataObject)) {
            return obj;  
        }
        DataObject dataObject = (DataObject) obj;
        if (dataObject.getClass().getName().endsWith("WrapperImpl")) {
            if (dataObject.get(0) != null) {
                System.out.println("unwrapped object: " + dataObject.get(0).getClass().getName());
            }
            return dataObject.get(0);
        }

        return obj;
    }


    @SuppressWarnings("CallToThreadDumpStack")
    public static void printDOM(Source src) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            StreamResult sr = new StreamResult(bos);
            Transformer trans = TransformerFactory.newInstance().newTransformer();
            trans.transform(src, sr);
            System.out.println("**********\n" + bos.toString());
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String dom2String(DOMSource domSrc) throws TransformerConfigurationException, TransformerException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        StreamResult sr = new StreamResult(bos);
        Transformer trans = TransformerFactory.newInstance().newTransformer();
        trans.transform(domSrc, sr);
        return sr.toString();
    }

    // note only use for debugging, once the reader is read, the cursor can't be set back
    public static void printXMLReader(XMLStreamReader xml) {
          try {
            Document doc = SDOUtils.newDocument();
            SAX2DOMContentHandler handler = new SAX2DOMContentHandler(doc);
            Stax2SAXAdapter adapter = new Stax2SAXAdapter(xml, false);
            adapter.parse(handler);
            printDOM(new DOMSource(doc));
          }
          catch (Exception e) {
              e.printStackTrace();
          }
    }

    

    /**
     * get the element name represented by this property
     * @param context
     * @param p
     * @return
     */
    public static QName getPropertyElementName(HelperContext context, Property p) {        
        XSDHelper helper = context.getXSDHelper();
        String localName = p.getName();
        String ns = helper.getNamespaceURI(p);
        return new QName(ns, localName);
    }


    // used by tests
    public static List defineSchema(HelperContext hc, File f) throws Exception {
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(f);
            InputStreamReader reader = new InputStreamReader(fin);
            StreamSource source = new StreamSource(reader);
            return ((SDOXSDHelper) hc.getXSDHelper()).define(source, null);
        }
        finally {
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException ioe) {
                }
            }
        }
    }


    public static Document newDocument() {
        return newDocumentBuilder().newDocument();
    }

    /**
     * Check whether a java class is supported
     * The builtin type includes all the default type mappings specified in the SDO Spec   
     * @param javaType
     * @param qname
     * @return
     */
    public static boolean validateBuiltinType(String javaType, QName qname) {
        return validateSupportedType(HelperProvider.getDefaultContext(), javaType, qname);
    }

    public static boolean validateSupportedType(HelperContext hc, String javaType, QName qname) {
        TypeHelper typeHelper = hc.getTypeHelper();
        if (qname != null) {
            Type type = typeHelper.getType(qname.getNamespaceURI(), qname.getLocalPart());
            String java = ((SDOType) type).getInstanceClassName();
            if (java != null) {
                return java.equals(javaType);
            }
            return false;
        } else {
            if (isPrimitive(javaType)) {
                return true;
            }
            try {
                Class cls = Thread.currentThread().getContextClassLoader().loadClass(javaType);
                Type type = typeHelper.getType(cls);
                return type == null ? false : true;
            }
            catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }
    
    // all primitives listed here is supported
    private static boolean isPrimitive(String type) {
        if (type.equals("int") ||
                type.equals("short") ||
                type.equals("long") ||
                type.equals("byte") ||
                type.equals("float") ||
                type.equals("double") ||
                type.equals("boolean")) {
            return true;
        }
        // the rest we will let toplink handle it
        return false;
    }


    public static Set<SchemaInfo> getSchemas(File f) throws Exception {
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(f);
            StreamSource source = new StreamSource(fin);
            source.setSystemId(f.toURL().toExternalForm());
            List<Source> main_schema = new ArrayList<Source>();
            main_schema.add(source);
            Map<String, Source> map = SDOUtils.getMetadataClosure(main_schema);

            Set<SchemaInfo> schemas = new HashSet<SchemaInfo>();
            for (Map.Entry<String, Source> entry : map.entrySet()) {
                SchemaInfo info = new SchemaInfo(entry.getKey(), null, entry.getValue());
                schemas.add(info);
            }
            return schemas;
        }
        finally {
            if (fin != null) {
                fin.close();
            }
        }
    }

    
    public static Set<SchemaInfo> getSchemas(String filePath) throws Exception {
        Set<SchemaInfo> schemas = new HashSet<SchemaInfo>();
        
        Document document = newDocumentBuilder().parse(new File(filePath));
        Element rootEl = document.getDocumentElement();
        if (QNAME_SCHEMA.equals(new QName(rootEl.getNamespaceURI(), rootEl.getLocalName()))) {
            SchemaInfo info = new SchemaInfo(filePath, null, new DOMSource(rootEl));
            schemas.add(info);
        } else if ("http://schemas.xmlsoap.org/wsdl/".equals(rootEl.getNamespaceURI())) {
            Element types = null;
            Node n = rootEl.getFirstChild();
            while (types == null) {
                if (n instanceof Element && ((Element)n).getLocalName().equals("types")) types = (Element)n;
                else n = n.getNextSibling();
            }
            NodeList nl = types.getChildNodes();
            for (int i = 0; i < nl.getLength(); i++ ) {
                Node x = nl.item(i);
                if (x instanceof Element && ((Element)x).getLocalName().equals("schema")) {
                    SchemaInfo info = new SchemaInfo(filePath, null, new DOMSource((Element)x));
                    schemas.add(info);
                }
            }
//            Definition def = WSDLHelper.readWsdl(url, document);
//            SchemaCollection schemaCollection = SchemaAnalyzer.collectSchemas(def);
//            schemas = SchemaHelper.getSchemaInfos(url.toString(), schemaCollection,true);
//            for (SchemaInfo schema : schemas) {
//                Element schemaElement = schema.getSchemaElement();
//                SchemaHelper.fixMissingNamespaces(schemaElement);
//            }
        } else {
//            throw new IOException("Document found at '" + sdoSchema + "' is not a valid schema/wsdl document.");
        }

        return schemas;
    }
}
