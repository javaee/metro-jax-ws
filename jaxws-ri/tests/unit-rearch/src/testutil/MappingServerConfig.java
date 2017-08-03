/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004-2017 Oracle and/or its affiliates. All rights reserved.
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

package testutil;

import java.io.FileOutputStream;
import java.io.PrintStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class is called from ant to create mapping-server.xml,
 * which is used for consuming mapping file with gen:server
 * option.
 */

public class MappingServerConfig {

    /**
     * Must pass in files config-server.xml, mapping file to be consumed
     * and the location to save newly created mapping-server.xml file. 
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args == null || args.length < 3) {
            System.err.println(
                "ERROR: need args: config-server.xml,\n"
                    + "mapping file and\n temp dir to save mapping-server.xml");
            return;
        }
        try {
            String serverConfig = args[0];
            String mappingFile = args[1];
            String mappingConfig = args[2] + "mapping-server.xml";
            String tempdir = args[2];

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dbf.newDocumentBuilder();

            // get wsdl file names from config-server.xml
            Document doc = builder.parse(serverConfig);
            Element serviceElement =
                (Element) doc.getElementsByTagName("service").item(0);

            String wsdlName = "";
            if (serviceElement == null) {
                Element wsdlElement =
                    (Element) doc.getElementsByTagName("wsdl").item(0);
                String wsdlLocation = wsdlElement.getAttribute("location");
                wsdlName =
                    wsdlLocation.substring(
                        wsdlLocation.lastIndexOf("/"),
                        wsdlLocation.length());
            } else {
                wsdlName = serviceElement.getAttribute("name") + ".wsdl";
            }

            String mappingConfigString =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                    + "<configuration xmlns=\"http://java.sun.com/xml/ns/jax-rpc/ri/config\">\n"
                    + "<j2eeMappingFile location=\""
                    + tempdir
                    + mappingFile
                    + "\" "
                    + "wsdlLocation=\""
                    + tempdir
                    + wsdlName
                    + "\"/>\n"
                    + "</configuration>";

            FileOutputStream out;
            PrintStream ps;
            out = new FileOutputStream(mappingConfig);
            ps = new PrintStream(out);
            ps.println(mappingConfigString);
            ps.close();
        } catch (Exception e) {
            System.err.println("exception in JaxrpcRiRuntimeConfigCreator:");
            e.printStackTrace();
        }
    }
}
