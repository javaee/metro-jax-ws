/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004-2013 Oracle and/or its affiliates. All rights reserved.
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

package testutil;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import com.sun.tools.ws.wsdl.parser.Util;
import com.sun.xml.ws.util.xml.XmlUtil;


/**
 * This class is called from ant to transform
 * a regular jaxrpc:bindings customization file so that it looks
 * for the wsdl file in a local location instead
 * of getting it through http.
 */
public class LocalCustomizationFileTransformer {

    public LocalCustomizationFileTransformer() {
        super();
    }

    /**
     * Must pass in location of orginal file and
     * location to save new file to. 
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args == null || args.length < 1) {
            System.err.println("ERROR: need args: old config file location,\n" +
                "wsdl name");
            return;
        }

        try{
            Map<String, String> map = null;
            String port = "8080";

            boolean localExecType = "local".equals(args[0].toLowerCase());
            
            if (localExecType) {
                 File wsdlLocationFile = new File(args[2]);
                 File newLoc = new File(args[1]+"WEB-INF/wsdl/"+wsdlLocationFile.getName());
                 map = buildMap(new File(args[1]+"WEB-INF/wsdl/").getCanonicalPath(), newLoc.getCanonicalPath());
            } else {
                port = args[2];
            }

            int index = args[1].lastIndexOf('/');
            String path = "./";
            String files = args[1];
            if(index != -1){
                path = args[1].substring(0, index+1);
                files = args[1].substring(index+1);
            }

            StringTokenizer tokenizer = new StringTokenizer(files, ",");
            if(!tokenizer.hasMoreTokens())
                tokenizer = new StringTokenizer(files, " ");

            while(tokenizer.hasMoreTokens()){
                String token = tokenizer.nextToken().trim();
                String oldCustom = path+token;
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = dbf.newDocumentBuilder();

                // make change in config file
                File file = new java.io.File(oldCustom);
                Document doc = builder.parse(file);
                Element wsdlElement = doc.getDocumentElement();
                String localName = wsdlElement.getLocalName();
                String nspace = wsdlElement.getNamespaceURI();
                if((localName != null && localName.equals("bindings")) &&
                        (nspace != null && !wsdlElement.getNamespaceURI().equals("http://java.sun.com/xml/ns/jaxws")))
                    continue;
                Attr wsdlAttr = wsdlElement.getAttributeNode("wsdlLocation");

                String location = (wsdlAttr != null)?wsdlAttr.getValue():null;
                if (location != null) {
                    location = location.replaceFirst("/localhost:/", "/localhost:" + port + "/");
                }
                if (!localExecType) {
                    System.out.println(location);
                }

                token = "temp-config.xml";
                String newCustom = args[2]+token;

                if (location != null) {
                    File wsdlLocationFile = new File(args[3]);
                    File newLoc = new File(args[2]+"WEB-INF/wsdl/"+wsdlLocationFile.getName());
                    wsdlAttr.setValue(newLoc.getCanonicalPath());
                    System.out.println(newLoc.getCanonicalPath());
                }else{
                    transformSchemaLocation(wsdlElement, map);
                }
                // save file
                TransformerFactory factory = TransformerFactory.newInstance();
                Transformer transformer = factory.newTransformer();
                transformer.transform(new DOMSource(doc), new StreamResult(newCustom));
            }
        } catch (Exception e) {
            System.err.println("exception in LocalConfigFileTransformer:");
            e.printStackTrace();
        }
    }
    
    
    private static Attr getWSDLLocation(Element root){
        NamedNodeMap atts = root.getAttributes();
        for( int i=0; i<atts.getLength(); i++ ) {
            Attr a = (Attr)atts.item(i);
            if( a.getLocalName().equals("wsdlLocation"))
                return a;
        }
        return null;
    }
    
    private static void transformSchemaLocation(Element root, Map<String, String> map) throws IOException{
        for (Iterator iter = XmlUtil.getAllChildren(root); iter.hasNext();) {
            Element e = Util.nextElement(iter);
            if(e==null)
                return;
            Attr schemaLocationAttr = e.getAttributeNode("schemaLocation");
            if(schemaLocationAttr == null)
                continue;
            //this location is a URL
            String sl = schemaLocationAttr.getValue();
            int index = sl.lastIndexOf('?');
            if(index != -1){
                schemaLocationAttr.setValue("file:/"+map.get(sl.substring(index+1)));
            }
        }
    }

    /**
     *
     * @param dirName path to /x/y/z/WEB-INF/wsdl
     * @return Map is query-->path e.g: wsdl=sub/a.wsdl --> /WEB-INF/wsdl/sub/a.wsdl
     * @throws Exception
     */
    private static Map<String, String> buildMap(String dirName, String primaryWsdl)
    throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        List<String> schemaIds = new ArrayList<String>();
        List<String> wsdlIds = new ArrayList<String>();
        File dir = new File(dirName);
        List<String> list = new ArrayList<String>();
        buildDocList(dir, list);
        for(String file : list) {
            // Use this logic for now for identifying wsdl or schema
			// TODO: use the logic from runtime
            if (file.endsWith(".wsdl")) {
                // TODO: For primary wsdl need to put only "wsdl"
                if (!file.equals(primaryWsdl)) {
                    wsdlIds.add(file);
                } else {
                    map.put("wsdl", file);
                }
            } else if (file.endsWith(".xsd")) {
                schemaIds.add(file);
            }
        }
        Collections.sort(wsdlIds);
        Collections.sort(schemaIds);
        int wsdlNum = 1;
        for(String file : wsdlIds) {
            map.put("wsdl="+wsdlNum++, file);
        }
        int schemaNum = 1;
        for(String file : schemaIds) {
            map.put("xsd="+schemaNum++, file);
        }
        return map;
    }

    private static  void buildDocList(File dir, List<String> list)
    throws Exception {
        String[] files = dir.list();
        for (int i = 0; i < files.length; i++) {
            File sub = new File(dir, files[i]);
            if (sub.isDirectory()) {
                buildDocList(sub, list);
            } else {
                list.add(sub.getCanonicalPath());
            }
        }
    }
    
}
