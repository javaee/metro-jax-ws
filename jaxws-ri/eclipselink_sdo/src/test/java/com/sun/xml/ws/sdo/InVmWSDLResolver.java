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

package com.sun.xml.ws.sdo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Holder;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.oracle.webservices.api.databinding.WSDLResolver;
import com.sun.xml.ws.api.streaming.XMLStreamReaderFactory;
import com.sun.xml.ws.api.wsdl.parser.XMLEntityResolver;
import com.sun.xml.ws.api.wsdl.parser.XMLEntityResolver.Parser;
import com.sun.xml.ws.streaming.TidyXMLStreamReader;

public class InVmWSDLResolver implements WSDLResolver {
    String wsdlID = null;
    ByteArrayOutputStream wsdlIO = null;
    HashMap<String, ByteArrayOutputStream> files = new HashMap<String, ByteArrayOutputStream>();
    public Result getAbstractWSDL(Holder<String> h) {
//      System.out.println("---- getAbstractWSDL " + h.value);
        return result(h.value, null);
    }

    public Result getSchemaOutput(String v, Holder<String> h) {
//      System.out.println("---- getSchemaOutput " + v + " " + h.value);
        return result(h.value, null);
    }

    public Result getWSDL(String v) {
//      System.out.println("---- getWSDL " + v);
        wsdlID = v;
        wsdlIO = new ByteArrayOutputStream();
        return result(v, wsdlIO);
    }
    private Result result(String s, ByteArrayOutputStream bio) {
        if (bio == null) bio = new ByteArrayOutputStream();
        files.put(s, bio);
        Result result = new StreamResult(bio);
        result.setSystemId(s.replace('\\', '/'));
        return result;
    }
    
    public void print() {
        for(String s : files.keySet()) {
//          System.out.println("---- file name: " + s);
            System.out.println(new String(files.get(s).toByteArray()));             
        }
    }
    public void printXSD() {
        for(String s : files.keySet()) {
            if (s.endsWith("xsd")) {
//              System.out.println("---- file name: " + s);
                System.out.println(new String(files.get(s).toByteArray()));     
            }
        }
    }
    
    
    public Parser getWsdlSource() throws Exception  {
//      return new StreamSource(new ByteArrayInputStream(wsdlIO.toByteArray()), wsdlID);
        ByteArrayInputStream bi = new ByteArrayInputStream(wsdlIO.toByteArray()); 
        return new Parser(new URL("file://"+wsdlID), new TidyXMLStreamReader(XMLStreamReaderFactory.create(wsdlID, bi, true), bi));
    }
    
    public XMLEntityResolver getEntityResolver() {
        return new XMLEntityResolver() {
            public Parser resolveEntity(String publicId, String systemId) throws SAXException, IOException {
//              System.out.println("---- publicId: " + publicId);
//              System.out.println("---- systemId: " + systemId);
                ByteArrayOutputStream b = files.get(systemId);
                if (b!= null) {
                    ByteArrayInputStream bi = new ByteArrayInputStream(b.toByteArray()); 
                    return new Parser(null, new TidyXMLStreamReader(XMLStreamReaderFactory.create(systemId, bi, true), bi));
                }
                return null;
            }
            
        };
    }
    
}
