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

package testutil.converter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import testutil.converter.config.Configuration;
import testutil.converter.config.WsdlType;
import testutil.converter.custom.BindingsType;
import testutil.converter.custom.PackageType;
import testutil.converter.custom.JAXRPCPackageType;
import testutil.converter.custom.SchemaBindings;

import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPath;

import javax.xml.namespace.QName;
import java.util.List;

import static javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;

import com.sun.xml.bind.IXmlElementImpl;

/**
 * @author JAX-RPC Development Team
 */
public class ConfigToCustomizationConverter {
    File dir;
    
    private String FS = System.getProperty("file.separator");
    private String configClient = FS + "config" + FS + "config-client.xml";
    private String configServer = FS+ "config" + FS + "config-server.xml";
    private String customClientName = "custom-client.xml";
    private String customServerName = "custom-server.xml";
    private String customClient = FS + "config" + FS + customClientName;
    private String customServer = FS + "config" + FS + customServerName;
    private String buildProperties = FS + "config" + FS + "build.properties";
    
    public static void main(String[] args) {
        for (int i=0; i<args.length; i++)
            new ConfigToCustomizationConverter(args[i]);
    }
    
    public ConfigToCustomizationConverter(String dirName) {
        dir = new File(dirName);
        System.out.println("Processing \"" + dirName + "\" ...");
        if (!dir.isDirectory())
            throw new IllegalArgumentException(dirName + " must be a directory");
        convert();
    }
    
    protected void convert() {
        try {
            Properties props = readProperties();
            Configuration configuration = readConfig(configClient);
            convertToCustom(configuration, customClient, props.getProperty("client.features"));
            configuration = readConfig(configServer);
            updateProperties(configuration);
            convertToCustom(configuration, customServer, props.getProperty("server.features"));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    protected Properties readProperties() throws Exception {
        FileInputStream fis = new FileInputStream(new File(dir + buildProperties));
        Properties props = new Properties();
        props.load(fis);
        
        return props;
    }
    
    protected Configuration readConfig(String configFile) throws Exception {
        JAXBContext jc = JAXBContext.newInstance(testutil.converter.config.ObjectFactory.class);
        Unmarshaller u = jc.createUnmarshaller();
        Configuration configuration = (Configuration)u.unmarshal(new File(dir + configFile));
        
        return configuration;
    }
    
    protected void convertToCustom(Configuration configuration, String customFile, String features) throws Exception {
        WsdlType wsdlType = configuration.getWsdl();
        
        testutil.converter.custom.ObjectFactory of = new testutil.converter.custom.ObjectFactory();
        BindingsType bindingsType = new BindingsType();
        IXmlElementImpl<BindingsType> bindings = of.createBindings(bindingsType);

        bindingsType.setWsdlLocation(wsdlType.getLocation());

        if (features.contains("explicitcontext"))
            bindingsType.setEnableAdditionalSOAPHeaderMapping(Boolean.TRUE);
        
//        XPathFactory xpf = XPathFactory.newInstance();
//        XPath xpath = xpf.newXPath();
//        FileInputStream wsdlStream = new FileInputStream(wsdlType.getLocation());
//        String qname = xpath.evaluate("//definitions/types/schema", wsdlStream);
//        System.out.println(qname);
        
        List<BindingsType> childBindings = bindingsType.getBindings();
        BindingsType definitionsBindings = new BindingsType();
        definitionsBindings.setNode(new QName("http://schemas.xmlsoap.org/wsdl/", "definitions"));
        JAXRPCPackageType packageType = of.createJAXRPCPackageType();
        packageType.setName(wsdlType.getPackageName());
        definitionsBindings.setPackage(packageType);
        childBindings.add(definitionsBindings);

        BindingsType jaxbBindings = new BindingsType();
        childBindings.add(jaxbBindings);
        List<SchemaBindings> schemaBindingsList = jaxbBindings.getSchemaBindings();
        SchemaBindings schemaBindings = of.createSchemaBindings();
        PackageType jaxbPackageType = of.createPackageType();
        jaxbPackageType.setName(wsdlType.getPackageName());
        schemaBindings.setPackage(jaxbPackageType);
        schemaBindingsList.add(schemaBindings);
//        jaxbBindings.setNode(new QName("foobar", ""));
        
        OutputStream os = new FileOutputStream(dir + customFile);
        JAXBContext jc = JAXBContext.newInstance(testutil.converter.custom.ObjectFactory.class);
        Marshaller m = jc.createMarshaller();
        m.setProperty(JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.marshal(bindings, os);
        os.close();
    }
    
    protected void updateProperties(Configuration configuration) throws Exception {
        String wsdlLocation = configuration.getWsdl().getLocation();

        FileOutputStream fos = new FileOutputStream(new File(dir + buildProperties), true);
        PrintWriter writer = new PrintWriter(fos);
        writer.println();
        writer.println("wsdlname=" + wsdlLocation);
        writer.println();
        String basedir = wsdlLocation.substring(0, wsdlLocation.lastIndexOf("/"));
        writer.println("client.jaxrpc.binding=" + basedir + "/" + customClientName);
        writer.println("server.jaxrpc.binding=" + basedir + "/" + customServerName);
        writer.println("client.jaxb.binding=");
        writer.println("server.jaxb.binding=");
        writer.close();
    }
}

