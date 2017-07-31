/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

import org.eclipse.persistence.sdo.SDOType;
import org.eclipse.persistence.sdo.helper.SDOHelperContext;
import org.eclipse.persistence.sdo.helper.SDOXSDHelper;
import org.w3c.dom.Element;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
//import javax.xml.transform.stax.StAXSource;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for generating sdo java types from a set of given schemas.
 * Schemas can be passed in using SAX events, DOM or a set of closure source.
 * Created by IntelliJ IDEA.
 * User: giglee
 * Date: May 14, 2009
 * Time: 9:47:38 AM
 * To change this template use File | Settings | File Templates.
 */
public class SDOSchemaCompiler {

    private List<Source> schemas = null;
    private EntityResolver entityResolver = null;

    public SDOSchemaCompiler() {
        resetSchema();
    }

    /*public ContentHandler getParserHandler(String systemId) {
        Document doc = SDOUtils.newDocument();
        SAX2DOMContentHandler handler = new SAX2DOMContentHandler(doc);
        DOMSource src = new DOMSource(doc, systemId);
        schemas.add(src);
        return handler;
    }*/

    public void parseSchema(Source schema) {
        schemas.add(schema);
    }

    public void parseSchema(String systemId, Element element) {
        Source src = new DOMSource(element);
        src.setSystemId(systemId);
        schemas.add(src);
    }

    public void parseSchema(InputSource source) {
        schemas.add(new SAXSource(source));
    }

    public void parseSchemas(List<Source> inputSchemas) {
        this.schemas = inputSchemas;
    }

    public void parseSchema(String systemId, XMLStreamReader xmlStreamReader) throws XMLStreamException {
        //StAXSource src = new StAXSource(xmlStreamReader);
        SAXSource src = new com.sun.xml.ws.util.xml.StAXSource(xmlStreamReader, false);
        src.setSystemId(systemId);
        schemas.add(src);
    }


    public void setEntityResolver(EntityResolver entityResolver) {
        this.entityResolver = entityResolver;
    }

    public void forcePackageName(String packageName) {
        throw new SDODatabindingException("package change name not supported");
    }

    public void resetSchema() {
        if (schemas == null) {
            schemas = new ArrayList<Source>();
        } else {
            schemas.clear();
        }
    }

    public Xsd2JavaSDOModel bind() {
        SDOHelperContext hc = new SDOHelperContext();
        List<SDOType> types = new ArrayList<SDOType>();
        SDODatabindingSchemaResolver schemaResolver = new SDODatabindingSchemaResolver(schemas, entityResolver);
        for (Source schema : schemas) {
            List<SDOType> list = ((SDOXSDHelper) hc.getXSDHelper()).define(schema, schemaResolver);
            types.addAll(list);
        }
        return new Xsd2JavaSDOModel(hc, types);
    }

}
